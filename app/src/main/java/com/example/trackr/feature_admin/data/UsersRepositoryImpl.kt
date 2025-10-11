package com.example.trackr.feature_admin.data

import com.example.trackr.feature_admin.domain.TrackrUser
import com.example.trackr.feature_admin.domain.UsersRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UsersRepository {

    override suspend fun getAllUsers(): List<TrackrUser> {
        return firestore.collection("users")
            .get()
            .await()
            .documents.mapNotNull { doc ->
                // Manually map Firestore document to our data class
                TrackrUser(
                    uid = doc.id,
                    name = doc.getString("name") ?: "N/A",
                    email = doc.getString("email") ?: "N/A",
                    role = doc.getString("role") ?: "User"
                )
            }
    }

    override suspend fun updateUserRole(uid: String, newRole: String) {
        firestore.collection("users").document(uid)
            .update("role", newRole)
            .await()
    }
}