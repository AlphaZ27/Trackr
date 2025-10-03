package com.example.trackr.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Enums provide type-safety and make the code much cleaner than using strings.
enum class TicketStatus {
    Open, InProgress, Closed
}

enum class Priority {
    Low, Medium, High, Urgent
}

data class Ticket(
    // This annotation tells Firestore to automatically populate this field with the document ID
    @DocumentId  val id: String = "", // Firestore document ID
    val name: String = "",
    val assignee: String = "",
    val priority: Priority = Priority.Medium,
    val createdDate: Timestamp = Timestamp.now(),
    val description: String = "",
    val department: String = "",
    val status: TicketStatus = TicketStatus.Open,
    val resolutionDescription: String = "",
    val linkedArticles: List<String> = emptyList()  // List is empty by default to avoid nulls/crashes if there are no linked articles in tickets
)
