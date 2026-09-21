package com.example.data.model

data class ScreenContext(
    val packageName: String,
    val windowTitle: String = "",
    val visibleTextSnippet: String = "",
    val sessionDurationSeconds: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
