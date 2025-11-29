package com.example.trackr.data.repository

import com.example.trackr.domain.model.Priority
import com.example.trackr.domain.model.SLARule
import com.example.trackr.domain.repository.SLARepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SLARepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SLARepository {

    private val collection = firestore.collection("sla_rules")

    override fun getSLARules(): Flow<List<SLARule>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val rules = snapshot?.toObjects(SLARule::class.java) ?: emptyList()
            trySend(rules)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun updateSLARule(rule: SLARule): Result<Unit> {
        return try {
            // ID is the priority name to ensure uniqueness (e.g., "High")
            collection.document(rule.priority.name).set(rule, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun initializeDefaultRulesIfNeeded() {
        try {
            val snapshot = collection.get().await()
            if (snapshot.isEmpty) {
                // Create defaults if none exist
                val defaults = listOf(
                    SLARule("Low", Priority.Low, 48, 40),
                    SLARule("Medium", Priority.Medium, 24, 20),
                    SLARule("High", Priority.High, 8, 6),
                    SLARule("Urgent", Priority.Urgent, 2, 1)
                )
                defaults.forEach { updateSLARule(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}