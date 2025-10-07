package com.example.trackr.feature_settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trackr.feature_settings.ui.viewmodel.SettingsViewModel
import com.example.trackr.feature_settings.ui.components.SectionHeader
import com.example.trackr.feature_settings.ui.components.ThemeToggleItem
import com.example.trackr.ui.theme.TrackrTheme


@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    // 1. Collect the themeMode as a state. The UI will automatically
    //    recompose whenever this value changes.
    val themeMode by viewModel.themeMode.collectAsState()

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
        // You can add more settings items here
    }
}