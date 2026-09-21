package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.PersonaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val isMonitoringEnabled: Boolean = true,
    val personaType: PersonaType = PersonaType.SUPPORTIVE_BROTHER,
    val customPersonaPrompt: String = "",
    val sessionThresholdMinutes: Int = 20,
    val cooldownMinutes: Int = 5,
    val lateNightStartHour: Int = 23,
    val lateNightEndHour: Int = 5,
    val groqModel: String = "llama-3.3-70b-versatile",
    val isOverlayEnabled: Boolean = false
)

class UserSettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("salim_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val personaString = prefs.getString(KEY_PERSONA, PersonaType.SUPPORTIVE_BROTHER.name)
        val persona = try {
            PersonaType.valueOf(personaString ?: PersonaType.SUPPORTIVE_BROTHER.name)
        } catch (_: Exception) {
            PersonaType.SUPPORTIVE_BROTHER
        }

        return UserSettings(
            isMonitoringEnabled = prefs.getBoolean(KEY_MONITORING_ENABLED, true),
            personaType = persona,
            customPersonaPrompt = prefs.getString(KEY_CUSTOM_PERSONA, "") ?: "",
            sessionThresholdMinutes = prefs.getInt(KEY_SESSION_THRESHOLD, 20),
            cooldownMinutes = prefs.getInt(KEY_COOLDOWN_MINUTES, 5),
            lateNightStartHour = prefs.getInt(KEY_LATE_NIGHT_START, 23),
            lateNightEndHour = prefs.getInt(KEY_LATE_NIGHT_END, 5),
            groqModel = prefs.getString(KEY_GROQ_MODEL, "llama-3.3-70b-versatile") ?: "llama-3.3-70b-versatile",
            isOverlayEnabled = prefs.getBoolean(KEY_OVERLAY_ENABLED, false)
        )
    }

    fun setMonitoringEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MONITORING_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isMonitoringEnabled = enabled)
    }

    fun setPersonaType(personaType: PersonaType) {
        prefs.edit().putString(KEY_PERSONA, personaType.name).apply()
        _settings.value = _settings.value.copy(personaType = personaType)
    }

    fun setCustomPersonaPrompt(prompt: String) {
        prefs.edit().putString(KEY_CUSTOM_PERSONA, prompt).apply()
        _settings.value = _settings.value.copy(customPersonaPrompt = prompt)
    }

    fun setSessionThreshold(minutes: Int) {
        prefs.edit().putInt(KEY_SESSION_THRESHOLD, minutes).apply()
        _settings.value = _settings.value.copy(sessionThresholdMinutes = minutes)
    }

    fun setCooldownMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_COOLDOWN_MINUTES, minutes).apply()
        _settings.value = _settings.value.copy(cooldownMinutes = minutes)
    }

    fun setLateNightWindow(startHour: Int, endHour: Int) {
        prefs.edit()
            .putInt(KEY_LATE_NIGHT_START, startHour)
            .putInt(KEY_LATE_NIGHT_END, endHour)
            .apply()
        _settings.value = _settings.value.copy(
            lateNightStartHour = startHour,
            lateNightEndHour = endHour
        )
    }

    fun setGroqModel(model: String) {
        prefs.edit().putString(KEY_GROQ_MODEL, model).apply()
        _settings.value = _settings.value.copy(groqModel = model)
    }

    fun setOverlayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isOverlayEnabled = enabled)
    }

    companion object {
        private const val KEY_MONITORING_ENABLED = "monitoring_enabled"
        private const val KEY_PERSONA = "persona_type"
        private const val KEY_CUSTOM_PERSONA = "custom_persona_prompt"
        private const val KEY_SESSION_THRESHOLD = "session_threshold_minutes"
        private const val KEY_COOLDOWN_MINUTES = "cooldown_minutes"
        private const val KEY_LATE_NIGHT_START = "late_night_start"
        private const val KEY_LATE_NIGHT_END = "late_night_end"
        private const val KEY_GROQ_MODEL = "groq_model"
        private const val KEY_OVERLAY_ENABLED = "overlay_enabled"
    }
}
