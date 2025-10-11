package com.example.trackr.navigation


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.trackr.domain.model.UserRole
import com.example.trackr.domain.repository.AuthRepository
import com.example.trackr.feature_admin.AdminDashboardScreen
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
import com.example.trackr.feature_kb.KBListScreen
import com.example.trackr.feature_manager.ManagerDashboardScreen
import com.example.trackr.feature_settings.ui.SettingsScreen
import com.example.trackr.feature_settings.domain.model.UserType
import com.example.trackr.feature_tickets.TicketsScreen
import com.example.trackr.feature_user.UserDashboardScreen
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
                onLoginSuccess = { userRole -> // ❗️ The lambda now receives userRole
                    // ❗️ Use the user's role to navigate to the correct start destination
                    val startDestination = when (userRole) {
                        UserRole.Admin -> "admin_dashboard"
                        UserRole.Manager -> "manager_dashboard"
                        UserRole.User -> "user_dashboard" // Or your main tickets screen
                    }

                    // Navigate to the main app graph, starting at the correct dashboard
                    navController.navigate(startDestination) {
                        // Clear the entire auth back stack
                        popUpTo("auth") { inclusive = true }
                        restoreState = false
                        launchSingleTop = true
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
        //val authRepository: AuthRepository = hiltViewModel()
        SplashScreen(
            onUserAuthenticated = { userRole ->
                val startDestination = when (userRole) {
                    UserRole.Admin -> "admin_dashboard"
                    UserRole.Manager -> "manager_dashboard"
                    UserRole.User -> "user_dashboard"
                }
                navController.navigate(startDestination) {
                    popUpTo("splash") { inclusive = true }
                    restoreState = false
                    launchSingleTop = true
                }
            },
            onUserNotAuthenticated = {
                navController.navigate("auth") { popUpTo("splash") { inclusive = true } }
            }
        )
    }
}

private fun NavGraphBuilder.mainGraph(navController: NavController) {
    // MainGraph uses HomeScreen as a shell for the tabs.
    navigation(startDestination = "tickets_route", route = "main_app") {

        composable("tickets_route") {
            HomeScreen(
                navController = navController,
                onLogout = { navController.navigate("auth") { popUpTo("main_app") { inclusive = true } } }
            ) { modifier ->
                TicketsScreen(
                    modifier = modifier,
                    onTicketClick = { ticketId -> navController.navigate("ticket_detail/$ticketId") }
                )
            }
        }

        composable("kb_route") {
            HomeScreen(
                navController = navController,
                onLogout = { navController.navigate("auth") { popUpTo("main_app") { inclusive = true } } }
            ) { modifier ->
                KBListScreen(
                    modifier = modifier,
                    onNavigateToArticle = { articleId -> navController.navigate("kb_detail/$articleId") },
                    onNavigateToCreateArticle = { navController.navigate("kb_edit") }
                )
            }
        }

        composable("settings_route") {
            HomeScreen(
                navController = navController,
                onLogout = { navController.navigate("auth") { popUpTo("main_app") { inclusive = true } } }
            ) { modifier ->
                SettingsScreen(modifier = modifier, navController = navController)
            }
        }

        // --- Dashboard Screens ---
        composable("admin_dashboard") {
            // You will eventually replace this with a real screen
            AdminDashboardScreen(navController = navController)
        }
        composable("manager_dashboard") {
            ManagerDashboardScreen(navController = navController)
        }
        composable("user_dashboard") {
            HomeScreen(
                navController = navController,
                onLogout = { navController.navigate("auth") { popUpTo("main_app") { inclusive = true } } }
            ) { modifier ->
                TicketsScreen(
                    modifier = modifier,
                    onTicketClick = { ticketId ->
                        navController.navigate("ticket_detail/$ticketId")
                    }
                )
            }
        }

        // --- Detail screens that navigate on top of the tabs ---
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

