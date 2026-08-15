package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Md. Aj bahar",
    val username: String = "@mdajbahar1002",
    val email: String = "mdajbahar200@gmail.com",
    val streakDays: Int = 1,
    val bestStreakDays: Int = 1,
    val karmaXp: Int = 30,
    val streakRank: String = "Neutral",
    val isPro: Boolean = false,
    val selectedThemeId: String = "cyberpunk"
)
