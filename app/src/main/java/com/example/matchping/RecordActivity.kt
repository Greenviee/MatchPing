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

    private val vm: RecordViewModel by viewModels()
    private lateinit var rv: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var chips: ChipGroup
    private lateinit var tvWinRate: TextView
    private lateinit var adapter: RecordAdapter

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_record)

        rv = findViewById(R.id.recyclerView)
        etSearch = findViewById(R.id.editTextSearch)
        btnSearch = findViewById(R.id.buttonSearch)
        chips = findViewById(R.id.chipGroupTags)
        tvWinRate = findViewById(R.id.textViewWinRate)

        adapter = RecordAdapter()
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)

        val tagList = listOf(
            "왼손잡이", "쉐이크", "펜홀더", "너클 서브", "전진 서브", "커트 서브", "드라이브", "속공", "수비형"
        )
        tagList.forEach { t ->
            chips.addView(Chip(this).apply {
                text = t; isCheckable = true
            })
        }

        vm.loadRecent()
        vm.matches.observe(this, Observer { applyList(it) })
        vm.searchResults.observe(this, Observer { applyList(it) })

        btnSearch.setOnClickListener {
            val q = etSearch.text.toString().trim()
            if (q.isNotBlank()) vm.searchByName(q)
            else vm.loadRecent()
        }

        chips.setOnCheckedChangeListener { _, id ->
            if (id == -1) vm.loadRecent()
            else {
                val chip = findViewById<Chip>(id)
                vm.filterByTag(chip.text.toString())
            }
        }
    }

    private fun applyList(list: List<MatchResult>) {
        adapter.submitList(list)
        val w = list.count { it.mySetScore > it.opponentSetScore }
        val l = list.count { it.mySetScore < it.opponentSetScore }
        val r = if (w + l > 0) (w * 100 / (w + l)) else 0
        tvWinRate.text = "승: $w, 패: $l, 승률: $r%"
    }
}
