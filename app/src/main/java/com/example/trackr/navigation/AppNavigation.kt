package com.example.trackr.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.UserRole
import com.example.trackr.domain.repository.AuthRepository
import com.example.trackr.feature_admin.AdminDashboardScreen
import com.example.trackr.feature_admin.ui.AdminDashboardViewModel
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
import com.example.trackr.feature_manager.ManagerDashboardViewModel
import com.example.trackr.feature_settings.SLAConfigScreen
import com.example.trackr.feature_settings.ui.SettingsScreen
import com.example.trackr.feature_settings.domain.model.UserType
import com.example.trackr.feature_tickets.TicketViewModel
import com.example.trackr.feature_tickets.TicketsScreen
import com.example.trackr.feature_user.UserDashboardScreen
import com.example.trackr.ui.HomeScreen
import com.example.trackr.util.ReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

// The Share Report function is now defined here because I want it to be a FAB
private fun shareReport(context: Context, uri: Uri, subject: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Report"))
}

private fun NavGraphBuilder.mainGraph(navController: NavController) {
    // MainGraph uses HomeScreen as a shell for the tabs.
    navigation(startDestination = "tickets_route", route = "main_app") {

        composable("tickets_route") {
            // Hoist the ViewModel and state here to control the FAB
            val ticketViewModel: TicketViewModel = hiltViewModel()
            val tickets by ticketViewModel.filteredTickets.collectAsState()
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val reportGenerator = remember { ReportGenerator() }

            HomeScreen(
                navController = navController,
                onLogout = { navController.navigate("auth") { popUpTo("main_app") { inclusive = true } } },
                floatingActionButton = {
                    TicketScreenFABs(
                        tickets = tickets,
                        reportGenerator = reportGenerator,
                        context = context,
                        onNavigateToCreate = { navController.navigate("create_ticket") }
                    )
                }
            ) { modifier ->
                TicketsScreen(
                    ticketViewModel = ticketViewModel,
                    modifier = modifier,
                    onTicketClick = { ticketId -> navController.navigate("ticket_detail/$ticketId") }
                )
            }
        }

        composable("kb_route") {
            HomeScreen(
                navController = navController,
                onLogout = { navController.navigate("auth") { popUpTo("main_app") { inclusive = true } } },
                floatingActionButton = {
                    FloatingActionButton(onClick = { navController.navigate("kb_edit") }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Article")
                    }
                }
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
                onLogout = { navController.navigate("auth") { popUpTo("main_app") { inclusive = true } } },
                floatingActionButton = {} // No FAB for settings
            ) { modifier ->
                SettingsScreen(modifier = modifier, navController = navController)
            }
        }

        // --- Dashboard Screens ---
        composable("admin_dashboard") {
            val adminViewModel: AdminDashboardViewModel = hiltViewModel()
            val users by adminViewModel.filteredUsers.collectAsState()
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val reportGenerator = remember { ReportGenerator() }

            HomeScreen(
                navController = navController,
                onLogout = { /* ... */ },
                floatingActionButton = {
                    // FAB for Admin Screen
                    FloatingActionButton(
                        onClick = {
                            if (users.isNotEmpty()) {
                                scope.launch {
                                    val uri = withContext(Dispatchers.IO) {
                                        reportGenerator.generateUserReport(context, users)
                                    }
                                    if (uri != null) {
                                        shareReport(context, uri, "Admin User Report")
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Report")
                    }
                }
            ) { modifier ->
                AdminDashboardScreen(
                    viewModel = adminViewModel,
                    navController = navController,
                    modifier = modifier
                )
            }
        }
        /* *
        *
        * Manager Dashboard Composable
        *
         */
        composable("manager_dashboard") {
            val managerViewModel: ManagerDashboardViewModel = hiltViewModel()
            val users by managerViewModel.userActivityReport.collectAsState()
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val reportGenerator = remember { ReportGenerator() }

            HomeScreen(
                navController = navController,
                onLogout = { /* ... */ },
                floatingActionButton = {
                    // FAB for Manager Screen
                    FloatingActionButton(
                        onClick = {
                            if (users.isNotEmpty()) {
                                scope.launch {
                                    val uri = withContext(Dispatchers.IO) {
                                        reportGenerator.generateUserReport(context, users.map { it.user })
                                    }
                                    if (uri != null) {
                                        shareReport(context, uri, "Manager User Report")
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Report")
                    }
                }
            ) { modifier ->
                ManagerDashboardScreen(
                    viewModel = managerViewModel,
                    navController = navController,
                    modifier = modifier
                )
            }
        }

        /*
        *
        * User Dashboard Composable
        *
         */

        composable("user_dashboard") {
            // "User" dashboard is the same as the "Tickets" route
            val ticketViewModel: TicketViewModel = hiltViewModel()
            val tickets by ticketViewModel.filteredTickets.collectAsState()
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val reportGenerator = remember { ReportGenerator() }

            HomeScreen(
                navController = navController,
                onLogout = { /* ... */ },
                floatingActionButton = {
                    TicketScreenFABs(
                        tickets = tickets,
                        reportGenerator = reportGenerator,
                        context = context,
                        onNavigateToCreate = { navController.navigate("create_ticket") }
                    )
                }
            ) { modifier ->
                TicketsScreen(
                    ticketViewModel = ticketViewModel,
                    modifier = modifier,
                    onTicketClick = { ticketId ->
                        navController.navigate("ticket_detail/$ticketId")
                    }
                )
            }
        }

        composable("sla_config") {
            SLAConfigScreen(
                onNavigateBack = { navController.popBackStack() }
            )
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
                onNavigateToEdit = { ticketId ->
                    navController.navigate("update_ticket/$ticketId")
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToArticle = { articleId ->
                    navController.navigate("kb_detail/$articleId")
                },
                onSubmitCsat = { score ->
                    //ticketViewModel.submitCsatScore(ticketId, score)
                }
            )

        }
        composable(
            route = "update_Ticket/{ticketId}",
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

// A new helper composable to hold the two FABs for the ticket screen
@Composable
private fun TicketScreenFABs(
    tickets: List<Ticket>,
    reportGenerator: ReportGenerator,
    context: Context,
    onNavigateToCreate: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Share Report FAB (Small)
        FloatingActionButton(
            onClick = {
                scope.launch {
                    val uri = withContext(Dispatchers.IO) {
                        reportGenerator.generateTicketReport(context, tickets)
                    }
                    if (uri != null) {
                        shareReport(context, uri, "Ticket Report")
                    }
                }
            },
            // Use secondary colors to make it less prominent than the "Add" button
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Icon(Icons.Default.Share, contentDescription = "Share Report")
        }

        // Create Ticket FAB (Primary)
        FloatingActionButton(
            onClick = onNavigateToCreate
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Ticket")
        }
    }
}