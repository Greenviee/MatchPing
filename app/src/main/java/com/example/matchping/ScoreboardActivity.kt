package com.example.matchping

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.abs

class ScoreBoardActivity : AppCompatActivity() {

    private var setNum = 1
    private val maxSet = 5
    private val setWinCount = 3
    private var mySetScore = 0
    private var opponentSetScore = 0

    private var myScore = 0
    private var opponentScore = 0
    private val setHistory = mutableListOf<Pair<Int, Int>>()

    private var isMyServe = true

    private var myHandicap = 0
    private var opponentHandicap = 0

    private var opponentName: String = ""
    private var opponentUnit: String = ""
    private var myName: String = ""
    private var myUnit: String = ""

    // 서브 반전 관리용 변수
    private var serveChangedAfterHandicap = false
    private var serveChangeStartScore = 0

    // UI
    private lateinit var textMyNameUnit: TextView
    private lateinit var textOpponentNameUnit: TextView
    private lateinit var textSetInfo: TextView
    private lateinit var textMySetScore: TextView
    private lateinit var textOpponentSetScore: TextView
    private lateinit var textMyScore: TextView
    private lateinit var textOpponentScore: TextView
    private lateinit var buttonMyScorePlus: Button
    private lateinit var buttonMyScoreMinus: Button
    private lateinit var buttonOpponentScorePlus: Button
    private lateinit var buttonOpponentScoreMinus: Button
    private lateinit var textServeInfo: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_board)

        textMyNameUnit = findViewById(R.id.textMyNameUnit)
        textOpponentNameUnit = findViewById(R.id.textOpponentNameUnit)
        textSetInfo = findViewById(R.id.textSetInfo)
        textMySetScore = findViewById(R.id.textMySetScore)
        textOpponentSetScore = findViewById(R.id.textOpponentSetScore)
        textMyScore = findViewById(R.id.textMyScore)
        textOpponentScore = findViewById(R.id.textOpponentScore)
        buttonMyScorePlus = findViewById(R.id.buttonMyScorePlus)
        buttonMyScoreMinus = findViewById(R.id.buttonMyScoreMinus)
        buttonOpponentScorePlus = findViewById(R.id.buttonOpponentScorePlus)
        buttonOpponentScoreMinus = findViewById(R.id.buttonOpponentScoreMinus)
        textServeInfo = findViewById(R.id.textServeInfo)
        progressBar = findViewById<ProgressBar?>(R.id.progressBar) ?: ProgressBar(this)

        opponentName = intent.getStringExtra("opponentName") ?: "상대"
        opponentUnit = intent.getStringExtra("opponentUnit") ?: ""

        progressBar.visibility = ProgressBar.VISIBLE
        fetchMyInfoAndInit()
    }

    private fun fetchMyInfoAndInit() {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUid == null) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        FirebaseFirestore.getInstance().collection("Users").document(currentUid)
            .get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                myUnit = user?.unit ?: ""
                myName = user?.name ?: "나"
                progressBar.visibility = ProgressBar.GONE

                if (myUnit.isEmpty()) {
                    Toast.makeText(this, "내 부수 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    setupScoreBoard()
                }
            }
            .addOnFailureListener {
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(this, "내 정보 불러오기 실패", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun setupScoreBoard() {
        textMyNameUnit.text = "$myName ($myUnit)"
        textOpponentNameUnit.text = "$opponentName ($opponentUnit)"

        // 핸디 계산
        myHandicap = 0
        opponentHandicap = 0
        try {
            val myUnitInt = unitStringToInt(myUnit)
            val oppUnitInt = unitStringToInt(opponentUnit)
            val diff = abs(myUnitInt - oppUnitInt)
            if (myUnitInt > oppUnitInt) {
                myHandicap = diff
            } else if (oppUnitInt > myUnitInt) {
                opponentHandicap = diff
            }
        } catch (_: Exception) {}

        myScore = myHandicap
        opponentScore = opponentHandicap

        val handicapSum = myHandicap + opponentHandicap
        serveChangedAfterHandicap = false
        serveChangeStartScore = myScore + opponentScore

        fun updateUI() {
            textSetInfo.text = "세트: $setNum / $maxSet"
            textMySetScore.text = "$mySetScore"
            textOpponentSetScore.text = "$opponentSetScore"
            textMyScore.text = "$myScore"
            textOpponentScore.text = "$opponentScore"
            textServeInfo.text = if (isMyServe) "내 서브" else "상대 서브"
        }

        fun resetSetScore() {
            myScore = myHandicap
            opponentScore = opponentHandicap
            serveChangedAfterHandicap = false
            serveChangeStartScore = myScore + opponentScore
            isMyServe = true
        }

        fun checkServeChange() {
            val totalScore = myScore + opponentScore
            val isDeuce = myScore >= 10 && opponentScore >= 10

            // 핸디 홀수: 최초 한 번, 합이 짝수인 순간 즉시 반전 & 그 점수에서는 추가 계산 없이 return
            if (!serveChangedAfterHandicap && handicapSum % 2 == 1 && totalScore > handicapSum && totalScore % 2 == 0) {
                isMyServe = !isMyServe
                serveChangedAfterHandicap = true
                serveChangeStartScore = totalScore
                return
            }

            // serveChangeStartScore까지는 서브 유지
            if (serveChangedAfterHandicap && totalScore <= serveChangeStartScore) {
                return
            }

            if (isDeuce) {
                isMyServe = (totalScore % 2 == 0)
            } else {
                val baseScore = if (handicapSum % 2 == 1 && serveChangedAfterHandicap) serveChangeStartScore else handicapSum
                isMyServe = ((totalScore - baseScore) / 2 % 2 == 0)
            }
        }

        fun checkSetEnd() {
            val minScore = 11
            val diff = abs(myScore - opponentScore)
            val isDeuce = myScore >= 10 && opponentScore >= 10
            if ((myScore >= minScore || opponentScore >= minScore) && diff >= 2) {
                setHistory.add(Pair(myScore, opponentScore))
                if (myScore > opponentScore) mySetScore++ else opponentSetScore++
                if (mySetScore == setWinCount || opponentSetScore == setWinCount || setNum == maxSet) {
                    showResult()
                    return
                }
                setNum++
                resetSetScore()
                updateUI()
                Toast.makeText(this, "다음 세트로 넘어갑니다!", Toast.LENGTH_SHORT).show()
            }
        }

        buttonMyScorePlus.setOnClickListener {
            myScore++
            checkServeChange()
            updateUI()
            checkSetEnd()
        }
        buttonMyScoreMinus.setOnClickListener {
            if (myScore > myHandicap) {
                myScore--
                checkServeChange()
                updateUI()
            }
        }
        buttonOpponentScorePlus.setOnClickListener {
            opponentScore++
            checkServeChange()
            updateUI()
            checkSetEnd()
        }
        buttonOpponentScoreMinus.setOnClickListener {
            if (opponentScore > opponentHandicap) {
                opponentScore--
                checkServeChange()
                updateUI()
            }
        }

        resetSetScore()
        updateUI()
    }

    private fun showResult() {
        val intent = Intent(this, GameResultActivity::class.java).apply {
            putExtra("mySetScore", mySetScore)
            putExtra("opponentSetScore", opponentSetScore)
            putExtra("setHistory", setHistory.map { "${it.first}:${it.second}" }.joinToString(","))
            putExtra("opponentName", opponentName)
            putExtra("opponentUnit", opponentUnit)
        }
        startActivity(intent)
        finish()
    }

    private fun unitStringToInt(unit: String): Int {
        return when (unit) {
            "선수부" -> 0
            "1부" -> 1
            "2부" -> 2
            "3부" -> 3
            "4부" -> 4
            "5부" -> 5
            "6부" -> 6
            "7부" -> 7
            "8부" -> 8
            else -> 99
        }
    }
}
