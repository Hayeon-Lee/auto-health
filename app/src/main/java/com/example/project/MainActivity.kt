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
import android.content.Intent
import android.util.Log
import java.io.File
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import android.provider.OpenableColumns


class MainActivity : ComponentActivity() {

    private lateinit var openGallerybutton: Button
    private lateinit var imageView: ImageView
    private lateinit var doOcrbutton: Button
    private var selectedImageUri: Uri? = null

    // Activity Result API를 활용한 권한 요청
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            selectImageFromGallery()
        } else {
            Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // Activity Result API를 활용한 갤러리 결과 처리
    private val getImageFromGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Glide를 사용하여 이미지 로드
            selectedImageUri = uri
            Log.d("GallerySelection", "Selected URI: $uri")
            Glide.with(this)
                .load(uri)
                .into(imageView)
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

        openGallerybutton.setOnClickListener {
            checkPermissionAndOpenGallery()
        }

        doOcrbutton.setOnClickListener {
            doOcr()
            val intent = Intent(this, SecondActivity::class.java).apply {
                putExtra("key", "value")
            }
            startActivity(intent)
        }
    }

    private fun checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                selectImageFromGallery()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                selectImageFromGallery()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun selectImageFromGallery() {
        // 갤러리 열기
        getImageFromGallery.launch("image/*")
    }

    private fun doOcr() {
        val file = getFileFromUri(selectedImageUri!!)
        if (file == null) {
            Toast.makeText(this, "파일 변환 실패!", Toast.LENGTH_SHORT).show()
            return
        }
        else {
            Log.d("FileUriSuccess", "이미지 uri 파일 변환 성공!")
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            // ContentResolver를 사용하여 파일 이름과 입력 스트림 가져오기
            val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: return null

            // 캐시 디렉토리에 파일 복사
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(cacheDir, fileName)
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            tempFile // 복사된 임시 파일 반환
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
