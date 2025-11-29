package com.example.trackr.service

import android.util.Log
import com.example.trackr.domain.repository.AuthRepository
import com.example.trackr.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TrackrFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // Save token to Firestore so we can target this user later
        CoroutineScope(Dispatchers.IO).launch {
            authRepository.updateFcmToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle data payload (if any)
        remoteMessage.data.isNotEmpty().let {
            Log.d("FCM", "Message data payload: " + remoteMessage.data)
        }

        // Handle notification payload
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            notificationHelper.showGeneralNotification(
                title = it.title ?: "New Notification",
                message = it.body ?: "Check the app for updates"
            )
        }
    }
}