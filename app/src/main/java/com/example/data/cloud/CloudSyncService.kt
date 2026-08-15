package com.example.data.cloud

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CloudSyncService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val projectId = "social-sentry-78d1a"
    private val apiKey = "AIzaSyCvNHhfp6bCdhykxy1v3sSygaPcvW8i5Dk"
    private val firestoreBaseUrl = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"

    fun getDeviceId(): String {
        return try {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            if (!androidId.isNullOrBlank()) "dev_$androidId" else "dev_sentry_user"
        } catch (_: Exception) {
            "dev_sentry_user"
        }
    }

    suspend fun syncUserToCloud(
        profile: UserProfile,
        totalBlockedEvents: Int,
        isMasterShieldActive: Boolean
    ): CloudSyncResult = withContext(Dispatchers.IO) {
        val deviceId = getDeviceId()
        val docUrl = "$firestoreBaseUrl/users/$deviceId?key=$apiKey"
        val configUrl = "$firestoreBaseUrl/app_config/features?key=$apiKey"

        try {
            val jsonBody = JSONObject().apply {
                val fields = JSONObject().apply {
                    put("deviceId", JSONObject().put("stringValue", deviceId))
                    put("name", JSONObject().put("stringValue", profile.name))
                    put("username", JSONObject().put("stringValue", profile.username))
                    put("email", JSONObject().put("stringValue", profile.email))
                    put("streakDays", JSONObject().put("integerValue", profile.streakDays.toString()))
                    put("karmaXp", JSONObject().put("integerValue", profile.karmaXp.toString()))
                    put("streakRank", JSONObject().put("stringValue", profile.streakRank))
                    put("isPro", JSONObject().put("booleanValue", profile.isPro))
                    put("totalBlockedCount", JSONObject().put("integerValue", totalBlockedEvents.toString()))
                    put("isMasterShieldActive", JSONObject().put("booleanValue", isMasterShieldActive))
                    put("deviceModel", JSONObject().put("stringValue", "${Build.MANUFACTURER} ${Build.MODEL}"))
                    put("androidVersion", JSONObject().put("stringValue", "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"))
                    put("appVersion", JSONObject().put("stringValue", "1.0-Release"))
                    put("lastActiveTimestamp", JSONObject().put("integerValue", System.currentTimeMillis().toString()))
                }
                put("fields", fields)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            
            // Try PATCH (create or update document with fields mask)
            val patchUrl = "$docUrl&updateMask.fieldPaths=name&updateMask.fieldPaths=username&updateMask.fieldPaths=email&updateMask.fieldPaths=streakDays&updateMask.fieldPaths=karmaXp&updateMask.fieldPaths=streakRank&updateMask.fieldPaths=totalBlockedCount&updateMask.fieldPaths=isMasterShieldActive&updateMask.fieldPaths=deviceModel&updateMask.fieldPaths=androidVersion&updateMask.fieldPaths=appVersion&updateMask.fieldPaths=lastActiveTimestamp"
            
            val patchRequest = Request.Builder()
                .url(patchUrl)
                .patch(requestBody)
                .build()

            val patchResponse = client.newCall(patchRequest).execute()
            var remotePro: Boolean? = null
            var remoteMessage: String? = null

            if (patchResponse.isSuccessful) {
                val responseString = patchResponse.body?.string() ?: ""
                remotePro = parseProStatus(responseString)
                remoteMessage = parseRemoteAnnouncement(responseString)
            } else if (patchResponse.code == 404) {
                val putRequest = Request.Builder().url(docUrl).post(requestBody).build()
                val putResponse = client.newCall(putRequest).execute()
                val responseString = putResponse.body?.string() ?: ""
                remotePro = parseProStatus(responseString)
            }

            // Fetch Global Feature Tiers and Announcements set by Admin
            val featureTiers = fetchRemoteFeatureTiers(configUrl)
            val globalNotice = fetchGlobalNotice(configUrl)

            val finalAnnouncement = if (!remoteMessage.isNullOrBlank()) remoteMessage else globalNotice

            return@withContext CloudSyncResult(
                success = true,
                isRemotePro = remotePro,
                remoteAnnouncement = finalAnnouncement,
                featureTiers = featureTiers,
                message = "Synced successfully with Firebase"
            )
        } catch (e: Exception) {
            Log.e("CloudSyncService", "Sync error", e)
            return@withContext CloudSyncResult(
                success = false,
                isRemotePro = profile.isPro,
                message = e.localizedMessage ?: "Network connection error"
            )
        }
    }

    private fun fetchRemoteFeatureTiers(configUrl: String): Map<String, Boolean> {
        return try {
            val request = Request.Builder().url(configUrl).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return emptyMap()
                val json = JSONObject(body)
                val fields = json.optJSONObject("fields") ?: return emptyMap()
                val map = mutableMapOf<String, Boolean>()
                
                val keys = fields.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (key.startsWith("pro_")) {
                        val featureKey = key.removePrefix("pro_")
                        val boolObj = fields.optJSONObject(key)
                        if (boolObj != null) {
                            map[featureKey] = boolObj.optBoolean("booleanValue", false)
                        }
                    }
                }
                map
            } else {
                emptyMap()
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun fetchGlobalNotice(configUrl: String): String? {
        return try {
            val request = Request.Builder().url(configUrl).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val json = JSONObject(body)
                val fields = json.optJSONObject("fields") ?: return null
                val noticeObj = fields.optJSONObject("global_announcement") ?: return null
                noticeObj.optString("stringValue")
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun checkSubscriptionStatus(): CloudSyncResult = withContext(Dispatchers.IO) {
        val deviceId = getDeviceId()
        val docUrl = "$firestoreBaseUrl/users/$deviceId?key=$apiKey"
        try {
            val request = Request.Builder().url(docUrl).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val isPro = parseProStatus(body)
                val announcement = parseRemoteAnnouncement(body)
                return@withContext CloudSyncResult(
                    success = true,
                    isRemotePro = isPro,
                    remoteAnnouncement = announcement,
                    message = "Subscription verified from Cloud"
                )
            }
            return@withContext CloudSyncResult(success = false, isRemotePro = null, message = "Could not fetch remote status")
        } catch (e: Exception) {
            return@withContext CloudSyncResult(success = false, isRemotePro = null, message = e.localizedMessage ?: "Sync error")
        }
    }

    private fun parseProStatus(jsonStr: String): Boolean? {
        return try {
            val json = JSONObject(jsonStr)
            val fields = json.optJSONObject("fields") ?: return null
            val isProObj = fields.optJSONObject("isPro") ?: return null
            isProObj.optBoolean("booleanValue")
        } catch (_: Exception) {
            null
        }
    }

    private fun parseRemoteAnnouncement(jsonStr: String): String? {
        return try {
            val json = JSONObject(jsonStr)
            val fields = json.optJSONObject("fields") ?: return null
            val annObj = fields.optJSONObject("adminAnnouncement") ?: return null
            annObj.optString("stringValue")
        } catch (_: Exception) {
            null
        }
    }
}

data class CloudSyncResult(
    val success: Boolean,
    val isRemotePro: Boolean?,
    val remoteAnnouncement: String? = null,
    val featureTiers: Map<String, Boolean> = emptyMap(),
    val message: String
)
