package com.example.trackr.data.repository


import com.example.trackr.domain.model.*
import com.example.trackr.domain.repository.DashboardRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DashboardRepository {

    override fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .whereEqualTo("status", UserStatus.Active.name)
            //.orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                trySend(users).isSuccess
            }
        awaitClose { listener.remove() }
    }

    override fun getStandardUsers(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .whereEqualTo("role", UserRole.User.name) // Query for "User" role
            .whereEqualTo("status", UserStatus.Active.name)
            //.orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                trySend(users).isSuccess
            }
        awaitClose { listener.remove() }
    }

    override fun getTicketStats(): Flow<DashboardStats> = callbackFlow {
        val listener = firestore.collection("tickets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tickets = snapshot?.toObjects(Ticket::class.java) ?: emptyList()

                // Perform the counts client-side on the real-time list
                val open = tickets.count { it.status == TicketStatus.Open || it.status == TicketStatus.InProgress }
                val closed = tickets.count { it.status == TicketStatus.Closed }

                trySend(DashboardStats(
                    openTickets = open,
                    closedTickets = closed,
                    totalTickets = tickets.size
                )).isSuccess
            }
        awaitClose { listener.remove() }
    }

    override fun getUserRoleStats(): Flow<UserRoleStats> = callbackFlow {
        // Query for active users, consistent with the other dashboard lists
        val listener = firestore.collection("users")
            .whereEqualTo("status", UserStatus.Active.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.toObjects(User::class.java) ?: emptyList()

                // Perform counts client-side
                val adminCount = users.count { it.role == UserRole.Admin }
                val managerCount = users.count { it.role == UserRole.Manager }
                val userCount = users.count { it.role == UserRole.User }

                trySend(UserRoleStats(
                    adminCount = adminCount,
                    managerCount = managerCount,
                    userCount = userCount,
                    totalUsers = users.size
                )).isSuccess
            }
        awaitClose { listener.remove() }
    }

    override fun getTicketCategoryStats(): Flow<List<CategoryStat>> = callbackFlow {
        // This query just gets all tickets
        val listener = firestore.collection("tickets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tickets = snapshot?.toObjects(Ticket::class.java) ?: emptyList()

                // Group by category and count them client-side
                val stats = tickets
                    .groupBy { it.category }
                    .map { (category, ticketList) ->
                        CategoryStat(category = category, count = ticketList.size)
                    }
                    .sortedByDescending { it.count } // Sort to show biggest slices first

                trySend(stats).isSuccess
            }
        awaitClose { listener.remove() }
    }

    override fun getTicketResolutionStats(): Flow<ResolutionTimeStats> = callbackFlow {
        val listener = firestore.collection("tickets")
            // We only care about tickets that are closed
            .whereEqualTo("status", TicketStatus.Closed.name)
            // We only care about tickets that have a closedAt timestamp
            .whereNotEqualTo("closedAt", null)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tickets = snapshot?.toObjects(Ticket::class.java) ?: emptyList()

                // Calculate the resolution time in milliseconds for each ticket
                val resolutionTimesInMillis = tickets.mapNotNull { ticket ->
                    val createdAt = ticket.createdDate.toDate().time
                    val closedAt = ticket.closedAt?.toDate()?.time
                    if (closedAt != null) {
                        closedAt - createdAt
                    } else {
                        null
                    }
                }

                if (resolutionTimesInMillis.isEmpty()) {
                    // No data, send default stats
                    trySend(ResolutionTimeStats()).isSuccess
                } else {
                    // Calculate stats in hours
                    val avgInMillis = resolutionTimesInMillis.average()
                    val minInMillis = resolutionTimesInMillis.minOrNull()
                    val maxInMillis = resolutionTimesInMillis.maxOrNull()

                    val toHours = { millis: Double -> millis / (1000 * 60 * 60) }

                    trySend(ResolutionTimeStats(
                        averageResolutionHours = toHours(avgInMillis),
                        fastestResolutionHours = minInMillis?.let { toHours(it.toDouble()) },
                        slowestResolutionHours = maxInMillis?.let { toHours(it.toDouble()) }
                    )).isSuccess
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateUserRole(userId: String, newRole: UserRole): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("role", newRole.name) // Update the role field to the enum's string name
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