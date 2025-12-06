package com.example.trackr.domain.repository

import com.example.trackr.domain.model.Ticket
import kotlinx.coroutines.flow.Flow

interface ReportingRepository {
    fun getTicketsInRange(startDate: Long, endDate: Long): Flow<List<Ticket>>
}