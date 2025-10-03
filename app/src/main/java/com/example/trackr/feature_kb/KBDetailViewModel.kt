package com.example.trackr.feature_kb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.KBArticle
import com.example.trackr.domain.model.Feedback
import com.example.trackr.domain.repository.KBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val kbRepository: KBRepository
) : ViewModel() {

    private val _article = MutableStateFlow<KBArticle?>(null)
    val article = _article.asStateFlow()


    private val _uiState = MutableStateFlow<KBDetailState>(KBDetailState.Idle)
    val uiState = _uiState.asStateFlow()

    // StateFlow to hold the feedback list
    private val _feedbackList = MutableStateFlow<List<Feedback>>(emptyList())
    val feedbackList = _feedbackList.asStateFlow()
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
            // No state change needed, success will be reflected by the real-time feedback list updating
        }
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