// RecordActivity.kt
package com.example.matchping

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class RecordActivity : AppCompatActivity() {

    private val vm: RecordViewModel by viewModels()

    private lateinit var rv: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var spinnerUnits: Spinner
    private lateinit var chips: ChipGroup
    private lateinit var tvWinRate: TextView
    private lateinit var adapter: RecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        rv           = findViewById(R.id.recyclerView)
        etSearch     = findViewById(R.id.editTextSearch)
        btnSearch    = findViewById(R.id.buttonSearch)
        spinnerUnits = findViewById(R.id.spinnerUnits)
        chips        = findViewById(R.id.chipGroupTags)
        tvWinRate    = findViewById(R.id.textViewWinRate)

        // RecyclerView 세팅
        adapter = RecordAdapter()
        rv.adapter       = adapter
        rv.layoutManager = LinearLayoutManager(this)

        // 부수 Spinner 에 어댑터 연결
        val unitsWithDefault = listOf("부수 선택") + RecordViewModel.ALL_UNITS
        ArrayAdapter(this,
            android.R.layout.simple_spinner_item,
            unitsWithDefault
        ).also { arr ->
            arr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerUnits.adapter = arr
        }

        spinnerUnits.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (position == 0) {
                    // "부수 선택" 기본
                    vm.loadRecent()
                } else {
                    // 실제 부수 리스트 인덱스는 position-1
                    val unit = RecordViewModel.ALL_UNITS[position - 1]
                    vm.searchByUnit(unit)
                }
                // EditText 검색 결과는 초기화
                etSearch.text.clear()
                spinnerUnits.clearFocus()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무것도 안 함
            }
        }

        // 태그칩 초기화
        RecordViewModel.ALL_TAGS.forEach { tag ->
            chips.addView(Chip(this).apply {
                text        = tag
                isCheckable = true
                setOnClickListener {
                    vm.filterByTag(tag)
                    // Spinner 선택 초기화
                    spinnerUnits.setSelection(0)
                    etSearch.text.clear()
                }
            })
        }

        // 기본 로드 & 옵저버 연결
        vm.loadRecent()
        vm.matches.observe(this)       { applyList(it) }
        vm.searchResults.observe(this) { applyList(it) }

        // 이름 검색 버튼 클릭
        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            if (query.isNotBlank()) {
                vm.searchByName(query)
                // Spinner 초기화
                spinnerUnits.setSelection(0)
            } else {
                vm.loadRecent()
            }
            // ChipGroup 선택 초기화
            chips.clearCheck()
        }
    }

    private fun applyList(list: List<MatchResult>) {
        adapter.submitList(list)
        val wins   = list.count { it.mySetScore > it.opponentSetScore }
        val losses = list.count { it.mySetScore < it.opponentSetScore }
        val rate   = if (wins + losses > 0) wins * 100 / (wins + losses) else 0
        tvWinRate.text = "승: $wins, 패: $losses, 승률: $rate%"
    }
}
