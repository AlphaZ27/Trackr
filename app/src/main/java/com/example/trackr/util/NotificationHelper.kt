package com.example.trackr.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.trackr.MainActivity
import com.example.trackr.R // Ensure you have your R file imported
import com.example.trackr.domain.model.Priority
import com.example.trackr.domain.model.SLAStatus
import com.example.trackr.domain.model.Ticket
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID_SLA = "sla_alerts_channel"
        const val CHANNEL_ID_GENERAL = "general_updates_channel"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // SLA Channel (High Importance)
            val slaChannel = NotificationChannel(
                CHANNEL_ID_SLA,
                "SLA Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for SLA Warnings and Breaches"
                enableVibration(true)
            }

            // General Channel (Default Importance)
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "General Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ticket assignments and updates"
            }

            manager.createNotificationChannel(slaChannel)
            manager.createNotificationChannel(generalChannel)
        }
    }

    fun showSlaNotification(ticket: Ticket, status: SLAStatus) {
        // Don't notify for OK status
        if (status == SLAStatus.OK) return

        val title = when (status) {
            SLAStatus.Warning -> "⚠️ SLA Warning: ${ticket.name}"
            SLAStatus.Breached -> "🚨 SLA BREACHED: ${ticket.name}"
            else -> "SLA Update"
        }

        val message = "Ticket #${ticket.id.take(6)} is ${status.name}. Priority: ${ticket.priority.name}"

        showNotification(CHANNEL_ID_SLA, ticket.id.hashCode(), title, message)
    }

    fun showGeneralNotification(title: String, message: String) {
        showNotification(CHANNEL_ID_GENERAL, System.currentTimeMillis().toInt(), title, message)
    }

    private fun showNotification(channelId: String, notificationId: Int, title: String, message: String) {
        // Intent to open the app when clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Use a standard icon (e.g., ic_launcher_foreground) if you don't have a specific notification icon
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with R.drawable.ic_notification if added
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            // Check permission logic should technically be handled before calling this,
            // but SecurityException handling is good practice.
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}