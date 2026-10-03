package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DiscussionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscussionDao {
    @Query("SELECT * FROM discussions ORDER BY timestamp DESC")
    fun getAllDiscussions(): Flow<List<DiscussionEntity>>

    @Query("SELECT * FROM discussions WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedDiscussions(): Flow<List<DiscussionEntity>>

    @Query("""
        SELECT * FROM discussions 
        WHERE (:query = '' OR title LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' OR fullTranscript LIKE '%' || :query || '%')
        AND (:category = 'Semua' OR category = :category)
        ORDER BY timestamp DESC
    """)
    fun searchDiscussions(query: String, category: String): Flow<List<DiscussionEntity>>

    @Query("SELECT * FROM discussions WHERE id = :id LIMIT 1")
    suspend fun getDiscussionById(id: Long): DiscussionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscussion(discussion: DiscussionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(discussions: List<DiscussionEntity>)

    @Update
    suspend fun updateDiscussion(discussion: DiscussionEntity)

    @Delete
    suspend fun deleteDiscussion(discussion: DiscussionEntity)

    @Query("UPDATE discussions SET isBookmarked = NOT isBookmarked WHERE id = :id")
    suspend fun toggleBookmark(id: Long)

    @Query("SELECT COUNT(*) FROM discussions")
    suspend fun getCount(): Int
}
