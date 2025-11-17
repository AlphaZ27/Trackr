package com.example.trackr.feature_settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

        // 2. Control the ThemeToggleItem with the state from the ViewModel.
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