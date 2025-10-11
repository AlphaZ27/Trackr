package com.example.trackr.feature_auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.trackr.feature_auth.AuthViewModel
import com.example.trackr.feature_auth.AuthScreenState
import com.example.trackr.feature_settings.domain.model.UserType

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (userType: UserType) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by authViewModel.authState.collectAsState()

    // This LaunchedEffect is now the single source of truth for handling success.
    LaunchedEffect(loginState) {
        if (loginState is AuthScreenState.Success) {
            // Correctly call the lambda without a named argument
            onLoginSuccess((loginState as AuthScreenState.Success).userType)
            // Reset the state to prevent re-triggering on configuration changes
            authViewModel.resetAuthState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Trackr Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            // The onClick now calls the simplified ViewModel function
            onClick = { authViewModel.loginUser(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = loginState !is AuthScreenState.Loading
        ) {
            if (loginState is AuthScreenState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Login")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onNavigateToForgotPassword) {
                Text("Forgot Password?")
            }
            TextButton(onClick = onNavigateToRegister) {
                Text("Register")
            }
        }

        if (loginState is AuthScreenState.Error) {
            Text(
                text = (loginState as AuthScreenState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}