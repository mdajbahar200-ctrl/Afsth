package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feature_items")
data class FeatureItem(
    @PrimaryKey
    val key: String,
    val title: String,
    val subtitle: String,
    val isEnabled: Boolean,
    val isPro: Boolean = false,
    val category: String, // "LOCKDOWN", "BLOCKING", "LIMITS", "SCHEDULE_BLOCK", "MODS"
    val iconResName: String = "ic_shield"
)

@Entity(tableName = "blocked_urls")
data class BlockedUrl(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val domain: String,
    val isEnabled: Boolean = true,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "block_schedules")
data class BlockSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "Work Focus",
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 17,
    val endMinute: Int = 0,
    val daysOfWeek: String = "Mon,Tue,Wed,Thu,Fri",
    val isEnabled: Boolean = true
)
