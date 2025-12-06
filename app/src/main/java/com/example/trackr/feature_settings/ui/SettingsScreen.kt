package com.example.trackr.feature_settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.trackr.feature_settings.domain.model.UserType
import com.example.trackr.feature_settings.ui.viewmodel.SettingsViewModel
import com.example.trackr.feature_settings.ui.components.SectionHeader
import com.example.trackr.feature_settings.ui.components.ThemeToggleItem


@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // 1. Collect the themeMode as a state. The UI will automatically
    //    recompose whenever this value changes.
    val themeMode by viewModel.themeMode.collectAsState()

    // Collect the userType as a state.
    val userType by viewModel.userType.collectAsState()


    Column(modifier = modifier.fillMaxSize()) {
        SectionHeader(title = "Appearance")

        // Control the ThemeToggleItem with the state from the ViewModel.
        ThemeToggleItem(
            title = "Dark Mode",
            // The toggle is on if the themeMode is "dark".
            isToggled = themeMode == "dark",
            // When the user clicks the toggle, call the ViewModel's update function.
            onToggle = { isChecked ->
                val newMode = if (isChecked) "dark" else "light"
                viewModel.updateTheme(newMode)
            }
        )
        Spacer(Modifier.height(24.dp))

        if (userType == UserType.Admin || userType == UserType.Manager) {
            SectionHeader(title = "Admin Settings")

            SettingsNavItem(
                title = "SLA Configuration",
                onClick = { navController.navigate("sla_config") }
            )
            SettingsNavItem(
                title = "Ticket Categories",
                onClick = { navController.navigate("category_config") }
            )
        }

        Spacer(Modifier.height(24.dp))

        AccountSection(
            userType = userType,
            onDashboardClick = { type ->
                // Navigate to the correct dashboard based on user type
                when (type) {
                    UserType.Admin -> navController.navigate("admin_dashboard")
                    UserType.Manager -> navController.navigate("manager_dashboard")
                    UserType.User -> navController.navigate("user_dashboard")
                }
            },
            onLogoutClick = {
                viewModel.logout()
                // After logout, navigate back to the auth graph
                navController.navigate("auth") {
                    popUpTo("main_app") { inclusive = true }
                }
            }
        )
    }
}

// Simple composable for a clickable settings row
@Composable
fun SettingsNavItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.height(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}