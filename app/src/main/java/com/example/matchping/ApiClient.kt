// src/main/java/com/example/matchping/ApiClient.kt
package com.example.matchping

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit 싱글톤 클라이언트
 */
object ApiClient {
    val quoteApi: QuoteApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://zenquotes.io/")   // 대체 API로 교체
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuoteApi::class.java)
    }
}
