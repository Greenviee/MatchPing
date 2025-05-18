package com.example.matchping

import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class RecordActivity : AppCompatActivity() {

    private val viewModel: RecordViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var chipGroup: ChipGroup
    private lateinit var winRateText: TextView
    private lateinit var adapter: RecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.editTextSearch)
        searchButton = findViewById(R.id.buttonSearch)
        chipGroup = findViewById(R.id.chipGroupTags)
        winRateText = findViewById(R.id.textViewWinRate)

        adapter = RecordAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 태그 chip 동적 추가 (예시 태그)
        val tagList = listOf("왼손잡이", "쉐이크", "횡서브", "커트", "빠른플레이")
        for (tag in tagList) {
            val chip = Chip(this).apply {
                text = tag
                isCheckable = true
            }
            chipGroup.addView(chip)
        }

        viewModel.loadRecent()

        viewModel.matches.observe(this, Observer {
            adapter.submitList(it)
            showWinRate(it)
        })
        viewModel.searchResults.observe(this, Observer {
            adapter.submitList(it)
            showWinRate(it)
        })

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            if (query.isNotBlank()) {
                viewModel.searchByName(query)
            }
        }

        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == -1) {
                viewModel.loadRecent()
            } else {
                val chip = findViewById<Chip>(checkedId)
                viewModel.filterByTag(chip.text?.toString() ?: "")
            }
        }
    }

    private fun showWinRate(list: List<MatchResult>) {
        val win = list.count { it.mySetScore > it.opponentSetScore }
        val lose = list.count { it.mySetScore < it.opponentSetScore }
        val rate = if (win + lose > 0) (win * 100 / (win + lose)) else 0
        winRateText.text = "승: $win, 패: $lose, 승률: $rate%"
    }
}
