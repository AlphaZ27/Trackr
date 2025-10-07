package com.example.trackr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.trackr.feature_settings.domain.repository.SettingsRepository
import com.example.trackr.navigation.AppNavigation
import com.example.trackr.ui.HomeScreen
import com.example.trackr.ui.theme.TrackrTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// For the darkmode toggle in the settings screen: collect the theme mode Flow as a Compose State
// 'system' is the initial default value while it loads.
// Based on the toggle, determine if dark mode should be enabled.
// Pass the result to the app's theme.

/**
 * MainActivity is the entry point of the app.
 * It sets up the UI and the navigation.
 */

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState(initial = "system")

            val useDarkTheme = when (themeMode) {
                    "light" -> false
                    "dark" -> true
                    else -> isSystemInDarkTheme()
                }

            TrackrTheme(
                darkTheme = useDarkTheme
            ) {
                AppNavigation()
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TrackrTheme {
        AppNavigation()
    }
}