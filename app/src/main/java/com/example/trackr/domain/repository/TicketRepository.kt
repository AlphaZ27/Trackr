package com.example.trackr.domain.repository

import com.example.trackr.domain.model.Ticket
import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    // We use a Flow here so the UI can update in real-time when tickets change in Firestore
    fun getOpenTickets(): Flow<List<Ticket>>
    // Functions for creating tickets
    suspend fun createTicket(ticket: Ticket): Result<Unit>
    // Functions for ticket details
    suspend fun getTicketById(ticketId: String): Result<Ticket?>
    suspend fun updateTicket(ticket: Ticket): Result<Unit>
    suspend fun deleteTicket(ticketId: String): Result<Unit>
}
