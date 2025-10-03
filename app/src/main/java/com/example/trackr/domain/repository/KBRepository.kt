package com.example.trackr.domain.repository

/*
*  This repository interface defines the contract for interacting with the Knowledge Base (KB)
* */

import com.example.trackr.domain.model.Feedback
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

    /**
     * Creates or updates a Knowledge Base article.
     */
    suspend fun createOrUpdateArticle(article: KBArticle): Result<Unit>

    /**
     * Deletes a Knowledge Base article.
     */
    suspend fun deleteArticle(articleId: String): Result<Unit>

    /**
     * Gets a list of Knowledge Base articles by their unique IDs.
     */
    fun getArticlesByIds(articleIds: List<String>): Flow<List<KBArticle>>

    /**
     * Gets a real-time stream of feedback for a specific Knowledge Base article.
     */
    fun getFeedbackForArticle(articleId: String): Flow<List<Feedback>>

    /**
     * Submits feedback for a specific Knowledge Base article.
     */
    suspend fun submitFeedback(feedback: Feedback): Result<Unit>
}