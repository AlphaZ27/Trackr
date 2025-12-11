package com.example.trackr.domain.repository

import com.example.trackr.domain.model.CsatResponse
import kotlinx.coroutines.flow.Flow

interface CsatRepository {
    suspend fun submitCsat(response: CsatResponse): Result<Unit>
    fun getCsatResponsesForTimeRange(startTime: Long, endTime: Long): Flow<List<CsatResponse>>
}