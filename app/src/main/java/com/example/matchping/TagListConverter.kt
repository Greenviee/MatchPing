package com.example.matchping

import androidx.room.TypeConverter

class TagListConverter {
    @TypeConverter
    fun fromList(list: List<String>?): String =
        list?.joinToString(",") ?: ""
    @TypeConverter
    fun toList(data: String?): List<String> =
        data?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
}
