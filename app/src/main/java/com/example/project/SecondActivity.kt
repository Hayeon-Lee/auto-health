package com.example.project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import android.widget.Button
import android.widget.EditText
import android.content.Intent

class SecondActivity : AppCompatActivity() {

    private lateinit var sendSamsungHealthButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // Intent로부터 전달된 데이터 수신
        val response = intent.getStringExtra("response")

        val result = parseOcrResponse(response ?: "데이터 없음")

        // Map의 내용을 문자열로 변환
        val resultText = if (result.isNotEmpty()) {
            result.entries.joinToString(separator = "\n") { "${it.key}: ${it.value}" }
        } else {
            "데이터 없음"
        }

        // EditText 참조
        val editCalories = findViewById<EditText>(R.id.editCalories)
        val editCarbs = findViewById<EditText>(R.id.editCarbs)
        val editFat = findViewById<EditText>(R.id.editFat)
        val editProtein = findViewById<EditText>(R.id.editProtein)
        val editSaturatedFat = findViewById<EditText>(R.id.editSaturatedFat)
        val editTransFat = findViewById<EditText>(R.id.editTransFat)
        val editCholesterol = findViewById<EditText>(R.id.editCholesterol)
        val editSodium = findViewById<EditText>(R.id.editSodium)
        val editPotassium = findViewById<EditText>(R.id.editPotassium)
        val editFiber = findViewById<EditText>(R.id.editFiber)
        val editSugar = findViewById<EditText>(R.id.editSugar)
        val editVitaminA = findViewById<EditText>(R.id.editVitaminA)
        val editVitaminC = findViewById<EditText>(R.id.editVitaminC)
        val editCalcium = findViewById<EditText>(R.id.editCalcium)
        val editIron = findViewById<EditText>(R.id.editIron)

        // OCR 결과를 각 EditText에 채움
        editCalories.setText(result["총열량"]?.toString() ?: "")
        editCarbs.setText(result["탄수화물"]?.toString() ?: "")
        editFat.setText(result["지방"]?.toString() ?: "")
        editProtein.setText(result["단백질"]?.toString() ?: "")
        editSaturatedFat.setText(result["포화지방"]?.toString() ?: "")
        editTransFat.setText(result["트랜스지방"]?.toString() ?: "")
        editCholesterol.setText(result["콜레스테롤"]?.toString() ?: "")
        editSodium.setText(result["나트륨"]?.toString() ?: "")
        editPotassium.setText(result["칼륨"]?.toString() ?: "")
        editFiber.setText(result["식이섬유"]?.toString() ?: "")
        editSugar.setText(result["당류"]?.toString() ?: "")
        editVitaminA.setText(result["비타민A"]?.toString() ?: "")
        editVitaminC.setText(result["비타민C"]?.toString() ?: "")
        editCalcium.setText(result["칼슘"]?.toString() ?: "")
        editIron.setText(result["철분"]?.toString() ?: "")

        sendSamsungHealthButton = findViewById(R.id.sendSamsungHealthButton)
        sendSamsungHealthButton.setOnClickListener {
            var intent = Intent(this@SecondActivity, SamsungHealthActivity::class.java)
            startActivity(intent)
        }

    }

    fun parseOcrResponse(response: String): Map<String, Any> {
        val result = mutableMapOf<String, Any>() // 최종 결과 저장
        val nutritionalItems = listOf(
            "탄수화물", "지방", "단백질", "포화지방", "트랜스지방",
            "콜레스테롤", "나트륨", "칼륨", "식이섬유", "당류",
            "비타민A", "비타민C", "칼슘", "철분"
        )

        try {
            val jsonObject = JSONObject(response)
            val images = jsonObject.getJSONArray("images")
            if (images.length() > 0) {
                val firstImage = images.getJSONObject(0)
                if (firstImage.has("fields")) {
                    val fields = firstImage.getJSONArray("fields")

                    // fields 배열을 순회하면서 필요한 값 추출
                    for (i in 0 until fields.length()) {
                        val currentField = fields.getJSONObject(i)
                        val currentText = currentField.getString("inferText")

                        if (nutritionalItems.contains(currentText) && i + 1 < fields.length()) {
                            val nextField = fields.getJSONObject(i + 1)
                            val nextText = nextField.getString("inferText")
                            result[currentText] = nextText
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }

}


