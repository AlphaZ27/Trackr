package com.example.trackr.feature_admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.repository.ConfigurationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryConfigViewModel @Inject constructor(
    private val configRepository: ConfigurationRepository
) : ViewModel() {

    val categories = configRepository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            configRepository.initializeDefaultsIfNeeded()
        }
    }

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            configRepository.addCategory(name.trim())
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            configRepository.deleteCategory(id)
        }
    }
}