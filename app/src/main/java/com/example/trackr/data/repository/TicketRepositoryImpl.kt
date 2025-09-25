package com.example.trackr.data.repository

import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.domain.repository.TicketRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TicketRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TicketRepository {

    override fun getOpenTickets(): Flow<List<Ticket>> = callbackFlow {
        // Query Firestore for tickets that are not "Closed"
        val subscription = firestore.collection("tickets")
            .whereNotEqualTo("status", TicketStatus.Closed.name)
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Close the flow on error
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tickets = snapshot.toObjects(Ticket::class.java).mapIndexed { index, ticket ->
                        ticket.copy(id = snapshot.documents[index].id)
                    }
                    trySend(tickets).isSuccess // Send the latest list to the flow
                }
            }

        // This is called when the flow is cancelled
        awaitClose { subscription.remove() }
    }

    override suspend fun createTicket(ticket: Ticket): Result<Unit> {
        return try {
            firestore.collection("tickets").add(ticket).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}