package com.example.trackr.domain.repository


import com.example.trackr.domain.model.CategoryStat
import com.example.trackr.domain.model.DashboardStats
import com.example.trackr.domain.model.User
import com.example.trackr.domain.model.UserRole
import com.example.trackr.domain.model.UserRoleStats
import com.example.trackr.domain.model.UserStatus
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {

    /**
     * Gets a real-time list of all users (for the Admin).
     */
    fun getAllUsers(): Flow<List<User>>

    /**
     * Gets a real-time list of users with the 'User' role (for the Manager).
     */
    fun getStandardUsers(): Flow<List<User>>

    /**
     * Gets real-time counts of all tickets, grouped by status.
     */
    fun getTicketStats(): Flow<DashboardStats>

    /**
     * Gets real-time counts of all users, grouped by role.
     */
    fun getUserRoleStats(): Flow<UserRoleStats>

    /**
     * Gets real-time counts of all tickets, grouped by category.
     */
    fun getTicketCategoryStats(): Flow<List<CategoryStat>>

    /**
     * Updates a specific user's role in Firestore.
     */
    suspend fun updateUserRole(userId: String, newRole: UserRole): Result<Unit>

    /**
     * Updates a specific user's status in Firestore.
     */
    suspend fun updateUserStatus(userId: String, newStatus: UserStatus): Result<Unit>
}