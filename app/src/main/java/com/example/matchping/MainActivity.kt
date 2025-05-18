package com.example.matchping

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonStartGame = findViewById<Button>(R.id.buttonStartGame)
        val buttonRecord = findViewById<Button>(R.id.buttonRecord)
        val buttonAnalysis = findViewById<Button>(R.id.buttonAnalysis)
        val buttonUserInfo = findViewById<Button>(R.id.buttonUserInfo)

        buttonStartGame.setOnClickListener {
            startActivity(Intent(this, GameMatchActivity::class.java))
        }
        buttonRecord.setOnClickListener {
            startActivity(Intent(this, RecordActivity::class.java))
        }
        buttonAnalysis.setOnClickListener {
            startActivity(Intent(this, AnalysisActivity::class.java))
        }
        buttonUserInfo.setOnClickListener {
            startActivity(Intent(this, UserInfoActivity::class.java))
        }

        findViewById<ImageButton>(R.id.buttonFriend).setOnClickListener {
            startActivity(Intent(this, FriendActivity::class.java))
        }

    }
}
