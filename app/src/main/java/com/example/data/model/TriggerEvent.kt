package com.example.data.model

enum class TriggerSeverity {
    LOW,
    MEDIUM,
    HIGH
}

enum class TriggerType {
    BLOCKLIST_APP,
    KEYWORD_MATCH,
    SESSION_THRESHOLD,
    LATE_NIGHT_USAGE,
    TEST_ALERT
}

enum class PersonaType(val id: String, val displayName: String) {
    SUPPORTIVE_BROTHER("supportive_brother", "Supportive Brother"),
    STERN_MENTOR("stern_mentor", "Stern Mentor"),
    GENTLE_REMINDER("gentle_reminder", "Gentle Reminder"),
    CUSTOM("custom", "Custom Persona")
}

data class TriggerEvent(
    val type: TriggerType,
    val severity: TriggerSeverity,
    val context: ScreenContext,
    val reasonDescription: String,
    val timestamp: Long = System.currentTimeMillis()
)
