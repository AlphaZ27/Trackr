package com.example.trackr.data.repository

import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.domain.repository.ActivityLogRepository
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
    private val auth: FirebaseAuth,
    private val activityLogRepository: ActivityLogRepository
) : TicketRepository {

    override fun getAllTickets(): Flow<List<Ticket>> = callbackFlow {
        val subscription = firestore.collection("tickets")
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val tickets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Ticket::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(tickets)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getTicketsForCurrentUser(): List<Ticket> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return firestore.collection("tickets")
            .whereEqualTo("createdBy", userId)
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Ticket::class.java)
    }

    override suspend fun getTicketsForTeam(teamId: String): List<Ticket> {
        val userIdsOnTeam = firestore.collection("users")
            .whereEqualTo("teamId", teamId)
            .get()
            .await()
            .documents.map { it.id }

        if (userIdsOnTeam.isEmpty()) {
            return emptyList()
        }

        return firestore.collection("tickets")
            .whereIn("createdBy", userIdsOnTeam)
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Ticket::class.java)
    }

    override suspend fun createTicket(ticket: Ticket): Result<Unit> {
        return try {
            val docRef = firestore.collection("tickets").add(ticket).await()
            activityLogRepository.logAction("TICKET_CREATED", "Created ticket '${ticket.name}' (ID: ${docRef.id})")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTicketById(ticketId: String): Flow<Ticket?> = callbackFlow {
        val listener = firestore.collection("tickets").document(ticketId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val ticket = snapshot?.toObject(Ticket::class.java)?.copy(id = snapshot.id)
                trySend(ticket)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateTicket(ticket: Ticket): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
            val updatedTicket = when {
                ticket.status == TicketStatus.Closed && ticket.closedDate == null -> {
                    ticket.copy(closedDate = Timestamp.now(), closedBy = currentUserId)
                }
                ticket.status != TicketStatus.Closed && ticket.closedDate != null -> {
                    ticket.copy(closedDate = null, closedBy = null)
                }
                else -> ticket
            }

            firestore.collection("tickets").document(updatedTicket.id)
                .set(updatedTicket, SetOptions.merge()).await()

            activityLogRepository.logAction("TICKET_UPDATED", "Updated ticket #${ticket.id.take(4)}, '${ticket.name}' status to ${ticket.status}")
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
            activityLogRepository.logAction("TICKET_DELETED", "Deleted ticket ID: $ticketId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    override fun getAllTicketsForReport(): Flow<List<Ticket>> = callbackFlow {
        val listener = firestore.collection("tickets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tickets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Ticket::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(tickets)
            }
        awaitClose { listener.remove() }
    }

    override fun getRecentOpenTickets(): Flow<List<Ticket>> = callbackFlow {
        val listener = firestore.collection("tickets")
            .whereIn("status", listOf(TicketStatus.Open.name, TicketStatus.InProgress.name))
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val tickets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Ticket::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(tickets)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun groupTickets(ticketIds: List<String>, groupId: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            ticketIds.forEach { id ->
                val ref = firestore.collection("tickets").document(id)
                batch.update(ref, "groupId", groupId)
            }
            batch.commit().await()
            activityLogRepository.logAction("TICKET_GROUPED", "Grouped ${ticketIds.size} tickets into Group $groupId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}