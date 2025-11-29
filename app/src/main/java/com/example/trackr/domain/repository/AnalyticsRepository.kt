package com.example.trackr.domain.repository

import com.example.trackr.domain.model.KBGlobalStats
import kotlinx.coroutines.flow.Flow

data class QualityMetrics(
    val averageCsat: Double = 0.0,
    val csatResponseRate: Double = 0.0,
    val reopenRate: Double = 0.0, // Percentage of tickets reopened
    val totalResolved: Int = 0
)

interface AnalyticsRepository {
    fun getQualityMetrics(): Flow<QualityMetrics>
    fun getKBStats(): Flow<KBGlobalStats>
    // Helper to log events (e.g. for deflection)
    suspend fun logKbView(articleId: String)
    suspend fun logTicketCreationAttempt(avoided: Boolean)
}