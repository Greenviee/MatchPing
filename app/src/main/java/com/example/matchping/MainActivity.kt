// src/main/java/com/example/matchping/MainActivity.kt
package com.example.matchping

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.matchping.ApiClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tvTip: TextView
    private lateinit var buttonFriend: ImageButton
    private lateinit var buttonStartGame: Button
    private lateinit var buttonRecord: Button
    private lateinit var buttonAnalysis: Button
    private lateinit var buttonUserInfo: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // View 초기화
        tvTip            = findViewById(R.id.tvTip)
        buttonFriend     = findViewById(R.id.buttonFriend)
        buttonStartGame  = findViewById(R.id.buttonStartGame)
        buttonRecord     = findViewById(R.id.buttonRecord)
        buttonAnalysis   = findViewById(R.id.buttonAnalysis)
        buttonUserInfo   = findViewById(R.id.buttonUserInfo)

        // 1) 앱 실행 시 탁구 팁(quote) 로드해서 tvTip에 표시
        lifecycleScope.launch {
            try {
                val resp = ApiClient.quoteApi.randomQuote().firstOrNull()
                if (resp != null) {
                    tvTip.text = "\"${resp.q}\"\n- ${resp.a}"
                } else {
                    tvTip.text = "탁구 팁을 불러올 수 없습니다."
                }
            } catch (e: Exception) {
                tvTip.text = "탁구 팁을 불러올 수 없습니다."
            }
        }

        // 2) 친구 버튼 클릭 → FriendActivity로 이동
        buttonFriend.setOnClickListener {
            startActivity(Intent(this, FriendActivity::class.java))
        }

        // 3) 나머지 메뉴 버튼 클릭 리스너
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
    }
}
