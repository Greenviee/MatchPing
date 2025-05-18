package com.example.matchping

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameResultActivity : AppCompatActivity() {
    private lateinit var appDatabase: AppDatabase
    private lateinit var matchResultDao: MatchResultDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_result)

        appDatabase = AppDatabase.getDatabase(this)
        matchResultDao = appDatabase.matchResultDao()

        // 데이터 전달 받기
        val mySetScore = intent.getIntExtra("mySetScore", 0)
        val opponentSetScore = intent.getIntExtra("opponentSetScore", 0)
        val setHistory = intent.getStringExtra("setHistory") ?: ""
        val opponentName = intent.getStringExtra("opponentName") ?: ""
        val opponentUnit = intent.getStringExtra("opponentUnit") ?: ""
        val opponentTags = intent.getStringExtra("opponentTags") ?: ""

        // 화면 표시
        findViewById<TextView>(R.id.textOpponentInfo).text =
            "상대: $opponentName / $opponentUnit\n태그: $opponentTags"

        findViewById<TextView>(R.id.textSetScore).text =
            "최종 세트 점수: $mySetScore : $opponentSetScore"

        findViewById<TextView>(R.id.textSetHistory).text =
            "세트별 점수: ${setHistory.replace(",", "  ")}"

        findViewById<TextView>(R.id.textGameResult).text =
            when {
                mySetScore > opponentSetScore -> "승리!"
                mySetScore == opponentSetScore -> "무승부"
                else -> "패배"
            }

        // 전적 DB 저장 (Room)
        val myUid = FirebaseAuth.getInstance().currentUser?.uid
        val now = System.currentTimeMillis()

        lifecycleScope.launch(Dispatchers.IO) {
            val matchResult = MatchResult(
                myUid = myUid,
                opponentName = opponentName,
                opponentUnit = opponentUnit,
                date = now,
                result = when {
                    mySetScore > opponentSetScore -> "승"
                    mySetScore == opponentSetScore -> "무"
                    else -> "패"
                },
                mySetScore = mySetScore,
                opponentSetScore = opponentSetScore,
                detail = setHistory
            )
            matchResultDao.insert(matchResult)
        }

        // 다시하기: ScoreBoardActivity로 이동(상대 정보 유지)
        findViewById<Button>(R.id.buttonRematch).setOnClickListener {
            val intent = Intent(this, ScoreBoardActivity::class.java).apply {
                putExtra("opponentName", opponentName)
                putExtra("opponentUnit", opponentUnit)
                putExtra("opponentTags", opponentTags)
            }
            startActivity(intent)
            finish()
        }

        // 메인으로
        findViewById<Button>(R.id.buttonGoMain).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
    }
}
