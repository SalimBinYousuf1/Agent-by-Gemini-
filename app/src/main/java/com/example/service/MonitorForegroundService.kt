package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.SalimApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class MonitorForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "MonitorForegroundService created")

        val app = application as SalimApplication
        val notificationHelper = app.notificationHelper

        val notification = notificationHelper.getForegroundServiceNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationHelper.SERVICE_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NotificationHelper.SERVICE_NOTIFICATION_ID, notification)
        }

        // Start listening in TriggerEngine
        app.triggerEngine.startListening(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        val app = application as SalimApplication
        app.triggerEngine.stopListening()
        Log.i(TAG, "MonitorForegroundService destroyed")
    }

    companion object {
        private const val TAG = "ForegroundService"
        const val ACTION_START = "com.example.service.START"
        const val ACTION_STOP = "com.example.service.STOP"

        fun start(context: Context) {
            val intent = Intent(context, MonitorForegroundService::class.java).apply {
                action = ACTION_START
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to start foreground service: ${e.message}")
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, MonitorForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
