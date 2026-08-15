package com.example.data.repository

import com.example.data.local.SentryDao
import com.example.data.model.AppLimit
import com.example.data.model.BlockSchedule
import com.example.data.model.BlockedEvent
import com.example.data.model.BlockedUrl
import com.example.data.model.FeatureItem
import com.example.data.model.FocusSession
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

class SentryRepository(private val dao: SentryDao) {

    val allBlockedEvents: Flow<List<BlockedEvent>> = dao.getAllBlockedEvents()
    val allAppLimits: Flow<List<AppLimit>> = dao.getAllAppLimits()
    val allFocusSessions: Flow<List<FocusSession>> = dao.getAllFocusSessions()
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allFeatureItems: Flow<List<FeatureItem>> = dao.getAllFeatureItems()
    val allBlockedUrls: Flow<List<BlockedUrl>> = dao.getAllBlockedUrls()
    val allBlockSchedules: Flow<List<BlockSchedule>> = dao.getAllBlockSchedules()

    fun getBlockedEventsSince(sinceTimestamp: Long): Flow<List<BlockedEvent>> {
        return dao.getBlockedEventsSince(sinceTimestamp)
    }

    fun getBlockedCountSince(sinceTimestamp: Long): Flow<Int> {
        return dao.getBlockedCountSince(sinceTimestamp)
    }

    fun getBlockedCountByAppSince(appName: String, sinceTimestamp: Long): Flow<Int> {
        return dao.getBlockedCountByAppSince(appName, sinceTimestamp)
    }

    suspend fun logBlockedEvent(
        packageName: String,
        appName: String,
        feedType: String,
        actionTaken: String = "Auto-Dismissed"
    ): Long {
        return dao.insertBlockedEvent(
            BlockedEvent(
                packageName = packageName,
                appName = appName,
                feedType = feedType,
                actionTaken = actionTaken
            )
        )
    }

    suspend fun updateAppLimit(limit: AppLimit) {
        dao.updateAppLimit(limit)
    }

    suspend fun saveAppLimit(limit: AppLimit) {
        dao.insertAppLimit(limit)
    }

    suspend fun getAppLimit(packageName: String): AppLimit? {
        return dao.getAppLimit(packageName)
    }

    suspend fun logFocusSession(session: FocusSession): Long {
        return dao.insertFocusSession(session)
    }

    suspend fun updateFocusSession(session: FocusSession) {
        dao.updateFocusSession(session)
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        dao.updateUserProfile(profile)
    }

    suspend fun updateFeatureItem(item: FeatureItem) {
        dao.updateFeatureItem(item)
    }

    suspend fun addBlockedUrl(domain: String): Long {
        return dao.insertBlockedUrl(BlockedUrl(domain = domain))
    }

    suspend fun deleteBlockedUrl(id: Long) {
        dao.deleteBlockedUrl(id)
    }

    suspend fun saveBlockSchedule(schedule: BlockSchedule): Long {
        return if (schedule.id == 0L) {
            dao.insertBlockSchedule(schedule)
        } else {
            dao.updateBlockSchedule(schedule)
            schedule.id
        }
    }
}

