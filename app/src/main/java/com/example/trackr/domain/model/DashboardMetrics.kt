package com.example.trackr.domain.model

data class DashboardMetrics(
    val totalTickets: Int = 0,
    val openTickets: Int = 0,
    val closedTickets: Int = 0,
    val inProgressTickets: Int = 0,

    // Advanced Metrics
    val avgResolutionTimeHours: Double = 0.0,
    val slaBreachRate: Double = 0.0, // Percentage 0-100
    val reopenRate: Double = 0.0, // Percentage 0-100

    // Distributions for Charts
    val ticketsByStatus: Map<String, Int> = emptyMap(),
    val ticketsByPriority: Map<String, Int> = emptyMap(),
    val ticketsByDepartment: Map<String, Int> = emptyMap(),

    // Volume Trend (Day -> Count)
    val ticketVolumeLast7Days: List<Pair<String, Int>> = emptyList()
)