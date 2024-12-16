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

    private lateinit var healthConnectClient: HealthConnectClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_samsung_health)

        healthConnectClient = HealthConnectClient.getOrCreate(this)

        val gson = Gson()
        val nutritionJson = intent.getStringExtra("nutritionData")
        Log.d("SamsungHealthActivityResult", "Nutrition Record JSON: $nutritionJson")
        val nutritionRecord: NutritionRecord? = if (nutritionJson != null) {
            gson.fromJson(nutritionJson, NutritionRecord::class.java)
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
    }

    suspend fun insertReceivedNutritionRecord(nutritionRecord: NutritionRecord) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(PERMISSIONS)) {
            try {
                healthConnectClient.insertRecords(listOf(nutritionRecord))
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
