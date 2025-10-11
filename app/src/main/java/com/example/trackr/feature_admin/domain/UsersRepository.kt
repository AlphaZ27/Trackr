package com.example.trackr.feature_admin.domain

// A simple data class to represent a user in the list
data class TrackrUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = ""
)

interface UsersRepository {
    suspend fun getAllUsers(): List<TrackrUser>
    suspend fun updateUserRole(uid: String, newRole: String)

}