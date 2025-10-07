package com.example.trackr.feature_settings.data.repository

import com.example.trackr.feature_settings.data.preferences.SettingsPreferences
import com.example.trackr.feature_settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsPreferences: SettingsPreferences
) : SettingsRepository {

    override val themeMode: Flow<String> = settingsPreferences.themeMode

    override suspend fun setThemeMode(mode: String) {
        settingsPreferences.setThemeMode(mode)
    }
}