package com.example

import com.example.data.local.UserSettings
import com.example.data.local.entity.KeywordEntity
import com.example.data.local.entity.RuleAppEntity
import com.example.data.model.PersonaType
import com.example.data.model.ScreenContext
import com.example.data.model.TriggerSeverity
import com.example.data.model.TriggerType
import com.example.engine.PersonaPromptBuilder
import com.example.engine.TriggerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TriggerEngineTest {

    private val sampleApps = listOf(
        RuleAppEntity("com.instagram.android", "Instagram", "social"),
        RuleAppEntity("com.zhiliaoapp.musically", "TikTok", "social")
    )

    private val sampleKeywords = listOf(
        KeywordEntity(id = 1, keyword = "casino", category = "gambling", severity = "HIGH"),
        KeywordEntity(id = 2, keyword = "slots", category = "gambling", severity = "HIGH")
    )

    private val defaultSettings = UserSettings(
        isMonitoringEnabled = true,
        sessionThresholdMinutes = 20,
        cooldownMinutes = 5,
        lateNightStartHour = 23,
        lateNightEndHour = 5
    )

    @Test
    fun `evaluates keyword match with high severity`() {
        val context = ScreenContext(
            packageName = "com.sample.browser",
            windowTitle = "Online Casino Review",
            visibleTextSnippet = "Play slots online now",
            sessionDurationSeconds = 30L
        )

        val event = TriggerEngine.evaluateRules(
            context = context,
            enabledApps = sampleApps,
            enabledKeywords = sampleKeywords,
            settings = defaultSettings,
            currentHour = 14,
            currentTimeMillis = 100000L,
            cooldownChecker = { _, _ -> false }
        )

        assertNotNull(event)
        assertEquals(TriggerType.KEYWORD_MATCH, event?.type)
        assertEquals(TriggerSeverity.HIGH, event?.severity)
    }

    @Test
    fun `evaluates continuous session duration threshold`() {
        val context = ScreenContext(
            packageName = "com.instagram.android",
            sessionDurationSeconds = 1500L // 25 minutes > 20 minute limit
        )

        val event = TriggerEngine.evaluateRules(
            context = context,
            enabledApps = sampleApps,
            enabledKeywords = sampleKeywords,
            settings = defaultSettings,
            currentHour = 14,
            currentTimeMillis = 100000L,
            cooldownChecker = { _, _ -> false }
        )

        assertNotNull(event)
        assertEquals(TriggerType.SESSION_THRESHOLD, event?.type)
    }

    @Test
    fun `evaluates late night window correctly`() {
        assertTrue(TriggerEngine.isHourInWindow(23, 23, 5))
        assertTrue(TriggerEngine.isHourInWindow(1, 23, 5))
        assertTrue(TriggerEngine.isHourInWindow(4, 23, 5))
        assertFalse(TriggerEngine.isHourInWindow(14, 23, 5))
    }

    @Test
    fun `skips trigger when cooling down`() {
        val context = ScreenContext(
            packageName = "com.instagram.android",
            sessionDurationSeconds = 1500L
        )

        val event = TriggerEngine.evaluateRules(
            context = context,
            enabledApps = sampleApps,
            enabledKeywords = sampleKeywords,
            settings = defaultSettings,
            currentHour = 14,
            currentTimeMillis = 100000L,
            cooldownChecker = { _, _ -> true } // cooling down
        )

        assertNull(event)
    }

    @Test
    fun `persona prompt builder never sends raw extracted text`() {
        val secretSnippet = "Secret banking text or sensitive raw text snippet"
        val context = ScreenContext(
            packageName = "com.instagram.android",
            visibleTextSnippet = secretSnippet,
            sessionDurationSeconds = 600L
        )
        val event = com.example.data.model.TriggerEvent(
            type = TriggerType.BLOCKLIST_APP,
            severity = TriggerSeverity.LOW,
            context = context,
            reasonDescription = "Instagram (social)"
        )

        val userPrompt = PersonaPromptBuilder.buildUserMetadataPrompt(event, "Instagram")

        assertFalse(userPrompt.contains(secretSnippet))
        assertTrue(userPrompt.contains("Instagram"))
    }

    @Test
    fun `persona prompt system instructions enforce brevity and no emojis`() {
        val prompt = PersonaPromptBuilder.buildSystemPrompt(PersonaType.SUPPORTIVE_BROTHER, "")
        assertTrue(prompt.contains("Do NOT use emojis"))
        assertTrue(prompt.contains("Salim"))
    }
}
