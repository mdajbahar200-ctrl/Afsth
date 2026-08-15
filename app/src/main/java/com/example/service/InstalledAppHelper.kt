package com.example.service

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.data.model.AppLimit

object InstalledAppHelper {

    private val KNOWN_SOCIAL_PACKAGES = mapOf(
        "com.google.android.youtube" to Triple("YouTube", "▶️", "Entertainment"),
        "com.instagram.android" to Triple("Instagram", "📸", "Social Media"),
        "com.facebook.katana" to Triple("Facebook", "👥", "Social Network"),
        "com.facebook.orca" to Triple("Messenger", "💬", "Communication"),
        "com.zhiliaoapp.musically" to Triple("TikTok", "🎵", "Short Video"),
        "com.ss.android.ugc.trill" to Triple("TikTok", "🎵", "Short Video"),
        "com.twitter.android" to Triple("X (Twitter)", "🐦", "Social Media"),
        "com.snapchat.android" to Triple("Snapchat", "👻", "Social Media"),
        "com.reddit.frontpage" to Triple("Reddit", "🤖", "Forum & Feeds"),
        "com.pinterest" to Triple("Pinterest", "📌", "Lifestyle"),
        "com.whatsapp" to Triple("WhatsApp", "💬", "Communication"),
        "org.telegram.messenger" to Triple("Telegram", "✈️", "Communication"),
        "com.netflix.mediaclient" to Triple("Netflix", "🎬", "Entertainment"),
        "com.discord" to Triple("Discord", "🎮", "Gaming & Community"),
        "com.linkedin.android" to Triple("LinkedIn", "💼", "Professional"),
        "com.instagram.barcelona" to Triple("Threads", "🧵", "Social Media"),
        "com.google.android.apps.youtube.music" to Triple("YT Music", "🎧", "Music"),
        "com.spotify.music" to Triple("Spotify", "🎵", "Music"),
        "com.android.chrome" to Triple("Chrome", "🌐", "Browser"),
        "org.mozilla.firefox" to Triple("Firefox", "🦊", "Browser"),
        "com.brave.browser" to Triple("Brave", "🦁", "Browser")
    )

    fun getInstalledSocialAndDistractingApps(context: Context): List<AppLimit> {
        val pm = context.packageManager
        val resultList = mutableListOf<AppLimit>()
        val foundPackageNames = mutableSetOf<String>()

        try {
            // 1. Query launcher apps
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

            for (info in resolveInfos) {
                val pkg = info.activityInfo.packageName
                if (pkg == context.packageName) continue // Skip our own app

                if (isSocialOrDistractingPackage(pkg, info.activityInfo.applicationInfo, pm)) {
                    val appName = info.loadLabel(pm).toString()
                    val meta = KNOWN_SOCIAL_PACKAGES[pkg]
                    val emoji = meta?.second ?: getEmojiForCategory(appName, pkg)
                    val category = meta?.third ?: "Social & Media"

                    resultList.add(
                        AppLimit(
                            packageName = pkg,
                            appName = appName,
                            dailyLimitMinutes = 30,
                            currentUsageMinutes = 0,
                            isShortsBlocked = isShortsSupported(pkg),
                            isHardLocked = false,
                            isEnabled = true,
                            iconEmoji = emoji,
                            category = category
                        )
                    )
                    foundPackageNames.add(pkg)
                }
            }
        } catch (_: Exception) {}

        // 2. Ensure prominent social apps are present in fallback list if not installed
        for ((pkg, triple) in KNOWN_SOCIAL_PACKAGES) {
            if (!foundPackageNames.contains(pkg) && isPopularDistraction(pkg)) {
                resultList.add(
                    AppLimit(
                        packageName = pkg,
                        appName = triple.first,
                        dailyLimitMinutes = 30,
                        currentUsageMinutes = 0,
                        isShortsBlocked = isShortsSupported(pkg),
                        isHardLocked = false,
                        isEnabled = true,
                        iconEmoji = triple.second,
                        category = triple.third
                    )
                )
                foundPackageNames.add(pkg)
            }
        }

        return resultList
    }

    private fun isSocialOrDistractingPackage(pkg: String, appInfo: ApplicationInfo, pm: PackageManager): Boolean {
        if (KNOWN_SOCIAL_PACKAGES.containsKey(pkg)) return true

        // Filter system apps unless they are known social apps
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        if (isSystem) return false

        val lower = pkg.lowercase()
        return lower.contains("video") ||
                lower.contains("social") ||
                lower.contains("game") ||
                lower.contains("media") ||
                lower.contains("stream") ||
                lower.contains("chat") ||
                lower.contains("tube")
    }

    private fun isShortsSupported(pkg: String): Boolean {
        return pkg in listOf(
            "com.google.android.youtube",
            "com.instagram.android",
            "com.facebook.katana",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.snapchat.android",
            "com.twitter.android"
        )
    }

    private fun isPopularDistraction(pkg: String): Boolean {
        return pkg in listOf(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.facebook.katana",
            "com.zhiliaoapp.musically",
            "com.twitter.android",
            "com.snapchat.android",
            "com.reddit.frontpage"
        )
    }

    private fun getEmojiForCategory(appName: String, pkg: String): String {
        val lower = (appName + pkg).lowercase()
        return when {
            lower.contains("video") || lower.contains("tube") || lower.contains("movie") -> "🎬"
            lower.contains("game") -> "🎮"
            lower.contains("photo") || lower.contains("cam") -> "📸"
            lower.contains("chat") || lower.contains("msg") || lower.contains("talk") -> "💬"
            lower.contains("music") || lower.contains("sound") || lower.contains("audio") -> "🎵"
            lower.contains("browser") || lower.contains("web") -> "🌐"
            else -> "📱"
        }
    }
}
