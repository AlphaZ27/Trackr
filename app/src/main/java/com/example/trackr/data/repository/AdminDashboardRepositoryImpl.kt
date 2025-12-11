package com.example.trackr.data.repository

import com.example.trackr.domain.model.*
import com.example.trackr.domain.repository.AdminDashboardRepository
import com.example.trackr.domain.repository.DashboardRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AdminDashboardRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdminDashboardRepository {



    // --- Metrics Engine (Manager/Admin Overview) ---
    override fun getDashboardMetrics(forceRefresh: Boolean): Flow<DashboardMetrics> = callbackFlow {
        val listener = firestore.collection("tickets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tickets = snapshot?.toObjects(Ticket::class.java) ?: emptyList()
                trySend(calculateMetrics(tickets))
            }
        awaitClose { listener.remove() }
    }

    private fun calculateMetrics(tickets: List<Ticket>): DashboardMetrics {
        if (tickets.isEmpty()) return DashboardMetrics()

        val total = tickets.size
        val open = tickets.count { it.status == TicketStatus.Open }
        val closed = tickets.count { it.status == TicketStatus.Closed }
        val inProgress = tickets.count { it.status == TicketStatus.InProgress }

        val breachedCount = tickets.count { it.slaStatus == SLAStatus.Breached }
        val breachRate = if (total > 0) (breachedCount.toDouble() / total) * 100 else 0.0

        val reopenedCount = tickets.count { it.reopenedCount > 0 }
        val reopenRate = if (total > 0) (reopenedCount.toDouble() / total) * 100 else 0.0

        // Distributions
        val byStatus = tickets.groupBy { it.status.name }.mapValues { it.value.size }
        val byPriority = tickets.groupBy { it.priority.name }.mapValues { it.value.size }
        val byDept = tickets.groupBy { it.department.ifBlank { "Unknown" } }.mapValues { it.value.size }

        return DashboardMetrics(
            totalTickets = total,
            openTickets = open,
            closedTickets = closed,
            inProgressTickets = inProgress,
            slaBreachRate = breachRate,
            reopenRate = reopenRate,
            ticketsByStatus = byStatus,
            ticketsByPriority = byPriority,
            ticketsByDepartment = byDept
        )
    }

    // --- User Management ---

    override fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    override fun getStandardUsers(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .whereEqualTo("role", UserRole.User.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    // --- Admin Stats ---

    override fun getTicketStats(): Flow<DashboardStats> = callbackFlow {
        val listener = firestore.collection("tickets").addSnapshotListener { snapshot, _ ->
            val tickets = snapshot?.toObjects(Ticket::class.java) ?: emptyList()
            val total = tickets.size
            val open = tickets.count { it.status == TicketStatus.Open }
            val closed = tickets.count { it.status == TicketStatus.Closed }
            val inProgress = tickets.count { it.status == TicketStatus.InProgress }
            val status = open + closed
            trySend(DashboardStats(status, total, inProgress))
        }
        awaitClose { listener.remove() }
    }

    override fun getUserRoleStats(): Flow<UserRoleStats> = callbackFlow {
        val listener = firestore.collection("users").addSnapshotListener { snapshot, _ ->
            val users = snapshot?.toObjects(User::class.java) ?: emptyList()
            val admins = users.count { it.role == UserRole.Admin }
            val managers = users.count { it.role == UserRole.Manager }
            val usersCount = users.count { it.role == UserRole.User }
            trySend(UserRoleStats(admins, managers, usersCount, users.size))
        }
        awaitClose { listener.remove() }
    }

    override fun getTicketCategoryStats(): Flow<List<CategoryStat>> = callbackFlow {
        val listener = firestore.collection("tickets").addSnapshotListener { snapshot, _ ->
            val tickets = snapshot?.toObjects(Ticket::class.java) ?: emptyList()
            val stats = tickets.groupBy { it.category }
                .map { (cat, list) -> CategoryStat(cat, list.size) }
                .sortedByDescending { it.count }
            trySend(stats)
        }
        awaitClose { listener.remove() }
    }

    override fun getTicketResolutionStats(): Flow<ResolutionTimeStats> = callbackFlow {
        val listener = firestore.collection("tickets")
            .whereEqualTo("status", TicketStatus.Closed.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val tickets = snapshot?.toObjects(Ticket::class.java) ?: emptyList()

                val resolutionTimesInMillis = tickets.mapNotNull { ticket ->
                    val createdAt = ticket.createdDate.toDate().time
                    val closedAt = ticket.closedDate?.toDate()?.time
                    if (closedAt != null) closedAt - createdAt else null
                }

                if (resolutionTimesInMillis.isEmpty()) {
                    trySend(ResolutionTimeStats())
                } else {
                    val avg = resolutionTimesInMillis.average()
                    val min = resolutionTimesInMillis.minOrNull() ?: 0L
                    val max = resolutionTimesInMillis.maxOrNull() ?: 0L

                    val toHours = { ms: Double -> ms / (1000 * 60 * 60) }

                    trySend(ResolutionTimeStats(
                        averageResolutionHours = toHours(avg),
                        fastestResolutionHours = toHours(min.toDouble()),
                        slowestResolutionHours = toHours(max.toDouble())
                    ))
                }
            }
        awaitClose { listener.remove() }
    }

    // --- Updates ---

    override suspend fun updateUserRole(userId: String, newRole: UserRole): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("role", newRole.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserStatus(userId: String, newStatus: UserStatus): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("status", newStatus.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}