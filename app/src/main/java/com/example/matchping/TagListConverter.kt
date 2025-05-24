// TagListConverter.kt
package com.example.matchping

import androidx.room.TypeConverter

class TagListConverter {
    @TypeConverter fun fromTagList(tags: List<String>?) = tags?.joinToString(",") ?: ""
    @TypeConverter fun toTagList(data: String?) =
        data?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
}
