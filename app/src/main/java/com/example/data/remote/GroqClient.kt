package com.example.data.remote

import android.util.Log
import com.example.data.local.SecureKeyStorage
import com.example.data.model.PersonaType
import com.example.data.model.TriggerEvent
import com.example.engine.PersonaPromptBuilder
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class GroqClient(private val keyStorage: SecureKeyStorage) {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.groq.com/openai/v1/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService: GroqApiService = retrofit.create(GroqApiService::class.java)

    suspend fun generateInterventionMessage(
        persona: PersonaType,
        customPersonaPrompt: String,
        event: TriggerEvent,
        appLabel: String,
        model: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = keyStorage.getGroqApiKey()
        if (apiKey.isNullOrBlank()) {
            Log.d(TAG, "No Groq API key configured. Using local fallback message.")
            return@withContext PersonaPromptBuilder.getLocalFallbackMessage(persona, event, appLabel)
        }

        try {
            val systemPrompt = PersonaPromptBuilder.buildSystemPrompt(persona, customPersonaPrompt)
            val userPrompt = PersonaPromptBuilder.buildUserMetadataPrompt(event, appLabel)

            val request = ChatCompletionRequest(
                model = model.trim(),
                messages = listOf(
                    ChatMessage(role = "system", content = systemPrompt),
                    ChatMessage(role = "user", content = userPrompt)
                ),
                temperature = 0.6,
                maxTokens = 80
            )

            val authHeader = "Bearer $apiKey"
            val response = apiService.createChatCompletion(authHeader, request)
            val generatedContent = response.choices?.firstOrNull()?.message?.content?.trim()

            if (!generatedContent.isNullOrBlank()) {
                // Clean and cap message at ~200 chars for notification display
                val cleaned = cleanMessage(generatedContent)
                Log.d(TAG, "Successfully generated Groq message: $cleaned")
                return@withContext cleaned
            }
        } catch (e: Exception) {
            Log.w(TAG, "Groq API call failed (${e.message}). Falling back to local template.", e)
        }

        return@withContext PersonaPromptBuilder.getLocalFallbackMessage(persona, event, appLabel)
    }

    private fun cleanMessage(raw: String): String {
        // Strip quotes if LLM wrapped in quotation marks
        var cleaned = raw.removePrefix("\"").removeSuffix("\"").trim()
        if (cleaned.length > 200) {
            cleaned = cleaned.take(197) + "..."
        }
        return cleaned
    }

    companion object {
        private const val TAG = "GroqClient"
    }
}
