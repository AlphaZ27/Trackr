package com.example.trackr.feature_splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SplashScreen(
    // It now gets its own ViewModel using Hilt, making it self-contained
    splashViewModel: SplashViewModel = hiltViewModel(),
    onUserAuthenticated: () -> Unit,
    onUserNotAuthenticated: () -> Unit
) {
    // Observe the authentication status from the SplashViewModel
    val authStatus by splashViewModel.authStatus.collectAsState()

    // This effect runs when 'authStatus' changes
    LaunchedEffect(authStatus) {
        when (authStatus) {
            is AuthStatus.Authenticated -> onUserAuthenticated()
            is AuthStatus.Unauthenticated -> onUserNotAuthenticated()
            is AuthStatus.Unknown -> { /* Do nothing, just wait for the check to complete */ }
        }
    }

    // Display a loading indicator while the check is in progress
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}