package com.example.project

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import android.content.Intent

class MainActivity : ComponentActivity() {

    private lateinit var openGallerybutton: Button
    private lateinit var imageView: ImageView
    private lateinit var doOcrbutton: Button
    private var selectedImageUri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) selectImageFromGallery()
        else Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
    }

    private val getImageFromGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Log.d("GallerySelection", "Selected URI: $uri")
            Glide.with(this).load(uri).into(imageView)
        } else {
            Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        openGallerybutton = findViewById(R.id.openGalleryButton)
        imageView = findViewById(R.id.imageView)
        doOcrbutton = findViewById(R.id.doOcrButton)

        openGallerybutton.setOnClickListener { checkPermissionAndOpenGallery() }

        doOcrbutton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val response = doOcr() // suspend 함수 호출
                response?.let {
                    val intent = Intent(this@MainActivity, SecondActivity::class.java).apply {
                        putExtra("response", it) // OCR 결과값 전달
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun checkPermissionAndOpenGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            selectImageFromGallery()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun selectImageFromGallery() {
        getImageFromGallery.launch("image/*")
    }

    private suspend fun doOcr(): String? {
        if (selectedImageUri == null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "사진을 골라주세요.", Toast.LENGTH_SHORT).show()
            }
            return null
        }

        val file = getFileFromUri(selectedImageUri!!)
        if (file != null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "OCR을 실행합니다...", Toast.LENGTH_SHORT).show()
            }
            return callNaverOcrApi(file) // API 결과 반환
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "파일 변환 실패!", Toast.LENGTH_SHORT).show()
            }
            return null
        }
    }


    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: return null

            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(cacheDir, fileName)
            tempFile.outputStream().use { inputStream.copyTo(it) }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun callNaverOcrApi(file: File): String {
        val apiURL = BuildConfig.NAVER_OCR_API_URL
        val secretKey = BuildConfig.NAVER_OCR_API_KEY

        Log.d("url", "$apiURL")
        Log.d("key", "$secretKey")

        return withContext(Dispatchers.IO) {
            try {
                val url = URL(apiURL)
                val con = url.openConnection() as HttpURLConnection
                val boundary = "----" + UUID.randomUUID().toString().replace("-", "")

                con.apply {
                    useCaches = false
                    doInput = true
                    doOutput = true
                    readTimeout = 30000
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                    setRequestProperty("X-OCR-SECRET", secretKey)
                }

                val json = JSONObject().apply {
                    put("version", "V2")
                    put("requestId", UUID.randomUUID().toString())
                    put("timestamp", System.currentTimeMillis())
                    put("images", JSONArray().put(JSONObject().put("format", "jpg").put("name", "demo")))
                }

                DataOutputStream(con.outputStream).use { writeMultiPart(it, json.toString(), file, boundary) }

                val responseCode = con.responseCode
                val reader = BufferedReader(InputStreamReader(
                    if (responseCode == 200) con.inputStream else con.errorStream
                ))

                val response = reader.readText()
                reader.close()
                response
            } catch (e: Exception) {
                e.printStackTrace()
                "API 호출 실패: ${e.message}"
            }
        }
    }

    private fun writeMultiPart(out: OutputStream, jsonMessage: String, file: File, boundary: String) {
        val sb = StringBuilder()
        sb.append("--").append(boundary).append("\r\n")
        sb.append("Content-Disposition:form-data; name=\"message\"\r\n\r\n")
        sb.append(jsonMessage).append("\r\n")
        out.write(sb.toString().toByteArray(Charsets.UTF_8))

        if (file.exists()) {
            val fileString = "Content-Disposition:form-data; name=\"file\"; filename=\"${file.name}\"\r\nContent-Type: application/octet-stream\r\n\r\n"
            out.write(("--$boundary\r\n").toByteArray(Charsets.UTF_8))
            out.write(fileString.toByteArray(Charsets.UTF_8))

            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var count: Int
                while (fis.read(buffer).also { count = it } != -1) {
                    out.write(buffer, 0, count)
                }
            }
            out.write("\r\n--$boundary--\r\n".toByteArray(Charsets.UTF_8))
        }
        out.flush()
    }
}
