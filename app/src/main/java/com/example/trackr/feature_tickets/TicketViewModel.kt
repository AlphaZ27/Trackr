package com.example.trackr.feature_tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.Priority
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.domain.repository.TicketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// This sealed class represents the state of the Ticket Detail screen
sealed class TicketDetailState {
    object Idle : TicketDetailState()
    object Loading : TicketDetailState()
    object Success : TicketDetailState()
    data class Error(val message: String) : TicketDetailState()
}

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val ticketRepository: TicketRepository
) : ViewModel() {

    // For the list of tickets on the home screen
//    val tickets: StateFlow<List<Ticket>> = ticketRepository.getOpenTickets()
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = emptyList()
//        )
//
//    // For the ticket detail screen, holds the currently viewed ticket
//    private val _selectedTicket = MutableStateFlow<Ticket?>(null)
//    val selectedTicket = _selectedTicket.asStateFlow()
//
//    // This new StateFlow will communicate the state of operations to the UI
//    private val _detailState = MutableStateFlow<TicketDetailState>(TicketDetailState.Idle)
//    val detailState = _detailState.asStateFlow()

    // Internal state for filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedStatus = MutableStateFlow<TicketStatus?>(null)
    val selectedStatus = _selectedStatus.asStateFlow()

    private val _selectedPriority = MutableStateFlow<Priority?>(null)
    val selectedPriority = _selectedPriority.asStateFlow()

    // This is the original, unfiltered list of tickets from the repository
    private val _tickets = ticketRepository.getAllTickets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // This is the final, filtered list that the UI will display
    val filteredTickets: StateFlow<List<Ticket>> =
        combine(_tickets, _searchQuery, _selectedStatus, _selectedPriority) { tickets, query, status, priority ->
            tickets.filter { ticket ->
                val queryMatch = if (query.isBlank()) {
                    true
                } else {
                    ticket.name.contains(query, ignoreCase = true) ||
                            ticket.description.contains(query, ignoreCase = true) ||
                            ticket.id.contains(query, ignoreCase = true)
                }
                val statusMatch = status == null || ticket.status == status
                val priorityMatch = priority == null || ticket.priority == priority

                queryMatch && statusMatch && priorityMatch
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Functions for the UI to call to update filters
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStatusSelected(status: TicketStatus?) {
        _selectedStatus.value = status
    }

    fun onPrioritySelected(priority: Priority?) {
        _selectedPriority.value = priority
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedStatus.value = null
        _selectedPriority.value = null
    }


    // ************* For the list of tickets on the home screen

    // For the ticket detail screen, holds the currently viewed ticket
    private val _selectedTicket = MutableStateFlow<Ticket?>(null)
    val selectedTicket = _selectedTicket.asStateFlow()

    private val _detailState = MutableStateFlow<TicketDetailState>(TicketDetailState.Idle)
    val detailState = _detailState.asStateFlow()

    fun createTicket(ticket: Ticket) {
        viewModelScope.launch {
            ticketRepository.createTicket(ticket)
        }
    }

    fun getTicketById(ticketId: String) {
        viewModelScope.launch {
            _detailState.value = TicketDetailState.Loading
            _selectedTicket.value = null // Clear previous ticket to show loading state
            ticketRepository.getTicketById(ticketId)
                .onSuccess {
                    _selectedTicket.value = it
                    _detailState.value = TicketDetailState.Idle
                }
                .onFailure {
                    _detailState.value = TicketDetailState.Error(it.message ?: "Failed to load ticket")
                }
        }
    }

    fun updateTicket(ticket: Ticket) {
        viewModelScope.launch {
            _detailState.value = TicketDetailState.Loading
            ticketRepository.updateTicket(ticket)
                .onSuccess {
                    _detailState.value = TicketDetailState.Success
                }
                .onFailure {
                    _detailState.value = TicketDetailState.Error(it.message ?: "Failed to update ticket")
                }
        }
    }

    fun deleteTicket(ticketId: String) {
        viewModelScope.launch {
            _detailState.value = TicketDetailState.Loading
            ticketRepository.deleteTicket(ticketId)
                .onSuccess {
                    _detailState.value = TicketDetailState.Success
                }
                .onFailure {
                    _detailState.value = TicketDetailState.Error(it.message ?: "Failed to delete ticket")
                }
        }
    }
}