package com.example.trackr.domain.model

import com.google.firebase.Timestamp

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.User, // "admin", "manager", "user"
    val status: UserStatus = UserStatus.Active, // "active", "inactive"
    val createdAt: Timestamp? = null,
    val lastLogin: Timestamp? = null

)