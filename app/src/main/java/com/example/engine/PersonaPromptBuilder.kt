package com.example.engine

import com.example.data.model.PersonaType
import com.example.data.model.TriggerEvent
import com.example.data.model.TriggerType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PersonaPromptBuilder {

    fun buildSystemPrompt(persona: PersonaType, customPrompt: String): String {
        val baseInstruction = "You are Salim, a personal digital accountability agent (not a conversational chatbot, not an AI assistant). " +
                "Your role is to write a single proactive reflection or intervention message to be delivered as a phone notification. " +
                "Requirements: Output strictly ONE or TWO short, plain sentences (under 180 characters total). " +
                "Do NOT use exclamation points. Do NOT use emojis. Do NOT say 'as an AI' or 'hey there'. " +
                "Speak directly, calmly, and sincerely to help the user pause and remember their intentions."

        val personaTone = when (persona) {
            PersonaType.SUPPORTIVE_BROTHER ->
                "Tone: A caring, observant brother. Honest, empathetic, grounded. You want what is genuinely best for them without sounding preachy."

            PersonaType.STERN_MENTOR ->
                "Tone: A disciplined, direct mentor. Firm, stoic, no-nonsense. Call out avoidance and remind them of their standards and focus."

            PersonaType.GENTLE_REMINDER ->
                "Tone: A calm, quiet mirror. Peaceful, reflective, minimal. A gentle nudge prompting them to take a breath and check if they are being present."

            PersonaType.CUSTOM ->
                if (customPrompt.isNotBlank()) "User's custom persona guidance: $customPrompt"
                else "Tone: A calm, thoughtful friend asking if this is how they truly intended to spend their current moment."
        }

        return "$baseInstruction\n\n$personaTone"
    }

    /**
     * Builds safe, privacy-respecting metadata user message.
     * Note: NEVER passes raw extracted screen text to the external API.
     */
    fun buildUserMetadataPrompt(event: TriggerEvent, appLabel: String): String {
        val durationMinutes = event.context.sessionDurationSeconds / 60
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val currentTime = timeFormat.format(Date(event.timestamp))

        val metadataReason = when (event.type) {
            TriggerType.BLOCKLIST_APP ->
                "The user opened a flagged monitored app ($appLabel) at $currentTime."

            TriggerType.SESSION_THRESHOLD ->
                "The user has been continuously active in $appLabel for $durationMinutes minutes at $currentTime."

            TriggerType.KEYWORD_MATCH ->
                "The user encountered flagged recreational or adult/gambling content category in $appLabel at $currentTime."

            TriggerType.LATE_NIGHT_USAGE ->
                "The user is actively using $appLabel late at night at $currentTime (session duration: $durationMinutes minutes)."

            TriggerType.TEST_ALERT ->
                "A diagnostic test trigger was fired to verify accountability notification delivery."
        }

        return "Usage event: $metadataReason\n" +
                "App category: ${event.reasonDescription}\n" +
                "Write a short, direct message (under 180 characters) urging intentionality."
    }

    /**
     * Local deterministic fallback message if offline, rate limited, or no Groq key configured.
     */
    fun getLocalFallbackMessage(persona: PersonaType, event: TriggerEvent, appLabel: String): String {
        val minutes = event.context.sessionDurationSeconds / 60
        return when (persona) {
            PersonaType.SUPPORTIVE_BROTHER -> {
                when (event.type) {
                    TriggerType.SESSION_THRESHOLD -> "You have been on $appLabel for $minutes minutes. Put the phone down and check what you actually wanted to get done."
                    TriggerType.LATE_NIGHT_USAGE -> "It is getting late while browsing $appLabel. Close the screen and get some rest."
                    TriggerType.KEYWORD_MATCH -> "Take a second before going down this feed. You know where this usually leads."
                    TriggerType.BLOCKLIST_APP -> "You just opened $appLabel. Is this what you actually planned to do right now?"
                    TriggerType.TEST_ALERT -> "Salim accountability check. The notification system is working normally."
                }
            }
            PersonaType.STERN_MENTOR -> {
                when (event.type) {
                    TriggerType.SESSION_THRESHOLD -> "$minutes minutes spent on $appLabel. This is not moving you toward your goals. Step away."
                    TriggerType.LATE_NIGHT_USAGE -> "Late night phone use is eroding your discipline. Lock the screen."
                    TriggerType.KEYWORD_MATCH -> "You flagged this category for a reason. Do not compromise on your boundaries."
                    TriggerType.BLOCKLIST_APP -> "$appLabel is on your restriction list. Decide if this serves your focus."
                    TriggerType.TEST_ALERT -> "Discipline check. Alert delivery verified."
                }
            }
            PersonaType.GENTLE_REMINDER -> {
                when (event.type) {
                    TriggerType.SESSION_THRESHOLD -> "You have been in $appLabel for $minutes minutes. Take a breath and check in with yourself."
                    TriggerType.LATE_NIGHT_USAGE -> "The night is quiet. Consider setting the device aside and resting your mind."
                    TriggerType.KEYWORD_MATCH -> "Notice what you are looking for right now. Pause before proceeding."
                    TriggerType.BLOCKLIST_APP -> "A gentle pause as you open $appLabel. Are you using your time intentionally?"
                    TriggerType.TEST_ALERT -> "A gentle test reflection from Salim."
                }
            }
            PersonaType.CUSTOM -> {
                "Take a deliberate pause. You have been in $appLabel for $minutes minutes."
            }
        }
    }
}
