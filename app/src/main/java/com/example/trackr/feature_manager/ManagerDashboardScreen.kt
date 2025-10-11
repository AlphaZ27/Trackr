package com.example.trackr.feature_manager


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDashboardScreen(
    navController: NavController,
    viewModel: ManagerViewModel = hiltViewModel()
) {

    val teamTickets by viewModel.teamTickets.collectAsState()
    val managerName by viewModel.managerName.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manager Dashboard") },
                navigationIcon = {
                    IconButton(onClick = {
                        // The logic is the same, just the popUpTo route changes
                        navController.navigate("tickets_route") {
                            popUpTo("manager_dashboard") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to App")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            item {
                Text(
                    text = "Welcome, $managerName!",
                    style = MaterialTheme.typography.headlineMedium
                    // Add padding
                )
            }
            item {
                Text(
                    text = "Team Tickets",
                    style = MaterialTheme.typography.titleLarge
                    // Add padding
                )
            }
            items(teamTickets) { ticket ->
                // Here you would use a composable to display ticket info,
                // similar to your main TicketsScreen.
                Text("Ticket: ${ticket.name} - Status: ${ticket.status}")
                // Add an onClick to navigate to ticket details
            }
        }
    }
}