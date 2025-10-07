package com.example.trackr.feature_kb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.KBArticle
import com.example.trackr.domain.model.Feedback
import com.example.trackr.domain.repository.KBRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// This state class helps communicate loading/success/error back to the UI
sealed class KBDetailState {
    object Idle : KBDetailState()
    object Loading : KBDetailState()
    object Success : KBDetailState()
    data class Error(val message: String) : KBDetailState()
}

@HiltViewModel
class KBDetailViewModel @Inject constructor(
    private val kbRepository: KBRepository,
    private val auth: FirebaseAuth // Inject FirebaseAuth
) : ViewModel() {

    private val _article = MutableStateFlow<KBArticle?>(null)
    val article = _article.asStateFlow()


    private val _uiState = MutableStateFlow<KBDetailState>(KBDetailState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    // StateFlow to hold the feedback list
    private val _feedbackList = MutableStateFlow<List<Feedback>>(emptyList())
    val feedbackList = _feedbackList.asStateFlow()

    private val _userFeedback = MutableStateFlow<Feedback?>(null)
    val userFeedback = _userFeedback.asStateFlow()

    private var feedbackJob: Job? = null

    fun getArticleById(articleId: String) {
        viewModelScope.launch {
            _uiState.value = KBDetailState.Loading
            feedbackJob?.cancel() // Cancel previous feedback listener

            kbRepository.getArticleById(articleId)
                .onSuccess {
                    _article.value = it
                    _uiState.value = KBDetailState.Idle
                    // Start listening for feedback for this article
                    feedbackJob = launch {
                        kbRepository.getFeedbackForArticle(articleId).collect { feedback ->
                            _feedbackList.value = feedback
                            _userFeedback.value = feedback.find { it.userId == auth.currentUser?.uid }
                        }
                    }
                }
                .onFailure {
                    _uiState.value = KBDetailState.Error(it.message ?: "Failed to load article")
                }
        }
    }

    // Function to submit feedback
    fun submitFeedback(articleId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val feedback = Feedback(
                articleId = articleId,
                rating = rating,
                comment = comment.takeIf { it.isNotBlank() }
            )
            kbRepository.submitFeedback(feedback)
                .onSuccess { _uiState.value = KBDetailState.Success }
                .onFailure { _uiState.value = KBDetailState.Error(it.message ?: "Failed to submit feedback") }
        }
    }



    fun saveTicket(articleId: String) {
        viewModelScope.launch {
            kbRepository.saveArticle(articleId)
            _saveSuccess.value = true
        }
    }

    fun resetSaveState() {
        _saveSuccess.value = false
    }

    // This function will be called by the UI after it shows a message
    fun resetUiState() {
        _uiState.value = KBDetailState.Idle
    }

    // Function to handle the deletion logic
    fun deleteArticle(articleId: String) {
        viewModelScope.launch {
            _uiState.value = KBDetailState.Loading
            kbRepository.deleteArticle(articleId)
                .onSuccess { _uiState.value = KBDetailState.Success }
                .onFailure { _uiState.value = KBDetailState.Error(it.message ?: "Failed to delete article") }
        }
    }
}