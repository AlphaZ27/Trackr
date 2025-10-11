package com.example.trackr.domain.repository

import com.example.trackr.domain.model.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun getAuthState(): Flow<FirebaseUser?>

    suspend fun login(email: String, password: String): Result<AuthResult>

    // This function is required by the Splash screen to check the current user's status.
    fun isUserAuthenticated(): Boolean

    // Register function
    suspend fun register(email: String, name: String, password: String): Result<AuthResult>

    // Forgot password/password reset function
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    // Logout function
    fun logoutUser()

    suspend fun getCurrentUserData(): User?

}