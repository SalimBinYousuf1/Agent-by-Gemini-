package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.SecureKeyStorage
import com.example.data.local.UserSettingsRepository
import com.example.data.remote.GroqClient
import com.example.engine.TriggerEngine
import com.example.service.MonitorForegroundService
import com.example.service.NotificationHelper
import com.example.service.OverlayHelper

class SalimApplication : Application() {

    lateinit var database: AppDatabase private set
    lateinit var settingsRepository: UserSettingsRepository private set
    lateinit var secureKeyStorage: SecureKeyStorage private set
    lateinit var groqClient: GroqClient private set
    lateinit var notificationHelper: NotificationHelper private set
    lateinit var overlayHelper: OverlayHelper private set
    lateinit var triggerEngine: TriggerEngine private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        settingsRepository = UserSettingsRepository(this)
        secureKeyStorage = SecureKeyStorage(this)
        groqClient = GroqClient(secureKeyStorage)
        notificationHelper = NotificationHelper(this)
        overlayHelper = OverlayHelper(this)

        triggerEngine = TriggerEngine(
            database = database,
            settingsRepository = settingsRepository,
            groqClient = groqClient,
            notificationHelper = notificationHelper,
            overlayHelper = overlayHelper
        )
    }

    companion object {
        lateinit var instance: SalimApplication private set
    }
}
