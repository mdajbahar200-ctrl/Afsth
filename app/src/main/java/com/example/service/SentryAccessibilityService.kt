package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.InterceptionDetail
import com.example.PauseAppDetail
import com.example.SentryApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SentryAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastInterceptTimestamp = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        // Ignore our own app package
        if (packageName == this.packageName) return

        val now = System.currentTimeMillis()

        // 1. UNINSTALL PROTECTION (Check even if master shield is toggled off)
        if (SentryApp.isFeatureEnabled("uninstall_protection")) {
            if (isSystemSettingsApp(packageName)) {
                if (checkUninstallAttempt(event, now)) {
                    return
                }
            }
        }

        // Master Shield Gate
        if (!SentryApp.isMasterShieldActive.value) return

        // Throttle rapid repeated triggers (min 800ms)
        if (now - lastInterceptTimestamp < 800) return

        // 2. PHONE LOCKDOWN DETOX MODE
        if (SentryApp.isPhoneCurrentlyLocked()) {
            if (isDistractingApp(packageName)) {
                lastInterceptTimestamp = now
                interceptAction(
                    packageName = packageName,
                    appName = getAppName(packageName),
                    feedType = "Detox Phone Lock",
                    reason = "Phone is currently in full digital detox lockdown mode"
                )
                return
            }
        }

        // 3. SCHEDULE BLOCKER
        if (SentryApp.isWithinScheduleBlock()) {
            if (isDistractingApp(packageName)) {
                lastInterceptTimestamp = now
                interceptAction(
                    packageName = packageName,
                    appName = getAppName(packageName),
                    feedType = "Scheduled Focus Hours",
                    reason = "App blocked by active Work & Study Schedule"
                )
                return
            }
        }

        // 4. STUDY / FOCUS SESSION LOCKDOWN
        if (SentryApp.isStudyModeActive.value && isDistractingApp(packageName)) {
            lastInterceptTimestamp = now
            interceptAction(
                packageName = packageName,
                appName = getAppName(packageName),
                feedType = "Focus Shield Lockdown",
                reason = "Active Focus Session locked down this app"
            )
            return
        }

        // 5. APP BLOCK (Complete Hard Lock) & DAILY APP LIMITS
        val limitItem = SentryApp.activeAppLimitsList.value.find { it.packageName == packageName }
        if (limitItem != null && limitItem.isEnabled) {
            val isAppBlockActive = SentryApp.isFeatureEnabled("app_block")
            if (limitItem.isHardLocked || isAppBlockActive) {
                lastInterceptTimestamp = now
                interceptAction(
                    packageName = packageName,
                    appName = limitItem.appName,
                    feedType = "App Block Hard Lock",
                    reason = "${limitItem.appName} is completely blocked by Hard Lock rule"
                )
                return
            }

            val isAppLimitsActive = SentryApp.isFeatureEnabled("app_limits")
            if (isAppLimitsActive && limitItem.dailyLimitMinutes > 0 && limitItem.currentUsageMinutes >= limitItem.dailyLimitMinutes) {
                lastInterceptTimestamp = now
                interceptAction(
                    packageName = packageName,
                    appName = limitItem.appName,
                    feedType = "Daily Limit Exceeded",
                    reason = "Daily limit of ${limitItem.dailyLimitMinutes} mins reached for ${limitItem.appName}"
                )
                return
            }
        }

        // 6. 5-SECOND PAUSE (Mindfulness Speedbump on launch)
        if (SentryApp.isFeatureEnabled("five_second_pause") && isDistractingApp(packageName)) {
            val pauseExpiry = SentryApp.passed5sPauseCache[packageName] ?: 0L
            if (now > pauseExpiry) {
                // If it's a new window state change (opening app)
                if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    lastInterceptTimestamp = now
                    SentryApp.triggerMindfulnessPause(
                        PauseAppDetail(
                            packageName = packageName,
                            appName = getAppName(packageName)
                        )
                    )
                    // Haptic buzz and detox bell sound
                    vibratePhone(100)
                    SoundEffectsHelper.playDetoxBellSound()
                    return
                }
            }
        }

        // 7. BROWSER PORN & WEBSITE BLOCKER
        if (isBrowserApp(packageName)) {
            if (checkBrowserContent(event, packageName, now)) {
                return
            }
        }

        // 8. SURGICAL SHORT-FORM REELS & SHORTS BLOCKING
        val isReelsBlockActive = SentryApp.isFeatureEnabled("reels_block")
        val isShortsEnabledForApp = limitItem?.isShortsBlocked ?: true

        if (isReelsBlockActive && isShortsEnabledForApp) {
            when (packageName) {
                "com.instagram.android" -> checkInstagram(event, now)
                "com.google.android.youtube" -> checkYouTube(event, now)
                "com.zhiliaoapp.musically", "com.ss.android.ugc.trill" -> checkTikTok(event, now)
                "com.facebook.katana" -> checkFacebook(event, now)
                "com.snapchat.android" -> checkSnapchat(event, now)
                "com.twitter.android" -> checkTwitter(event, now)
            }
        }

        // 9. SCROLL LIMIT (PRO)
        if (SentryApp.isFeatureEnabled("scroll_limit") && isDistractingApp(packageName)) {
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
                val currentCount = SentryApp.scrollCounterMap.getOrDefault(packageName, 0) + 1
                SentryApp.scrollCounterMap[packageName] = currentCount

                if (currentCount >= 15) {
                    SentryApp.scrollCounterMap[packageName] = 0
                    lastInterceptTimestamp = now
                    interceptAction(
                        packageName = packageName,
                        appName = getAppName(packageName),
                        feedType = "Scroll Limit Exceeded",
                        reason = "Exceeded 15 continuous scrolls! Take a mindful break."
                    )
                }
            }
        }
    }

    private fun checkUninstallAttempt(event: AccessibilityEvent, now: Long): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val textList = mutableListOf<String>()
        extractAllText(rootNode, textList)
        val combined = textList.joinToString(" ").lowercase()

        val isTargetingSentry = combined.contains("social sentry") ||
                combined.contains("com.aistudio") ||
                combined.contains("com.example") ||
                combined.contains("social addiction")

        val isDangerAction = combined.contains("uninstall") ||
                combined.contains("force stop") ||
                combined.contains("disable") ||
                combined.contains("clear data")

        if (isTargetingSentry && isDangerAction) {
            lastInterceptTimestamp = now
            interceptAction(
                packageName = "com.android.settings",
                appName = "Device Settings",
                feedType = "Uninstall Protection",
                reason = "Tamper protection prevented modifying or uninstalling Social Sentry"
            )
            return true
        }
        return false
    }

    private fun checkInstagram(event: AccessibilityEvent, now: Long) {
        val rootNode = rootInActiveWindow ?: return
        val className = event.className?.toString() ?: ""

        val isReelsActivity = className.contains("ClipsViewerActivity", ignoreCase = true) ||
                className.contains("ReelViewerActivity", ignoreCase = true) ||
                className.contains("ModalActivity", ignoreCase = true) && className.contains("clips", ignoreCase = true)

        var foundReelNode = isReelsActivity

        if (!foundReelNode) {
            val reelsIds = listOf(
                "com.instagram.android:id/clips_video_container",
                "com.instagram.android:id/clips_viewer_view_pager",
                "com.instagram.android:id/reel_viewer_tall_image_view",
                "com.instagram.android:id/clips_swipe_refresh_layout"
            )

            for (id in reelsIds) {
                if (rootNode.findAccessibilityNodeInfosByViewId(id).isNotEmpty()) {
                    foundReelNode = true
                    break
                }
            }
        }

        if (foundReelNode) {
            lastInterceptTimestamp = now
            interceptAction(
                packageName = "com.instagram.android",
                appName = getAppName("com.instagram.android"),
                feedType = "Instagram Reels",
                reason = "Surgical Feed Blocker intercepted short-form video"
            )
        }
    }

    private fun checkYouTube(event: AccessibilityEvent, now: Long) {
        val rootNode = rootInActiveWindow ?: return
        val className = event.className?.toString() ?: ""

        val isShortsClass = className.contains("ShortsPlayer", ignoreCase = true) ||
                className.contains("ReelWatchFragment", ignoreCase = true) ||
                className.contains("ShortsActivity", ignoreCase = true)

        var foundShorts = isShortsClass

        if (!foundShorts) {
            val shortsIds = listOf(
                "com.google.android.youtube:id/shorts_video_player",
                "com.google.android.youtube:id/reel_player_page_view",
                "com.google.android.youtube:id/shorts_container",
                "com.google.android.youtube:id/reel_recycler"
            )

            for (id in shortsIds) {
                if (rootNode.findAccessibilityNodeInfosByViewId(id).isNotEmpty()) {
                    foundShorts = true
                    break
                }
            }
        }

        if (foundShorts) {
            lastInterceptTimestamp = now
            interceptAction(
                packageName = "com.google.android.youtube",
                appName = getAppName("com.google.android.youtube"),
                feedType = "YouTube Shorts",
                reason = "Surgical Feed Blocker intercepted YouTube Shorts"
            )
        }
    }

    private fun checkTikTok(event: AccessibilityEvent, now: Long) {
        lastInterceptTimestamp = now
        interceptAction(
            packageName = "com.zhiliaoapp.musically",
            appName = getAppName("com.zhiliaoapp.musically"),
            feedType = "TikTok Feed",
            reason = "Short video feed is blocked during active shield"
        )
    }

    private fun checkFacebook(event: AccessibilityEvent, now: Long) {
        val rootNode = rootInActiveWindow ?: return
        val className = event.className?.toString() ?: ""

        val isFbReelsClass = className.contains("ReelsViewerActivity", ignoreCase = true) ||
                className.contains("FbReelsViewer", ignoreCase = true) ||
                className.contains("ClipsViewer", ignoreCase = true)

        var foundFbReels = isFbReelsClass

        if (!foundFbReels) {
            val fbIds = listOf(
                "com.facebook.katana:id/fb_reels_viewer",
                "com.facebook.katana:id/fb_reels_video_container",
                "com.facebook.katana:id/reel_video_view"
            )

            for (id in fbIds) {
                if (rootNode.findAccessibilityNodeInfosByViewId(id).isNotEmpty()) {
                    foundFbReels = true
                    break
                }
            }
        }

        if (foundFbReels) {
            lastInterceptTimestamp = now
            interceptAction(
                packageName = "com.facebook.katana",
                appName = getAppName("com.facebook.katana"),
                feedType = "Facebook Reels",
                reason = "Surgical Feed Blocker intercepted Facebook Reels"
            )
        }
    }

    private fun checkSnapchat(event: AccessibilityEvent, now: Long) {
        val rootNode = rootInActiveWindow ?: return
        val foundSpotlight = hasShortFormKeyword(rootNode, listOf("Spotlight", "Discover", "Spotlight & Discover"))

        if (foundSpotlight) {
            lastInterceptTimestamp = now
            interceptAction(
                packageName = "com.snapchat.android",
                appName = "Snapchat",
                feedType = "Snapchat Spotlight",
                reason = "Surgical Feed Blocker intercepted Spotlight"
            )
        }
    }

    private fun checkTwitter(event: AccessibilityEvent, now: Long) {
        val rootNode = rootInActiveWindow ?: return
        val foundMedia = hasShortFormKeyword(rootNode, listOf("Immersive Media", "For you video"))

        if (foundMedia) {
            lastInterceptTimestamp = now
            interceptAction(
                packageName = "com.twitter.android",
                appName = "X (Twitter)",
                feedType = "Video Feed",
                reason = "Endless video scroll intercepted"
            )
        }
    }

    private fun checkBrowserContent(event: AccessibilityEvent, packageName: String, now: Long): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val textList = mutableListOf<String>()
        extractAllText(rootNode, textList)
        val combinedText = textList.joinToString(" ").lowercase()

        // 1. Check custom blocked URLs if Website Blocker is active
        if (SentryApp.isFeatureEnabled("website_block")) {
            val blockedUrls = SentryApp.activeBlockedUrlsSet
            for (domain in blockedUrls) {
                if (domain.isNotBlank() && combinedText.contains(domain)) {
                    lastInterceptTimestamp = now
                    interceptAction(
                        packageName = packageName,
                        appName = getAppName(packageName),
                        feedType = "Website Blocker ($domain)",
                        reason = "Custom blocked domain '$domain' detected in browser"
                    )
                    return true
                }
            }
        }

        // 2. Check adult content filter if Porn Blocker is active
        if (SentryApp.isFeatureEnabled("porn_block")) {
            val adultKeywords = listOf(
                "porn", "xxx", "xvideos", "pornhub", "xnxx", "adult content",
                "nsfw", "onlyfans", "redtube", "youporn", "erotic", "sex videos",
                "hentai", "chaturbate", "stripchat", "camster", "rule34"
            )
            for (kw in adultKeywords) {
                if (combinedText.contains(kw)) {
                    lastInterceptTimestamp = now
                    interceptAction(
                        packageName = packageName,
                        appName = getAppName(packageName),
                        feedType = "Adult Content Filter",
                        reason = "Porn Blocker intercepted explicit content: $kw"
                    )
                    return true
                }
            }
        }

        return false
    }

    private fun extractAllText(node: AccessibilityNodeInfo?, list: MutableList<String>) {
        if (node == null) return
        node.text?.let { list.add(it.toString()) }
        node.contentDescription?.let { list.add(it.toString()) }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                extractAllText(child, list)
                child.recycle()
            }
        }
    }

    private fun hasShortFormKeyword(node: AccessibilityNodeInfo?, keywords: List<String>): Boolean {
        if (node == null) return false
        val text = node.text?.toString() ?: ""
        val desc = node.contentDescription?.toString() ?: ""

        for (kw in keywords) {
            if (text.contains(kw, ignoreCase = true) || desc.contains(kw, ignoreCase = true)) {
                return true
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                if (hasShortFormKeyword(child, keywords)) {
                    child.recycle()
                    return true
                }
                child.recycle()
            }
        }
        return false
    }

    private fun interceptAction(packageName: String, appName: String, feedType: String, reason: String) {
        // Haptic feedback
        vibratePhone(150)

        // Sound effect according to active mods
        if (SentryApp.isFahhModeActive.value) {
            SoundEffectsHelper.playFahhSound()
        } else if (SentryApp.isRagKorlaActive.value) {
            SoundEffectsHelper.playRagKorlaSound()
        } else {
            SoundEffectsHelper.playSirenSound()
        }

        // Kick back programmatically to break doomscroll loop
        performGlobalAction(GLOBAL_ACTION_BACK)

        // Log into database
        serviceScope.launch {
            try {
                SentryApp.instance.repository.logBlockedEvent(
                    packageName = packageName,
                    appName = appName,
                    feedType = feedType,
                    actionTaken = "Auto-Dismissed"
                )
            } catch (_: Exception) {}
        }

        // Notify UI state
        SentryApp.triggerInterception(
            InterceptionDetail(
                appName = appName,
                feedType = feedType,
                reason = reason
            )
        )
    }

    private fun vibratePhone(durationMillis: Long = 120) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMillis)
                }
            }
        } catch (_: Exception) {}
    }

    private fun isBrowserApp(pkg: String): Boolean {
        return pkg in listOf(
            "com.android.chrome",
            "com.brave.browser",
            "org.mozilla.firefox",
            "com.microsoft.emmx",
            "com.opera.browser",
            "com.sec.android.app.sbrowser",
            "com.duckduckgo.mobile.android",
            "com.vivaldi.browser",
            "com.kiwibrowser.browser"
        )
    }

    private fun isSystemSettingsApp(pkg: String): Boolean {
        return pkg in listOf(
            "com.android.settings",
            "com.google.android.packageinstaller",
            "com.android.packageinstaller"
        )
    }

    private fun isDistractingApp(pkg: String): Boolean {
        // First check if user configured this app in active limits
        if (SentryApp.activeAppLimitsList.value.any { it.packageName == pkg && it.isEnabled }) {
            return true
        }

        return pkg in listOf(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.facebook.katana",
            "com.facebook.orca",
            "com.twitter.android",
            "com.snapchat.android",
            "com.reddit.frontpage",
            "com.pinterest",
            "com.netflix.mediaclient",
            "com.instagram.barcelona"
        )
    }

    private fun getAppName(pkg: String): String {
        // 1. Check in custom limits list
        val customApp = SentryApp.activeAppLimitsList.value.find { it.packageName == pkg }
        if (customApp != null && customApp.appName.isNotBlank()) {
            return customApp.appName
        }

        // 2. Query PackageManager for real installed name
        try {
            val pm = applicationContext.packageManager
            val info = pm.getApplicationInfo(pkg, 0)
            val label = pm.getApplicationLabel(info).toString()
            if (label.isNotBlank()) return label
        } catch (_: Exception) {}

        // 3. Fallback to known list
        return when (pkg) {
            "com.instagram.android" -> "Instagram"
            "com.google.android.youtube" -> "YouTube"
            "com.zhiliaoapp.musically", "com.ss.android.ugc.trill" -> "TikTok"
            "com.facebook.katana" -> "Facebook"
            "com.facebook.orca" -> "Messenger"
            "com.twitter.android" -> "X (Twitter)"
            "com.snapchat.android" -> "Snapchat"
            "com.reddit.frontpage" -> "Reddit"
            "com.pinterest" -> "Pinterest"
            "com.netflix.mediaclient" -> "Netflix"
            "com.instagram.barcelona" -> "Threads"
            "com.android.settings" -> "Device Settings"
            else -> "Social Media App"
        }
    }

    override fun onInterrupt() {}
}
