package com.example.trackr.data.repository

import com.example.trackr.domain.model.KBGlobalStats
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.domain.repository.AnalyticsRepository
import com.example.trackr.domain.repository.QualityMetrics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AnalyticsRepository {

    override fun getQualityMetrics(): Flow<QualityMetrics> = callbackFlow {
        // Listen to ALL tickets to calculate aggregate stats
        // In a real app with millions of tickets, you'd use a Cloud Function to aggregate this to a separate stats document.
        // For this scale, client-side aggregation is instant and reactive.
        val listener = firestore.collection("tickets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }

                val tickets = snapshot?.toObjects(Ticket::class.java) ?: emptyList()
                val closedTickets = tickets.filter { it.status == TicketStatus.Closed }

                // CSAT Calculation
                val ratedTickets = closedTickets.filter { it.csatScore != null }
                val avgCsat = if (ratedTickets.isNotEmpty()) {
                    ratedTickets.map { it.csatScore!! }.average()
                } else 0.0

                val responseRate = if (closedTickets.isNotEmpty()) {
                    (ratedTickets.size.toDouble() / closedTickets.size.toDouble()) * 100
                } else 0.0

                // Reopen Rate (Assuming 'reopenedCount' is tracked on Ticket)
                val reopenedTickets = tickets.count { it.reopenedCount > 0 }
                val reopenRate = if (tickets.isNotEmpty()) {
                    (reopenedTickets.toDouble() / tickets.size.toDouble()) * 100
                } else 0.0

                trySend(QualityMetrics(avgCsat, responseRate, reopenRate, closedTickets.size))
            }
        awaitClose { listener.remove() }
    }

    override fun getKBStats(): Flow<KBGlobalStats> = callbackFlow {
        // This would listen to a specific analytics document
        val docRef = firestore.collection("analytics").document("kb_global")
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val stats = snapshot?.toObject(KBGlobalStats::class.java) ?: KBGlobalStats()
            trySend(stats)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun logKbView(articleId: String) {
        // Increment global view count in a transaction or atomic update
        // Simplified here for demo
    }

    override suspend fun logTicketCreationAttempt(avoided: Boolean) {
        // Logic to update deflection stats
    }
}