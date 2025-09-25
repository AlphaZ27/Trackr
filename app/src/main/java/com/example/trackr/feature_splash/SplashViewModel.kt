package com.example.trackr.feature_splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Defines the possible outcomes of the authentication check
sealed class AuthStatus {
    object Authenticated : AuthStatus()
    object Unauthenticated : AuthStatus()
    object Unknown : AuthStatus() // Initial state
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authStatus = MutableStateFlow<AuthStatus>(AuthStatus.Unknown)
    val authStatus = _authStatus.asStateFlow()

    init {
        // Check the user's status as soon as the ViewModel is created
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            if (authRepository.isUserAuthenticated()) {
                _authStatus.value = AuthStatus.Authenticated
            } else {
                _authStatus.value = AuthStatus.Unauthenticated
            }
        }
    }
}