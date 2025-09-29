package com.example.trackr.feature_kb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.KBArticle
import com.example.trackr.domain.repository.KBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KBDetailViewModel @Inject constructor(
    private val kbRepository: KBRepository
) : ViewModel() {

    private val _article = MutableStateFlow<KBArticle?>(null)
    val article = _article.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun getArticleById(articleId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            kbRepository.getArticleById(articleId)
                .onSuccess { _article.value = it }
                .onFailure { /* TODO: Handle error */ }
            _isLoading.value = false
        }
    }
}