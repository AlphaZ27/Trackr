package com.example.trackr.domain.model

import com.google.firebase.Timestamp

// Enums provide type-safety and make the code much cleaner than using strings.
enum class TicketStatus {
    Open, InProgress, Closed
}

enum class Priority {
    Low, Medium, High, Urgent
}

data class Ticket(
    val id: String = "", // Firestore document ID
    val name: String = "",
    val assigneeId: String? = null,
    val priority: Priority = Priority.Medium,
    val createdDate: Timestamp = Timestamp.now(),
    val description: String = "",
    val department: String = "",
    val status: TicketStatus = TicketStatus.Open,
    val resolution: String? = null
)
