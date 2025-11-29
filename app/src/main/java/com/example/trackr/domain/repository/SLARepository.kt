package com.example.trackr.domain.repository

import com.example.trackr.domain.model.Priority
import com.example.trackr.domain.model.SLARule
import kotlinx.coroutines.flow.Flow

interface SLARepository {
    fun getSLARules(): Flow<List<SLARule>>
    suspend fun updateSLARule(rule: SLARule): Result<Unit>
    suspend fun initializeDefaultRulesIfNeeded()
}