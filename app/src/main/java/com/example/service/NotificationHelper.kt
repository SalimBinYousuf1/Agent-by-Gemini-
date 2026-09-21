package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.TriggerSeverity

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        // 1. Accountability Alerts Channel
        val alertsChannel = NotificationChannel(
            CHANNEL_ALERTS,
            context.getString(R.string.notification_channel_alerts_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_alerts_desc)
            enableVibration(true)
            setShowBadge(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(alertsChannel)

        // 2. Foreground Monitoring Service Channel (Low priority, non-intrusive)
        val serviceChannel = NotificationChannel(
            CHANNEL_SERVICE,
            context.getString(R.string.notification_channel_service_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_service_desc)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(serviceChannel)
    }

    fun sendAlertNotification(
        title: String,
        message: String,
        severity: TriggerSeverity
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        // Tapping notification opens Salim dashboard, NEVER the flagged app
        val dashboardIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            dashboardIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priority = when (severity) {
            TriggerSeverity.HIGH -> NotificationCompat.PRIORITY_MAX
            TriggerSeverity.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            TriggerSeverity.LOW -> NotificationCompat.PRIORITY_LOW
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        notificationManager.notify(notificationId, notification)
    }

    fun getForegroundServiceNotification(): Notification {
        val dashboardIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            dashboardIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.service_running_title))
            .setContentText(context.getString(R.string.service_running_content))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val CHANNEL_ALERTS = "mindful_guard_alerts"
        const val CHANNEL_SERVICE = "salim_monitoring_service"
        const val SERVICE_NOTIFICATION_ID = 1001
    }
}
