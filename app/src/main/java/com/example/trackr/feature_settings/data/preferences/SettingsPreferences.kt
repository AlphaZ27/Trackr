package com.example.trackr.feature_settings.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsPreferences @Inject constructor(private val dataStore: DataStore<Preferences>) {
    // 1. Define a key for the theme preference
    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    // 2. Expose a Flow to observe theme changes
    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "system" // Default to system theme
    }

    // 3. Create a function to update the theme
    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }
}