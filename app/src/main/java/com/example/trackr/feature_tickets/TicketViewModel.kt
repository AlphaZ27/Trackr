package com.example.trackr.feature_tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.repository.TicketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val ticketRepository: TicketRepository
) : ViewModel() {

    // Expose the flow of tickets as StateFlow so the UI can collect it
    val tickets: StateFlow<List<Ticket>> = ticketRepository.getOpenTickets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createTicket(ticket: Ticket) {
        viewModelScope.launch {
            ticketRepository.createTicket(ticket)
            // You can add error handling here if needed
        }
    }
}