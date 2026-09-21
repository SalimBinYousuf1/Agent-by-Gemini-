package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.UserSettingsRepository
import com.example.service.MonitorForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.i(TAG, "Device reboot detected. Checking if monitoring was enabled.")
            val settings = UserSettingsRepository(context).settings.value
            if (settings.isMonitoringEnabled) {
                MonitorForegroundService.start(context)
            }
        }
    }

    companion object {
        private const val TAG = "SalimBootReceiver"
    }
}
