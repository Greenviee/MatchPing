package com.example.matchping

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.abs

class ScoreBoardActivity : AppCompatActivity() {

    private var setNum = 1; private val maxSet = 5; private val setWinCount = 3
    private var mySetScore = 0; private var opponentSetScore = 0
    private var myScore = 0; private var opponentScore = 0
    private val setHistory = mutableListOf<Pair<Int, Int>>()
    private var isMyServe = true
    private var myHandicap = 0; private var opponentHandicap = 0

    private var opponentName = ""
    private var opponentUnit = ""
    private var opponentTagsString = ""
    private var myName = ""; private var myUnit = ""

    // UI
    private lateinit var tvMyNameUnit: TextView
    private lateinit var tvOppNameUnit: TextView
    private lateinit var tvSetInfo: TextView
    private lateinit var tvMySetScore: TextView
    private lateinit var tvOppSetScore: TextView
    private lateinit var tvMyScore: TextView
    private lateinit var tvOppScore: TextView
    private lateinit var btnMyPlus: Button
    private lateinit var btnMyMinus: Button
    private lateinit var btnOppPlus: Button
    private lateinit var btnOppMinus: Button
    private lateinit var tvServeInfo: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_board)

        tvMyNameUnit    = findViewById(R.id.textMyNameUnit)
        tvOppNameUnit   = findViewById(R.id.textOpponentNameUnit)
        tvSetInfo       = findViewById(R.id.textSetInfo)
        tvMySetScore    = findViewById(R.id.textMySetScore)
        tvOppSetScore   = findViewById(R.id.textOpponentSetScore)
        tvMyScore       = findViewById(R.id.textMyScore)
        tvOppScore      = findViewById(R.id.textOpponentScore)
        btnMyPlus       = findViewById(R.id.buttonMyScorePlus)
        btnMyMinus      = findViewById(R.id.buttonMyScoreMinus)
        btnOppPlus      = findViewById(R.id.buttonOpponentScorePlus)
        btnOppMinus     = findViewById(R.id.buttonOpponentScoreMinus)
        tvServeInfo     = findViewById(R.id.textServeInfo)
        progressBar     = findViewById(R.id.progressBar)

        opponentName       = intent.getStringExtra("opponentName") ?: "상대"
        opponentUnit       = intent.getStringExtra("opponentUnit") ?: ""
        opponentTagsString = intent.getStringExtra("opponentTags") ?: ""

        progressBar.visibility = ProgressBar.VISIBLE
        fetchMyInfoAndInit()
    }

    private fun fetchMyInfoAndInit() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) { finish(); return }
        FirebaseFirestore.getInstance()
            .collection("Users").document(uid)
            .get()
            .addOnSuccessListener {
                val u = it.toObject(User::class.java)
                myUnit = u?.unit ?: ""
                myName = u?.name ?: "나"
                progressBar.visibility = ProgressBar.GONE
                setupScoreBoard()
            }
            .addOnFailureListener { finish() }
    }

    private fun setupScoreBoard() {
        tvMyNameUnit.text  = "$myName ($myUnit)"
        tvOppNameUnit.text = "$opponentName ($opponentUnit)"

        val myUnitInt  = unitStringToInt(myUnit)
        val oppUnitInt = unitStringToInt(opponentUnit)
        val diff = abs(myUnitInt - oppUnitInt)
        if (myUnitInt > oppUnitInt) myHandicap = diff
        else if (oppUnitInt > myUnitInt) opponentHandicap = diff

        myScore       = myHandicap
        opponentScore = opponentHandicap

        btnMyPlus.setOnClickListener    { myScore++; onScoreChanged() }
        btnMyMinus.setOnClickListener   { if (myScore > myHandicap) { myScore--; onScoreChanged() } }
        btnOppPlus.setOnClickListener   { opponentScore++; onScoreChanged() }
        btnOppMinus.setOnClickListener  { if (opponentScore > opponentHandicap) { opponentScore--; onScoreChanged() } }

        updateUI()
    }

    private fun onScoreChanged() {
        checkServeChange()
        updateUI()
        checkSetEnd()
    }

    private fun checkServeChange() {
        val total = myScore + opponentScore
        val isDeuce = myScore >= 10 && opponentScore >= 10
        val sumH = myHandicap + opponentHandicap

        if (sumH % 2 == 1) {
            // 홀수 핸디: sumH 넘고, total 짝수일 때만 toggle
            if (total > sumH && total % 2 == 0) isMyServe = !isMyServe
            return
        }
        if (isDeuce) {
            isMyServe = (total % 2 == 0)
        } else {
            val os = total - sumH
            if (os >= 0) isMyServe = ((os / 2) % 2 == 0)
        }
    }

    private fun updateUI() {
        tvSetInfo.text       = "세트: $setNum / $maxSet"
        tvMySetScore.text    = "$mySetScore"
        tvOppSetScore.text   = "$opponentSetScore"
        tvMyScore.text       = "$myScore"
        tvOppScore.text      = "$opponentScore"
        tvServeInfo.text     = if (isMyServe) "내 서브" else "상대 서브"
    }

    private fun checkSetEnd() {
        val minScore = 11
        val diff = abs(myScore - opponentScore)
        if ((myScore >= minScore || opponentScore >= minScore) && diff >= 2) {
            setHistory.add(myScore to opponentScore)
            if (myScore > opponentScore) mySetScore++ else opponentSetScore++
            if (mySetScore == setWinCount || opponentSetScore == setWinCount || setNum == maxSet) {
                finishGame()
                return
            }
            setNum++
            myScore = myHandicap
            opponentScore = opponentHandicap
            isMyServe = true
            updateUI()
            Toast.makeText(this, "다음 세트!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun finishGame() {
        startActivity(Intent(this, GameResultActivity::class.java).apply {
            putExtra("mySetScore", mySetScore)
            putExtra("opponentSetScore", opponentSetScore)
            putExtra("setHistory", setHistory.joinToString(",") { "${it.first}:${it.second}" })
            putExtra("opponentName", opponentName)
            putExtra("opponentUnit", opponentUnit)
            putExtra("opponentTags", opponentTagsString)
        })
        finish()
    }

    private fun unitStringToInt(unit: String): Int = when (unit) {
        "선수부" -> 0; "1부" -> 1; "2부" -> 2; "3부" -> 3; "4부" -> 4
        "5부" -> 5; "6부" -> 6; "7부" -> 7; "8부" -> 8
        else -> 99
    }
}
