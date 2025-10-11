package com.example.trackr.domain.model

data class UserActivity(
    val user: User,
    val openTickets: Int,
    val closedTickets: Int
)
