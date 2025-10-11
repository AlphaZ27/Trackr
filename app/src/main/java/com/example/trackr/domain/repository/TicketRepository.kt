package com.example.trackr.domain.repository

import com.example.trackr.domain.model.Ticket
import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    // We use a Flow here so the UI can update in real-time when tickets change in Firestore
    fun getAllTickets(): Flow<List<Ticket>>
    // Functions for creating tickets
    suspend fun createTicket(ticket: Ticket): Result<Unit>

    // Functions for Dashboards
    suspend fun getTicketsForCurrentUser(): List<Ticket>
    suspend fun getTicketsForTeam(teamId: String): List<Ticket>


    // Functions for ticket details
    fun getTicketById(ticketId: String): Flow<Ticket?>
    suspend fun updateTicket(ticket: Ticket): Result<Unit>
    suspend fun deleteTicket(ticketId: String): Result<Unit>
    suspend fun saveTicket(ticket: Ticket): Result<Unit>

    // Functions for linking articles to tickets
    suspend fun linkArticleToTicket(ticketId: String, articleId: String): Result<Unit>
    suspend fun unlinkArticleFromTicket(ticketId: String, articleId: String): Result<Unit>

    // Functions for ticket reports (for admins) gets all tickets for report
    fun getAllTicketsForReport(): Flow<List<Ticket>>
}
