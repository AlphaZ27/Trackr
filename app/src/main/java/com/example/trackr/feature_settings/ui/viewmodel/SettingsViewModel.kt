package com.example.trackr.feature_settings.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.UserRole
import com.example.trackr.feature_settings.domain.model.UserType
import com.example.trackr.domain.repository.AuthRepository
import com.example.trackr.domain.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
//    private val getThemeUseCase: GetThemeUseCase,
//    private val updateThemeUseCase: UpdateThemeUseCase,
//    private val getUserTypeUseCase: GetUserTypeUseCase,
//    private val logoutUseCase: LogoutUseCase
    private val authRepository: AuthRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    // Get the theme and expose it as a StateFlow
    val themeMode: StateFlow<String> = dataStoreRepository.getTheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "light"
        )

    // Get the user data and map the data-layer 'UserRole' to the UI-layer 'UserType'
    val userType: StateFlow<UserType> = authRepository.getAuthState()
        .map { firebaseUser ->
            if (firebaseUser == null) return@map UserType.User
            val user = authRepository.getCurrentUserData()
            when (user?.role) {
                UserRole.Admin -> UserType.Admin
                UserRole.Manager -> UserType.Manager
                else -> UserType.User
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserType.User
        )

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            dataStoreRepository.setTheme(theme)
        }
    }

    fun logout() {
        authRepository.logoutUser()
    }

    // --- Theme State ---
//    val themeMode = getThemeUseCase()
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5_000),
//            initialValue = "system"
//        )
//
//    fun updateTheme(newTheme: String) {
//        viewModelScope.launch {
//            updateThemeUseCase(newTheme)
//        }
//    }
//
//    // --- Account State (New) ---
//    val userType = getUserTypeUseCase()
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5_000),
//            initialValue = UserType.User
//        )
//
//    fun logout() {
//        viewModelScope.launch {
//            logoutUseCase()
//            // Here you would also add logic to navigate the user
//            // back to the login screen after their data is cleared.
//        }
//    }
}