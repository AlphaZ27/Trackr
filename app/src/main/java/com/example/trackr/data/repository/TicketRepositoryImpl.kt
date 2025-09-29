package com.example.trackr.data.repository

import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.repository.TicketRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TicketRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TicketRepository {

    override fun getAllTickets(): Flow<List<Ticket>> = callbackFlow {
        // Query Firestore for tickets that are not "Closed"
        val subscription = firestore.collection("tickets")
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Close the flow on error
                    return@addSnapshotListener
                }
                // Manually map the document ID to the Ticket's 'id' field and send them to the flow
                val tickets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Ticket::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(tickets)
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

    override suspend fun getTicketById(ticketId: String): Result<Ticket?> {
        return try {
            val document = firestore.collection("tickets").document(ticketId).get().await()
            val ticket = document.toObject(Ticket::class.java)?.copy(id = document.id)
            Result.success(ticket)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTicket(ticket: Ticket): Result<Unit> {
        return try {
            // Create a map of the fields that can be changed on the detail screen.
            // This is more efficient and explicit than merging the whole object.
//            val ticketUpdates = mapOf(
//                "status" to ticket.status.name,
//                "priority" to ticket.priority.name,
//                "description" to ticket.description,
//                "assignee" to ticket.assignee,
//                "resolutionDescription" to ticket.resolutionDescription
//            )

            // This prevents the updateTicket function
//            firestore.collection("tickets").document(ticket.id)
//                .set(ticket, SetOptions.merge()).await()

            // This merges the updateTicket function and will update all fields from the ticket object passed to it.
            firestore.collection("tickets").document(ticket.id)
                .set(ticket, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTicket(ticketId: String): Result<Unit> {
        return try {
            firestore.collection("tickets").document(ticketId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}