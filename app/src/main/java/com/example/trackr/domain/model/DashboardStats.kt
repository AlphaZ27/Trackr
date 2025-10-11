package com.example.trackr.domain.model

// A data class to hold the stats that will show on the dashboard
data class DashboardStats(
    val openTickets: Int = 0,
    val closedTickets: Int = 0,
    val totalTickets: Int = 0
)

// Data Class for the chart data
data class CategoryStat(
    val category: String,
    val count: Int
)