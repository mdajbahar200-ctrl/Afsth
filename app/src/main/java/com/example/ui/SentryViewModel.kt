package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.InterceptionDetail
import com.example.SentryApp
import com.example.data.model.AppLimit
import com.example.data.model.BlockSchedule
import com.example.data.model.BlockedEvent
import com.example.data.model.BlockedUrl
import com.example.data.model.FeatureItem
import com.example.data.model.FocusSession
import com.example.data.model.UserProfile
import com.example.data.repository.SentryRepository
import com.example.service.SentryForegroundService
import com.example.service.SoundEffectsHelper
import com.example.service.UsageStatsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class SentryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SentryRepository = (application as SentryApp).repository

    val isMasterShieldActive: StateFlow<Boolean> = SentryApp.isMasterShieldActive
    val isStudyModeActive: StateFlow<Boolean> = SentryApp.isStudyModeActive
    val isFahhModeActive: StateFlow<Boolean> = SentryApp.isFahhModeActive
    val isRagKorlaActive: StateFlow<Boolean> = SentryApp.isRagKorlaActive
    val focusRemainingSeconds: StateFlow<Int> = SentryApp.focusRemainingSeconds
    val interceptionOverlayEvent: StateFlow<InterceptionDetail?> = SentryApp.interceptionOverlayEvent
    val mindfulnessPauseApp: StateFlow<com.example.PauseAppDetail?> = SentryApp.mindfulnessPauseApp
    val phoneLockUntilTimestamp: StateFlow<Long> = SentryApp.phoneLockUntilTimestamp

    // Cloud Sync States
    private val _cloudSyncStatus = MutableStateFlow("Connected (Firebase)")
    val cloudSyncStatus = _cloudSyncStatus.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(0L)
    val lastSyncTimestamp = _lastSyncTimestamp.asStateFlow()

    private val _remoteAnnouncement = MutableStateFlow<String?>(null)
    val remoteAnnouncement = _remoteAnnouncement.asStateFlow()

    private val _isGoogleSignedIn = MutableStateFlow(false)
    val isGoogleSignedIn: StateFlow<Boolean> = _isGoogleSignedIn.asStateFlow()

    private val _isSignInBannerDismissed = MutableStateFlow(false)
    val isSignInBannerDismissed: StateFlow<Boolean> = _isSignInBannerDismissed.asStateFlow()

    val allBlockedEvents: StateFlow<List<BlockedEvent>> = repository.allBlockedEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppLimits: StateFlow<List<AppLimit>> = repository.allAppLimits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFocusSessions: StateFlow<List<FocusSession>> = repository.allFocusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val allFeatureItems: StateFlow<List<FeatureItem>> = repository.allFeatureItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBlockedUrls: StateFlow<List<BlockedUrl>> = repository.allBlockedUrls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBlockSchedules: StateFlow<List<BlockSchedule>> = repository.allBlockSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-Time Computed Metrics (Strictly Real Data, Zero Fake Placeholders)
    private val _realTotalScreenTimeMinutes = MutableStateFlow(0)
    val realTotalScreenTimeMinutes: StateFlow<Int> = _realTotalScreenTimeMinutes.asStateFlow()

    // Active Focus Session State
    private val _selectedDurationMinutes = MutableStateFlow(25)
    val selectedDurationMinutes: StateFlow<Int> = _selectedDurationMinutes.asStateFlow()

    private val _isStrictMode = MutableStateFlow(true)
    val isStrictMode: StateFlow<Boolean> = _isStrictMode.asStateFlow()

    private val _emergencyPassesRemaining = MutableStateFlow(2)
    val emergencyPassesRemaining: StateFlow<Int> = _emergencyPassesRemaining.asStateFlow()

    private val _showBypassChallengeDialog = MutableStateFlow(false)
    val showBypassChallengeDialog: StateFlow<Boolean> = _showBypassChallengeDialog.asStateFlow()

    private val _editingAppLimit = MutableStateFlow<AppLimit?>(null)
    val editingAppLimit: StateFlow<AppLimit?> = _editingAppLimit.asStateFlow()

    private val _showPermissionWizard = MutableStateFlow(false)
    val showPermissionWizard: StateFlow<Boolean> = _showPermissionWizard.asStateFlow()

    private var localTimerJob: Job? = null
    private var activeSessionStartTime = 0L

    init {
        scanAndSyncInstalledApps(application)
        refreshUsageStats(application)
        viewModelScope.launch {
            delay(1500)
            triggerCloudSync()
        }
    }

    fun toggleMasterShield() {
        val newState = !isMasterShieldActive.value
        SentryApp.setMasterShield(newState)
    }

    fun setDuration(minutes: Int) {
        _selectedDurationMinutes.value = minutes
    }

    fun toggleStrictMode() {
        _isStrictMode.value = !_isStrictMode.value
    }

    fun startFocusSession(context: Context, durationMinutes: Int = _selectedDurationMinutes.value) {
        activeSessionStartTime = System.currentTimeMillis()
        _selectedDurationMinutes.value = durationMinutes
        SentryForegroundService.startService(context, durationMinutes)

        localTimerJob?.cancel()
        localTimerJob = viewModelScope.launch {
            var seconds = durationMinutes * 60
            SentryApp.setStudyModeActive(true)
            SentryApp.setFocusRemainingSeconds(seconds)

            while (isActive && seconds > 0) {
                delay(1000)
                seconds--
                SentryApp.setFocusRemainingSeconds(seconds)
            }

            if (seconds <= 0) {
                onSessionCompleted(durationMinutes)
            }
        }
    }

    fun pauseOrResumeSession(context: Context) {
        if (isStudyModeActive.value) {
            if (_isStrictMode.value) {
                _showBypassChallengeDialog.value = true
            } else {
                stopFocusSession(context)
            }
        } else {
            startFocusSession(context, _selectedDurationMinutes.value)
        }
    }

    fun forceStopSession(context: Context) {
        stopFocusSession(context)
        _showBypassChallengeDialog.value = false
    }

    private fun stopFocusSession(context: Context) {
        localTimerJob?.cancel()
        SentryForegroundService.stopService(context)
        SentryApp.setStudyModeActive(false)
    }

    private fun onSessionCompleted(durationMinutes: Int) {
        SentryApp.setStudyModeActive(false)
        viewModelScope.launch {
            repository.logFocusSession(
                FocusSession(
                    startTime = activeSessionStartTime,
                    durationMinutes = durationMinutes,
                    completed = true,
                    strictMode = _isStrictMode.value,
                    blockedAttempts = 0,
                    title = "Deep Study Block"
                )
            )
            // Reward Karma XP
            val current = userProfile.value
            repository.updateUserProfile(
                current.copy(karmaXp = current.karmaXp + (durationMinutes / 5) * 5)
            )
        }
    }

    fun dismissBypassDialog() {
        _showBypassChallengeDialog.value = false
    }

    fun useEmergencyPass(context: Context) {
        if (_emergencyPassesRemaining.value > 0) {
            _emergencyPassesRemaining.value -= 1
            _showBypassChallengeDialog.value = false
            viewModelScope.launch {
                SentryApp.setStudyModeActive(false)
                delay(60_000)
                if (focusRemainingSeconds.value > 0) {
                    SentryApp.setStudyModeActive(true)
                }
            }
        }
    }

    fun testSimulateInterception(appName: String = "Instagram", feedType: String = "Instagram Reels") {
        viewModelScope.launch {
            val pkg = when (appName) {
                "Instagram" -> "com.instagram.android"
                "YouTube" -> "com.google.android.youtube"
                "TikTok" -> "com.zhiliaoapp.musically"
                "Facebook" -> "com.facebook.katana"
                else -> "com.example.distraction"
            }
            repository.logBlockedEvent(
                packageName = pkg,
                appName = appName,
                feedType = feedType,
                actionTaken = "Auto-Dismissed"
            )

            // Play sound if mod active
            if (isFahhModeActive.value) {
                SoundEffectsHelper.playFahhSound()
            } else if (isRagKorlaActive.value) {
                SoundEffectsHelper.playRagKorlaSound()
            }

            SentryApp.triggerInterception(
                InterceptionDetail(
                    appName = appName,
                    feedType = feedType,
                    reason = "Surgical Feed Blocker intercepted $feedType"
                )
            )

            // Reward Karma XP
            val current = userProfile.value
            repository.updateUserProfile(
                current.copy(karmaXp = current.karmaXp + 2)
            )
        }
    }

    fun dismissInterceptionOverlay() {
        SentryApp.clearInterception()
    }

    fun openAppLimitEditor(limit: AppLimit) {
        _editingAppLimit.value = limit
    }

    fun closeAppLimitEditor() {
        _editingAppLimit.value = null
    }

    fun saveAppLimit(limit: AppLimit) {
        viewModelScope.launch {
            repository.saveAppLimit(limit)
            _editingAppLimit.value = null
        }
    }

    fun toggleAppShorts(limit: AppLimit) {
        viewModelScope.launch {
            repository.updateAppLimit(limit.copy(isShortsBlocked = !limit.isShortsBlocked))
        }
    }

    fun toggleAppHardLock(limit: AppLimit) {
        viewModelScope.launch {
            repository.updateAppLimit(limit.copy(isHardLocked = !limit.isHardLocked))
        }
    }

    fun toggleAppEnabled(limit: AppLimit) {
        viewModelScope.launch {
            repository.updateAppLimit(limit.copy(isEnabled = !limit.isEnabled))
        }
    }

    fun toggleFeature(featureKey: String) {
        viewModelScope.launch {
            val list = allFeatureItems.value
            val item = list.find { it.key == featureKey } ?: return@launch
            val updated = item.copy(isEnabled = !item.isEnabled)
            repository.updateFeatureItem(updated)

            // Sync global state for special features
            when (featureKey) {
                "fahh_mode" -> SentryApp.setFahhModeActive(updated.isEnabled)
                "rag_korla" -> SentryApp.setRagKorlaActive(updated.isEnabled)
                "reels_block" -> SentryApp.setMasterShield(updated.isEnabled)
            }
        }
    }

    fun completeMindfulnessPause(packageName: String) {
        SentryApp.completeMindfulnessPause(packageName)
    }

    fun dismissMindfulnessPause() {
        SentryApp.clearMindfulnessPause()
    }

    fun setPhoneLockDuration(durationMinutes: Int) {
        SentryApp.setPhoneLockDuration(durationMinutes)
        viewModelScope.launch {
            val list = allFeatureItems.value
            val item = list.find { it.key == "lock_phone" }
            if (item != null && !item.isEnabled) {
                repository.updateFeatureItem(item.copy(isEnabled = true))
            }
        }
    }

    fun clearPhoneLock() {
        SentryApp.clearPhoneLock()
        viewModelScope.launch {
            val list = allFeatureItems.value
            val item = list.find { it.key == "lock_phone" }
            if (item != null && item.isEnabled) {
                repository.updateFeatureItem(item.copy(isEnabled = false))
            }
        }
    }

    fun saveMeFromReels() {
        viewModelScope.launch {
            SentryApp.setMasterShield(true)
            val list = allFeatureItems.value
            val reelsItem = list.find { it.key == "reels_block" }
            if (reelsItem != null && !reelsItem.isEnabled) {
                repository.updateFeatureItem(reelsItem.copy(isEnabled = true))
            }
            // Ensure all target apps have shorts blocked
            val appLimits = allAppLimits.value
            for (app in appLimits) {
                if (!app.isShortsBlocked) {
                    repository.updateAppLimit(app.copy(isShortsBlocked = true))
                }
            }
        }
    }

    fun updateUserProfile(name: String, username: String, email: String) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.updateUserProfile(
                current.copy(name = name, username = username, email = email)
            )
        }
    }

    fun performGoogleSignIn(accountName: String = "Md. Aj bahar", accountEmail: String = "mdajbahar200@gmail.com") {
        viewModelScope.launch {
            _isGoogleSignedIn.value = true
            _isSignInBannerDismissed.value = true
            val current = userProfile.value
            val cleanUsername = "@" + accountEmail.substringBefore("@").replace(".", "_")
            repository.updateUserProfile(
                current.copy(
                    name = accountName,
                    username = cleanUsername,
                    email = accountEmail
                )
            )
            triggerCloudSync()
        }
    }

    fun skipGoogleSignIn() {
        _isSignInBannerDismissed.value = true
    }

    fun signOutGoogle() {
        _isGoogleSignedIn.value = false
        _isSignInBannerDismissed.value = false
    }

    fun setTheme(themeId: String) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.updateUserProfile(
                current.copy(selectedThemeId = themeId)
            )
        }
    }

    fun toggleProAccount() {
        viewModelScope.launch {
            val current = userProfile.value
            val newPro = !current.isPro
            repository.updateUserProfile(
                current.copy(
                    isPro = newPro,
                    streakRank = if (newPro) "Master" else "Neutral"
                )
            )
        }
    }

    fun addBlockedUrl(domain: String) {
        viewModelScope.launch {
            val cleanDomain = domain.trim().removePrefix("https://").removePrefix("http://")
            if (cleanDomain.isNotEmpty()) {
                repository.addBlockedUrl(cleanDomain)
            }
        }
    }

    fun deleteBlockedUrl(id: Long) {
        viewModelScope.launch {
            repository.deleteBlockedUrl(id)
        }
    }

    fun saveBlockSchedule(schedule: BlockSchedule) {
        viewModelScope.launch {
            repository.saveBlockSchedule(schedule)
        }
    }

    fun playSoundTest(soundName: String) {
        if (soundName == "fahh") {
            SoundEffectsHelper.playFahhSound()
        } else if (soundName == "rag_korla") {
            SoundEffectsHelper.playRagKorlaSound()
        }
    }

    fun refreshUsageStats(context: Context) {
        viewModelScope.launch {
            val totalMinutes = UsageStatsHelper.getTotalDailyScreenTimeMinutes(context)
            _realTotalScreenTimeMinutes.value = totalMinutes

            val limits = allAppLimits.value
            for (limit in limits) {
                val realUsage = UsageStatsHelper.getDailyUsageMinutes(context, limit.packageName)
                if (realUsage != limit.currentUsageMinutes) {
                    repository.updateAppLimit(limit.copy(currentUsageMinutes = realUsage))
                }
            }
        }
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            _cloudSyncStatus.value = "Syncing with Cloud..."
            try {
                val profile = userProfile.value
                val totalBlocked = allBlockedEvents.value.size
                val shieldActive = isMasterShieldActive.value
                val result = (getApplication() as SentryApp).cloudSyncService.syncUserToCloud(
                    profile = profile,
                    totalBlockedEvents = totalBlocked,
                    isMasterShieldActive = shieldActive
                )

                _lastSyncTimestamp.value = System.currentTimeMillis()
                _cloudSyncStatus.value = if (result.success) "Synced (Cloud Active)" else "Sync Paused"
                
                result.remoteAnnouncement?.let { ann ->
                    _remoteAnnouncement.value = ann
                }

                // If admin granted or updated PRO from Web Dashboard, update local Room user profile
                if (result.isRemotePro != null && result.isRemotePro != profile.isPro) {
                    repository.updateUserProfile(profile.copy(isPro = result.isRemotePro))
                }

                // If admin modified feature PRO/Free tiers from Admin Dashboard, update Room features
                if (result.featureTiers.isNotEmpty()) {
                    val currentFeatures = allFeatureItems.value
                    for (feature in currentFeatures) {
                        val remoteTierIsPro = result.featureTiers[feature.key]
                        if (remoteTierIsPro != null && remoteTierIsPro != feature.isPro) {
                            repository.updateFeatureItem(feature.copy(isPro = remoteTierIsPro))
                        }
                    }
                }
            } catch (e: Exception) {
                _cloudSyncStatus.value = "Offline (Local Shield Active)"
            }
        }
    }

    fun getCloudDeviceId(): String {
        return (getApplication() as SentryApp).cloudSyncService.getDeviceId()
    }

    fun openPermissionWizard() {
        _showPermissionWizard.value = true
    }

    fun closePermissionWizard() {
        _showPermissionWizard.value = false
    }

    fun scanAndSyncInstalledApps(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val scannedApps = com.example.service.InstalledAppHelper.getInstalledSocialAndDistractingApps(context)
                val currentDbApps = allAppLimits.value.associateBy { it.packageName }

                for (scanned in scannedApps) {
                    val existing = currentDbApps[scanned.packageName]
                    if (existing == null) {
                        repository.saveAppLimit(scanned)
                    } else {
                        // Keep user configured settings, update appName / icon if missing
                        if (existing.appName.isBlank() || existing.appName == "Distracting App") {
                            repository.updateAppLimit(existing.copy(appName = scanned.appName, iconEmoji = scanned.iconEmoji, category = scanned.category))
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // Helper to get start of today timestamp
    fun getStartOfTodayMillis(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
