package com.example.trackr.data.repository

import com.example.trackr.domain.model.ArticleStatus
import com.example.trackr.domain.model.KBArticle
import com.example.trackr.domain.repository.KBRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class KBRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : KBRepository {

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
                val articles = snapshot?.toObjects(KBArticle::class.java) ?: emptyList()
                trySend(articles).isSuccess
            }
        // This is called when the flow is cancelled.
        awaitClose { listener.remove() }
    }

    override suspend fun getArticleById(articleId: String): Result<KBArticle?> {
        return try {
            val document = firestore.collection("articles").document(articleId).get().await()
            val article = document.toObject(KBArticle::class.java)
            Result.success(article)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}