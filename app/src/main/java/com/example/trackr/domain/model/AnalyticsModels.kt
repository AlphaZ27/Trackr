package com.example.trackr.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Collection: daily_ticket_stats
// ID Format: "YYYY-MM-DD"
data class DailyTicketStats(
    @DocumentId val dateId: String = "",
    val totalCreated: Int = 0,
    val totalResolved: Int = 0,
    val totalBreached: Int = 0,
    val ticketsByCategory: Map<String, Int> = emptyMap(), // e.g., {"IT": 5, "HR": 2}
    val ticketsByPriority: Map<String, Int> = emptyMap()
)

// Collection: kb_stats
// Single document: "global_stats"
data class KBGlobalStats(
    @DocumentId val id: String = "global_stats",
    val totalViews: Int = 0,
    val totalDeflections: Int = 0, // User viewed article -> Did NOT create ticket
    val searchGaps: List<String> = emptyList() // Top search terms returning 0 results
)

// Collection: infrastructure_events
data class InfrastructureEvent(
    @DocumentId val id: String = "",
    val type: String = "", // "Server Outage", "Network Slowdown"
    val detectedAt: Timestamp = Timestamp.now(),
    val resolvedAt: Timestamp? = null,
    val affectedLocations: List<String> = emptyList(),
    val relatedTicketIds: List<String> = emptyList()
)