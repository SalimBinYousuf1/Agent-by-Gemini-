package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<ChatMessage>,
    @Json(name = "temperature") val temperature: Double = 0.7,
    @Json(name = "max_tokens") val maxTokens: Int = 120
)

@JsonClass(generateAdapter = true)
data class ChatChoice(
    @Json(name = "index") val index: Int? = null,
    @Json(name = "message") val message: ChatMessage
)

@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "choices") val choices: List<ChatChoice>? = null
)
