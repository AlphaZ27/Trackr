package com.example.trackr.domain.model

import com.google.firebase.firestore.DocumentId

data class TicketCategory(
    @DocumentId val id: String = "",
    val name: String = ""
)