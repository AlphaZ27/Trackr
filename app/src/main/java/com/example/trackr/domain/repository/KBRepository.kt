package com.example.trackr.domain.repository

import com.example.trackr.domain.model.KBArticle
import kotlinx.coroutines.flow.Flow

interface KBRepository {
    /**
     * Gets a real-time stream of all published Knowledge Base articles.
     */
    fun getAllArticles(): Flow<List<KBArticle>>

    /**
     * Gets a single Knowledge Base article by its unique ID.
     */
    suspend fun getArticleById(articleId: String): Result<KBArticle?>
}