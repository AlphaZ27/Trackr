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

    /**
     * Saves a specific Knowledge Base article.
     */
    suspend fun saveArticle(articleId: String): Result<Unit>

    /**
    * Get a list of the most frequently accessed articles. (views + recency)
    * @param limit The maximum number of articles to retrieve.
     */
    fun getFrequentArticles(limit: Int = 10): Flow<List<KBArticle>>

    // 2.5 Article Metrics: Increment view count when an article is opened
    /**
     * Increments the view count for a specific Knowledge Base article.
     */
    suspend fun incrementViewCount(articleId: String): Result<Unit>

    // 2.3 Gap Detection: Log a search term that had 0 results
    /**
     * Logs a search term that had 0 results.
     */
    suspend fun logSearchGap(searchTerm: String): Result<Unit>

}