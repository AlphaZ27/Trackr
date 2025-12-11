package com.example.trackr.data.repository

import com.example.trackr.domain.model.User
import com.example.trackr.domain.model.UserRole
import com.example.trackr.domain.model.UserStatus
import com.example.trackr.domain.repository.ActivityLogRepository
import com.google.firebase.auth.AuthResult
import com.example.trackr.domain.repository.AuthRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val activityLogRepository: ActivityLogRepository
) : AuthRepository {

    override fun getAuthState(): Flow<FirebaseUser?> {
        val authState = MutableStateFlow(firebaseAuth.currentUser)
        firebaseAuth.addAuthStateListener { authState.value = it.currentUser }
        return authState
    }

    override suspend fun login(email: String, password: String): Result<AuthResult> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            try {
                result.user?.uid?.let { uid ->
                    firestore.collection("users").document(uid)
                        .update("lastLogin", Timestamp.now()).await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            activityLogRepository.logAction("USER_LOGIN", "User $email logged in.")
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // This function was missing its implementation
    override suspend fun register(email: String, name: String, password: String): Result<AuthResult> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            // Create a corresponding user document in Firestore
            val user = User(
                id = result.user!!.uid,
                name = name,
                email = email,
                role = UserRole.User, // Default new users to the 'User' role
                status = UserStatus.Active,
                createdAt = Timestamp.now(),
                lastLogin = Timestamp.now()
            )
            firestore.collection("users").document(user.id).set(user).await()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // This function was missing its implementation
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // This function was missing its implementation
    override fun logoutUser() {
        val email = firebaseAuth.currentUser?.email ?: "Unknown"
        firebaseAuth.signOut()
    }

    override fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun getCurrentUserData(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return try {
            firestore.collection("users").document(firebaseUser.uid).get()
                .await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateFcmToken(token: String) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            try {
                firestore.collection("users").document(currentUser.uid)
                    .update("fcmToken", token)
                    .await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}