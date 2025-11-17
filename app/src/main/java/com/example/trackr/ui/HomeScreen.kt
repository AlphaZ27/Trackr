package com.example.trackr.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.trackr.feature_auth.AuthViewModel
import com.example.trackr.feature_kb.KBListScreen
import com.example.trackr.feature_tickets.TicketsScreen
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

// This was previously MainScreen inside AppNavigation.kt
// It's good practice to move large composables to their own files.
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")  //Needed for paddingValues
@Composable
fun HomeScreen(
    navController: NavController,
    onLogout: () -> Unit, // This is now unused by the TopAppBar
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val navItems = listOf(
        BottomNavItem("Tickets", Icons.Default.Home, "tickets_route"),
        BottomNavItem("Knowledge Base", Icons.Default.Info, "kb_route"),
        BottomNavItem("Settings", Icons.Default.Settings, "settings_route")
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val selectedItemIndex = remember(currentDestination) {
        navItems.indexOfFirst { it.route == currentDestination?.route }.coerceAtLeast(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trackr") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logoutUser()
                        // This logic correctly clears the back stack
                        // and, crucially, prevents it from saving its state.
                        navController.navigate("auth") {
                            // Pop the entire graph from the root
                            popUpTo(navController.graph.id) {
                                inclusive = true
                                saveState = false
                            }
                            // Do not save the state of the graph we are leaving
                            restoreState = false
                            // Navigate to the login screen
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
//        floatingActionButton = {
//            when (selectedItemIndex) {
//                0 -> { // Tickets Screen
//                    FloatingActionButton(onClick = { navController.navigate("create_ticket") }) {
//                        Icon(Icons.Default.Add, contentDescription = "Create Ticket")
//                    }
//                }
//                1 -> { // KB Screen
//                    FloatingActionButton(onClick = { navController.navigate("kb_edit") }) {
//                        Icon(Icons.Default.Add, contentDescription = "Create Article")
//                    }
//                }
//            }
//        },
        floatingActionButton = floatingActionButton,
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = item.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        // The content from AppNavigation is rendered here with the correct padding.
        content(Modifier.padding(paddingValues))
    }
}

private data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)