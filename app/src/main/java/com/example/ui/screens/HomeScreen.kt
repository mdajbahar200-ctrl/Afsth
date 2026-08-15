package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SentryViewModel
import com.example.ui.theme.SentryTheme

@Composable
fun HomeScreen(
    viewModel: SentryViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToReelsBlock: () -> Unit,
    onNavigateToActiveFeatures: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val theme = SentryTheme
    val isMasterActive by viewModel.isMasterShieldActive.collectAsState()
    val allEvents by viewModel.allBlockedEvents.collectAsState()
    val allFeatures by viewModel.allFeatureItems.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val realTotalScreenTime by viewModel.realTotalScreenTimeMinutes.collectAsState()

    val startOfToday = viewModel.getStartOfTodayMillis()
    val todayBlockedEvents = allEvents.filter { it.timestamp >= startOfToday }
    val todayBlockedCount = todayBlockedEvents.size

    val enabledFeaturesCount = allFeatures.count { it.isEnabled }
    val totalFeaturesCount = 14

    var showTutorialDialog by remember { mutableStateOf(false) }

    // Pulse animation for radar
    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_spin"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.canvas)
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hamburger Menu
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Menu",
                    tint = theme.textMain,
                    modifier = Modifier.size(26.dp)
                )
            }

            // "Social Addiction" Brand
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Social ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primary
                )
                Text(
                    text = "Addiction",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMain
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fire Streak Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.card)
                        .border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = theme.themeIcon, fontSize = 13.sp)
                    Text(
                        text = "${userProfile.streakDays}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMain
                    )
                }

                // Profile Avatar Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(theme.accent)
                        .clickable { onNavigateToProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userProfile.name.take(1).uppercase(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (theme.accent.isLight()) Color.Black else Color.White
                    )
                }
            }
        }

        // Body Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsState()
            val isBannerDismissed by viewModel.isSignInBannerDismissed.collectAsState()

            // Google Sign-In Banner with Skip Button
            if (!isGoogleSignedIn && !isBannerDismissed) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = theme.card,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4285F4)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sign in with Google",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textMain
                                )
                                Text(
                                    text = "Sync cloud stats, streaks & settings across devices",
                                    fontSize = 11.sp,
                                    color = theme.textMuted,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        // Action Buttons: Sign in & Skip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Skip Button (Small text button)
                            androidx.compose.material3.TextButton(
                                onClick = { viewModel.skipGoogleSignIn() },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = "Skip for now",
                                    fontSize = 12.sp,
                                    color = theme.textMuted
                                )
                            }

                            // Google Sign-In Main Action Button
                            Button(
                                onClick = { viewModel.performGoogleSignIn() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = theme.primary
                                ),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Sign In",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Permission & Shield Status Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = theme.cardSecondary,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.primary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openPermissionWizard() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
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
                                .background(theme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🛡️", fontSize = 16.sp)
                        }

                        Column {
                            Text(
                                text = "পারমিশন সেটআপ ও চেক (Permission Guide)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textMain
                            )
                            Text(
                                text = "রিলস ও শর্টস ব্লকিং ঠিকমতো চালু রাখতে ট্যাপ করুন",
                                fontSize = 11.sp,
                                color = theme.textMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.primary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "সেটআপ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            // Screen Time & Blocked Today Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = theme.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToStats() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Total Screen Time
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (realTotalScreenTime > 0) {
                                val hrs = realTotalScreenTime / 60
                                val mins = realTotalScreenTime % 60
                                if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                            } else "0m",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textMain
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Screen Time",
                            fontSize = 12.sp,
                            color = theme.textMuted
                        )
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(38.dp)
                            .background(theme.cardBorder)
                    )

                    // Right Column: Blocked Today Count
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "$todayBlockedCount",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Blocked Today",
                            fontSize = 12.sp,
                            color = theme.textMuted
                        )
                    }
                }
            }

            // Radar Circle & Center Power Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                val primaryColor = theme.primary
                val borderCol = theme.cardBorder
                val cardColor = theme.card

                // Decorative Radar Canvas with animated pulses
                Canvas(modifier = Modifier.size(240.dp)) {
                    val radius = size.minDimension / 2
                    val centerOffset = center

                    // Outer dashed circle
                    drawCircle(
                        color = borderCol,
                        radius = radius * 0.95f,
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                        )
                    )

                    // Middle ring
                    drawCircle(
                        color = borderCol.copy(alpha = 0.6f),
                        radius = radius * 0.72f,
                        style = Stroke(width = 1.2.dp.toPx())
                    )

                    // Inner ring
                    drawCircle(
                        color = if (isMasterActive) primaryColor.copy(alpha = 0.25f) else borderCol.copy(alpha = 0.3f),
                        radius = radius * 0.48f,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Rotating Radar Beam when Active
                if (isMasterActive) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .rotate(radarAngle)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = primaryColor.copy(alpha = 0.18f),
                                startAngle = 0f,
                                sweepAngle = 45f,
                                useCenter = true
                            )
                        }
                    }
                }

                // Center Circle Power Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isMasterActive) theme.cardSecondary else theme.card)
                        .border(
                            width = 2.dp,
                            color = if (isMasterActive) theme.success else theme.cardBorder,
                            shape = CircleShape
                        )
                        .clickable { viewModel.toggleMasterShield() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Toggle Master Shield",
                        tint = if (isMasterActive) theme.success else theme.textMuted,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            // Status Pill
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.card)
                        .border(1.dp, theme.cardBorder, RoundedCornerShape(20.dp))
                        .clickable { onNavigateToActiveFeatures() }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isMasterActive) theme.success else theme.error)
                    )
                    Text(
                        text = if (isMasterActive) "$enabledFeaturesCount/$totalFeaturesCount Protection ON >" else "0/$totalFeaturesCount Protection OFF >",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.textMain
                    )
                }
            }

            // "REDUCE USAGE" Section Title
            Text(
                text = "REDUCE USAGE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            // 2-Column Grid Cards
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Row 1: Study Mode & Reels Block V5
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReduceUsageCard(
                        iconEmoji = "📖",
                        title = "Study Mode",
                        subtitle = "Focus session",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTimer
                    )
                    ReduceUsageCard(
                        iconEmoji = "🎬",
                        title = "Reels Block V5",
                        subtitle = "Feed interceptor",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToReelsBlock
                    )
                }

                // Row 2: Schedule Blocker (PRO) & Adult Content Blocker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReduceUsageCard(
                        iconEmoji = "⏰",
                        title = "Schedule Blocker",
                        subtitle = "Timed discipline",
                        isPro = true,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToActiveFeatures
                    )
                    val pornFeature = allFeatures.find { it.key == "porn_block" }
                    ReduceUsageCard(
                        iconEmoji = "⚠️",
                        title = "Adult Content Blocker",
                        subtitle = if (pornFeature?.isEnabled == true) "Active" else "Filtered",
                        hasRedDot = pornFeature?.isEnabled != true,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.toggleFeature("porn_block") }
                    )
                }

                // Row 3: 5 Second Pause & App Tutorial
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val pauseFeature = allFeatures.find { it.key == "five_second_pause" }
                    ReduceUsageCard(
                        iconEmoji = "🛡️",
                        title = "5 Second Pause",
                        subtitle = if (pauseFeature?.isEnabled == true) "Active" else "Delay opening",
                        hasRedDot = pauseFeature?.isEnabled != true,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.toggleFeature("five_second_pause") }
                    )
                    ReduceUsageCard(
                        iconEmoji = "▶️",
                        title = "App Tutorial",
                        subtitle = "How it works",
                        hasRedDot = true,
                        modifier = Modifier.weight(1f),
                        onClick = { showTutorialDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showTutorialDialog) {
        AlertDialog(
            onDismissRequest = { showTutorialDialog = false },
            title = { Text("How Social Addiction Works", color = theme.textMain, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("1. Real-time surgical feed blocker intercepts Reels, Shorts, and doomscrolling traps before they trigger dopamine loops.", color = theme.textMuted, fontSize = 13.sp)
                    Text("2. Usage Access tracks exact on-device screen time with zero fake or simulated data.", color = theme.textMuted, fontSize = 13.sp)
                    Text("3. Study Mode & App Limits enforce deep focus sessions with hardcore bypass protections.", color = theme.textMuted, fontSize = 13.sp)
                    Text("4. Choose from 10 immersive visual themes to customize your digital detox aesthetic.", color = theme.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTutorialDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accent)
                ) {
                    Text("Got It", color = if (theme.accent.isLight()) Color.Black else Color.White)
                }
            },
            containerColor = theme.card
        )
    }
}

@Composable
fun ReduceUsageCard(
    iconEmoji: String,
    title: String,
    subtitle: String,
    isPro: Boolean = false,
    hasRedDot: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = SentryTheme
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = theme.card,
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(theme.cardSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = iconEmoji, fontSize = 18.sp)
                }

                if (isPro) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(theme.primary.copy(alpha = 0.2f))
                            .border(1.dp, theme.primary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PRO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primary
                        )
                    }
                } else if (hasRedDot) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(theme.error)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = theme.textMain,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = theme.textMuted
            )
        }
    }
}

private fun Color.isLight(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.5
}
