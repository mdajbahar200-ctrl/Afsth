package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockSchedule
import com.example.data.model.BlockedUrl
import com.example.data.model.FeatureItem
import com.example.service.UsageStatsHelper
import com.example.ui.SentryViewModel
import com.example.ui.theme.SentryBrandRed
import com.example.ui.theme.SentryDarkCanvas
import com.example.ui.theme.SentryDarkCard
import com.example.ui.theme.SentryDarkCardBorder
import com.example.ui.theme.SentryDarkCardSecondary
import com.example.ui.theme.SentryDarkTextMain
import com.example.ui.theme.SentryDarkTextMuted
import com.example.ui.theme.SentryGoldCTA
import com.example.ui.theme.SentryProPurple
import com.example.ui.theme.SentryProPurpleBg
import com.example.ui.theme.SentrySuccessGreen
import com.example.ui.theme.SentrySuccessGreenBg

@Composable
fun ActiveFeaturesScreen(
    viewModel: SentryViewModel,
    onBack: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onOpenHelp: () -> Unit
) {
    val context = LocalContext.current
    val features by viewModel.allFeatureItems.collectAsState()
    val blockedUrls by viewModel.allBlockedUrls.collectAsState()
    val blockSchedules by viewModel.allBlockSchedules.collectAsState()
    val appLimits by viewModel.allAppLimits.collectAsState()
    val phoneLockUntil by viewModel.phoneLockUntilTimestamp.collectAsState()

    val enabledCount = features.count { it.isEnabled }
    val totalFeatures = 14

    // Permissions check
    val hasUsage = UsageStatsHelper.hasUsageStatsPermission(context)
    val hasOverlay = UsageStatsHelper.hasOverlayPermission(context)
    val hasBattery = UsageStatsHelper.isIgnoringBatteryOptimizations(context)
    val hasNotification = UsageStatsHelper.isNotificationAccessGranted(context)
    val hasAccessibility = true // Active service instance
    val grantedPermissionsCount = listOf(hasUsage, hasOverlay, hasBattery, hasNotification, hasAccessibility).count { it }

    var arePermissionsExpanded by remember { mutableStateOf(false) }
    var areModsExpanded by remember { mutableStateOf(false) }

    // Dialog States
    var showLockPhoneDialog by remember { mutableStateOf(false) }
    var showAppBlockDialog by remember { mutableStateOf(false) }
    var showWebsiteBlockerDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showQuickFixDialog by remember { mutableStateOf(false) }
    var showReportBugDialog by remember { mutableStateOf(false) }
    var showWhatsNewDialog by remember { mutableStateOf(false) }

    var newUrlInput by remember { mutableStateOf("") }
    var bugReportText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentryDarkCanvas)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SentryDarkTextMain
                )
            }

            Text(
                text = "Active Features $enabledCount/$totalFeatures",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = SentryDarkTextMain
            )

            // Help button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SentryDarkCard)
                    .border(1.dp, SentryDarkCardBorder, RoundedCornerShape(20.dp))
                    .clickable { onOpenHelp() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Help",
                    tint = SentryDarkTextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Help",
                    fontSize = 12.sp,
                    color = SentryDarkTextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Prime Mode PRO Hero Card
            item {
                val primeFeature = features.find { it.key == "prime_mode" }
                val isPrimeActive = primeFeature?.isEnabled ?: false

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SentryDarkCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isPrimeActive) SentryGoldCTA.copy(alpha = 0.5f) else SentryDarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Prime Mode",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SentryDarkTextMain
                                )
                                ProBadge()
                            }

                            Switch(
                                checked = isPrimeActive,
                                onCheckedChange = { viewModel.toggleFeature("prime_mode") },
                                colors = sentrySwitchColors()
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isPrimeActive) "✓ Hardcore commitment lock is ACTIVE. Strict anti-bypass enabled." else "Hardcore commitment lock prevents disabling or bypassing protections.",
                            fontSize = 12.sp,
                            color = if (isPrimeActive) SentryGoldCTA else SentryDarkTextMuted
                        )
                    }
                }
            }

            // Uninstall Protection PRO & Lock My Phone
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SentryDarkCard)
                        .border(1.dp, SentryDarkCardBorder, RoundedCornerShape(20.dp))
                ) {
                    // Uninstall Protection PRO
                    val uninstallFeature = features.find { it.key == "uninstall_protection" }
                    FeatureToggleRow(
                        title = "Uninstall Protection",
                        subtitle = "Prevent tampering & uninstallation",
                        isPro = true,
                        isChecked = uninstallFeature?.isEnabled ?: false,
                        onToggle = { viewModel.toggleFeature("uninstall_protection") }
                    )

                    DividerLine()

                    // Lock My Phone
                    val isLocked = phoneLockUntil > System.currentTimeMillis()
                    val remainingMins = if (isLocked) (phoneLockUntil - System.currentTimeMillis()) / (60 * 1000) else 0L

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLockPhoneDialog = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Lock My Phone",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SentryDarkTextMain
                                )
                                if (isLocked) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SentryGoldCTA.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${remainingMins}m LEFT",
                                            color = SentryGoldCTA,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (isLocked) "Detox lockdown active! Tap to manage or unlock" else "Digital detox lockdown (15m up to 3 days)",
                                fontSize = 12.sp,
                                color = if (isLocked) SentryGoldCTA else SentryDarkTextMuted
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = if (isLocked) SentryGoldCTA else SentryDarkTextMuted
                        )
                    }
                }
            }

            // Mods dropdown (Fahh Mode, Rag korla !)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SentryDarkCard)
                        .border(1.dp, SentryDarkCardBorder, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { areModsExpanded = !areModsExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Mods",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SentryDarkTextMain
                            )
                            Text(
                                text = "Social Addiction extra sound triggers & audio mods",
                                fontSize = 12.sp,
                                color = SentryDarkTextMuted
                            )
                        }
                        Icon(
                            imageVector = if (areModsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand Mods",
                            tint = SentryDarkTextMuted
                        )
                    }

                    AnimatedVisibility(visible = areModsExpanded) {
                        Column {
                            DividerLine()
                            val fahhFeature = features.find { it.key == "fahh_mode" }
                            FeatureToggleWithSound(
                                title = "Fahh Mode",
                                subtitle = "Plays sound on block",
                                isChecked = fahhFeature?.isEnabled ?: false,
                                onToggle = { viewModel.toggleFeature("fahh_mode") },
                                onTestSound = { viewModel.playSoundTest("fahh") }
                            )
                            DividerLine()
                            val ragFeature = features.find { it.key == "rag_korla" }
                            FeatureToggleWithSound(
                                title = "Rag korla !",
                                subtitle = "Plays sound on block",
                                isChecked = ragFeature?.isEnabled ?: false,
                                onToggle = { viewModel.toggleFeature("rag_korla") },
                                onTestSound = { viewModel.playSoundTest("rag_korla") }
                            )
                        }
                    }
                }
            }

            // BLOCKING Section (Reels Blocker, Porn Blocker, Website Blocker, App Block)
            item {
                CategorySectionHeader(title = "BLOCKING")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SentryDarkCard)
                        .border(1.dp, SentryDarkCardBorder, RoundedCornerShape(20.dp))
                ) {
                    val reels = features.find { it.key == "reels_block" }
                    FeatureToggleRow(
                        title = "Reels Blocker",
                        subtitle = "Block reels on social media",
                        isChecked = reels?.isEnabled ?: true,
                        onToggle = { viewModel.toggleFeature("reels_block") }
                    )

                    DividerLine()

                    val porn = features.find { it.key == "porn_block" }
                    FeatureToggleRow(
                        title = "Porn Blocker",
                        subtitle = "Block adult content & nudity",
                        isChecked = porn?.isEnabled ?: false,
                        onToggle = { viewModel.toggleFeature("porn_block") }
                    )

                    DividerLine()

                    // Website Blocker
                    val web = features.find { it.key == "website_block" }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showWebsiteBlockerDialog = true }
                        ) {
                            Text(
                                text = "Website Blocker",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SentryDarkTextMain
                            )
                            Text(
                                text = if (blockedUrls.isNotEmpty()) "${blockedUrls.size} custom domains configured" else "Block specific URLs",
                                fontSize = 12.sp,
                                color = SentryGoldCTA
                            )
                        }
                        Switch(
                            checked = web?.isEnabled ?: false,
                            onCheckedChange = { viewModel.toggleFeature("website_block") },
                            colors = sentrySwitchColors()
                        )
                    }

                    DividerLine()

                    // App Block
                    val appBlock = features.find { it.key == "app_block" }
                    val isAppBlockOn = appBlock?.isEnabled ?: false
                    val hardLockedCount = appLimits.count { it.isHardLocked }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showAppBlockDialog = true }
                        ) {
                            Text(
                                text = "App Block",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SentryDarkTextMain
                            )
                            Text(
                                text = if (isAppBlockOn) "All distracting apps blocked" else if (hardLockedCount > 0) "$hardLockedCount apps hard locked (tap to manage)" else "Block apps completely (tap to select)",
                                fontSize = 12.sp,
                                color = if (isAppBlockOn || hardLockedCount > 0) SentryGoldCTA else SentryDarkTextMuted
                            )
                        }
                        Switch(
                            checked = isAppBlockOn,
                            onCheckedChange = { viewModel.toggleFeature("app_block") },
                            colors = sentrySwitchColors()
                        )
                    }
                }
            }

            // LIMITS Section (5 Second Pause, App Limits, Scroll Limit PRO)
            item {
                CategorySectionHeader(title = "LIMITS")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SentryDarkCard)
                        .border(1.dp, SentryDarkCardBorder, RoundedCornerShape(20.dp))
                ) {
                    val pause = features.find { it.key == "five_second_pause" }
                    FeatureToggleRow(
                        title = "5 Second Pause",
                        subtitle = "5s pause before app open",
                        isChecked = pause?.isEnabled ?: false,
                        onToggle = { viewModel.toggleFeature("five_second_pause") }
                    )

                    DividerLine()

                    val appLimitsFeature = features.find { it.key == "app_limits" }
                    FeatureToggleRow(
                        title = "App Limits",
                        subtitle = "Set daily time limits",
                        isChecked = appLimitsFeature?.isEnabled ?: true,
                        onToggle = { viewModel.toggleFeature("app_limits") }
                    )

                    DividerLine()

                    val scroll = features.find { it.key == "scroll_limit" }
                    FeatureToggleRow(
                        title = "Scroll Limit",
                        subtitle = "Limit endless scrolling",
                        isPro = true,
                        isChecked = scroll?.isEnabled ?: false,
                        onToggle = { viewModel.toggleFeature("scroll_limit") }
                    )
                }
            }

            // SCHEDULE BLOCK Section (Schedule blocker PRO, Block Notifications, Study Mode)
            item {
                CategorySectionHeader(title = "SCHEDULE BLOCK")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SentryDarkCard)
                        .border(1.dp, SentryDarkCardBorder, RoundedCornerShape(20.dp))
                ) {
                    // Schedule blocker PRO
                    val schedule = features.find { it.key == "schedule_block" }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showScheduleDialog = true }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Schedule blocker",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SentryDarkTextMain
                                )
                                ProBadge()
                            }
                            Text(
                                text = "Setup blocking schedule (e.g. 9AM - 5PM)",
                                fontSize = 12.sp,
                                color = SentryGoldCTA
                            )
                        }
                        Switch(
                            checked = schedule?.isEnabled ?: false,
                            onCheckedChange = { viewModel.toggleFeature("schedule_block") },
                            colors = sentrySwitchColors()
                        )
                    }

                    DividerLine()

                    val blockNotifs = features.find { it.key == "block_notifications" }
                    FeatureToggleRow(
                        title = "Block Notifications",
                        subtitle = "Setup schedule or timer",
                        isChecked = blockNotifs?.isEnabled ?: false,
                        onToggle = { viewModel.toggleFeature("block_notifications") }
                    )

                    DividerLine()

                    // Study Mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToTimer() }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Study Mode",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SentryDarkTextMain
                            )
                            Text(
                                text = "Block apps during study & deep focus",
                                fontSize = 12.sp,
                                color = SentryDarkTextMuted
                            )
                        }
                        Text(
                            text = "START >",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SentryBrandRed
                        )
                    }
                }
            }

            // PERMISSIONS (X/5) Collapsible Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SentryDarkCard)
                        .border(1.dp, SentryDarkCardBorder, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { arePermissionsExpanded = !arePermissionsExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PERMISSIONS ($grantedPermissionsCount/5)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SentryDarkTextMuted,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = if (arePermissionsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = SentryDarkTextMuted
                        )
                    }

                    AnimatedVisibility(visible = arePermissionsExpanded) {
                        Column {
                            DividerLine()
                            PermissionItemRow(
                                title = "Accessibility Service",
                                isGranted = hasAccessibility,
                                onClick = { UsageStatsHelper.openAccessibilitySettings(context) }
                            )
                            DividerLine()
                            PermissionItemRow(
                                title = "Usage Stats",
                                isGranted = hasUsage,
                                onClick = { UsageStatsHelper.openUsageAccessSettings(context) }
                            )
                            DividerLine()
                            PermissionItemRow(
                                title = "Display Over Apps",
                                isGranted = hasOverlay,
                                onClick = { UsageStatsHelper.openOverlaySettings(context) }
                            )
                            DividerLine()
                            PermissionItemRow(
                                title = "Ignore Battery Optimization",
                                isGranted = hasBattery,
                                onClick = { UsageStatsHelper.openBatteryOptimizationSettings(context) }
                            )
                            DividerLine()
                            PermissionItemRow(
                                title = "Notification Access",
                                isGranted = hasNotification,
                                onClick = { UsageStatsHelper.openNotificationAccessSettings(context) }
                            )
                        }
                    }
                }
            }

            // ACCOUNT Section (Subscription, Profile Settings, Contribution)
            item {
                CategorySectionHeader(title = "ACCOUNT")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SentryDarkCard)
                        .border(1.dp, SentryDarkCardBorder, RoundedCornerShape(20.dp))
                ) {
                    NavigationItemRow(
                        title = "Subscription",
                        subtitle = "Social Addiction Pro Plans",
                        onClick = onNavigateToSubscription
                    )
                    DividerLine()
                    NavigationItemRow(
                        title = "Profile Settings",
                        subtitle = "Md. Aj bahar (@mdajbahar1002)",
                        onClick = onNavigateToProfile
                    )
                    DividerLine()
                    NavigationItemRow(
                        title = "Contribution",
                        subtitle = "Help from our community & open source",
                        onClick = { showWhatsNewDialog = true }
                    )
                }
            }

            // SUPPORT Section (Quick Fix, Report a Bug, What's New)
            item {
                CategorySectionHeader(title = "SUPPORT")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SentryDarkCard)
                        .border(1.dp, SentryDarkCardBorder, RoundedCornerShape(20.dp))
                ) {
                    NavigationItemRow(
                        title = "Quick Fix",
                        subtitle = "Resolve permission & sync issues",
                        onClick = { showQuickFixDialog = true }
                    )
                    DividerLine()
                    NavigationItemRow(
                        title = "Report a Bug",
                        subtitle = "Help us improve Social Addiction",
                        onClick = { showReportBugDialog = true }
                    )
                    DividerLine()
                    NavigationItemRow(
                        title = "What's New",
                        subtitle = "Discover latest features in V5",
                        onClick = { showWhatsNewDialog = true }
                    )
                }
            }

            // Cute Avocado Mascot at Bottom
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🥑",
                        fontSize = 44.sp
                    )
                }
            }
        }
    }

    // Lock My Phone Dialog
    if (showLockPhoneDialog) {
        val isLocked = phoneLockUntil > System.currentTimeMillis()
        val remainingMins = if (isLocked) (phoneLockUntil - System.currentTimeMillis()) / (60 * 1000) else 0L

        AlertDialog(
            onDismissRequest = { showLockPhoneDialog = false },
            title = { Text("Lock My Phone (Detox)", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isLocked) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SentryGoldCTA.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("DETOX LOCKDOWN ACTIVE", color = SentryGoldCTA, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("$remainingMins minutes remaining", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = {
                                viewModel.clearPhoneLock()
                                showLockPhoneDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SentryBrandRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Unlock Phone", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("Select lockdown duration. Distracting apps will be completely inaccessible:", color = SentryDarkTextMuted, fontSize = 13.sp)
                        val durationOptions = listOf(
                            "15 Minutes" to 15,
                            "1 Hour" to 60,
                            "3 Hours" to 180,
                            "12 Hours" to 720,
                            "24 Hours" to 1440,
                            "3 Days" to 4320
                        )
                        durationOptions.forEach { (label, minutes) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SentryDarkCardSecondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setPhoneLockDuration(minutes)
                                        showLockPhoneDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        color = SentryGoldCTA,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(Icons.Default.Lock, contentDescription = "Lock", tint = SentryGoldCTA, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLockPhoneDialog = false }) {
                    Text("Close", color = SentryDarkTextMuted)
                }
            },
            containerColor = SentryDarkCard
        )
    }

    // App Block Selector Dialog
    if (showAppBlockDialog) {
        AlertDialog(
            onDismissRequest = { showAppBlockDialog = false },
            title = { Text("App Block (Hard Lock)", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Toggle which apps should be completely blocked from opening:", color = SentryDarkTextMuted, fontSize = 13.sp)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(appLimits.size) { index ->
                            val app = appLimits[index]
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SentryDarkCardSecondary,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(app.appName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = if (app.isHardLocked) "Hard Locked (Blocked)" else "Normal Access",
                                            fontSize = 11.sp,
                                            color = if (app.isHardLocked) SentryBrandRed else SentryDarkTextMuted
                                        )
                                    }
                                    Switch(
                                        checked = app.isHardLocked,
                                        onCheckedChange = { isChecked ->
                                            viewModel.saveAppLimit(app.copy(isHardLocked = isChecked))
                                        },
                                        colors = sentrySwitchColors()
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAppBlockDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SentryGoldCTA)
                ) {
                    Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SentryDarkCard
        )
    }

    // Website Blocker Dialog
    if (showWebsiteBlockerDialog) {
        AlertDialog(
            onDismissRequest = { showWebsiteBlockerDialog = false },
            title = { Text("Website Blocker", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add domain names to block (e.g. reddit.com):", color = SentryDarkTextMuted, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newUrlInput,
                            onValueChange = { newUrlInput = it },
                            placeholder = { Text("domain.com", color = SentryDarkTextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SentryGoldCTA,
                                unfocusedBorderColor = SentryDarkCardBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (newUrlInput.isNotBlank()) {
                                    viewModel.addBlockedUrl(newUrlInput)
                                    newUrlInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SentryGoldCTA)
                        ) {
                            Text("Add", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (blockedUrls.isNotEmpty()) {
                        Text("Configured Domains:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        blockedUrls.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SentryDarkCardSecondary, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.domain, color = Color.White, fontSize = 13.sp)
                                IconButton(onClick = { viewModel.deleteBlockedUrl(item.id) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SentryBrandRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showWebsiteBlockerDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SentryGoldCTA)) {
                    Text("Done", color = Color.Black)
                }
            },
            containerColor = SentryDarkCard
        )
    }

    // Schedule Dialog
    if (showScheduleDialog) {
        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            title = { Text("Schedule Blocker PRO", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Active Rule: Work Hours Focus", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("Time: 09:00 AM - 05:00 PM", color = SentryGoldCTA, fontSize = 14.sp)
                    Text("Days: Mon, Tue, Wed, Thu, Fri", color = SentryDarkTextMuted, fontSize = 13.sp)
                    Text("Distracting social feeds and reels will be automatically blocked during this window.", color = SentryDarkTextMuted, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleFeature("schedule_block")
                        showScheduleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SentryGoldCTA)
                ) {
                    Text("Save & Enable", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleDialog = false }) {
                    Text("Cancel", color = SentryDarkTextMuted)
                }
            },
            containerColor = SentryDarkCard
        )
    }

    // Quick Fix Wizard Dialog
    if (showQuickFixDialog) {
        AlertDialog(
            onDismissRequest = { showQuickFixDialog = false },
            title = { Text("Quick Fix & Diagnostics", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("System Permission Health:", color = SentryDarkTextMuted, fontSize = 13.sp)
                    QuickFixItem("Accessibility Service", hasAccessibility) { UsageStatsHelper.openAccessibilitySettings(context) }
                    QuickFixItem("Usage Stats", hasUsage) { UsageStatsHelper.openUsageAccessSettings(context) }
                    QuickFixItem("Display Over Apps", hasOverlay) { UsageStatsHelper.openOverlaySettings(context) }
                    QuickFixItem("Battery Optimization", hasBattery) { UsageStatsHelper.openBatteryOptimizationSettings(context) }
                }
            },
            confirmButton = {
                Button(onClick = { showQuickFixDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SentryGoldCTA)) {
                    Text("All Set", color = Color.Black)
                }
            },
            containerColor = SentryDarkCard
        )
    }

    // Bug Report Dialog
    if (showReportBugDialog) {
        AlertDialog(
            onDismissRequest = { showReportBugDialog = false },
            title = { Text("Report an Issue", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tell us what happened:", color = SentryDarkTextMuted, fontSize = 13.sp)
                    OutlinedTextField(
                        value = bugReportText,
                        onValueChange = { bugReportText = it },
                        placeholder = { Text("e.g. YouTube Shorts wasn't detected...", color = SentryDarkTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SentryGoldCTA,
                            unfocusedBorderColor = SentryDarkCardBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showReportBugDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SentryGoldCTA)
                ) {
                    Text("Submit Report", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportBugDialog = false }) {
                    Text("Cancel", color = SentryDarkTextMuted)
                }
            },
            containerColor = SentryDarkCard
        )
    }

    // What's New Dialog
    if (showWhatsNewDialog) {
        AlertDialog(
            onDismissRequest = { showWhatsNewDialog = false },
            title = { Text("What's New in Social Sentry V5", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✨ Real-time Reels & Shorts Interception engine", color = Color.White, fontSize = 13.sp)
                    Text("🛡️ Hardcore Prime Mode & Uninstall Protection", color = Color.White, fontSize = 13.sp)
                    Text("🔊 Audible Mods (Fahh Mode & Rag korla !)", color = Color.White, fontSize = 13.sp)
                    Text("📊 Real on-device usage analytics with zero mock data", color = Color.White, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showWhatsNewDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SentryGoldCTA)) {
                    Text("Awesome", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SentryDarkCard
        )
    }
}

@Composable
fun FeatureToggleRow(
    title: String,
    subtitle: String,
    isPro: Boolean = false,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SentryDarkTextMain
                )
                if (isPro) ProBadge()
            }
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SentryDarkTextMuted
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = sentrySwitchColors()
        )
    }
}

@Composable
fun FeatureToggleWithSound(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    onTestSound: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = SentryDarkTextMain
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SentryDarkTextMuted
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onTestSound, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Test Sound",
                    tint = SentryGoldCTA,
                    modifier = Modifier.size(20.dp)
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = sentrySwitchColors()
            )
        }
    }
}

@Composable
fun PermissionItemRow(
    title: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = SentryDarkTextMain
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (isGranted) "Granted" else "Tap to Grant",
                fontSize = 12.sp,
                color = if (isGranted) SentrySuccessGreen else SentryBrandRed,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
                contentDescription = if (isGranted) "Granted" else "Not Granted",
                tint = if (isGranted) SentrySuccessGreen else SentryBrandRed,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun NavigationItemRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = SentryDarkTextMain
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SentryDarkTextMuted
            )
        }
        Text(
            text = ">",
            fontSize = 16.sp,
            color = SentryDarkTextMuted,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CategorySectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = SentryDarkTextMuted,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
fun ProBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SentryProPurpleBg)
            .border(1.dp, SentryProPurple, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "PRO",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = SentryProPurple
        )
    }
}

@Composable
fun QuickFixItem(title: String, isOk: Boolean, onFix: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SentryDarkCardSecondary, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 13.sp)
        if (isOk) {
            Text("OK", color = SentrySuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        } else {
            Button(
                onClick = onFix,
                colors = ButtonDefaults.buttonColors(containerColor = SentryBrandRed),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Fix", fontSize = 11.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(SentryDarkCardBorder)
    )
}

@Composable
fun sentrySwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = SentryGoldCTA,
    uncheckedThumbColor = SentryDarkTextMuted,
    uncheckedTrackColor = SentryDarkCardBorder
)
