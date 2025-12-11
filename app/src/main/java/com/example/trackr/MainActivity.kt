package com.example.trackr

import android.animation.ObjectAnimator
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.trackr.domain.repository.DataStoreRepository
import com.example.trackr.navigation.AppNavigation
import com.example.trackr.ui.theme.TrackrTheme
import com.example.trackr.workers.SLAWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// For the darkmode toggle in the settings screen: collect the theme mode Flow as a Compose State
// 'system' is the initial default value while it loads.
// Based on the toggle, determine if dark mode should be enabled.
// Pass the result to the app's theme.

/**
 * MainActivity is the entry point of the app.
 * It sets up the UI and the navigation.
 */

private val Context.dataStore by preferencesDataStore(name = "settings")

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    //private val splashScreen = installSplashScreen()


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val iconView = splashScreenViewProvider.iconView

            // Scale down animation
            val scaleX = ObjectAnimator.ofFloat(iconView, View.SCALE_X, 1f, 0f)
            val scaleY = ObjectAnimator.ofFloat(iconView, View.SCALE_Y, 1f, 0f)
            val fade = ObjectAnimator.ofFloat(iconView, View.ALPHA, 1f, 0f)

            scaleX.interpolator = OvershootInterpolator()
            scaleX.duration = 500L
            scaleY.interpolator = OvershootInterpolator()
            scaleY.duration = 500L
            fade.duration = 300L

            scaleX.start()
            scaleY.start()
            fade.start()

            // Remove splash screen when animation ends
            scaleX.doOnEnd {
                splashScreenViewProvider.remove()
            }
        }
        setContent {

            val themeMode by dataStoreRepository.getTheme().collectAsState(initial = "light")

            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            val slaRequest = PeriodicWorkRequestBuilder<SLAWorker>(15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "SLACheck",
                ExistingPeriodicWorkPolicy.KEEP, // Don't replace if already running
                slaRequest
            )

            TrackrTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
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