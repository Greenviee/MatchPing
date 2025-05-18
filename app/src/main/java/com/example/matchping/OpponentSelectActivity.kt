package com.example.matchping

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OpponentSelectActivity : AppCompatActivity() {
    private lateinit var listViewOpponents: ListView
    private lateinit var buttonSelectOpponent: Button
    private lateinit var appDatabase: AppDatabase
    private lateinit var opponentDao: OpponentDao

    private var opponentList: List<Opponent> = emptyList()
    private var selectedPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opponent_select)

        listViewOpponents = findViewById(R.id.listViewOpponents)
        buttonSelectOpponent = findViewById(R.id.buttonSelectOpponent)

        appDatabase = AppDatabase.getDatabase(this)
        opponentDao = appDatabase.opponentDao()

        loadOpponents()

        listViewOpponents.setOnItemClickListener { _, _, position, _ ->
            selectedPosition = position
            listViewOpponents.setItemChecked(position, true)
        }

        buttonSelectOpponent.setOnClickListener {
            if (selectedPosition == -1) {
                Toast.makeText(this, "상대를 선택하세요.", Toast.LENGTH_SHORT).show()
            } else {
                val selectedOpponent = opponentList[selectedPosition]
                // ScoreBoardActivity로 이동
                val intent = Intent(this, ScoreBoardActivity::class.java).apply {
                    putExtra("opponentName", selectedOpponent.name)
                    putExtra("opponentUnit", selectedOpponent.unit)
                    putExtra("opponentTags", selectedOpponent.tags)
                }
                startActivity(intent)
            }
        }
    }

    private fun loadOpponents() {
        lifecycleScope.launch(Dispatchers.IO) {
            opponentList = opponentDao.getAllOpponents()
            val opponentNames = opponentList.map { it.name + " (" + it.unit + ")" }
            runOnUiThread {
                val adapter = ArrayAdapter(this@OpponentSelectActivity, android.R.layout.simple_list_item_single_choice, opponentNames)
                listViewOpponents.adapter = adapter
                listViewOpponents.choiceMode = ListView.CHOICE_MODE_SINGLE
            }
        }
    }
}
