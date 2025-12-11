package com.example.trackr.domain.repository

import com.example.trackr.domain.model.ActivityLog
import kotlinx.coroutines.flow.Flow

interface ActivityLogRepository {
    fun getLogs(): Flow<List<ActivityLog>>
    suspend fun logAction(action: String, details: String)
}