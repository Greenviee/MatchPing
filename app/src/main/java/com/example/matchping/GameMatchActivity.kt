package com.example.matchping

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameMatchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_match)

        val buttonBack = findViewById<ImageButton>(R.id.buttonBack)
        val buttonMatchOpponent = findViewById<Button>(R.id.buttonMatchOpponent)
        val buttonAddOpponent = findViewById<Button>(R.id.buttonAddOpponent)

        buttonBack.setOnClickListener {
            finish() // 또는 MainActivity로 이동
        }

        buttonMatchOpponent.setOnClickListener {
            // 저장된 상대 선택 화면으로 이동
            val intent = Intent(this, OpponentSelectActivity::class.java)
            startActivity(intent)
        }

        buttonAddOpponent.setOnClickListener {
            // 새 상대 등록 화면으로 이동
            val intent = Intent(this, OpponentAddActivity::class.java)
            startActivity(intent)
        }
    }
}
