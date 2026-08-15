package com.example

import android.app.Application
import com.example.data.local.SentryDatabase
import com.example.data.model.AppLimit
import com.example.data.model.BlockSchedule
import com.example.data.model.FeatureItem
import com.example.data.repository.SentryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

class SentryApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { SentryDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { SentryRepository(database.sentryDao()) }
    val cloudSyncService by lazy { com.example.data.cloud.CloudSyncService(this) }

    companion object {
        lateinit var instance: SentryApp
            private set

        // Global master toggles and active states
        private val _isMasterShieldActive = MutableStateFlow(true)
        val isMasterShieldActive = _isMasterShieldActive.asStateFlow()

        private val _isStudyModeActive = MutableStateFlow(false)
        val isStudyModeActive = _isStudyModeActive.asStateFlow()

        private val _isFahhModeActive = MutableStateFlow(false)
        val isFahhModeActive = _isFahhModeActive.asStateFlow()

        private val _isRagKorlaActive = MutableStateFlow(false)
        val isRagKorlaActive = _isRagKorlaActive.asStateFlow()

        private val _focusRemainingSeconds = MutableStateFlow(24 * 60)
        val focusRemainingSeconds = _focusRemainingSeconds.asStateFlow()

        private val _interceptionOverlayEvent = MutableStateFlow<InterceptionDetail?>(null)
        val interceptionOverlayEvent = _interceptionOverlayEvent.asStateFlow()

        // 5-Second Mindfulness Pause State
        private val _mindfulnessPauseApp = MutableStateFlow<PauseAppDetail?>(null)
        val mindfulnessPauseApp = _mindfulnessPauseApp.asStateFlow()

        // Phone Lockdown Detox State
        private val _phoneLockUntilTimestamp = MutableStateFlow(0L)
        val phoneLockUntilTimestamp = _phoneLockUntilTimestamp.asStateFlow()

        // Real-time cached features & limits for zero-latency Accessibility Service inspection
        val activeFeaturesMap = ConcurrentHashMap<String, Boolean>()
        val activeAppLimitsList = MutableStateFlow<List<AppLimit>>(emptyList())
        val activeBlockedUrlsSet = ConcurrentHashMap.newKeySet<String>()
        val activeBlockSchedulesList = MutableStateFlow<List<BlockSchedule>>(emptyList())

        // Cache of passed 5-second pauses per package: pkg -> expiryTimestamp (e.g. valid for 15 mins)
        val passed5sPauseCache = ConcurrentHashMap<String, Long>()

        // Scroll counter per package in current window session
        val scrollCounterMap = ConcurrentHashMap<String, Int>()

        fun setMasterShield(active: Boolean) {
            _isMasterShieldActive.value = active
        }

        fun setStudyModeActive(active: Boolean) {
            _isStudyModeActive.value = active
        }

        fun setFahhModeActive(active: Boolean) {
            _isFahhModeActive.value = active
        }

        fun setRagKorlaActive(active: Boolean) {
            _isRagKorlaActive.value = active
        }

        fun setFocusRemainingSeconds(seconds: Int) {
            _focusRemainingSeconds.value = seconds
        }

        fun triggerInterception(detail: InterceptionDetail) {
            _interceptionOverlayEvent.value = detail
        }

        fun clearInterception() {
            _interceptionOverlayEvent.value = null
        }

        fun triggerMindfulnessPause(detail: PauseAppDetail) {
            _mindfulnessPauseApp.value = detail
        }

        fun clearMindfulnessPause() {
            _mindfulnessPauseApp.value = null
        }

        fun completeMindfulnessPause(packageName: String) {
            _mindfulnessPauseApp.value = null
            // Grant 15 minutes pass before prompting again
            passed5sPauseCache[packageName] = System.currentTimeMillis() + 15 * 60 * 1000L
        }

        fun setPhoneLockDuration(durationMinutes: Int) {
            _phoneLockUntilTimestamp.value = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        }

        fun clearPhoneLock() {
            _phoneLockUntilTimestamp.value = 0L
        }

        fun isFeatureEnabled(featureKey: String): Boolean {
            return activeFeaturesMap[featureKey] ?: false
        }

        fun isPhoneCurrentlyLocked(): Boolean {
            val lockUntil = _phoneLockUntilTimestamp.value
            return lockUntil > System.currentTimeMillis()
        }

        fun isWithinScheduleBlock(): Boolean {
            if (!isFeatureEnabled("schedule_block")) return false
            val schedules = activeBlockSchedulesList.value
            if (schedules.isEmpty()) return false

            val cal = Calendar.getInstance()
            val currentHour = cal.get(Calendar.HOUR_OF_DAY)
            val currentMinute = cal.get(Calendar.MINUTE)
            val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val dayStr = when (currentDayOfWeek) {
                Calendar.SUNDAY -> "Sun"
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                else -> ""
            }

            val currentMinutesSinceMidnight = currentHour * 60 + currentMinute

            for (schedule in schedules) {
                if (schedule.isEnabled) {
                    if (schedule.daysOfWeek.contains(dayStr, ignoreCase = true) || schedule.daysOfWeek.contains("Everyday", ignoreCase = true)) {
                        val startMin = schedule.startHour * 60 + schedule.startMinute
                        val endMin = schedule.endHour * 60 + schedule.endMinute
                        if (startMin <= endMin) {
                            if (currentMinutesSinceMidnight in startMin..endMin) return true
                        } else {
                            // Overnight schedule (e.g. 22:00 to 06:00)
                            if (currentMinutesSinceMidnight >= startMin || currentMinutesSinceMidnight <= endMin) return true
                        }
                    }
                }
            }
            return false
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Start background collectors to keep in-memory caches strictly synchronized
        applicationScope.launch {
            repository.allFeatureItems.collect { features ->
                activeFeaturesMap.clear()
                for (item in features) {
                    activeFeaturesMap[item.key] = item.isEnabled
                }
                // Sync special flags
                _isFahhModeActive.value = activeFeaturesMap["fahh_mode"] ?: false
                _isRagKorlaActive.value = activeFeaturesMap["rag_korla"] ?: false
            }
        }

        applicationScope.launch {
            repository.allAppLimits.collect { limits ->
                activeAppLimitsList.value = limits
            }
        }

        applicationScope.launch {
            repository.allBlockedUrls.collect { urls ->
                activeBlockedUrlsSet.clear()
                for (u in urls) {
                    activeBlockedUrlsSet.add(u.domain.trim().lowercase())
                }
            }
        }

        applicationScope.launch {
            repository.allBlockSchedules.collect { schedules ->
                activeBlockSchedulesList.value = schedules
            }
        }
    }
}

data class InterceptionDetail(
    val appName: String,
    val feedType: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class PauseAppDetail(
    val packageName: String,
    val appName: String,
    val timestamp: Long = System.currentTimeMillis()
)
