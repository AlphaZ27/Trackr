package com.example.trackr.domain.repository

import com.example.trackr.domain.model.TicketCategory
import kotlinx.coroutines.flow.Flow

interface ConfigurationRepository {
    fun getCategories(): Flow<List<TicketCategory>>
    suspend fun addCategory(name: String): Result<Unit>
    suspend fun deleteCategory(categoryId: String): Result<Unit>
    suspend fun initializeDefaultsIfNeeded()
}