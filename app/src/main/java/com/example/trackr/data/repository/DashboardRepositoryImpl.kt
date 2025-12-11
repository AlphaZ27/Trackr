package com.example.trackr.data.repository


import com.example.trackr.domain.model.*
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

class DashboardRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DashboardRepository {

    // This ensures the dashboard updates automatically when data changes.
    override fun getDashboardMetrics(forceRefresh: Boolean): Flow<DashboardMetrics> = callbackFlow {
        val listener = firestore.collection("tickets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val tickets = snapshot?.toObjects(Ticket::class.java) ?: emptyList()

                // Calculate metrics immediately on update
                val metrics = calculateMetrics(tickets)
                trySend(metrics)
            }
        awaitClose { listener.remove() }
    }

    private fun calculateMetrics(tickets: List<Ticket>): DashboardMetrics {
        if (tickets.isEmpty()) return DashboardMetrics()

        // Counts
        val total = tickets.size
        val open = tickets.count { it.status == TicketStatus.Open }
        val closed = tickets.count { it.status == TicketStatus.Closed }
        val inProgress = tickets.count { it.status == TicketStatus.InProgress }

        // SLA Breach Rate
        val breachedCount = tickets.count { it.slaStatus == SLAStatus.Breached }
        val breachRate = if (total > 0) (breachedCount.toDouble() / total) * 100 else 0.0

        // Reopen Rate
        val reopenedCount = tickets.count { it.reopenedCount > 0 }
        val reopenRate = if (total > 0) (reopenedCount.toDouble() / total) * 100 else 0.0

        // Avg Resolution Time (for Closed tickets only)
        val closedTicketsList = tickets.filter { it.status == TicketStatus.Closed && it.closedDate != null }
        val totalResolutionMillis = closedTicketsList.sumOf {
            (it.closedDate!!.toDate().time - it.createdDate.toDate().time)
        }
        val avgResTimeHours = if (closedTicketsList.isNotEmpty()) {
            TimeUnit.MILLISECONDS.toHours(totalResolutionMillis / closedTicketsList.size).toDouble()
        } else 0.0

        // Distributions
        val byStatus = tickets.groupBy { it.status.name }.mapValues { it.value.size }
        val byPriority = tickets.groupBy { it.priority.name }.mapValues { it.value.size }
        val byDept = tickets.groupBy { it.department.ifBlank { "Unknown" } }.mapValues { it.value.size }

        // Volume Last 7 Days
        val volumeList = mutableListOf<Pair<String, Int>>()
        val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())

        for (i in 6 downTo 0) {
            val targetDay = Calendar.getInstance()
            targetDay.add(Calendar.DAY_OF_YEAR, -i)
            val dayStr = dateFormat.format(targetDay.time)

            val count = tickets.count {
                dateFormat.format(it.createdDate.toDate()) == dayStr
            }
            volumeList.add(dayStr to count)
        }

        return DashboardMetrics(
            totalTickets = total,
            openTickets = open,
            closedTickets = closed,
            inProgressTickets = inProgress,
            avgResolutionTimeHours = avgResTimeHours,
            slaBreachRate = breachRate,
            reopenRate = reopenRate,
            ticketsByStatus = byStatus,
            ticketsByPriority = byPriority,
            ticketsByDepartment = byDept,
            ticketVolumeLast7Days = volumeList
        )
    }

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
            .whereEqualTo("status", UserStatus.Active.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    // --- Admin Stats Implementations ---

    override fun getTicketStats(): Flow<DashboardStats> = callbackFlow {
        val listener = firestore.collection("tickets").addSnapshotListener { snapshot, _ ->
            val tickets = snapshot?.toObjects(Ticket::class.java) ?: emptyList()
            val total = tickets.size
            val open = tickets.count { it.status == TicketStatus.Open }
            val closed = tickets.count { it.status == TicketStatus.Closed }
            val inProgress = tickets.count { it.status == TicketStatus.InProgress }
            val status = open + closed + inProgress
            trySend(DashboardStats(total, status))
        }
        awaitClose { listener.remove() }
    }

    override fun getUserRoleStats(): Flow<UserRoleStats> = callbackFlow {
        val listener = firestore.collection("users").addSnapshotListener { snapshot, _ ->
            val users = snapshot?.toObjects(User::class.java) ?: emptyList()
            val admins = users.count { it.role == UserRole.Admin }
            val managers = users.count { it.role == UserRole.Manager }
            val usersCount = users.count { it.role == UserRole.User }
            trySend(UserRoleStats(admins, managers, usersCount))
        }
        awaitClose { listener.remove() }
    }

    override fun getTicketCategoryStats(): Flow<List<CategoryStat>> = callbackFlow {
        val listener = firestore.collection("tickets").addSnapshotListener { snapshot, _ ->
            val tickets = snapshot?.toObjects(Ticket::class.java) ?: emptyList()
            val stats = tickets.groupBy { it.category }
                .map { (cat, list) -> CategoryStat(cat, list.size) }
            trySend(stats)
        }
        awaitClose { listener.remove() }
    }

    override fun getTicketResolutionStats(): Flow<ResolutionTimeStats> = kotlinx.coroutines.flow.flow {
        emit(ResolutionTimeStats())
    }

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

    override suspend fun updateUserStatus(userId: String, status: UserStatus): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("status", status.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}