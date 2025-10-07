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
    private val kbRepository: KBRepository
) : ViewModel() {

    val name = mutableStateOf("")
    val description = mutableStateOf("")
    val department = mutableStateOf("")
    val assignee = mutableStateOf("")
    val resolution = mutableStateOf("")
    val priority = mutableStateOf(Priority.Medium)
    val status = mutableStateOf(TicketStatus.Open)

    private val _suggestedArticles = MutableStateFlow<List<KBArticle>>(emptyList())
    val suggestedArticles = _suggestedArticles.asStateFlow()

    private val _createState = MutableStateFlow<CreateState>(CreateState.Idle)
    val createState = _createState.asStateFlow()

    init {
        // Listen for changes in the description field
        snapshotFlow { description.value }
            .debounce(500) // Wait for 500ms of no typing
            .filter { it.isNotBlank() && it.length > 10 } // Only search if description is long enough
            .onEach { desc ->
                // Use keywords from the description to search
                val keywords = desc.split(" ").filter { it.length > 3 }
                if (keywords.isNotEmpty()) {
                    kbRepository.getAllArticles().collect { articles ->
                        _suggestedArticles.value = articles.filter { article ->
                            keywords.any { keyword ->
                                article.title.contains(keyword, true) ||
                                        article.content.contains(keyword, true) ||
                                        article.tags.any { it.contains(keyword, true) }
                            }
                        }
                    }
                } else {
                    _suggestedArticles.value = emptyList()
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
                assignee = assignee.value,
                resolutionDescription = resolution.value,
                priority = priority.value,
                status = status.value
            )
            ticketRepository.createTicket(newTicket)
                .onSuccess { _createState.value = CreateState.Success }
                .onFailure { _createState.value = CreateState.Error(it.message ?: "Failed to create ticket") }
        }
    }

//    fun resetSaveState() {
//        _createState.value = false
//    }
}