package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppLimit
import com.example.data.model.BlockSchedule
import com.example.data.model.BlockedEvent
import com.example.data.model.BlockedUrl
import com.example.data.model.FeatureItem
import com.example.data.model.FocusSession
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BlockedEvent::class,
        AppLimit::class,
        FocusSession::class,
        UserProfile::class,
        FeatureItem::class,
        BlockedUrl::class,
        BlockSchedule::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SentryDatabase : RoomDatabase() {
    abstract fun sentryDao(): SentryDao

    companion object {
        @Volatile
        private var INSTANCE: SentryDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SentryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SentryDatabase::class.java,
                    "sentry_database"
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(SentryDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SentryDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.sentryDao())
                }
            }
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.sentryDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: SentryDao) {
            // User Profile (Md. Aj bahar)
            dao.insertUserProfile(
                UserProfile(
                    id = 1,
                    name = "Md. Aj bahar",
                    username = "@mdajbahar1002",
                    email = "mdajbahar200@gmail.com",
                    streakDays = 1,
                    bestStreakDays = 1,
                    karmaXp = 30,
                    streakRank = "Neutral",
                    isPro = false,
                    selectedThemeId = "cyberpunk"
                )
            )

            // Default Apps for Surgical Reels and Limits
            val defaultApps = listOf(
                AppLimit(
                    packageName = "com.instagram.android",
                    appName = "Instagram",
                    dailyLimitMinutes = 30,
                    currentUsageMinutes = 0,
                    isShortsBlocked = true,
                    isHardLocked = true,
                    isEnabled = true,
                    iconEmoji = "📸",
                    category = "Social Media"
                ),
                AppLimit(
                    packageName = "com.google.android.youtube",
                    appName = "YouTube",
                    dailyLimitMinutes = 45,
                    currentUsageMinutes = 0,
                    isShortsBlocked = true,
                    isHardLocked = false,
                    isEnabled = true,
                    iconEmoji = "▶️",
                    category = "Entertainment"
                ),
                AppLimit(
                    packageName = "com.zhiliaoapp.musically",
                    appName = "TikTok",
                    dailyLimitMinutes = 20,
                    currentUsageMinutes = 0,
                    isShortsBlocked = true,
                    isHardLocked = true,
                    isEnabled = true,
                    iconEmoji = "🎵",
                    category = "Short Video"
                ),
                AppLimit(
                    packageName = "com.facebook.katana",
                    appName = "Facebook",
                    dailyLimitMinutes = 30,
                    currentUsageMinutes = 0,
                    isShortsBlocked = true,
                    isHardLocked = false,
                    isEnabled = true,
                    iconEmoji = "👥",
                    category = "Social Network"
                ),
                AppLimit(
                    packageName = "com.twitter.android",
                    appName = "X (Twitter)",
                    dailyLimitMinutes = 25,
                    currentUsageMinutes = 0,
                    isShortsBlocked = true,
                    isHardLocked = false,
                    isEnabled = true,
                    iconEmoji = "🐦",
                    category = "News & Feeds"
                )
            )
            dao.insertAppLimits(defaultApps)

            // 14 Core Features Setup
            val features = listOf(
                FeatureItem("prime_mode", "Prime Mode", "Hardcore commitment lock", isEnabled = false, isPro = true, category = "PRIME"),
                FeatureItem("uninstall_protection", "Uninstall Protection", "Prevent app uninstallation", isEnabled = false, isPro = true, category = "PRIME"),
                FeatureItem("lock_phone", "Lock My Phone", "Digital detox lockdown", isEnabled = false, isPro = false, category = "PRIME"),
                FeatureItem("fahh_mode", "Fahh Mode", "Plays sound on block", isEnabled = false, isPro = false, category = "MODS"),
                FeatureItem("rag_korla", "Rag korla !", "Plays sound on block", isEnabled = false, isPro = false, category = "MODS"),
                FeatureItem("reels_block", "Reels Blocker", "Block reels on social media", isEnabled = true, isPro = false, category = "BLOCKING"),
                FeatureItem("porn_block", "Porn Blocker", "Block adult content & nudity", isEnabled = false, isPro = false, category = "BLOCKING"),
                FeatureItem("website_block", "Website Blocker", "Block specific URLs", isEnabled = false, isPro = false, category = "BLOCKING"),
                FeatureItem("app_block", "App Block", "Block apps completely", isEnabled = false, isPro = false, category = "BLOCKING"),
                FeatureItem("five_second_pause", "5 Second Pause", "5s pause before app open", isEnabled = false, isPro = false, category = "LIMITS"),
                FeatureItem("app_limits", "App Limits", "Set daily time limits", isEnabled = true, isPro = false, category = "LIMITS"),
                FeatureItem("scroll_limit", "Scroll Limit", "Limit endless scrolling", isEnabled = false, isPro = true, category = "LIMITS"),
                FeatureItem("schedule_block", "Schedule blocker", "Setup blocking schedule", isEnabled = false, isPro = true, category = "SCHEDULE_BLOCK"),
                FeatureItem("block_notifications", "Block Notifications", "Setup schedule or timer", isEnabled = false, isPro = false, category = "SCHEDULE_BLOCK")
            )
            dao.insertFeatureItems(features)

            // Default block schedule
            dao.insertBlockSchedule(
                BlockSchedule(
                    title = "Work & Study Focus",
                    startHour = 9,
                    startMinute = 0,
                    endHour = 17,
                    endMinute = 0,
                    daysOfWeek = "Mon, Tue, Wed, Thu, Fri",
                    isEnabled = false
                )
            )
        }
    }
}

