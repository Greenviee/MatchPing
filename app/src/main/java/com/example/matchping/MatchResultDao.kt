package com.example.matchping

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MatchResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(matchResult: MatchResult)

    @Query("SELECT * FROM matches ORDER BY date DESC")
    suspend fun getAllMatches(): List<MatchResult>

    @Query("SELECT * FROM matches ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentMatches(limit: Int = 20): List<MatchResult>

    @Query("SELECT * FROM matches WHERE opponentName = :name ORDER BY date DESC")
    suspend fun getMatchesByOpponent(name: String): List<MatchResult>
}
