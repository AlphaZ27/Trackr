package com.example.trackr.domain.model

data class ResolvedTicketStats(
    val last7Days: Int = 0,
    val last30Days: Int = 0,
    val last90Days: Int = 0
)
