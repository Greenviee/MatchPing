package com.example.matchping

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameResultActivity : AppCompatActivity() {
    private lateinit var matchResultDao: MatchResultDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_result)

        matchResultDao = AppDatabase.getDatabase(this).matchResultDao()

        // 1) Intent 로부터 값 꺼내기
        val mySetScore       = intent.getIntExtra("mySetScore", 0)
        val opponentSetScore = intent.getIntExtra("opponentSetScore", 0)
        val setHistory       = intent.getStringExtra("setHistory") ?: ""
        val opponentName     = intent.getStringExtra("opponentName") ?: ""
        val opponentUnit     = intent.getStringExtra("opponentUnit") ?: ""
        val opponentTagsStr  = intent.getStringExtra("opponentTags") ?: ""

        // 2) 태그 스트링 → 리스트
        val selectedTags = opponentTagsStr
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // 3) 화면 표시
        findViewById<TextView>(R.id.textOpponentInfo).text =
            "상대: $opponentName / $opponentUnit\n태그: ${selectedTags.joinToString(", ")}"
        findViewById<TextView>(R.id.textSetScore).text =
            "최종 세트 점수: $mySetScore : $opponentSetScore"
        findViewById<TextView>(R.id.textSetHistory).text =
            "세트별 점수: ${setHistory.replace(",", "  ")}"
        findViewById<TextView>(R.id.textGameResult).text =
            when {
                mySetScore > opponentSetScore -> "승리!"
                mySetScore == opponentSetScore -> "무승부"
                else                           -> "패배"
            }

        // 4) DB 저장
        val myUid = FirebaseAuth.getInstance().currentUser?.uid
        val now   = System.currentTimeMillis()
        lifecycleScope.launch(Dispatchers.IO) {
            val mr = MatchResult(
                myUid = myUid,
                opponentName = opponentName,
                opponentUnit = opponentUnit,
                date = now,
                result = when {
                    mySetScore > opponentSetScore -> "승"
                    mySetScore == opponentSetScore -> "무"
                    else                           -> "패"
                },
                mySetScore = mySetScore,
                opponentSetScore = opponentSetScore,
                detail = setHistory,
                tags = selectedTags
            )
            matchResultDao.insert(mr)
        }

        // 5) 다시하기
        findViewById<Button>(R.id.buttonRematch).setOnClickListener {
            val intent = Intent(this, ScoreBoardActivity::class.java).apply {
                putExtra("opponentName", opponentName)
                putExtra("opponentUnit", opponentUnit)
                putExtra("opponentTags", opponentTagsStr)
            }
            startActivity(intent)
            finish()
        }

        // 6) 메인으로
        findViewById<Button>(R.id.buttonGoMain).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }
    }
}
