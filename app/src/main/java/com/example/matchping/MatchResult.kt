// MatchResult.kt
package com.example.matchping

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "matches")
@TypeConverters(TagListConverter::class)
data class MatchResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val myUid: String?,
    val opponentName: String,
    val opponentUnit: String,
    val date: Long,
    val result: String,           // "승", "무", "패"
    val mySetScore: Int,
    val opponentSetScore: Int,

    val detail: String,           // 세트별 점수 기록
    val tags: List<String>        // 경기에 설정된 태그들
)
