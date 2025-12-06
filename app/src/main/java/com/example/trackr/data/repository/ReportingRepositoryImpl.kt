package com.example.trackr.data.repository

import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.repository.ReportingRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date
import javax.inject.Inject

class ReportingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReportingRepository {

    override fun getTicketsInRange(startDate: Long, endDate: Long): Flow<List<Ticket>> = callbackFlow {
        // Query Firestore for tickets created between start and end date
        val startTimestamp = Timestamp(Date(startDate))
        val endTimestamp = Timestamp(Date(endDate))

        val listener = firestore.collection("tickets")
            .whereGreaterThanOrEqualTo("createdDate", startTimestamp)
            .whereLessThanOrEqualTo("createdDate", endTimestamp)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val tickets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Ticket::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(tickets)
            }
        awaitClose { listener.remove() }
    }
}