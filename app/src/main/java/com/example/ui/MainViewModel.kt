package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.SecureKeyStorage
import com.example.data.local.ThemeMode
import com.example.data.local.UserSettings
import com.example.data.local.UserSettingsRepository
import com.example.data.local.dao.CategoryStat
import com.example.data.local.entity.KeywordEntity
import com.example.data.local.entity.RuleAppEntity
import com.example.data.local.entity.TriggerLogEntity
import com.example.data.model.PersonaType
import com.example.data.model.ScreenContext
import com.example.data.model.TriggerSeverity
import com.example.engine.EventBus
import com.example.engine.InAppAlertNotification
import com.example.service.MonitorForegroundService
import com.example.util.PermissionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class PermissionStatus(
    val isAccessibilityGranted: Boolean = false,
    val isUsageAccessGranted: Boolean = false,
    val isNotificationsGranted: Boolean = false,
    val isOverlayGranted: Boolean = false
) {
    val allCoreGranted: Boolean
        get() = isAccessibilityGranted && isUsageAccessGranted && isNotificationsGranted
}

class MainViewModel(
    private val app: SalimApplication
) : ViewModel() {

    private val db = app.database
    private val settingsRepo: UserSettingsRepository = app.settingsRepository
    private val keyStorage: SecureKeyStorage = app.secureKeyStorage

    val settings: StateFlow<UserSettings> = settingsRepo.settings

    val currentScreenContext: StateFlow<ScreenContext?> = EventBus.currentScreenContext

    val monitoredApps: StateFlow<List<RuleAppEntity>> = db.ruleDao().getAllApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val keywords: StateFlow<List<KeywordEntity>> = db.keywordDao().getAllKeywords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<TriggerLogEntity>> = db.triggerLogDao().getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val startOfToday: Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val startOfWeek: Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -7)
    }.timeInMillis

    val todayCount: StateFlow<Int> = db.triggerLogDao().getCountSince(startOfToday)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weekCount: StateFlow<Int> = db.triggerLogDao().getCountSince(startOfWeek)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val categoryStats: StateFlow<List<CategoryStat>> = db.triggerLogDao().getStatsByCategorySince(startOfWeek)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _groqApiKey = MutableStateFlow<String?>(keyStorage.getGroqApiKey())
    val groqApiKey: StateFlow<String?> = _groqApiKey.asStateFlow()

    private val _permissions = MutableStateFlow(PermissionStatus())
    val permissions: StateFlow<PermissionStatus> = _permissions.asStateFlow()

    private val _isTestFiring = MutableStateFlow(false)
    val isTestFiring: StateFlow<Boolean> = _isTestFiring.asStateFlow()

    fun refreshPermissions(context: Context) {
        _permissions.value = PermissionStatus(
            isAccessibilityGranted = PermissionUtils.isAccessibilityEnabled(context),
            isUsageAccessGranted = PermissionUtils.isUsageAccessGranted(context),
            isNotificationsGranted = PermissionUtils.isNotificationsGranted(context),
            isOverlayGranted = PermissionUtils.isOverlayGranted(context)
        )
    }

    fun toggleMonitoring(context: Context, enabled: Boolean) {
        settingsRepo.setMonitoringEnabled(enabled)
        if (enabled) {
            MonitorForegroundService.start(context)
        } else {
            MonitorForegroundService.stop(context)
        }
    }

    fun setPersona(personaType: PersonaType) {
        settingsRepo.setPersonaType(personaType)
    }

    fun setCustomPersonaPrompt(prompt: String) {
        settingsRepo.setCustomPersonaPrompt(prompt)
    }

    fun setSessionThreshold(minutes: Int) {
        settingsRepo.setSessionThreshold(minutes)
    }

    fun setCooldownMinutes(minutes: Int) {
        settingsRepo.setCooldownMinutes(minutes)
    }

    fun setLateNightWindow(startHour: Int, endHour: Int) {
        settingsRepo.setLateNightWindow(startHour, endHour)
    }

    fun setGroqModel(model: String) {
        settingsRepo.setGroqModel(model)
    }

    fun setThemeMode(mode: ThemeMode) {
        settingsRepo.setThemeMode(mode)
    }

    fun setInAppPopupEnabled(enabled: Boolean) {
        settingsRepo.setInAppPopupEnabled(enabled)
    }

    fun setSoundEnabled(enabled: Boolean) {
        settingsRepo.setSoundEnabled(enabled)
    }

    fun setHapticEnabled(enabled: Boolean) {
        settingsRepo.setHapticEnabled(enabled)
    }

    fun setOverlayEnabled(enabled: Boolean) {
        settingsRepo.setOverlayEnabled(enabled)
    }

    fun saveGroqApiKey(key: String) {
        keyStorage.saveGroqApiKey(key)
        _groqApiKey.value = keyStorage.getGroqApiKey()
    }

    fun deleteGroqApiKey() {
        keyStorage.clearGroqApiKey()
        _groqApiKey.value = null
    }

    fun applyPresetPack(packName: String) {
        viewModelScope.launch {
            when (packName) {
                "digital_detox" -> {
                    listOf(
                        Triple("com.instagram.android", "Instagram", "social"),
                        Triple("com.zhiliaoapp.musically", "TikTok", "social"),
                        Triple("com.google.android.youtube", "YouTube", "entertainment"),
                        Triple("com.reddit.frontpage", "Reddit", "social"),
                        Triple("com.twitter.android", "X (Twitter)", "social"),
                        Triple("tv.twitch.android.app", "Twitch", "entertainment")
                    ).forEach { (pkg, name, cat) ->
                        db.ruleDao().insertApp(RuleAppEntity(packageName = pkg, appName = name, category = cat))
                    }
                    settingsRepo.setSessionThreshold(15)
                }
                "late_night" -> {
                    settingsRepo.setLateNightWindow(23, 5)
                    settingsRepo.setCooldownMinutes(3)
                }
                "harm_reduction" -> {
                    listOf(
                        Triple("casino", "gambling", "HIGH"),
                        Triple("betting", "gambling", "HIGH"),
                        Triple("slots", "gambling", "HIGH"),
                        Triple("poker", "gambling", "HIGH"),
                        Triple("porn", "adult", "HIGH"),
                        Triple("xxx", "adult", "HIGH")
                    ).forEach { (kw, cat, sev) ->
                        db.keywordDao().insertKeyword(KeywordEntity(keyword = kw, category = cat, severity = sev))
                    }
                }
                "focus_work" -> {
                    settingsRepo.setSessionThreshold(10)
                    settingsRepo.setCooldownMinutes(5)
                    listOf(
                        Triple("shorts", "video", "MEDIUM"),
                        Triple("reels", "video", "MEDIUM"),
                        Triple("feed", "social", "MEDIUM")
                    ).forEach { (kw, cat, sev) ->
                        db.keywordDao().insertKeyword(KeywordEntity(keyword = kw, category = cat, severity = sev))
                    }
                }
            }
        }
    }

    fun simulateEvent(
        packageName: String,
        appName: String,
        durationMinutes: Int,
        textSnippet: String,
        severity: TriggerSeverity = TriggerSeverity.MEDIUM
    ) {
        viewModelScope.launch {
            _isTestFiring.value = true
            try {
                val screenContext = ScreenContext(
                    packageName = packageName.trim(),
                    windowTitle = textSnippet.ifBlank { "$appName Feed" },
                    visibleTextSnippet = textSnippet.trim(),
                    sessionDurationSeconds = durationMinutes * 60L
                )
                EventBus.emitScreenContext(screenContext)
                app.triggerEngine.processScreenContext(screenContext)
            } finally {
                _isTestFiring.value = false
            }
        }
    }

    fun addMonitoredApp(packageName: String, appName: String, category: String) {
        viewModelScope.launch {
            db.ruleDao().insertApp(
                RuleAppEntity(
                    packageName = packageName.trim(),
                    appName = appName.trim().ifBlank { packageName },
                    category = category.trim().lowercase().ifBlank { "social" }
                )
            )
        }
    }

    fun removeMonitoredApp(packageName: String) {
        viewModelScope.launch {
            db.ruleDao().deleteByPackage(packageName)
        }
    }

    fun toggleMonitoredApp(appEntity: RuleAppEntity) {
        viewModelScope.launch {
            db.ruleDao().updateApp(appEntity.copy(isEnabled = !appEntity.isEnabled))
        }
    }

    fun addKeyword(keyword: String, category: String, severity: String) {
        viewModelScope.launch {
            db.keywordDao().insertKeyword(
                KeywordEntity(
                    keyword = keyword.trim().lowercase(),
                    category = category.trim().lowercase().ifBlank { "general" },
                    severity = severity
                )
            )
        }
    }

    fun removeKeyword(id: Long) {
        viewModelScope.launch {
            db.keywordDao().deleteById(id)
        }
    }

    fun toggleKeyword(keywordEntity: KeywordEntity) {
        viewModelScope.launch {
            db.keywordDao().updateKeyword(keywordEntity.copy(isEnabled = !keywordEntity.isEnabled))
        }
    }

    fun fireTestAlert() {
        viewModelScope.launch {
            _isTestFiring.value = true
            try {
                app.triggerEngine.fireTestTrigger()
            } finally {
                _isTestFiring.value = false
            }
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            db.triggerLogDao().clearAll()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(SalimApplication.instance) as T
            }
        }
    }
}
