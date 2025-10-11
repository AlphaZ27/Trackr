package com.example.trackr.domain.model

// Holds the counts for the ticket aging buckets
data class TicketAgingStats(
    val bucket1: Int = 0, // 0-3 days
    val bucket2: Int = 0, // 4-7 days
    val bucket3: Int = 0 // 8+ days
)