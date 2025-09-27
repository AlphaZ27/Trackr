package com.example.trackr.data.repository

import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketStatus
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

    override suspend fun getTicketById(ticketId: String): Result<Ticket?> {
        return try {
            val document = firestore.collection("tickets").document(ticketId).get().await()
            val ticket = document.toObject(Ticket::class.java)
            Result.success(ticket)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTicket(ticket: Ticket): Result<Unit> {
        return try {
            // Create a map of the fields that can be changed on the detail screen.
            // This is more efficient and explicit than merging the whole object.
            val ticketUpdates = mapOf(
                "status" to ticket.status.name,
                "priority" to ticket.priority.name,
                "description" to ticket.description,
                "assignee" to ticket.assigneeId,
                "resolutionDescription" to ticket.resolution
            )

            // This prevents the updateTicket function
//            firestore.collection("tickets").document(ticket.id)
//                .set(ticket, SetOptions.merge()).await()
            firestore.collection("tickets").document(ticket.id)
                .update(ticketUpdates).await()
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