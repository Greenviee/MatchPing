// src/main/java/com/example/matchping/QuoteApi.kt
package com.example.matchping

import retrofit2.http.GET

/**
 * Quote API 응답 모델 (예: https://api.quotable.io/random)
 */
data class QuoteResponse(
    val content: String,
    val author: String?
)

/**
 * 탁구 팁(quote)용 Retrofit 인터페이스
 */
interface QuoteApi {
    @GET("api/random")
    suspend fun randomQuote(): List<ZenQuote>
}

data class ZenQuote(
    val q: String,  // 명언 내용
    val a: String   // 저자
)
