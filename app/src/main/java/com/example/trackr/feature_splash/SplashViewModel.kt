package com.example.trackr.feature_splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.UserRole
import com.example.trackr.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// Defines the possible outcomes of the authentication check
data class SplashState(
    val isLoading: Boolean = true,
    val userRole: UserRole? = null,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _splashState = MutableStateFlow(SplashState())
    val splashState = _splashState.asStateFlow()

    init {
        // Check the user's status as soon as the ViewModel is created
        checkAuthentication()
    }

    private fun checkAuthentication() {
        viewModelScope.launch {
            val user = authRepository.getAuthState().first()
            if (user == null) {
                _splashState.value = SplashState(isLoading = false, isAuthenticated = false)
            } else {
                val userData = authRepository.getCurrentUserData()
                val role = userData?.role ?: UserRole.User
                _splashState.value = SplashState(isLoading = false, isAuthenticated = true, userRole = role)
            }
        }
    }
}