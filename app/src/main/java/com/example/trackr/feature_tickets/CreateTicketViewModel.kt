package com.example.trackr.feature_tickets

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.KBArticle
import com.example.trackr.domain.model.Priority
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.domain.repository.KBRepository
import com.example.trackr.domain.repository.TicketRepository
import com.example.trackr.domain.model.User
import com.example.trackr.domain.logic.SimilarityEngine
import com.example.trackr.domain.repository.DashboardRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// This sealed class represents the state of the create operation
sealed class CreateState {
    object Idle : CreateState()
    object Loading : CreateState()
    object Success : CreateState()
    data class Error(val message: String) : CreateState()
}

@OptIn(FlowPreview::class)
@HiltViewModel
class CreateTicketViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val kbRepository: KBRepository,
    private val auth: FirebaseAuth,
    private val dashboardRepository: DashboardRepository,
    private val similarityEngine: SimilarityEngine
) : ViewModel() {

    val name = mutableStateOf("")
    val description = mutableStateOf("")
    val department = mutableStateOf("")
    val assignee = mutableStateOf<User?>(null) // Changed from String to User?
    val resolution = mutableStateOf("")
    val priority = mutableStateOf(Priority.Medium)
    val status = mutableStateOf(TicketStatus.Open)
    val category = mutableStateOf("General") // Default value

    // StateFlow for the list of all users
    val users: StateFlow<List<User>> = dashboardRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _allArticles = kbRepository.getAllArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _suggestedArticles = MutableStateFlow<List<KBArticle>>(emptyList())
    val suggestedArticles = _suggestedArticles.asStateFlow()

    private val _createState = MutableStateFlow<CreateState>(CreateState.Idle)
    val createState = _createState.asStateFlow()

    // State for duplicate warning
    private val _potentialDuplicates = MutableStateFlow<List<Ticket>>(emptyList())
    val potentialDuplicates = _potentialDuplicates.asStateFlow()

    // Load recent tickets for checking
    private val _recentTickets = ticketRepository.getRecentOpenTickets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Listen for changes in the description field
        snapshotFlow { description.value }
            .debounce(500) // Wait for 500ms of no typing
            .filter { it.isNotBlank() && it.length > 10 } // Only search if description is long enough
            .onEach { desc ->
                // Use keywords from the description to search
//                val keywords = desc.split(" ").filter { it.length > 3 }
//                if (keywords.isNotEmpty()) {
//                    kbRepository.getAllArticles().collect { articles ->
//                        _suggestedArticles.value = articles.filter { article ->
//                            keywords.any { keyword ->
//                                article.title.contains(keyword, true) ||
//                                        article.content.contains(keyword, true) ||
//                                        article.tags.any { it.contains(keyword, true) }
//                            }
//                        }
//                    }
//                } else {
//                    _suggestedArticles.value = emptyList()
//                }

                val articles = _allArticles.value
                if (articles.isNotEmpty()) {
                    _suggestedArticles.value = similarityEngine.findRelevantArticles(desc, articles).take(3)
                }

                // 2. Search for Duplicates
                val recent = _recentTickets.value
                if (recent.isNotEmpty()) {
                    // We reuse the same similarity engine!
                    // Map tickets to "Articles" temporarily or overload the engine function?
                    // Let's overloading the engine function below or mapping manually:

                    val duplicates = recent.filter { ticket ->
                        // Check similarity against ticket description + title
                        val score = similarityEngine.calculateSimilarity(desc, "${ticket.name} ${ticket.description}")
                        score > 0.4 // Threshold for duplicate detection or "This looks like a duplicate"
                    }
                    _potentialDuplicates.value = duplicates.take(2)
                }
            }
            .launchIn(viewModelScope)
    }

    fun createTicket() {
        viewModelScope.launch {
            _createState.value = CreateState.Loading
            val newTicket = Ticket(
                name = name.value,
                description = description.value,
                department = department.value,
                assignee = assignee.value?.id ?: "", // Changed from .value to .value?.id ?: ""
                resolutionDescription = resolution.value,
                priority = priority.value,
                status = status.value,
                createdBy = auth.currentUser?.uid ?: "unknown", // Use the current user's ID or default to "unknown"
                category = category.value
            )
            ticketRepository.createTicket(newTicket)
                .onSuccess { _createState.value = CreateState.Success }
                .onFailure { _createState.value = CreateState.Error(it.message ?: "Failed to create ticket") }
        }
    }

    fun resetSaveState() {
        _createState.value = CreateState.Idle
    }
}