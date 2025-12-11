package com.example.trackr.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class ActivityLog(
    @DocumentId val id: String = "",
    val action: String = "", // e.g., "TICKET_CREATED", "USER_LOGIN"
    val details: String = "", // e.g., "Ticket #1234 created by John"
    val performedBy: String = "", // User ID or Email
    @ServerTimestamp val timestamp: Timestamp? = null
)