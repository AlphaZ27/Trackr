package com.example.trackr.feature_kb


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.ArticleStatus
import com.example.trackr.domain.model.KBArticle
import com.example.trackr.domain.repository.KBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EditState {
    object Idle : EditState()
    object Loading : EditState()
    object Success : EditState()
    data class Error(val message: String) : EditState()
}

@HiltViewModel
class KBEditViewModel @Inject constructor(
    private val kbRepository: KBRepository
) : ViewModel() {

    // Mutable state for each form field
    val title = mutableStateOf("")
    val content = mutableStateOf("")
    val category = mutableStateOf("")
    val tags = mutableStateOf("")
    val status = mutableStateOf(ArticleStatus.Draft)
    private var currentArticleId: String? = null
    private val _editState = MutableStateFlow<EditState>(EditState.Idle)
    val editState = _editState.asStateFlow()


    fun loadArticle(articleId: String?) {
        if (articleId == null) return // This is a new article
        viewModelScope.launch {
            _editState.value = EditState.Loading
            currentArticleId = articleId
            kbRepository.getArticleById(articleId)
                .onSuccess { article ->
                    article?.let {
                        title.value = it.title
                        content.value = it.content
                        category.value = it.category
                        tags.value = it.tags.joinToString(", ")
                        status.value = it.status
                    }
                    _editState.value = EditState.Idle
                }
                .onFailure { _editState.value = EditState.Error(it.message ?: "Failed to load article") }
        }
    }

    fun saveArticle() {
        viewModelScope.launch {
            _editState.value = EditState.Loading
            val article = KBArticle(
                id = currentArticleId ?: "",
                title = title.value,
                content = content.value,
                category = category.value,
                tags = tags.value.split(",").map { it.trim() }.filter { it.isNotBlank() },
                status = status.value
            )
            kbRepository.createOrUpdateArticle(article)
                .onSuccess { _editState.value = EditState.Success }
                .onFailure { _editState.value = EditState.Error(it.message ?: "Failed to save article") }
        }
    }

    fun deleteArticle() {
        currentArticleId?.let { articleId ->
            viewModelScope.launch {
                _editState.value = EditState.Loading
                kbRepository.deleteArticle(articleId)
                    .onSuccess { _editState.value = EditState.Success }
                    .onFailure { _editState.value = EditState.Error(it.message ?: "Failed to delete article") }
            }
        }
    }
}