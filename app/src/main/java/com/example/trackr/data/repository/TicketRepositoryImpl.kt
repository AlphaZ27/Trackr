package com.example.trackr.data.repository

import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.repository.TicketRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
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

    override fun getTicketById(ticketId: String): Flow<Ticket?> = callbackFlow {
        val listener = firestore.collection("tickets").document(ticketId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val ticket = snapshot?.toObject(Ticket::class.java)
                trySend(ticket).isSuccess
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateTicket(ticket: Ticket): Result<Unit> {
        return try {
            // This merges the updateTicket function and will update all fields from the ticket object passed to it.
            firestore.collection("tickets").document(ticket.id)
                .set(ticket, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun saveTicket(ticket: Ticket): Result<Unit> {
        return try {
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

    // Function implementation to link an article
    override suspend fun linkArticleToTicket(ticketId: String, articleId: String): Result<Unit> {
        return try {
            firestore.collection("tickets").document(ticketId)
                .update("linkedArticles", FieldValue.arrayUnion(articleId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Function implementation to unlink an article
    override suspend fun unlinkArticleFromTicket(ticketId: String, articleId: String): Result<Unit> {
        return try {
            firestore.collection("tickets").document(ticketId)
                .update("linkedArticles", FieldValue.arrayRemove(articleId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}