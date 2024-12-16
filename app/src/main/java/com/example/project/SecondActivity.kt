package com.example.project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import android.util.Log
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import com.google.gson.Gson
import java.time.Instant
import java.time.ZoneOffset

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
        val editName = findViewById<EditText>(R.id.editName)
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
        editName.setText(result["이름"]?.toString() ?: "")
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
            // NutritionRecord에 필요한 데이터 수집
            val nutritionData = NutritionRecord(
                startTime = Instant.now(),
                startZoneOffset = ZoneOffset.UTC,
                endTime = Instant.now().plusSeconds(3600), // 1시간 동안의 데이터
                endZoneOffset = ZoneOffset.UTC,
                energy = Energy.kilocalories(editCalories.text.toString().toDoubleOrNull() ?: 0.0),
                totalCarbohydrate = Mass.grams(editCarbs.text.toString().toDoubleOrNull() ?: 0.0),
                totalFat = Mass.grams(editFat.text.toString().toDoubleOrNull() ?: 0.0),
                protein = Mass.grams(editProtein.text.toString().toDoubleOrNull() ?: 0.0),
                saturatedFat = Mass.grams(editSaturatedFat.text.toString().toDoubleOrNull() ?: 0.0),
                transFat = Mass.grams(editTransFat.text.toString().toDoubleOrNull() ?: 0.0),
                cholesterol = Mass.milligrams(editCholesterol.text.toString().toDoubleOrNull() ?: 0.0),
                sodium = Mass.milligrams(editSodium.text.toString().toDoubleOrNull() ?: 0.0),
                potassium = Mass.milligrams(editPotassium.text.toString().toDoubleOrNull() ?: 0.0),
                dietaryFiber = Mass.grams(editFiber.text.toString().toDoubleOrNull() ?: 0.0),
                sugar = Mass.grams(editSugar.text.toString().toDoubleOrNull() ?: 0.0),
                vitaminA = Mass.micrograms(editVitaminA.text.toString().toDoubleOrNull() ?: 0.0),
                vitaminC = Mass.milligrams(editVitaminC.text.toString().toDoubleOrNull() ?: 0.0),
                calcium = Mass.milligrams(editCalcium.text.toString().toDoubleOrNull() ?: 0.0),
                iron = Mass.milligrams(editIron.text.toString().toDoubleOrNull() ?: 0.0),
                name = editName.text.toString()?: null, // 이름은 고정 값 또는 사용자 입력 추가 가능
                mealType = 1
            )

            Log.d("name", "$nutritionData.name")

            // NutritionRecord를 JSON으로 변환하여 전달
            val gson = Gson()
            val nutritionJson = gson.toJson(nutritionData)

            val intent = Intent(this@SecondActivity, SamsungHealthActivity::class.java).apply {
                putExtra("nutritionData", nutritionJson)
            }
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

                            // 정규식을 사용해 숫자와 소수점만 추출
                            val numberMatch = Regex("""\d+(\.\d+)?""").find(nextText)
                            if (numberMatch != null) {
                                result[currentText] = numberMatch.value
                            }
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


