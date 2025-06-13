package com.example.matchping

import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AnalysisActivity : AppCompatActivity() {

    private val vm: RecordViewModel by viewModels()

    private lateinit var radarChart: RadarChart
    private lateinit var spinnerOpponentRank: Spinner
    private lateinit var chipGroupPredict: ChipGroup
    private lateinit var btnPredict: Button
    private lateinit var btnGenerateDummy: Button
    private lateinit var tvPredicted: TextView

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private var myRank: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        radarChart          = findViewById(R.id.radar_chart_abilities)
        spinnerOpponentRank = findViewById(R.id.spinner_opponent_rank)
        chipGroupPredict    = findViewById(R.id.chip_group_predict_tags)
        btnPredict          = findViewById(R.id.button_predict)
        btnGenerateDummy    = findViewById(R.id.button_generate_dummy)
        tvPredicted         = findViewById(R.id.text_predicted_win)

        // 내 부수 로드
        btnPredict.isEnabled = false
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "로그인 정보 없음", Toast.LENGTH_SHORT).show()
            finish(); return
        }
        db.collection("Users").document(uid).get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                myRank = RecordViewModel.unitStringToInt(user?.unit ?: "선수부")
                btnPredict.isEnabled = true
            }
            .addOnFailureListener {
                Toast.makeText(this, "사용자 정보 로드 실패", Toast.LENGTH_SHORT).show()
            }

        // 상대 부수 Spinner
        spinnerOpponentRank.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("선수부") + (1..8).map { "${it}부" }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // 태그 칩 세팅 (쉐이크, 펜홀더 단독 선택 로직 유지)
        var shakeChip: Chip?   = null
        var penholdChip: Chip? = null
        RecordViewModel.ALL_TAGS.forEach { tag ->
            val chip = Chip(this).apply { text = tag; isCheckable = true }
            if (tag == "쉐이크") {
                shakeChip = chip
                chip.setOnCheckedChangeListener { _, c ->
                    if (c) penholdChip?.isChecked = false
                    else if (penholdChip?.isChecked != true) chip.isChecked = true
                }
            }
            if (tag == "펜홀더") {
                penholdChip = chip
                chip.setOnCheckedChangeListener { _, c ->
                    if (c) shakeChip?.isChecked = false
                    else if (shakeChip?.isChecked != true) chip.isChecked = true
                }
            }
            chipGroupPredict.addView(chip)
        }
        shakeChip?.isChecked = true

        // 전적 관찰 → 차트
        vm.matches.observe(this) {
            val scores = vm.calculateAbilityScores(myRank)
            drawRadarChart(scores)
        }
        vm.loadRecent()

        // 예측 버튼 (입력 구조만 변경)
        btnPredict.setOnClickListener {
            val oppRank = spinnerOpponentRank.selectedItemPosition
            val tags    = chipGroupPredict.checkedChipIds.map { id ->
                findViewById<Chip>(id).text.toString()
            }
            val p = vm.predictWithTFLite(tags, myRank, oppRank) * 100
            tvPredicted.text = "예상 승률: %.1f%%".format(p)
        }

        // 더미 생성 버튼
        btnGenerateDummy.setOnClickListener {
            vm.generateDummyMatches(500, myRank)
        }
    }

    private fun drawRadarChart(scores: RecordViewModel.AbilityScores) {
        // 데이터 엔트리
        val entries = listOf(
            scores.leftHandAdapt,
            scores.penholdAdapt,
            scores.serveAdapt,
            scores.confidence,
            scores.overallWinRate
        ).map { RadarEntry(it * 100) }

        // 라벨
        val labels = listOf(
            "왼손 대응력","펜홀더 대응",
            "서브 대응","자신감","승률"
        )

        // 데이터셋 스타일
        val dataSet = RadarDataSet(entries, "").apply {
            // 선 색상 & 두께
            color = ContextCompat.getColor(this@AnalysisActivity, R.color.primary_blue)
            lineWidth = 2.5f
            // 채우기 색상 & 투명도
            fillColor = ContextCompat.getColor(this@AnalysisActivity, R.color.primary_blue)
            setDrawFilled(true)
            fillAlpha = 180
            // 점(원) 스타일
            setDrawHighlightCircleEnabled(true)
            highlightCircleStrokeWidth = 2f
            setDrawHighlightIndicators(false)
            // 값 텍스트 숨기기
            setDrawValues(false)
        }

        // RadarData 에 연결
        val radarData = RadarData(dataSet).apply {
            // 폰트
            setValueTextSize(0f) // 값 표시 안함
        }

        radarChart.apply {
            // ① Description(“Description Label”) 끄기
            description.isEnabled = false

            // ② Legend(파란 박스) 끄기
            legend.isEnabled = false
        }


        // X축(라벨) 스타일
        radarChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            textSize = 14f
            //typeface = ResourcesCompat.getFont(this@AnalysisActivity, R.font.roboto_medium)
            textColor = Color.DKGRAY
        }

        // Y축(축 범위) 스타일
        radarChart.yAxis.apply {
            axisMinimum = 0f
            axisMaximum = 100f
            setDrawLabels(false) // 숫자 레이블 숨기기
            // 그리드 라인 스타일만 유지
        }

        // 데이터 적용 & 갱신
        radarChart.data = radarData
        radarChart.invalidate()
    }
}
