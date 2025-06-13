package com.example.matchping

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OpponentAddActivity : AppCompatActivity() {
    private lateinit var appDatabase: AppDatabase
    private lateinit var opponentDao: OpponentDao
    private val tagList = RecordViewModel.ALL_TAGS
    private val unitList = listOf("선수부", "1부", "2부", "3부", "4부", "5부", "6부", "7부", "8부")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opponent_add)

        appDatabase = AppDatabase.getDatabase(this)
        opponentDao = appDatabase.opponentDao()

        val editTextOpponentName = findViewById<EditText>(R.id.editTextOpponentName)
        val spinnerOpponentUnit = findViewById<Spinner>(R.id.spinnerOpponentUnit)
        val layoutOpponentTags = findViewById<LinearLayout>(R.id.layoutOpponentTags)
        val buttonAddOpponent = findViewById<Button>(R.id.buttonAddOpponent)

        // Spinner 부수 항목 연결
        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, unitList)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOpponentUnit.adapter = unitAdapter

        // 태그 버튼 세로 배치
        val tagToggleMap = mutableMapOf<String, ToggleButton>()
        tagList.forEach { tagName ->
            val toggle = ToggleButton(this).apply {
                textOn = tagName
                textOff = tagName
                text = tagName
                isChecked = false
                setBackgroundColor(0xFFE0E0E0.toInt())
                setTextColor(0xFF444444.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        setBackgroundColor(0xFF2196F3.toInt())
                        setTextColor(0xFFFFFFFF.toInt())
                    } else {
                        setBackgroundColor(0xFFE0E0E0.toInt())
                        setTextColor(0xFF444444.toInt())
                    }
                }
            }
            layoutOpponentTags.addView(toggle)
            tagToggleMap[tagName] = toggle
        }

        buttonAddOpponent.setOnClickListener {
            val name = editTextOpponentName.text.toString().trim()
            val unit = spinnerOpponentUnit.selectedItem.toString()
            val selectedTags = tagList.filter { tagToggleMap[it]?.isChecked == true }
            val tagsStr = selectedTags.joinToString(",")

            if (name.isEmpty()) {
                Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val opponent = Opponent(
                name = name,
                unit = unit,
                tags = tagsStr
            )
            lifecycleScope.launch(Dispatchers.IO) {
                opponentDao.insert(opponent)
                runOnUiThread {
                    Toast.makeText(this@OpponentAddActivity, "상대가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@OpponentAddActivity, OpponentSelectActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
