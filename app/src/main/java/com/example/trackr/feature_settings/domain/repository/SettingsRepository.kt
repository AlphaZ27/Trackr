package com.example.trackr.feature_settings.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<String>
    suspend fun setThemeMode(mode: String)
}