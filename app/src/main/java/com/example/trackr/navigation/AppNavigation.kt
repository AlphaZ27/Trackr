package com.example.trackr.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.trackr.feature_auth.AuthViewModel
import com.example.trackr.feature_auth.ui.ForgotPasswordScreen
import com.example.trackr.feature_auth.ui.LoginScreen
import com.example.trackr.feature_auth.ui.RegisterScreen
import com.example.trackr.feature_splash.SplashScreen
import com.example.trackr.feature_tickets.CreateTicketScreen
import com.example.trackr.ui.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        splashScreen(navController)
        authGraph(navController)
        mainGraph(navController)
    }
}

private fun NavGraphBuilder.authGraph(navController: NavController) {
    navigation(startDestination = "login", route = "auth") {
        composable("login") {
            val authViewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("main_app") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") }
            )
        }
        composable("register") {
            val authViewModel: AuthViewModel = hiltViewModel()
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("main_app") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable("forgot_password") {
            val authViewModel: AuthViewModel = hiltViewModel()
            ForgotPasswordScreen(
                authViewModel = authViewModel,
                onEmailSent = { navController.popBackStack() }
            )
        }
    }
}

private fun NavGraphBuilder.mainGraph(navController: NavController) {
    navigation(startDestination = "home", route = "main_app") {
        composable("home") {
            HomeScreen(
                onNavigateToCreateTicket = { navController.navigate("create_ticket") },
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo("main_app") { inclusive = true }
                    }
                },
                // This parameter was missing, now it's added.
                onNavigateToTicketDetail = { ticketId ->
                    navController.navigate("ticket_detail/$ticketId")
                }
            )
        }
        composable("create_ticket") {
            CreateTicketScreen(
                onTicketCreated = { navController.popBackStack() }
            )
        }
        // This defines the destination for our ticket detail screen
        composable("ticket_detail/{ticketId}") { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId")
            Box(modifier = Modifier.padding(16.dp)) {
                Text("Showing details for Ticket ID: $ticketId")
            }
        }
    }
}

private fun NavGraphBuilder.splashScreen(navController: NavController) {
    composable("splash") {
        SplashScreen(
            onUserAuthenticated = {
                navController.navigate("main_app") {
                    popUpTo("splash") { inclusive = true }
                }
            },
            onUserNotAuthenticated = {
                navController.navigate("auth") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        )
    }
}