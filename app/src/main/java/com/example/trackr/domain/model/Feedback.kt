package com.example.trackr.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Feedback(
    @DocumentId val id: String = "",
    val articleId: String = "",
    val userId: String = "",
    val rating: Int = 0, // e.g., 1 for helpful, -1 for not helpful
    val comment: String? = null,
    @ServerTimestamp val createdAt: Date? = null
)