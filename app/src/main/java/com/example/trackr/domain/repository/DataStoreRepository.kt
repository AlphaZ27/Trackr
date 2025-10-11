package com.example.trackr.domain.repository

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    /**
     * Gets the saved theme preference (e.g., "dark" or "light").
     */
    fun getTheme(): Flow<String>

    /**
     * Saves the new theme preference.
     */
    suspend fun setTheme(theme: String)
}