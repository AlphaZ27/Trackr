package com.example.trackr.feature_auth


import com.example.trackr.feature_settings.domain.model.UserType

/**
 * Represents the different states the authentication screen can be in.
 */
sealed class AuthScreenState {
    object Idle : AuthScreenState()
    object Loading : AuthScreenState()
    data class Success(val userType: UserType) : AuthScreenState()
    data class Error(val message: String) : AuthScreenState()
}