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

// Enum for SLA Tracking
enum class SLAStatus {
    OK, Warning, Overdue, Breached
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
    val createdBy: String = "",
    val category: String = "General", // Default to "General" if not specified
    val closedDate: Timestamp? = null, // This allows the value to be null if the ticket is still open

    //val closedDate: Timestamp? = null, // This allows the value to be null if the ticket is still open
    val closedBy: String? = null, // User ID of the person who closed the ticket


    val linkedArticles: List<String> = emptyList(),  // List is empty by default to avoid nulls/crashes if there are no linked articles in tickets

    // Smart Grouping & Search
    val keywords: List<String> = emptyList(), // Extracted keywords for AI matching
    val groupId: String? = null, // ID to group similar tickets (e.g., "outage_123")

    // SLA & Time Tracking
    val slaStatus: SLAStatus = SLAStatus.OK,
    val firstResponseAt: Timestamp? = null, // Time of first comment/action by agent
    val resolutionTime: Long = 0, // Duration in milliseconds

    // Workflow & History
    val reopenedCount: Int = 0,
    val technicianHistory: List<String> = emptyList(), // List of user IDs who worked on this
    val createdChannel: String = "App", // "App", "Email", "Web", "API"

    // Quality & Analytics
    val rootCause: String = "", // e.g., "Bug", "User Error", "Hardware Failure"
    val errorType: String = "", // e.g., "Network", "Database", "UI"
    val csatScore: Int? = null, // Customer Satisfaction Score (1-5)
)
