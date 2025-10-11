package com.example.trackr.feature_admin


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.trackr.feature_admin.domain.TrackrUser
import com.example.trackr.feature_settings.domain.model.UserType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {

    val users by viewModel.users.collectAsState()
    var userToEdit by remember { mutableStateOf<TrackrUser?>(null) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("tickets_route") {
                            popUpTo("admin_dashboard") { inclusive = true }
                        }
                    })
                    {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to App")
                    }
                },
            )
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            item {
                Text(
                    text = "User Management",
                    style = MaterialTheme.typography.headlineMedium
                    // Add padding
                )
            }
            items(users) { user ->
                UserListItem(
                    user = user,
                    onClick = { userToEdit = user } // Set the user to edit on click
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
    // If a user is selected for editing, show the dialog
    userToEdit?.let { user ->
        RoleSelectionDialog(
            user = user,
            onDismiss = { userToEdit = null },
            onRoleSelected = { newRole ->
                viewModel.changeUserRole(user.uid, newRole.name)
                userToEdit = null // Close the dialog
            }
        )
    }
}

@Composable
fun UserListItem(
    user: TrackrUser,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth() // Make the item span the full width
        .clickable(onClick = onClick) // handle taps
        .padding(16.dp)
    ) {

        // A check to handle cases where the user's name might be missing in Firestore
        val displayName = if (user.name.isNullOrBlank()) "No Name" else user.name

        Text(text = user.name, style = MaterialTheme.typography.titleMedium)
        Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
        Text(text = "Role: ${user.role}", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun RoleSelectionDialog(
    user: TrackrUser,
    onDismiss: () -> Unit,
    onRoleSelected: (UserType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Role for ${user.name}") },
        text = {
            Column {
                Text("Current Role: ${user.role}")
                // Add radio buttons or a dropdown for role selection
                UserType.values().forEach { role ->
                    Button(
                        onClick = { onRoleSelected(role) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(role.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}