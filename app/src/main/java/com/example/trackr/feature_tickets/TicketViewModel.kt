package com.example.trackr.feature_tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.Ticket
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
    val tickets: StateFlow<List<Ticket>> = ticketRepository.getOpenTickets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // For the ticket detail screen, holds the currently viewed ticket
    private val _selectedTicket = MutableStateFlow<Ticket?>(null)
    val selectedTicket = _selectedTicket.asStateFlow()

    // This new StateFlow will communicate the state of operations to the UI
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

    //These functions did not not give good feedback on whether an operation was successful or not.

//    fun getTicketById(ticketId: String) {
//        viewModelScope.launch {
//            // Clear previous ticket to show loading state
//            _selectedTicket.value = null
//            ticketRepository.getTicketById(ticketId)
//                .onSuccess { _selectedTicket.value = it }
//                .onFailure { /* TODO: Handle error, e.g., show a toast */ }
//        }
//    }

//    fun updateTicket(ticket: Ticket) {
//        viewModelScope.launch {
//            ticketRepository.updateTicket(ticket)
//                .onSuccess { /* TODO: Maybe show a success message */ }
//                .onFailure { /* TODO: Handle error */ }
//        }
//    }

//    fun deleteTicket(ticketId: String) {
//        viewModelScope.launch {
//            ticketRepository.deleteTicket(ticketId)
//                .onSuccess { /* TODO: Navigate back or show message */ }
//                .onFailure { /* TODO: Handle error */ }
//        }
//    }
}