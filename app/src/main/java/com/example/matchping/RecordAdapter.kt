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

class RecordAdapter : ListAdapter<MatchResult, RecordAdapter.RecordViewHolder>(diffCallback) {
    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<MatchResult>() {
            override fun areItemsTheSame(oldItem: MatchResult, newItem: MatchResult) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: MatchResult, newItem: MatchResult) = oldItem == newItem
        }
    }

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: MatchResult) {
            val sdf = SimpleDateFormat("yy.MM.dd", Locale.KOREA)
            itemView.findViewById<TextView>(R.id.textOpponent).text = item.opponentName
            itemView.findViewById<TextView>(R.id.textScore).text = "${item.mySetScore} : ${item.opponentSetScore}"
            itemView.findViewById<TextView>(R.id.textDate).text = sdf.format(Date(item.date))
            itemView.findViewById<TextView>(R.id.textTags).text = item.detail // detail에 태그 등 저장시
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_match_result, parent, false)
        return RecordViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
