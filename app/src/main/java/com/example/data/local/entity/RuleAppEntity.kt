package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rule_apps")
data class RuleAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val category: String,
    val isEnabled: Boolean = true,
    val addedTimestamp: Long = System.currentTimeMillis()
)
