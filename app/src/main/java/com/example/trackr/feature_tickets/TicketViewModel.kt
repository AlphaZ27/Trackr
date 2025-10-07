package com.example.trackr.feature_tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.Priority
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.domain.repository.TicketRepository
import com.example.trackr.domain.model.KBArticle
import com.example.trackr.domain.repository.KBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TicketViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val kbRepository: KBRepository
) : ViewModel() {

    // --- State for Ticket List Filtering ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private val _selectedStatus = MutableStateFlow<TicketStatus?>(null)
    val selectedStatus = _selectedStatus.asStateFlow()
    private val _selectedPriority = MutableStateFlow<Priority?>(null)
    val selectedPriority = _selectedPriority.asStateFlow()
    private val _tickets = ticketRepository.getAllTickets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val filteredTickets: StateFlow<List<Ticket>> =
        combine(_tickets, _searchQuery, _selectedStatus, _selectedPriority) { tickets, query, status, priority ->
            tickets.filter { ticket ->
                val queryMatch = if (query.isBlank()) true else {
                    ticket.name.contains(query, ignoreCase = true) ||
                            ticket.description.contains(query, ignoreCase = true) ||
                            ticket.id.contains(query, ignoreCase = true)
                }
                val statusMatch = status == null || ticket.status == status
                val priorityMatch = priority == null || ticket.priority == priority
                queryMatch && statusMatch && priorityMatch
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- State for Ticket Detail Screen ---
    private val _selectedTicketId = MutableStateFlow<String?>(null)
    private val _detailState = MutableStateFlow<TicketDetailState>(TicketDetailState.Idle)
    val detailState = _detailState.asStateFlow()

    // This flow listens to the ID and automatically fetches the ticket in real-time.
    val selectedTicket: StateFlow<Ticket?> = _selectedTicketId
        .filterNotNull()
        .flatMapLatest { ticketId ->
            ticketRepository.getTicketById(ticketId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // This flow automatically listens to changes in the selectedTicket
    // and re-fetches the linked articles whenever the ticket's 'linkedArticles' list changes.
    val linkedArticles: StateFlow<List<KBArticle>> = selectedTicket
        .filterNotNull()
        .map { it.linkedArticles }
        .flatMapLatest { articleIds ->
            if (articleIds.isNotEmpty()) {
                kbRepository.getArticlesByIds(articleIds)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- State for "Link Article" Dialog ---
    private val _kbSearchQuery = MutableStateFlow("")
    val kbSearchQuery = _kbSearchQuery.asStateFlow()

    private val _allKbArticles = kbRepository.getAllArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchableArticles: StateFlow<List<KBArticle>> =
        combine(_allKbArticles, _kbSearchQuery) { articles, query ->
            if (query.isBlank()) articles else articles.filter { it.title.contains(query, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Functions ---
    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun onStatusSelected(status: TicketStatus?) { _selectedStatus.value = status }
    fun onPrioritySelected(priority: Priority?) { _selectedPriority.value = priority }
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedStatus.value = null
        _selectedPriority.value = null
    }
    fun onKbSearchQueryChange(query: String) { _kbSearchQuery.value = query }

    fun resetDetailState() {
        _detailState.value = TicketDetailState.Idle
    }

    fun getTicketById(ticketId: String) {
        _selectedTicketId.value = ticketId
    }

    fun linkArticle(ticketId: String, articleId: String) {
        viewModelScope.launch {
            _detailState.value = TicketDetailState.Loading
            ticketRepository.linkArticleToTicket(ticketId, articleId)
                .onSuccess { _detailState.value = TicketDetailState.Success }
                .onFailure { _detailState.value = TicketDetailState.Error(it.message ?: "Failed to link article") }
        }
    }

    fun unlinkArticle(ticketId: String, articleId: String) {
        viewModelScope.launch {
            _detailState.value = TicketDetailState.Loading
            ticketRepository.unlinkArticleFromTicket(ticketId, articleId)
                .onSuccess { _detailState.value = TicketDetailState.Success }
                .onFailure { _detailState.value = TicketDetailState.Error(it.message ?: "Failed to unlink article") }
        }
    }

    fun createTicket(ticket: Ticket) {
        viewModelScope.launch {
            ticketRepository.createTicket(ticket)
        }
    }

    fun updateTicket(ticket: Ticket) {
        viewModelScope.launch {
            _detailState.value = TicketDetailState.Loading
            ticketRepository.updateTicket(ticket)
                .onSuccess { _detailState.value = TicketDetailState.Success }
                .onFailure { _detailState.value = TicketDetailState.Error(it.message ?: "Failed to update ticket") }
        }
    }

    fun deleteTicket(ticketId: String) {
        viewModelScope.launch {
            _detailState.value = TicketDetailState.Loading
            ticketRepository.deleteTicket(ticketId)
                .onSuccess { _detailState.value = TicketDetailState.Success }
                .onFailure { _detailState.value = TicketDetailState.Error(it.message ?: "Failed to delete ticket") }
        }
    }
}