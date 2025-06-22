// MatchResultDao.kt
package com.example.matchping

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MatchResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(results: List<MatchResult>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(matchResult: MatchResult)

    /** 전체 전적 (기존) */
    @Query("SELECT * FROM matches ORDER BY date DESC")
    suspend fun getAllMatches(): List<MatchResult>

    /** 최근 N건 전적 (기존) */
    @Query("SELECT * FROM matches ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentMatches(limit: Int = 20): List<MatchResult>

    /** 상대 이름으로 검색 (기존) */
    @Query("SELECT * FROM matches WHERE opponentName = :name ORDER BY date DESC")
    suspend fun getMatchesByOpponent(name: String): List<MatchResult>

    /** 전적 삭제 */
    @Query("DELETE FROM matches WHERE myUid = :uid")
    suspend fun deleteByUser(uid: String)

    /** ◆ 사용자별 전적만 가져오기 (기존) */
    @Query("""
      SELECT * FROM matches
      WHERE myUid = :uid
      ORDER BY date DESC
    """)
    suspend fun getMatchesByUser(uid: String): List<MatchResult>

    @Query("""
  SELECT * FROM matches
  WHERE myUid = :uid
    AND opponentUnit = :unit
  ORDER BY date DESC
""")
    suspend fun getMatchesByUnit(uid: String, unit: String): List<MatchResult>
}
