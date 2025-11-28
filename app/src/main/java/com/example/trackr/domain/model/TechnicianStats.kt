package com.example.trackr.domain.model

import com.google.firebase.firestore.DocumentId

data class TechnicianStats(
    @DocumentId val userId: String = "", // Matches the User ID

    // Performance Counts
    val ticketsResolved: Int = 0,
    val ticketsReopened: Int = 0, // "Bounce back" rate

    // Time Metrics (in milliseconds)
    val avgResolutionTime: Long = 0,
    val avgFirstResponseTime: Long = 0,

    // Workload
    val workload: Int = 0, // Number of currently assigned OPEN tickets

    // Quality
    val peerEscalationRate: Double = 0.0, // % of tickets transferred to others
    val averageCsat: Double = 0.0 // Average Customer Satisfaction Score
)
