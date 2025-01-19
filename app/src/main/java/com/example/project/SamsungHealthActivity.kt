package com.example.project

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.NutritionRecord
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.gson.Gson
import java.time.Instant
import java.time.ZoneOffset
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import android.content.Intent
import androidx.activity.OnBackPressedCallback

class SamsungHealthActivity : AppCompatActivity() {

    val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(NutritionRecord::class),
        HealthPermission.getWritePermission(NutritionRecord::class),
    )

    private lateinit var healthConnectClient: HealthConnectClient

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_samsung_health)

        healthConnectClient = HealthConnectClient.getOrCreate(this)

        val gson = Gson()
        val nutritionJson = intent.getStringExtra("nutritionData")
        Log.d("SamsungHealthActivityResult", "Nutrition Record JSON: $nutritionJson")

        val nutritionRecord: NutritionRecord? = if (nutritionJson != null) {
            try {
                // JSON에서 NutritionRecord 객체로 변환
                val tempRecord = gson.fromJson(nutritionJson, NutritionRecord::class.java)

                // 새로운 NutritionRecord 객체 생성
                NutritionRecord(
                    startTime = Instant.now(),
                    startZoneOffset = ZoneOffset.UTC,
                    endTime = Instant.now().plusSeconds(3600), // 1시간 동안의 데이터
                    endZoneOffset = ZoneOffset.UTC,
                    energy = tempRecord.energy ?: Energy.kilocalories(0.0),
                    totalCarbohydrate = tempRecord.totalCarbohydrate ?: Mass.grams(0.0),
                    totalFat = tempRecord.totalFat ?: Mass.grams(0.0),
                    protein = tempRecord.protein ?: Mass.grams(0.0),
                    saturatedFat = tempRecord.saturatedFat ?: Mass.grams(0.0),
                    transFat = tempRecord.transFat ?: Mass.grams(0.0),
                    cholesterol = tempRecord.cholesterol ?: Mass.milligrams(0.0),
                    sodium = tempRecord.sodium ?: Mass.milligrams(0.0),
                    potassium = tempRecord.potassium ?: Mass.milligrams(0.0),
                    dietaryFiber = tempRecord.dietaryFiber ?: Mass.grams(0.0),
                    sugar = tempRecord.sugar ?: Mass.grams(0.0),
                    vitaminA = tempRecord.vitaminA ?: Mass.micrograms(0.0),
                    vitaminC = tempRecord.vitaminC ?: Mass.milligrams(0.0),
                    calcium = tempRecord.calcium ?: Mass.milligrams(0.0),
                    iron = tempRecord.iron ?: Mass.milligrams(0.0),
                    name = tempRecord.name ?: "Example Meal", // 기본 값
                    mealType = tempRecord.mealType ?: 1 // 기본 값
                )
            } catch (e: Exception) {
                Log.e("SamsungHealthActivity", "Failed to parse JSON: ${e.message}", e)
                null
            }
        } else {
            null
        }

        lifecycleScope.launch {
            if (nutritionRecord != null) {
                insertReceivedNutritionRecord(nutritionRecord)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // OnBackPressedDispatcher 설정
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 첫 화면으로 이동하는 Intent
                val intent = Intent(this@SamsungHealthActivity, UploadImageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish() // 현재 Activity 종료
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    suspend fun insertReceivedNutritionRecord(nutritionRecord: NutritionRecord) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(PERMISSIONS)) {
            try {
                val records = mutableListOf<androidx.health.connect.client.records.Record>()
                records.add(nutritionRecord)
                healthConnectClient.insertRecords(records)
                Log.d("SamsungHealthActivity", "Nutrition record inserted successfully.")
            } catch (e: Exception) {
                Log.e("SamsungHealthActivity", "Failed to insert NutritionRecord", e)
            }
        } else {
            requestPermissions.launch(PERMISSIONS)
        }
    }

    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
    val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(PERMISSIONS)) {
            Log.d("SamsungHealthActivity", "Permissions granted.")
        } else {
            Log.d("SamsungHealthActivity", "Permissions not granted.")
        }
    }
}
