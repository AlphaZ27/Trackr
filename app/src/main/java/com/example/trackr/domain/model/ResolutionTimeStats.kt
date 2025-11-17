package com.example.trackr.domain.model

/**
 * Holds statistics for ticket resolution times, in hours.
 */
data class ResolutionTimeStats(
    val averageResolutionHours: Double = 0.0,
    val fastestResolutionHours: Double? = null, // nullable in case there are no tickets that are closed
    val slowestResolutionHours: Double? = null // nullable in case there are no tickets that are closed
)
