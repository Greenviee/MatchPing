package com.example.matchping

data class User(
    val uid: String = "",
    val name: String = "",
    val id: String = "",
    val phone: String = "",
    val unit: String = "", // 부수: "선수부", "1부", ...
    val profileImageUrl: String = ""
)
