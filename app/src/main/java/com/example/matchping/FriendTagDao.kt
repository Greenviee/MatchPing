package com.example.matchping

import androidx.room.*

@Dao
interface FriendTagDao {
    @Query("SELECT * FROM friend_tags WHERE friendUid = :friendUid")
    suspend fun getTagsForFriend(friendUid: String): List<FriendTag>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: FriendTag)

    @Update
    suspend fun update(tag: FriendTag)
}