package com.example.trackr.domain.model

/**
 * Holds the counts for the user creation line chart.
 */
data class UserCreationStats(
    val last7Days: Int = 0,
    val last30Days: Int = 0,
    val last90Days: Int = 0
)
