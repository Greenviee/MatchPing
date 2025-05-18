package com.example.matchping

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friend_tags")
data class FriendTag(
    @PrimaryKey(autoGenerate = true) val tagId: Int = 0,
    val friendUid: String,    // 반드시 친구별 고유값!
    val tag: String,
    val enabled: Boolean
)
