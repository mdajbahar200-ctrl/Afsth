package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppLimit
import com.example.data.model.BlockSchedule
import com.example.data.model.BlockedEvent
import com.example.data.model.BlockedUrl
import com.example.data.model.FeatureItem
import com.example.data.model.FocusSession
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface SentryDao {
    // Blocked Events
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedEvent(event: BlockedEvent): Long

    @Query("SELECT * FROM blocked_events ORDER BY timestamp DESC")
    fun getAllBlockedEvents(): Flow<List<BlockedEvent>>

    @Query("SELECT * FROM blocked_events WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getBlockedEventsSince(sinceTimestamp: Long): Flow<List<BlockedEvent>>

    @Query("SELECT COUNT(*) FROM blocked_events WHERE timestamp >= :sinceTimestamp")
    fun getBlockedCountSince(sinceTimestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_events WHERE appName = :appName AND timestamp >= :sinceTimestamp")
    fun getBlockedCountByAppSince(appName: String, sinceTimestamp: Long): Flow<Int>

    // App Limits
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppLimits(limits: List<AppLimit>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppLimit(limit: AppLimit)

    @Update
    suspend fun updateAppLimit(limit: AppLimit)

    @Query("SELECT * FROM app_limits ORDER BY dailyLimitMinutes ASC")
    fun getAllAppLimits(): Flow<List<AppLimit>>

    @Query("SELECT * FROM app_limits WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppLimit(packageName: String): AppLimit?

    // Focus Sessions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSession): Long

    @Update
    suspend fun updateFocusSession(session: FocusSession)

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllFocusSessions(): Flow<List<FocusSession>>

    // User Profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Update
    suspend fun updateUserProfile(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    // Feature Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeatureItems(items: List<FeatureItem>)

    @Update
    suspend fun updateFeatureItem(item: FeatureItem)

    @Query("SELECT * FROM feature_items")
    fun getAllFeatureItems(): Flow<List<FeatureItem>>

    @Query("SELECT * FROM feature_items WHERE `key` = :key LIMIT 1")
    suspend fun getFeatureItem(key: String): FeatureItem?

    // Blocked URLs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedUrl(url: BlockedUrl): Long

    @Query("DELETE FROM blocked_urls WHERE id = :id")
    suspend fun deleteBlockedUrl(id: Long)

    @Query("SELECT * FROM blocked_urls ORDER BY id DESC")
    fun getAllBlockedUrls(): Flow<List<BlockedUrl>>

    // Block Schedules
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockSchedule(schedule: BlockSchedule): Long

    @Update
    suspend fun updateBlockSchedule(schedule: BlockSchedule)

    @Query("SELECT * FROM block_schedules ORDER BY id ASC")
    fun getAllBlockSchedules(): Flow<List<BlockSchedule>>
}

