package com.example.matchping

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey val uid: String,
    val name: String,
    val phone: String,
    val id: String,
    val profileImageUrl: String?
)
