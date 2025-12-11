package com.example.trackr.data.repository

import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.FirebaseAuth
import com.example.trackr.domain.model.ArticleStatus
import com.example.trackr.domain.model.KBArticle
import com.example.trackr.domain.model.Feedback
import com.example.trackr.domain.repository.ActivityLogRepository
import com.google.firebase.firestore.FieldPath
import com.example.trackr.domain.repository.KBRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class KBRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth, // get's the current user's id
    private val activityLogRepository: ActivityLogRepository
) : KBRepository {

    /**
    * Gets a real-time stream of all published Knowledge Base articles.
    * We only want to show published articles in the main list but a filter will be added
    * */
    override fun getAllArticles(): Flow<List<KBArticle>> = callbackFlow {
        // We only want to show published articles in the main list.
        val listener = firestore.collection("articles")
            .whereEqualTo("status", ArticleStatus.Published.name)
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                //val articles = snapshot?.toObjects(KBArticle::class.java) ?: emptyList()
                val articles = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(KBArticle::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(articles).isSuccess
            }
        // This is called when the flow is cancelled.
        awaitClose { listener.remove() }
    }

    /*
    * Gets a single Knowledge Base article by its unique ID.  */
    override suspend fun getArticleById(articleId: String): Result<KBArticle?> {
        return try {
            val document = firestore.collection("articles").document(articleId).get().await()
            //val article = document.toObject(KBArticle::class.java)
            val article = document.toObject(KBArticle::class.java)?.copy(id = document.id)
            Result.success(article)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /*
    * Gets a list of Knowledge Base articles by their unique IDs. */
    // Function implementation to fetch multiple articles
    override fun getArticlesByIds(articleIds: List<String>): Flow<List<KBArticle>> = callbackFlow {
        if (articleIds.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection("articles")
            .whereIn(FieldPath.documentId(), articleIds)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                //val articles = snapshot?.toObjects(KBArticle::class.java) ?: emptyList()
                val articles = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(KBArticle::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(articles).isSuccess
            }
        awaitClose { listener.remove() }
    }

    /*
    * Creates or updates a Knowledge Base article.
    * */
    override suspend fun createOrUpdateArticle(article: KBArticle): Result<Unit> {
        return try {
            val isNew = article.id.isBlank()
            val articleWithAuthor = if (isNew) {
                article.copy(
                    authorId = auth.currentUser?.uid ?: "unknown",
                    createdBy = auth.currentUser?.email ?: "Anonymous"
                )
            } else article

            if (isNew) {
                firestore.collection("articles").add(articleWithAuthor).await()
                activityLogRepository.logAction("KB_CREATED", "Created article: ${article.title}")
            } else {
                firestore.collection("articles").document(article.id)
                    .set(articleWithAuthor, SetOptions.merge()).await()
                activityLogRepository.logAction("KB_UPDATED", "Updated article: ${article.title}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /*
    * Deletes a Knowledge Base article.
    * */
    override suspend fun deleteArticle(articleId: String): Result<Unit> {
        return try {
            firestore.collection("articles").document(articleId).delete().await()
            activityLogRepository.logAction("KB_DELETED", "Deleted article ID: $articleId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /*
    * Gets a real-time stream of feedback for a specific Knowledge Base article.
     */

    override fun getFeedbackForArticle(articleId: String): Flow<List<Feedback>> = callbackFlow {
        val listener = firestore.collection("feedback")
            .whereEqualTo("articleId", articleId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val feedbackList = snapshot?.toObjects(Feedback::class.java) ?: emptyList()
                trySend(feedbackList).isSuccess
            }
        awaitClose { listener.remove() }
    }

    /**
     * Saves a specific Knowledge Base article.
     */
    override suspend fun saveArticle(articleId: String): Result<Unit> {
        return try {
            firestore.collection("articles").document(articleId)
                .set(articleId, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
    * Submits feedback for a specific Knowledge Base article.
     */

    override suspend fun submitFeedback(feedback: Feedback): Result<Unit> {
        return try {
            val feedbackWithUser = feedback.copy(userId = auth.currentUser?.uid ?: "unknown")
            firestore.collection("feedback").add(feedbackWithUser).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getFrequentArticles(limit: Int): Flow<List<KBArticle>> = callbackFlow {
        val listener = firestore.collection("articles")
            .whereEqualTo("status", ArticleStatus.Published.name)
            .whereGreaterThan("viewCount", 0)
            .orderBy("viewCount", Query.Direction.DESCENDING) // Simple "Frequent" logic for now
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val articles = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(KBArticle::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(articles)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun incrementViewCount(articleId: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "viewCount" to FieldValue.increment(1),
                "lastViewedAt" to FieldValue.serverTimestamp()
            )
            // Also update 'lastViewedAt' timestamp for recency logic
            firestore.collection("articles").document(articleId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logSearchGap(searchTerm: String): Result<Unit> {
        return try {
            // We store these in a separate collection for analytics
            val gapData = hashMapOf(
                "term" to searchTerm,
                "timestamp" to FieldValue.serverTimestamp(),
                "userId" to (auth.currentUser?.uid ?: "anonymous")
            )
            firestore.collection("search_gaps").add(gapData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}