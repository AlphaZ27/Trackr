package com.example.trackr.domain.model

data class UserPerformance(
    val user: User,
    val ticketsClosed: Int,
    val avgResolutionHours: Double // Average time to close in hours
)
