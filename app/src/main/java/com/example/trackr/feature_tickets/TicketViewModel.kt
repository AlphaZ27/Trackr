package com.example.trackr.feature_tickets

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.logic.GroupingEngine
import com.example.trackr.domain.repository.TicketRepository
import com.example.trackr.domain.model.*
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
    object Success : TicketDetailState() // For update, delete, and link operations
    data class Error(val message: String) : TicketDetailState()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TicketViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val kbRepository: KBRepository,
    private val groupingEngine: GroupingEngine
) : ViewModel() {

    // --- State for Ticket List Filtering ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private val _selectedStatus = MutableStateFlow<TicketStatus?>(null)
    val selectedStatus = _selectedStatus.asStateFlow()
    private val _selectedPriority = MutableStateFlow<Priority?>(null)
    val selectedPriority = _selectedPriority.asStateFlow()

    // Data Flows
    private val _tickets = ticketRepository.getAllTickets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Toggle for Group View
    private val _isGroupView = MutableStateFlow(false)
    val isGroupView = _isGroupView.asStateFlow()

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

    // Flow for grouped tickets based on the *filtered* list. Persisted and potential groups.
    // This means if you search for "Printer", you'll see groups related to printers.
    val ticketGroups: StateFlow<List<TicketGroup>> = filteredTickets.map { tickets ->
        // Only group open tickets to avoid cluttering with closed ones
        val openTickets = tickets.filter {
            it.status == TicketStatus.Open || it.status == TicketStatus.InProgress
        }
        groupingEngine.groupTickets(openTickets)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dialog State for User Grouping ---
    private val _showGroupingDialog = MutableStateFlow(false)
    val showGroupingDialog = _showGroupingDialog.asStateFlow()

    private val _suggestedGroups = MutableStateFlow<List<TicketGroup>>(emptyList())
    val suggestedGroups = _suggestedGroups.asStateFlow()

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
        .map { ticket ->
            Log.d("TrackrDebug", "1. Ticket Changed: ${ticket.id}, Links: ${ticket.linkedArticles}")
            ticket.linkedArticles
        }
        .flatMapLatest { articleIds ->
            if (articleIds.isNotEmpty()) {
                Log.d("TrackrDebug", "2. Fetching ${articleIds.size} articles from Repo...")
                kbRepository.getArticlesByIds(articleIds)
                    .onEach { articles ->
                        Log.d("TrackrDebug", "3. Articles Found: ${articles.size}")
                    }
                    .catch { e ->
                        Log.e("TrackrDebug", "Error fetching articles", e)
                        emit(emptyList())
                    }
            } else {
                Log.d("TrackrDebug", "2. No links found, returning empty.")
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


    private val _ticket = MutableStateFlow<List<Ticket>>(emptyList())
    val tickets: StateFlow<List<Ticket>> = _ticket

    init {
        loadCurrentUserTickets()
    }

    fun loadCurrentUserTickets() {
        viewModelScope.launch {
            // !! Call the new function
            _ticket.value = ticketRepository.getTicketsForCurrentUser()
        }
    }

    // --- Functions ---

    // Scan for groups to show in the Dialog
    fun scanForSimilarTickets() {
        val currentTickets = filteredTickets.value.filter {
            it.groupId == null && (it.status == TicketStatus.Open || it.status == TicketStatus.InProgress)
        }

        // This will only return potential new groups (because we filtered out groupId != null above)
        val suggestions = groupingEngine.groupTickets(currentTickets)

        _suggestedGroups.value = suggestions
        _showGroupingDialog.value = true
    }

    fun dismissGroupingDialog() {
        _showGroupingDialog.value = false
    }

    // User confirms a group -> Save to Firestore
    fun confirmGroup(group: TicketGroup) {
        viewModelScope.launch {
            val ids = group.tickets.map { it.id }
            // Using the first ticket's ID as the group ID is a simple unique strategy
            ticketRepository.groupTickets(ids, group.id)

            // Remove this group from suggestions immediately for UI responsiveness
            _suggestedGroups.value = _suggestedGroups.value.filter { it.id != group.id }

            if (_suggestedGroups.value.isEmpty()) {
                _showGroupingDialog.value = false
            }
        }
    }

    // Toggle function
    fun toggleGroupView() {
        _isGroupView.value = !_isGroupView.value
    }

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
                .onSuccess {
                    Log.d("TrackrDebug", "Link Success!")
                    _detailState.value = TicketDetailState.Success
                }
                .onFailure {
                    Log.e("TrackrDebug", "Link Failed", it)
                    _detailState.value = TicketDetailState.Error(it.message ?: "Failed to link")
                }
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

    fun submitCsat(ticketId: String, rating: Int) {
        viewModelScope.launch {
            // Update local state optimisticly if needed, or just wait for firestore
            val updates = mapOf("csatScore" to rating)
            // You might want to create a specific update function in Repository, but updateTicket works too if you fetch first
            // Here we can use a direct firestore patch via repository if exposed,
            // or just use the existing updateTicket flow:
            val current = selectedTicket.value ?: return@launch
            ticketRepository.updateTicket(current.copy(csatScore = rating))
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