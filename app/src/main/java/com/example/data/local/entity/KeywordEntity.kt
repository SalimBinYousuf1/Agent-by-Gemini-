package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keywords")
data class KeywordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val keyword: String,
    val category: String = "general",
    val severity: String = "MEDIUM",
    val isEnabled: Boolean = true
)
