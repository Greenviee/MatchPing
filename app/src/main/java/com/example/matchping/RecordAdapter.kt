package com.example.matchping

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class RecordAdapter : ListAdapter<MatchResult, RecordAdapter.VH>(diff) {

    companion object {
        private val diff = object : DiffUtil.ItemCallback<MatchResult>() {
            override fun areItemsTheSame(o: MatchResult, n: MatchResult) = o.id == n.id
            override fun areContentsTheSame(o: MatchResult, n: MatchResult) = o == n
        }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvName  = v.findViewById<TextView>(R.id.textOpponent)
        private val tvDate  = v.findViewById<TextView>(R.id.textDate)
        private val tvScore = v.findViewById<TextView>(R.id.textScore)
        private val tvTags  = v.findViewById<TextView>(R.id.textTags)
        private val fmt = SimpleDateFormat("yy.MM.dd", Locale.KOREA)

        fun bind(m: MatchResult) {
            tvName.text  = m.opponentName
            tvScore.text = "${m.mySetScore} : ${m.opponentSetScore}"
            tvDate.text  = fmt.format(Date(m.date))
            tvTags.apply {
                text  = m.tags.joinToString(", ")
                alpha = 0.6f
            }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(LayoutInflater.from(p.context)
            .inflate(R.layout.item_match_result, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
}
