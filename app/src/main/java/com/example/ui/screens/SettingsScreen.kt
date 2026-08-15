package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLimit
import com.example.service.SentryAccessibilityService
import com.example.service.UsageStatsHelper
import com.example.ui.SentryViewModel
import com.example.ui.theme.SentryBrandRed
import com.example.ui.theme.SentryDarkCanvas
import com.example.ui.theme.SentryDarkCard
import com.example.ui.theme.SentryDarkCardBorder
import com.example.ui.theme.SentryDarkTextMain
import com.example.ui.theme.SentryDarkTextMuted
import com.example.ui.theme.SentryGoldCTA
import com.example.ui.theme.SentrySuccessGreen

@Composable
fun SettingsScreen(
    viewModel: SentryViewModel
) {
    val context = LocalContext.current
    val appLimits by viewModel.allAppLimits.collectAsState()
    val isMasterActive by viewModel.isMasterShieldActive.collectAsState()

    var hasUsagePermission by remember { mutableStateOf(UsageStatsHelper.hasUsageStatsPermission(context)) }
    var hasOverlayPermission by remember { mutableStateOf(UsageStatsHelper.hasOverlayPermission(context)) }
    var hasAccessibilityPermission by remember {
        mutableStateOf(UsageStatsHelper.isAccessibilityServiceEnabled(context, SentryAccessibilityService::class.java))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SentryDarkCanvas)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "SETTINGS & PERMISSIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SentryBrandRed,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "System Diagnostics",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SentryDarkTextMain
                )
            }
        }

        // Master Shield Toggle Banner
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SentryDarkCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SentryDarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF262930),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Shield",
                                    tint = SentrySuccessGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Master Protection Shield",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SentryDarkTextMain
                            )
                            Text(
                                text = if (isMasterActive) "Active & guarding feeds" else "Temporarily disabled",
                                fontSize = 11.sp,
                                color = if (isMasterActive) SentrySuccessGreen else SentryDarkTextMuted
                            )
                        }
                    }

                    Switch(
                        checked = isMasterActive,
                        onCheckedChange = { viewModel.toggleMasterShield() },
                        colors = sentrySwitchColors()
                    )
                }
            }
        }

        // Required Permissions Status Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SentryDarkCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SentryDarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Core Protection Permissions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SentryDarkTextMain
                    )

                    PermissionRow(
                        title = "Accessibility Service",
                        subtitle = "Detects & bounces short-form video nodes",
                        isGranted = hasAccessibilityPermission,
                        onClick = { UsageStatsHelper.openAccessibilitySettings(context) }
                    )

                    PermissionRow(
                        title = "Usage Access",
                        subtitle = "Calculates foreground screen time limits",
                        isGranted = hasUsagePermission,
                        onClick = { UsageStatsHelper.openUsageAccessSettings(context) }
                    )

                    PermissionRow(
                        title = "Display Over Other Apps",
                        subtitle = "Renders focus lock screen & speedbumps",
                        isGranted = hasOverlayPermission,
                        onClick = { UsageStatsHelper.openOverlaySettings(context) }
                    )
                }
            }
        }

        // Granular Surgical Feed Controls
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SentryDarkCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SentryDarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Surgical Feed Interception Rules",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SentryDarkTextMain
                    )

                    appLimits.forEach { limit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openAppLimitEditor(limit) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(limit.iconEmoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${limit.appName} Shorts/Reels",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SentryDarkTextMain
                                    )
                                    Text(
                                        text = if (limit.isShortsBlocked) "Block & Exit Feed" else "Allowed",
                                        fontSize = 11.sp,
                                        color = if (limit.isShortsBlocked) SentryGoldCTA else SentryDarkTextMuted
                                    )
                                }
                            }

                            Switch(
                                checked = limit.isShortsBlocked,
                                onCheckedChange = { viewModel.toggleAppShorts(limit) },
                                colors = sentrySwitchColors()
                            )
                        }
                    }
                }
            }
        }

        // Interception Speedbump Preview
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SentryDarkCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SentryDarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Interactive Interception Speedbump",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SentryDarkTextMain
                    )
                    Text(
                        text = "Preview the mindful breathing circle overlay shown to users upon triggering a reel.",
                        fontSize = 11.sp,
                        color = SentryDarkTextMuted
                    )
                    Button(
                        onClick = { viewModel.testSimulateInterception("YouTube", "YouTube Shorts") },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SentryGoldCTA,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Preview Speedbump Overlay", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PermissionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = SentryDarkTextMain
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = SentryDarkTextMuted
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isGranted) SentrySuccessGreen.copy(alpha = 0.15f) else Color(0xFF2E1A23),
            modifier = Modifier.clickable { onClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = if (isGranted) SentrySuccessGreen else SentryBrandRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isGranted) "Enabled" else "Enable",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isGranted) SentrySuccessGreen else SentryBrandRed
                )
            }
        }
    }
}
