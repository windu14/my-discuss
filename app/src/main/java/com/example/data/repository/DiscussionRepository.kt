package com.example.data.repository

import com.example.data.local.DiscussionDao
import com.example.data.model.DiscussionEntity
import kotlinx.coroutines.flow.Flow

class DiscussionRepository(private val dao: DiscussionDao) {

    fun getAllDiscussions(): Flow<List<DiscussionEntity>> = dao.getAllDiscussions()

    fun getBookmarkedDiscussions(): Flow<List<DiscussionEntity>> = dao.getBookmarkedDiscussions()

    fun searchDiscussions(query: String, category: String): Flow<List<DiscussionEntity>> =
        dao.searchDiscussions(query, category)

    suspend fun insertDiscussion(discussion: DiscussionEntity): Long = dao.insertDiscussion(discussion)

    suspend fun updateDiscussion(discussion: DiscussionEntity) = dao.updateDiscussion(discussion)

    suspend fun deleteDiscussion(discussion: DiscussionEntity) = dao.deleteDiscussion(discussion)

    suspend fun toggleBookmark(id: Long) = dao.toggleBookmark(id)
}
