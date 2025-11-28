package com.example.trackr.feature_kb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.KBArticle
import com.example.trackr.domain.repository.KBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class KBListViewModel @Inject constructor(
   private val kbRepository: KBRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Tab selection state (0 = ALL, 1 = FREQUENT)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    // Original list of articles from the repository
    private val _articles = kbRepository.getAllArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered list that the UI will display
    val filteredArticles: StateFlow<List<KBArticle>> =
        combine(_articles, _searchQuery) { articles, query ->
            if (query.isBlank()) {
                articles
            } else {
                articles.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.content.contains(query, ignoreCase = true) ||
                            it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _frequentArticles = kbRepository.getFrequentArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined list based on search and tab selection
    val displayedArticles: StateFlow<List<KBArticle>> =
        combine(_articles, _frequentArticles, _searchQuery, _selectedTab) { all, frequent, query, tab ->

            // 1. Determine base list based on tab
            val baseList = if (tab == 0) all else frequent

            // 2. Apply search filter
            val results = if (query.isBlank()) {
                baseList
            } else {
                baseList.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.content.contains(query, ignoreCase = true) ||
                            it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                }
            }

            // Gap Detection Logic
            // If search is active (len > 3) and returns 0 results, log it.
            // We use a debounced effect in the init block for the actual logging to avoid spam.

            results
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Gap Detection - Debounced logging
        searchQuery
            .debounce(1000) // Wait 1 second after typing stops
            .filter { it.length > 3 } // Only log meaningful terms
            .onEach { query ->
                // Check if the query returns empty on the MAIN list
                val currentList = _articles.value
                val hits = currentList.count {
                    it.title.contains(query, true) || it.content.contains(query, true)
                }
                if (hits == 0) {
                    kbRepository.logSearchGap(query)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }
}