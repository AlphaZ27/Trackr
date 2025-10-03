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
import com.example.trackr.feature_tickets.UpdateTicketScreen
import com.example.trackr.feature_kb.KBDetailScreen
import com.example.trackr.feature_kb.KBEditScreen
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
                    navController.navigate("ticket_detail/$ticketId")
                },
                onNavigateToArticleDetail = { articleId ->
                    navController.navigate("kb_detail/$articleId")
                },
                onNavigateToCreateArticle = {
                    navController.navigate("kb_edit") // Note: No ID is passed
                }
            )
        }
        composable("create_ticket") {
            CreateTicketScreen(
                onTicketCreated = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToArticle = { articleId ->
                    navController.navigate("kb_detail/$articleId")
                }
            )
        }
        composable(
            route = "ticket_detail/{ticketId}",
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId")
            requireNotNull(ticketId) { "Ticket ID is required as an argument" }

            TicketDetailScreen(
                ticketId = ticketId,
                onNavigateToUpdate = { id -> navController.navigate("updateTicket/$id") },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToArticle = { articleId ->
                    navController.navigate("kb_detail/$articleId")
                }
            )

        }
        composable(
            route = "updateTicket/{ticketId}",
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: return@composable
            UpdateTicketScreen(
                ticketId = ticketId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "kb_detail/{articleId}",
            arguments = listOf(navArgument("articleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId")
            requireNotNull(articleId) { "Article ID is required" }
            KBDetailScreen(
                articleId = articleId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate("kb_edit?articleId=$id")
                }
            )
        }
        composable(
            route = "kb_edit?articleId={articleId}",
            arguments = listOf(navArgument("articleId") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId")
            KBEditScreen(
                articleId = articleId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

