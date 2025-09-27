package com.example.trackr.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.trackr.feature_auth.AuthViewModel
import com.example.trackr.feature_auth.ui.ForgotPasswordScreen
import com.example.trackr.feature_auth.ui.LoginScreen
import com.example.trackr.feature_auth.ui.RegisterScreen
import com.example.trackr.feature_splash.SplashScreen
import com.example.trackr.feature_tickets.CreateTicketScreen
import com.example.trackr.feature_tickets.TicketDetailScreen
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
                    navController.navigate("main_app") { popUpTo("auth") { inclusive = true } }
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
                    navController.navigate("main_app") { popUpTo("auth") { inclusive = true } }
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

private fun NavGraphBuilder.splashScreen(navController: NavController) {
    composable("splash") {
        SplashScreen(
            onUserAuthenticated = {
                navController.navigate("main_app") { popUpTo("splash") { inclusive = true } }
            },
            onUserNotAuthenticated = {
                navController.navigate("auth") { popUpTo("splash") { inclusive = true } }
            }
        )
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
                onNavigateToTicketDetail = { ticketId ->
                    // This is the action that sends the user to the detail screen
                    navController.navigate("ticket_detail/$ticketId")
                }
            )
        }
        composable("create_ticket") {
            CreateTicketScreen(
                onTicketCreated = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        // This is the new route for the Ticket Detail Screen
        composable(
            route = "ticket_detail/{ticketId}",
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) { backStackEntry ->
            // We retrieve the ticketId from the route's arguments
            val ticketId = backStackEntry.arguments?.getString("ticketId")
            requireNotNull(ticketId) { "Ticket ID is required as an argument" }

            // We pass the ticketId and the back navigation action to the screen
            TicketDetailScreen(
                ticketId = ticketId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}