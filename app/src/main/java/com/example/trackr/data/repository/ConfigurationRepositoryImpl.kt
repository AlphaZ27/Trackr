package com.example.trackr.data.repository

import com.example.trackr.domain.model.TicketCategory
import com.example.trackr.domain.repository.ConfigurationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ConfigurationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ConfigurationRepository {

    private val collection = firestore.collection("categories")

    override fun getCategories(): Flow<List<TicketCategory>> = callbackFlow {
        val listener = collection.orderBy("name").addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val categories = snapshot?.toObjects(TicketCategory::class.java) ?: emptyList()
            trySend(categories)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addCategory(name: String): Result<Unit> {
        return try {
            // Use the name as the ID to prevent duplicates easily, or let Firestore generate one
            // Let's use a random ID but check for duplicates if needed.
            val category = TicketCategory(name = name)
            collection.add(category).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            collection.document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun initializeDefaultsIfNeeded() {
        try {
            val snapshot = collection.get().await()
            if (snapshot.isEmpty) {
                val defaults = listOf("General", "Hardware", "Software", "Network", "Access")
                defaults.forEach { name ->
                    collection.add(TicketCategory(name = name))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}