package com.example.trackr.feature_auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// This sealed class represents all possible states for the authentication screens.
sealed class AuthScreenState {
    object Idle : AuthScreenState()
    object Loading : AuthScreenState()
    object Success : AuthScreenState()
    data class Error(val message: String) : AuthScreenState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthScreenState>(AuthScreenState.Idle)
    val authState = _authState.asStateFlow()

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthScreenState.Loading
            authRepository.login(email, password)
                .onSuccess { _authState.value = AuthScreenState.Success }
                .onFailure { _authState.value = AuthScreenState.Error(it.message ?: "Unknown login error") }
        }
    }

    // New function to handle the registration state
    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthScreenState.Loading
            authRepository.register(email, password)
                .onSuccess { _authState.value = AuthScreenState.Success }
                .onFailure { _authState.value = AuthScreenState.Error(it.message ?: "Registration failed") }
        }
    }

    // New function to handle the password reset state
    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _authState.value = AuthScreenState.Loading
            // Using a general Success state. Can be customized if a specific message is needed.
            authRepository.sendPasswordResetEmail(email)
                .onSuccess { _authState.value = AuthScreenState.Success }
                .onFailure { _authState.value = AuthScreenState.Error(it.message ?: "Failed to send reset email") }
        }
    }

    fun logoutUser() {
        authRepository.logout()
    }
}