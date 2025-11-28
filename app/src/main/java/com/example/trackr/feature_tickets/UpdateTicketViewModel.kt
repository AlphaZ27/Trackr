package com.example.trackr.feature_tickets

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.*
import com.example.trackr.domain.repository.DashboardRepository
import com.example.trackr.domain.repository.TicketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// This sealed class represents the state of the Update screen
sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}

@HiltViewModel
class UpdateTicketViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
    dashboardRepository: DashboardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // --- State for Editable Fields ---
    val name = mutableStateOf("")
    val description = mutableStateOf("")
    val department = mutableStateOf("")
    val resolution = mutableStateOf("")
    val priority = mutableStateOf(Priority.Medium)
    val status = mutableStateOf(TicketStatus.Open)
    val category = mutableStateOf("General")
    val assignee = mutableStateOf<User?>(null)

    // --- State for UI ---
    val users: StateFlow<List<User>> = dashboardRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _ticket = MutableStateFlow<Ticket?>(null)
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState = _updateState.asStateFlow()

    private val ticketId: String = savedStateHandle.get<String>("ticketId")!!

    init {
        if (ticketId.isNotBlank()) {
            viewModelScope.launch {
                // Combine the ticket and user flows
                combine(
                    ticketRepository.getTicketById(ticketId),
                    users
                ) { ticket, userList ->
                    ticket?.let {
                        _ticket.value = it
                        name.value = it.name
                        description.value = it.description
                        department.value = it.department
                        resolution.value = it.resolutionDescription
                        priority.value = it.priority
                        status.value = it.status
                        category.value = it.category
                        // Find the User object that matches the saved assignee ID
                        assignee.value = userList.find { user -> user.id == it.assignee }
                    }
                }.collect() // Collect to keep the flow active
            }
        }
    }

    fun updateTicket() {
        val currentTicket = _ticket.value ?: return
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val updatedTicket = currentTicket.copy(
                name = name.value,
                description = description.value,
                department = department.value,
                resolutionDescription = resolution.value,
                priority = priority.value,
                status = status.value,
                category = category.value,
                assignee = assignee.value?.id ?: "" // Save the ID
            )
            ticketRepository.updateTicket(updatedTicket)
                .onSuccess { _updateState.value = UpdateState.Success }
                .onFailure { _updateState.value = UpdateState.Error(it.message ?: "Failed to update") }
        }
    }

    fun deleteTicket() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            ticketRepository.deleteTicket(ticketId)
                .onSuccess { _updateState.value = UpdateState.Success }
                .onFailure { _updateState.value = UpdateState.Error(it.message ?: "Failed to delete") }
        }
    }
}