package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_events")
data class BlockedEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val packageName: String,
    val appName: String,
    val feedType: String, // "Reels", "Shorts", "TikTok", "Spotlight", "App Limit"
    val actionTaken: String, // "Dismissed", "Strict Block", "Interception Overlay"
    val estimatedSecondsSaved: Int = 108 // ~1.8 mins per blocked doomscroll loop
)
