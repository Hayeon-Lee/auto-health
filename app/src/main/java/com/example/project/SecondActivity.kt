package com.example.project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // Intent로부터 전달된 데이터 수신
        val response = intent.getStringExtra("response")

        // 값 확인 및 화면에 표시
        val textView = findViewById<TextView>(R.id.responseTextView)
        textView.text = response ?: "데이터 없음"
    }
}