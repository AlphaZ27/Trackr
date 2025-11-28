package com.example.trackr.domain.model

data class TicketGroup(
    val id: String, // Usually the ID of the "primary" ticket in the group
    val title: String, // Title of the group (e.g., "Printer Issues")
    val tickets: List<Ticket>,
    val size: Int
)
