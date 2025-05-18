package com.example.matchping

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MatchResultDao {
    @Insert
    suspend fun insert(match: MatchResult)

    @Query("SELECT * FROM matches ORDER BY date DESC LIMIT 20")
    suspend fun getRecentMatches(): List<MatchResult>

    @Query("SELECT * FROM matches WHERE opponentName LIKE :query ORDER BY date DESC")
    suspend fun searchMatchesByName(query: String): List<MatchResult>

    @Query("SELECT * FROM matches")
    suspend fun getAllMatches(): List<MatchResult>
}
