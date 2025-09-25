package com.example.trackr.domain.repository

import com.example.trackr.domain.model.Ticket
import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    // We use a Flow here so the UI can update in real-time when tickets change in Firestore
    fun getOpenTickets(): Flow<List<Ticket>>
    suspend fun createTicket(ticket: Ticket): Result<Unit>
}
