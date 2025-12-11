package com.example.trackr.data.repository

import com.example.trackr.domain.model.ActivityLog
import com.example.trackr.domain.repository.ActivityLogRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ActivityLogRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ActivityLogRepository {

    override fun getLogs(): Flow<List<ActivityLog>> = callbackFlow {
        val listener = firestore.collection("activity_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100) // Keep it performant
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val logs = snapshot?.toObjects(ActivityLog::class.java) ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun logAction(action: String, details: String) {
        try {
            val user = auth.currentUser
            val identifier = user?.email ?: user?.uid ?: "System"

            val log = ActivityLog(
                action = action,
                details = details,
                performedBy = identifier
            )
            firestore.collection("activity_logs").add(log).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}