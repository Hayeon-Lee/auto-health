package com.example.project

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
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

class SamsungHealthActivity : AppCompatActivity() {

    val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(NutritionRecord::class),
        HealthPermission.getWritePermission(NutritionRecord::class),
    )

    private lateinit var healthConnectClient: HealthConnectClient // lateinit으로 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_samsung_health)

        val gson = Gson()
        val nutritionJson = intent.getStringExtra("nutritionData")
        if (nutritionJson != null) {
            val nutritionRecord = gson.fromJson(nutritionJson, NutritionRecord::class.java)
            // nutritionRecord를 사용하여 화면에 표시하거나 처리
            Log.d("SamsungHealthActivity", "Received nutrition data: $nutritionRecord")
        }
        // HealthConnectClient 초기화
        healthConnectClient = HealthConnectClient.getOrCreate(this)

        // 권한 확인 및 요청
        lifecycleScope.launch { // Coroutine 사용
            checkPermissionsAndRun()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    suspend fun checkPermissionsAndRun() {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(PERMISSIONS)) {
            // 권한이 이미 부여됨; 데이터 삽입 또는 읽기 작업 수행
        } else {
            // 권한 요청
            requestPermissions.launch(PERMISSIONS)
        }
    }

    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
    val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(PERMISSIONS)) {
            // 권한이 성공적으로 부여됨
        } else {
            // 필요한 권한이 부족함
        }
    }
}
