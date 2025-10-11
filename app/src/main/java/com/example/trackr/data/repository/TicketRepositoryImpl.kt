package com.example.trackr.data.repository

import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.domain.repository.TicketRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TicketRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
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

    // Function to get only the current user's tickets
    override suspend fun getTicketsForCurrentUser(): List<Ticket> {
        val userId = auth.currentUser?.uid ?: return emptyList() // Return empty if no user is logged in
        return firestore.collection("tickets")
            .whereEqualTo("creatorId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Ticket::class.java)
    }

    override suspend fun getTicketsForTeam(teamId: String): List<Ticket> {
        // Step 1: Find all users who are part of the specified team.
        val userIdsOnTeam = firestore.collection("users")
            .whereEqualTo("teamId", teamId)
            .get()
            .await()
            .documents.map { it.id }

        // If no users are on the team, there are no tickets to fetch.
        if (userIdsOnTeam.isEmpty()) {
            return emptyList()
        }

        // Step 2: Fetch all tickets where the 'creatorId' is in our list of team members.
        // Firestore 'in' queries are limited to 30 items. For larger teams, you'd need a different data models.
        return firestore.collection("tickets")
            .whereIn("creatorId", userIdsOnTeam)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Ticket::class.java)
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
            val ticketRef = firestore.collection("tickets").document(ticket.id)

            // Check if the status is Closed
            val isClosing = ticket.status == TicketStatus.Closed

            // Create a map of fields to update
            val updates = mutableMapOf<String, Any?>(
                "name" to ticket.name,
                "assignee" to ticket.assignee,
                "priority" to ticket.priority,
                "description" to ticket.description,
                "department" to ticket.department,
                "status" to ticket.status,
                "resolutionDescription" to ticket.resolutionDescription,
                "category" to ticket.category,
                // Set closedAt timestamp if closing, otherwise set to null (re-opening)
                "closedAt" to if (isClosing) Timestamp.now() else null
            )

            // This merges the updateTicket function and will update all fields from the ticket object passed to it.
            firestore.collection("tickets").document(ticket.id)
                .set(ticket, SetOptions.merge()).await()
            // ticketRef.update(updates).await()
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

    // Function implementation to get all tickets for a tickets report
    override fun getAllTicketsForReport(): Flow<List<Ticket>> = callbackFlow {
        // This query fetches all tickets, without any status filtering
        val listener = firestore.collection("tickets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tickets = snapshot?.toObjects(Ticket::class.java) ?: emptyList()
                trySend(tickets).isSuccess
            }
        awaitClose { listener.remove() }
    }
}