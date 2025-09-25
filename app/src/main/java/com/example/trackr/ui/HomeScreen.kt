package com.example.trackr.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trackr.feature_auth.AuthViewModel
import com.example.trackr.feature_tickets.TicketsScreen

// This was previously MainScreen inside AppNavigation.kt
// It's good practice to move large composables to their own files.
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    onNavigateToCreateTicket: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToTicketDetail: (String) -> Unit
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    var selectedItemIndex by remember { mutableIntStateOf(0) }
    val navItems = listOf(
        BottomNavItem("Tickets", Icons.Default.Home),
        BottomNavItem("Knowledge Base", Icons.Default.Info),
        BottomNavItem("Settings", Icons.Default.Settings)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trackr") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logoutUser()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedItemIndex == 0) { // Only show FAB on tickets screen
                FloatingActionButton(onClick = onNavigateToCreateTicket) {
                    Icon(Icons.Default.Add, contentDescription = "Create Ticket")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = { selectedItemIndex = index },
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = item.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedItemIndex) {
                0 -> TicketsScreen(
                    onTicketClick = { ticketId ->
                        onNavigateToTicketDetail(ticketId)
                    }
                )
                1 -> Text("Knowledge Base Screen")
                2 -> Text("Settings Screen")
            }
        }
    }
}

private data class BottomNavItem(val label: String, val icon: ImageVector)