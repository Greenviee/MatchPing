package com.example.matchping

import androidx.room.*

@Dao
interface OpponentDao {
    @Query("SELECT * FROM opponents")
    suspend fun getAllOpponents(): List<Opponent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(opponent: Opponent)
}
