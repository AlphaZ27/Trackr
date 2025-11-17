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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.trackr.domain.model.UserRole
import com.example.trackr.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    // It now gets its own ViewModel using Hilt, making it self-contained
    splashViewModel: SplashViewModel = hiltViewModel(),
    onUserAuthenticated: (UserRole) -> Unit,
    onUserNotAuthenticated: () -> Unit,
) {
    val state by splashViewModel.splashState.collectAsState()

    // Display a loading indicator while the check is in progress
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (state.isLoading) {
            CircularProgressIndicator()
        }
    }

    // React to the state from the ViewModel
    LaunchedEffect(state) {
        if (!state.isLoading) {
            if (state.isAuthenticated) {
                onUserAuthenticated(state.userRole ?: UserRole.User)
            } else {
                onUserNotAuthenticated()
            }
        }
    }



// Observe the authentication status from the SplashViewModel
//    val authStatus by splashViewModel.splashState.collectAsState()


    // This effect runs when 'authStatus' changes
//    LaunchedEffect(authStatus) {
//        when (authStatus) {
//            is AuthStatus.Authenticated -> onUserAuthenticated()
//            is AuthStatus.Unauthenticated -> onUserNotAuthenticated()
//            is AuthStatus.Unknown -> { /* Do nothing, just wait for the check to complete */ }
//        }
//    }

//    LaunchedEffect(key1 = true) {
//        // Check auth state
//        val user = authRepository.getAuthState().first()
//        if (user == null) {
//            onUserNotAuthenticated()
//        } else {
//            // **THE FIX**: If authenticated, get the user's role
//            val userData = authRepository.getCurrentUserData()
//            val role = userData?.role ?: UserRole.User // Default to User if missing
//            onUserAuthenticated(role)
//        }
//    }

    // Display a loading indicator while the check is in progress
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        CircularProgressIndicator()
//    }
}