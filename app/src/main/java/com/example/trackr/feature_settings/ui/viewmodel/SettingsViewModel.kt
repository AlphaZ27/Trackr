package com.example.trackr.feature_settings.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.feature_settings.domain.model.UserType
import com.example.trackr.feature_settings.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getThemeUseCase: GetThemeUseCase,
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val getUserTypeUseCase: GetUserTypeUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    // --- Theme State ---
    val themeMode = getThemeUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "system"
        )

    fun updateTheme(newTheme: String) {
        viewModelScope.launch {
            updateThemeUseCase(newTheme)
        }
    }

    // --- Account State (New) ---
    val userType = getUserTypeUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserType.User
        )

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            // Here you would also add logic to navigate the user
            // back to the login screen after their data is cleared.
        }
    }
}