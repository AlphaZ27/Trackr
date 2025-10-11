package com.example.trackr.data.models

import com.google.firebase.firestore.DocumentId

data class Ticket(
    @DocumentId val ticketId: String = "",
    val title: String = "",
    val status: String = "open", // "open" | "closed"
    val assignedTo: String = "" // This is a User ID (uid)
)