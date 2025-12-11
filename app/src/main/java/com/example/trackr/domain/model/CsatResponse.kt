package com.example.trackr.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class CsatResponse(
    @DocumentId val id: String = "",
    val ticketId: String = "",
    val userId: String = "", // Who submitted the rating
    val technicianId: String = "", // Who resolved the ticket (for employee stats)
    val rating: Int = 0, // 1-5
    val comment: String = "",
    val isHelpful: Boolean = true,
    @ServerTimestamp val timestamp: Timestamp? = null
)