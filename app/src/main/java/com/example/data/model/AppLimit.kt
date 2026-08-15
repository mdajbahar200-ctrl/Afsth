package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_limits")
data class AppLimit(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int = 30,
    val currentUsageMinutes: Int = 0,
    val isShortsBlocked: Boolean = true,
    val isHardLocked: Boolean = true,
    val isEnabled: Boolean = true,
    val iconEmoji: String = "📱",
    val category: String = "Social"
)

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 25,
    val completed: Boolean = false,
    val strictMode: Boolean = true,
    val blockedAttempts: Int = 0,
    val title: String = "Study Mode"
)
