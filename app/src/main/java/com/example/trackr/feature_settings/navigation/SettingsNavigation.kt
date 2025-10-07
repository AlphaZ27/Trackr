package com.example.trackr.feature_settings.navigation

import androidx.navigation.*
import androidx.navigation.compose.composable
import com.example.trackr.feature_settings.ui.SettingsScreen

const val settingsRoute = "settings_route"

fun NavController.navigateToSettings(navOptions: NavOptions? = null) {
    this.navigate(settingsRoute, navOptions)
}

fun NavGraphBuilder.settingsScreen() {
    composable(route = settingsRoute) {
        SettingsScreen()
    }
}