package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.service.SentryAccessibilityService
import com.example.service.UsageStatsHelper
import com.example.ui.theme.SentryTheme

data class PermissionStep(
    val id: String,
    val stepNumber: Int,
    val title: String,
    val titleBn: String,
    val description: String,
    val descriptionBn: String,
    val icon: ImageVector,
    val isGranted: Boolean,
    val isCritical: Boolean,
    val onGrantClick: (Context) -> Unit
)

@Composable
fun PermissionWizardDialog(
    onDismiss: () -> Unit,
    onAllGranted: () -> Unit = {}
) {
    val context = LocalContext.current
    val theme = SentryTheme

    var hasAccessibility by remember { mutableStateOf(false) }
    var hasUsageStats by remember { mutableStateOf(false) }
    var hasOverlay by remember { mutableStateOf(false) }
    var hasBatteryOpt by remember { mutableStateOf(false) }
    var hasNotification by remember { mutableStateOf(false) }

    fun refreshPermissions() {
        hasAccessibility = UsageStatsHelper.isAccessibilityServiceEnabled(context, SentryAccessibilityService::class.java)
        hasUsageStats = UsageStatsHelper.hasUsageStatsPermission(context)
        hasOverlay = UsageStatsHelper.hasOverlayPermission(context)
        hasBatteryOpt = UsageStatsHelper.isIgnoringBatteryOptimizations(context)
        hasNotification = UsageStatsHelper.isNotificationAccessGranted(context)
    }

    LaunchedEffect(Unit) {
        refreshPermissions()
    }

    val steps = listOf(
        PermissionStep(
            id = "accessibility",
            stepNumber = 1,
            title = "Accessibility Service",
            titleBn = "১. অ্যাক্সেসিবিলিটি সার্ভিস (সবচেয়ে গুরুত্বপূর্ণ)",
            description = "Intercepts YouTube Shorts, Instagram Reels & TikTok feeds directly inside apps in real time.",
            descriptionBn = "ইউটিউব শর্টস, ফেসবুক রিলস ও ইনস্টাগ্রাম রিলস সরাসরি শনাক্ত ও বন্ধ করার জন্য এটি আবশ্যক।",
            icon = Icons.Default.AccessibilityNew,
            isGranted = hasAccessibility,
            isCritical = true,
            onGrantClick = { UsageStatsHelper.openAccessibilitySettings(it) }
        ),
        PermissionStep(
            id = "usage_stats",
            stepNumber = 2,
            title = "Usage Access",
            titleBn = "২. ব্যবহারের পরিসংখ্যান (Usage Access)",
            description = "Required to calculate genuine daily app screen time and enforce daily time limits accurately.",
            descriptionBn = "ফোনের সোশ্যাল মিডিয়ার সঠিক দৈনিক সময় ট্র্যাক ও দৈনিক অ্যাপ লিমিট কার্যকর করার জন্য প্রয়োজন।",
            icon = Icons.Default.DataUsage,
            isGranted = hasUsageStats,
            isCritical = true,
            onGrantClick = { UsageStatsHelper.openUsageAccessSettings(it) }
        ),
        PermissionStep(
            id = "overlay",
            stepNumber = 3,
            title = "Display Over Other Apps",
            titleBn = "৩. অন্যান্য অ্যাপের উপর প্রদর্শন (Overlay)",
            description = "Allows Social Sentry to display full-screen focus lockdown, mindfulness pause & speedbump.",
            descriptionBn = "ব্লক করার সময় ৫-সেকেন্ড স্পিডবাম্প, মাইন্ডফুলনেস পজ এবং লকডাউন স্ক্রিন প্রদর্শনের জন্য।",
            icon = Icons.Default.Layers,
            isGranted = hasOverlay,
            isCritical = true,
            onGrantClick = { UsageStatsHelper.openOverlaySettings(it) }
        ),
        PermissionStep(
            id = "battery",
            stepNumber = 4,
            title = "Battery Optimization Exemption",
            titleBn = "৪. ব্যাটারি অপ্টিমাইজেশন নিষ্ক্রিয়",
            description = "Prevents Android OS from killing the background detox protection service when apps are closed.",
            descriptionBn = "ব্যাকগ্রাউন্ডে ব্লকার যেন নিজে নিজে বন্ধ না হয়ে যায় তা নিশ্চিত করতে অপ্টিমাইজেশন বন্ধ রাখুন।",
            icon = Icons.Default.BatteryAlert,
            isGranted = hasBatteryOpt,
            isCritical = false,
            onGrantClick = { UsageStatsHelper.openBatteryOptimizationSettings(it) }
        ),
        PermissionStep(
            id = "notifications",
            stepNumber = 5,
            title = "Notification Access",
            titleBn = "৫. নোটিফিকেশন ফিল্টার ও অ্যালার্ট",
            description = "Allows deep study focus sessions to silence distracting push notifications.",
            descriptionBn = "পড়ার বা কাজের সময় সোশ্যাল মিডিয়ার ডেসট্রাক্টিং নোটিফিকেশন ফিল্টার করার জন্য।",
            icon = Icons.Default.NotificationsActive,
            isGranted = hasNotification,
            isCritical = false,
            onGrantClick = { UsageStatsHelper.openNotificationAccessSettings(it) }
        )
    )

    val grantedCount = steps.count { it.isGranted }
    val totalCount = steps.size
    val progress = grantedCount.toFloat() / totalCount.toFloat()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, theme.cardBorder, RoundedCornerShape(24.dp)),
            color = theme.card
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(theme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = theme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "পারমিশন সেটআপ গাইড",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textMain
                            )
                            Text(
                                text = "Permission Setup Guide ($grantedCount/$totalCount Granted)",
                                fontSize = 12.sp,
                                color = theme.textMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.textMuted
                        )
                    }
                }

                // Progress Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.cardSecondary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "প্রয়োজনীয় পারমিশন স্ট্যাটাস",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textMain
                            )
                            Text(
                                text = "${(progress * 100).toInt()}% কমপ্লিট",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (progress >= 0.8f) Color(0xFF10B981) else theme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = if (progress >= 0.8f) Color(0xFF10B981) else theme.primary,
                            trackColor = theme.cardBorder
                        )
                    }
                }

                // Step-by-Step Cards
                steps.forEach { step ->
                    PermissionStepCard(
                        step = step,
                        onClick = {
                            step.onGrantClick(context)
                            refreshPermissions()
                        }
                    )
                }

                // Refresh Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { refreshPermissions() },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.cardSecondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = theme.textMain,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("পুনরায় চেক করুন", color = theme.textMain, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            refreshPermissions()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (grantedCount == totalCount) "সব সম্পন্ন (Done)" else "সম্পন্ন (Close)",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionStepCard(
    step: PermissionStep,
    onClick: () -> Unit
) {
    val theme = SentryTheme
    val isGranted = step.isGranted

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) theme.cardSecondary.copy(alpha = 0.6f) else theme.cardSecondary
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isGranted) Color(0xFF10B981).copy(alpha = 0.4f) else theme.primary.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGranted) Color(0xFF10B981).copy(alpha = 0.2f)
                                else theme.primary.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = null,
                            tint = if (isGranted) Color(0xFF10B981) else theme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = step.titleBn,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textMain
                        )
                        Text(
                            text = step.title,
                            fontSize = 11.sp,
                            color = theme.textMuted
                        )
                    }
                }

                // Granted Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isGranted) Color(0xFF10B981).copy(alpha = 0.15f)
                            else Color(0xFFF59E0B).copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isGranted) Color(0xFF10B981) else Color(0xFFF59E0B),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isGranted) "অ্যাক্টিভ" else "প্রয়োজন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGranted) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }
            }

            Text(
                text = step.descriptionBn,
                fontSize = 12.sp,
                color = theme.textMain.copy(alpha = 0.9f),
                lineHeight = 16.sp
            )

            if (!isGranted) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "অনুমতি দিন (Enable Now)",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
