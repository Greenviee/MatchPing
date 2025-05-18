package com.example.matchping

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "opponents")
data class Opponent(
    @PrimaryKey(autoGenerate = true) val opponentId: Int = 0,
    val name: String,
    val unit: String,
    val tags: String
)
