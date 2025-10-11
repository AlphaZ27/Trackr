package com.example.trackr.feature_auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.UserRole
import com.example.trackr.domain.repository.AuthRepository
import com.example.trackr.feature_settings.domain.model.UserType
import com.example.trackr.feature_settings.domain.repository.AccountRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.trackr.feature_auth.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// This sealed class is used to define the UI states
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState() // A state for successful login/registration
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

//    fun loginUser(email: String, password: String) {
//        viewModelScope.launch {
//            _authState.value = AuthState.Loading
//            authRepository.login(email, password)
//                .onSuccess { _authState.value = AuthState.Success }
//                .onFailure { _authState.value = AuthState.Error(it.message ?: "Unknown login error") }
//        }
//    }

    fun loginUser(email: String, password: String, onLoginSuccess: (UserRole) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(email, password)
                .onSuccess {
                    // After successful login, fetch the user's role
                    val user = authRepository.getCurrentUserData()
                    val role = user?.role ?: UserRole.User // Default to User if role is missing
                    onLoginSuccess(role) // Pass the role to the callback
                    _authState.value = AuthState.Authenticated
                }
                .onFailure {
                    _authState.value = AuthState.Error(it.message ?: "An unknown error occurred")
                }
        }
    }

    // Function to handle the registration state
    // The onRegisterSuccess callback is now a parameter of the function
    fun registerUser(email: String, name: String, password: String, onRegisterSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.register(email, name, password)
                .onSuccess {
                    onRegisterSuccess() // Call the callback passed from the UI
                    _authState.value = AuthState.Authenticated
                }
                .onFailure {
                    _authState.value = AuthState.Error(it.message ?: "An unknown error occurred")
                }
        }
    }

    // Function to handle the password reset state
    // The onEmailSent callback is now a parameter of the function
    fun sendPasswordResetEmail(email: String, onEmailSent: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.sendPasswordResetEmail(email)
                .onSuccess {
                    onEmailSent()
                    _authState.value = AuthState.Idle
                }
                .onFailure {
                    _authState.value = AuthState.Error(it.message ?: "An unknown error occurred")
                }
        }
    }

//    fun logoutUser() {
//        authRepository.logout()
//    }

    fun logoutUser() {
        authRepository.logoutUser()
        _authState.value = AuthState.Idle
    }
}