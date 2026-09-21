package com.example.engine

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.UserSettings
import com.example.data.local.UserSettingsRepository
import com.example.data.local.entity.KeywordEntity
import com.example.data.local.entity.RuleAppEntity
import com.example.data.local.entity.TriggerLogEntity
import com.example.data.model.ScreenContext
import com.example.data.model.TriggerEvent
import com.example.data.model.TriggerSeverity
import com.example.data.model.TriggerType
import com.example.data.remote.GroqClient
import com.example.service.NotificationHelper
import com.example.service.OverlayHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

class TriggerEngine(
    private val database: AppDatabase,
    private val settingsRepository: UserSettingsRepository,
    private val groqClient: GroqClient,
    private val notificationHelper: NotificationHelper,
    private val overlayHelper: OverlayHelper
) {
    // Cooldown tracking: key -> last fired timestamp (millis)
    private val lastFiredTimestamps = ConcurrentHashMap<String, Long>()
    private var listeningJob: Job? = null

    fun startListening(scope: CoroutineScope) {
        if (listeningJob?.isActive == true) return

        listeningJob = scope.launch(Dispatchers.Default) {
            EventBus.screenContextFlow.collectLatest { screenContext ->
                processScreenContext(screenContext)
            }
        }
    }

    fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
    }

    suspend fun processScreenContext(screenContext: ScreenContext) {
        val settings = settingsRepository.settings.value
        if (!settings.isMonitoringEnabled) {
            return
        }

        // Don't monitor our own app package
        if (screenContext.packageName.contains("com.aistudio.salim") ||
            screenContext.packageName.contains("com.example")
        ) {
            return
        }

        val enabledApps = database.ruleDao().getEnabledAppsSync()
        val enabledKeywords = database.keywordDao().getEnabledKeywordsSync()

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentTimeMillis = System.currentTimeMillis()

        val event = evaluateRules(
            context = screenContext,
            enabledApps = enabledApps,
            enabledKeywords = enabledKeywords,
            settings = settings,
            currentHour = currentHour,
            currentTimeMillis = currentTimeMillis,
            cooldownChecker = { key, cooldownMs ->
                isCoolingDown(key, currentTimeMillis, cooldownMs)
            }
        )

        if (event != null) {
            handleFiredTrigger(event, settings)
        }
    }

    private fun isCoolingDown(key: String, now: Long, cooldownDurationMs: Long): Boolean {
        val lastFired = lastFiredTimestamps[key] ?: return false
        return (now - lastFired) < cooldownDurationMs
    }

    fun recordFiredTimestamp(key: String, timestamp: Long) {
        lastFiredTimestamps[key] = timestamp
    }

    private suspend fun handleFiredTrigger(event: TriggerEvent, settings: UserSettings) {
        // Record cooldown timestamp
        val cooldownKey = when (event.type) {
            TriggerType.KEYWORD_MATCH -> "KEYWORD_${event.reasonDescription}"
            TriggerType.LATE_NIGHT_USAGE -> "LATE_NIGHT_${event.context.packageName}"
            TriggerType.SESSION_THRESHOLD -> "SESSION_${event.context.packageName}"
            TriggerType.BLOCKLIST_APP -> "BLOCKLIST_${event.context.packageName}"
            TriggerType.TEST_ALERT -> "TEST_ALERT"
        }
        recordFiredTimestamp(cooldownKey, event.timestamp)

        // Broadcast to EventBus
        EventBus.emitTriggerEvent(event)

        // Resolve app label
        val matchedApp = database.ruleDao().getApp(event.context.packageName)
        val appLabel = matchedApp?.appName ?: event.context.packageName.substringAfterLast('.')

        // Call Groq API (or local fallback)
        val message = groqClient.generateInterventionMessage(
            persona = settings.personaType,
            customPersonaPrompt = settings.customPersonaPrompt,
            event = event,
            appLabel = appLabel,
            model = settings.groqModel
        )

        // Save in Room trigger_logs
        val logEntity = TriggerLogEntity(
            triggerType = event.type.name,
            severity = event.severity.name,
            appPackageName = event.context.packageName,
            appLabel = appLabel,
            category = matchedApp?.category ?: "general",
            firedAt = event.timestamp,
            notificationMessage = message,
            personaUsed = settings.personaType.displayName
        )
        database.triggerLogDao().insertLog(logEntity)

        // Deliver Notification
        notificationHelper.sendAlertNotification(
            title = "salim: $appLabel",
            message = message,
            severity = event.severity
        )

        // Optional high-severity overlay toast
        if (settings.isOverlayEnabled && event.severity == TriggerSeverity.HIGH) {
            overlayHelper.showOverlayAlert(message)
        }

        Log.i(TAG, "Trigger fired: ${event.type} -> $message")
    }

    /**
     * Diagnostic test trigger execution for user in settings
     */
    suspend fun fireTestTrigger() {
        val settings = settingsRepository.settings.value
        val testContext = ScreenContext(
            packageName = "com.sample.distraction",
            windowTitle = "Social Feed",
            sessionDurationSeconds = 1800L
        )
        val testEvent = TriggerEvent(
            type = TriggerType.TEST_ALERT,
            severity = TriggerSeverity.MEDIUM,
            context = testContext,
            reasonDescription = "Diagnostic test reflection"
        )
        handleFiredTrigger(testEvent, settings)
    }

    companion object {
        private const val TAG = "TriggerEngine"

        /**
         * Pure rule evaluation function — easily testable without mocks.
         */
        fun evaluateRules(
            context: ScreenContext,
            enabledApps: List<RuleAppEntity>,
            enabledKeywords: List<KeywordEntity>,
            settings: UserSettings,
            currentHour: Int,
            currentTimeMillis: Long,
            cooldownChecker: (String, Long) -> Boolean
        ): TriggerEvent? {
            val cooldownMs = settings.cooldownMinutes * 60 * 1000L
            val matchedApp = enabledApps.find { it.packageName == context.packageName }

            // 1. Check Keywords (Highest priority if present on screen)
            if (context.visibleTextSnippet.isNotBlank() || context.windowTitle.isNotBlank()) {
                val combinedText = "${context.windowTitle} ${context.visibleTextSnippet}".lowercase()
                for (kw in enabledKeywords) {
                    if (combinedText.contains(kw.keyword.lowercase())) {
                        val cooldownKey = "KEYWORD_${kw.keyword.lowercase()}"
                        if (!cooldownChecker(cooldownKey, cooldownMs)) {
                            val severity = try {
                                TriggerSeverity.valueOf(kw.severity)
                            } catch (_: Exception) {
                                TriggerSeverity.HIGH
                            }
                            return TriggerEvent(
                                type = TriggerType.KEYWORD_MATCH,
                                severity = severity,
                                context = context,
                                reasonDescription = "Keyword '${kw.keyword}' in category ${kw.category}",
                                timestamp = currentTimeMillis
                            )
                        }
                    }
                }
            }

            // 2. Late Night Usage Rule (On any flagged app or during late hours)
            val isLateNight = isHourInWindow(currentHour, settings.lateNightStartHour, settings.lateNightEndHour)
            if (isLateNight && matchedApp != null) {
                val cooldownKey = "LATE_NIGHT_${context.packageName}"
                if (!cooldownChecker(cooldownKey, cooldownMs)) {
                    return TriggerEvent(
                        type = TriggerType.LATE_NIGHT_USAGE,
                        severity = TriggerSeverity.HIGH,
                        context = context,
                        reasonDescription = "Late night usage of ${matchedApp.appName} (${matchedApp.category})",
                        timestamp = currentTimeMillis
                    )
                }
            }

            // 3. Continuous Session Duration Threshold
            val sessionThresholdSeconds = settings.sessionThresholdMinutes * 60L
            if (context.sessionDurationSeconds >= sessionThresholdSeconds && sessionThresholdSeconds > 0) {
                val cooldownKey = "SESSION_${context.packageName}"
                if (!cooldownChecker(cooldownKey, cooldownMs)) {
                    val appName = matchedApp?.appName ?: context.packageName
                    val category = matchedApp?.category ?: "general"
                    return TriggerEvent(
                        type = TriggerType.SESSION_THRESHOLD,
                        severity = TriggerSeverity.MEDIUM,
                        context = context,
                        reasonDescription = "Continuous session over ${settings.sessionThresholdMinutes}m on $appName ($category)",
                        timestamp = currentTimeMillis
                    )
                }
            }

            // 4. Blocklist App Entry Trigger
            if (matchedApp != null) {
                val cooldownKey = "BLOCKLIST_${context.packageName}"
                if (!cooldownChecker(cooldownKey, cooldownMs)) {
                    return TriggerEvent(
                        type = TriggerType.BLOCKLIST_APP,
                        severity = TriggerSeverity.LOW,
                        context = context,
                        reasonDescription = "${matchedApp.appName} (${matchedApp.category})",
                        timestamp = currentTimeMillis
                    )
                }
            }

            return null
        }

        fun isHourInWindow(currentHour: Int, startHour: Int, endHour: Int): Boolean {
            return if (startHour > endHour) {
                // e.g. 23 (11pm) to 5 (5am)
                currentHour >= startHour || currentHour < endHour
            } else {
                // e.g. 1 to 4
                currentHour in startHour until endHour
            }
        }
    }
}
