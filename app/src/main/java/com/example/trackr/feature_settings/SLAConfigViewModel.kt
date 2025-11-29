package com.example.trackr.feature_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.SLARule
import com.example.trackr.domain.repository.SLARepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SLAConfigViewModel @Inject constructor(
    private val slaRepository: SLARepository
) : ViewModel() {

    val slaRules = slaRepository.getSLARules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            slaRepository.initializeDefaultRulesIfNeeded()
        }
    }

    fun updateRule(rule: SLARule, hours: Int) {
        val updatedRule = rule.copy(
            maxResolutionTimeHours = hours,
            warningThresholdHours = (hours * 0.8).toInt() // Auto-calc warning at 80%
        )
        viewModelScope.launch {
            slaRepository.updateSLARule(updatedRule)
        }
    }
}