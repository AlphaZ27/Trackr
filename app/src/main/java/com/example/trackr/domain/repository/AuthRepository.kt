package com.example.trackr.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>

    // This function is required by the Splash screen to check the current user's status.
    fun isUserAuthenticated(): Boolean

    // Register function
    suspend fun register(email: String, password: String): Result<Unit>

    // Forgot password/password reset function
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    // Logout function
    fun logout()

}