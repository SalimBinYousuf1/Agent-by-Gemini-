package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trigger_logs")
data class TriggerLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val triggerType: String,
    val severity: String,
    val appPackageName: String,
    val appLabel: String,
    val category: String,
    val firedAt: Long = System.currentTimeMillis(),
    val notificationMessage: String,
    val personaUsed: String
)
