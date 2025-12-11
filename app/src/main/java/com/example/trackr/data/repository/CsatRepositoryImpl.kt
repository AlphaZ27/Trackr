package com.example.trackr.data.repository

import com.example.trackr.domain.model.CsatResponse
import com.example.trackr.domain.repository.CsatRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class CsatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CsatRepository {

    override suspend fun submitCsat(response: CsatResponse): Result<Unit> {
        return try {
            // 1. Save detailed response to 'csat_responses' collection
            firestore.collection("csat_responses").add(response).await()

            // 2. Denormalize score to 'tickets' collection for easy access
            val ticketUpdates = mapOf(
                "csatScore" to response.rating
            )
            firestore.collection("tickets").document(response.ticketId)
                .set(ticketUpdates, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCsatResponsesForTimeRange(startTime: Long, endTime: Long): Flow<List<CsatResponse>> = callbackFlow {
        val startTs = Timestamp(Date(startTime))
        val endTs = Timestamp(Date(endTime))

        val listener = firestore.collection("csat_responses")
            .whereGreaterThanOrEqualTo("timestamp", startTs)
            .whereLessThanOrEqualTo("timestamp", endTs)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val responses = snapshot?.toObjects(CsatResponse::class.java) ?: emptyList()
                trySend(responses)
            }
        awaitClose { listener.remove() }
    }
}