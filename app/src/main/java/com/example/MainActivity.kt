package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SentryViewModel
import com.example.ui.components.AppLimitDialog
import com.example.ui.components.BlockedInterceptionOverlay
import com.example.ui.components.BypassChallengeDialog
import com.example.ui.components.MindfulnessPauseDialog
import com.example.ui.screens.ActiveFeaturesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.ReelsBlockV5Screen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.SubscriptionScreen
import com.example.ui.screens.ThemesScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.theme.AppThemes
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SentryTheme
import kotlinx.coroutines.launch

enum class ScreenDestination {
    HOME,
    TIMER,
    STATS,
    SETTINGS,
    REELS_BLOCK,
    ACTIVE_FEATURES,
    PROFILE,
    SUBSCRIPTION,
    THEMES
}

class MainActivity : ComponentActivity() {

    private val viewModel: SentryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userProfile by viewModel.userProfile.collectAsState()
            val activeTheme = AppThemes.getThemeById(userProfile.selectedThemeId)

            MyApplicationTheme(themeData = activeTheme) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: SentryViewModel) {
    val context = LocalContext.current
    val theme = SentryTheme
    var currentDestination by remember { mutableStateOf(ScreenDestination.HOME) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val interceptionDetail by viewModel.interceptionOverlayEvent.collectAsState()
    val mindfulnessPauseApp by viewModel.mindfulnessPauseApp.collectAsState()
    val showBypassChallenge by viewModel.showBypassChallengeDialog.collectAsState()
    val emergencyPasses by viewModel.emergencyPassesRemaining.collectAsState()
    val editingLimit by viewModel.editingAppLimit.collectAsState()
    val showPermissionWizard by viewModel.showPermissionWizard.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    BackHandler(enabled = currentDestination != ScreenDestination.HOME || drawerState.isOpen) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            currentDestination = ScreenDestination.HOME
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = theme.card,
                drawerContentColor = theme.textMain,
                modifier = Modifier.width(300.dp).fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header User Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentDestination = ScreenDestination.PROFILE
                                coroutineScope.launch { drawerState.close() }
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(theme.accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.name.take(1).uppercase(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (theme.accent.isLight()) Color.Black else Color.White
                            )
                        }

                        Column {
                            Text(
                                text = userProfile.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textMain
                            )
                            Text(
                                text = "${theme.themeIcon} ${userProfile.streakDays} Day Streak",
                                fontSize = 12.sp,
                                color = theme.primary
                            )
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(theme.cardBorder))

                    // Menu Destinations
                    DrawerItem(
                        icon = Icons.Default.Home,
                        label = "Home",
                        isSelected = currentDestination == ScreenDestination.HOME,
                        onClick = {
                            currentDestination = ScreenDestination.HOME
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.Palette,
                        label = "Themes & Styles (১০টি থিম)",
                        isSelected = currentDestination == ScreenDestination.THEMES,
                        badgeText = "10 THEMES",
                        onClick = {
                            currentDestination = ScreenDestination.THEMES
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.Shield,
                        label = "Active Features (14)",
                        isSelected = currentDestination == ScreenDestination.ACTIVE_FEATURES,
                        onClick = {
                            currentDestination = ScreenDestination.ACTIVE_FEATURES
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.PlayCircle,
                        label = "Reels Block V5",
                        isSelected = currentDestination == ScreenDestination.REELS_BLOCK,
                        onClick = {
                            currentDestination = ScreenDestination.REELS_BLOCK
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.HourglassBottom,
                        label = "Study Mode Timer",
                        isSelected = currentDestination == ScreenDestination.TIMER,
                        onClick = {
                            currentDestination = ScreenDestination.TIMER
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.BarChart,
                        label = "Real-Time Stats",
                        isSelected = currentDestination == ScreenDestination.STATS,
                        onClick = {
                            currentDestination = ScreenDestination.STATS
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.CardMembership,
                        label = "Subscription (PRO)",
                        isSelected = currentDestination == ScreenDestination.SUBSCRIPTION,
                        onClick = {
                            currentDestination = ScreenDestination.SUBSCRIPTION
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.Person,
                        label = "Profile Settings",
                        isSelected = currentDestination == ScreenDestination.PROFILE,
                        onClick = {
                            currentDestination = ScreenDestination.PROFILE
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.Security,
                        label = "Permission Setup Guide (পারমিশন)",
                        isSelected = false,
                        badgeText = "SETUP",
                        onClick = {
                            viewModel.openPermissionWizard()
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.Settings,
                        label = "Diagnostics & Permissions",
                        isSelected = currentDestination == ScreenDestination.SETTINGS,
                        onClick = {
                            currentDestination = ScreenDestination.SETTINGS
                            coroutineScope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                SleekDarkNavigationBar(
                    currentDestination = currentDestination,
                    onNavigate = { currentDestination = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(theme.canvas)
            ) {
                when (currentDestination) {
                    ScreenDestination.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        onNavigateToTimer = { currentDestination = ScreenDestination.TIMER },
                        onNavigateToReelsBlock = { currentDestination = ScreenDestination.REELS_BLOCK },
                        onNavigateToActiveFeatures = { currentDestination = ScreenDestination.ACTIVE_FEATURES },
                        onNavigateToProfile = { currentDestination = ScreenDestination.PROFILE },
                        onNavigateToStats = { currentDestination = ScreenDestination.STATS }
                    )
                    ScreenDestination.THEMES -> ThemesScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = ScreenDestination.HOME }
                    )
                    ScreenDestination.REELS_BLOCK -> ReelsBlockV5Screen(
                        viewModel = viewModel,
                        onBack = { currentDestination = ScreenDestination.HOME },
                        onOpenHelp = { currentDestination = ScreenDestination.ACTIVE_FEATURES }
                    )
                    ScreenDestination.ACTIVE_FEATURES -> ActiveFeaturesScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = ScreenDestination.HOME },
                        onNavigateToSubscription = { currentDestination = ScreenDestination.SUBSCRIPTION },
                        onNavigateToProfile = { currentDestination = ScreenDestination.PROFILE },
                        onNavigateToTimer = { currentDestination = ScreenDestination.TIMER },
                        onOpenHelp = { currentDestination = ScreenDestination.SETTINGS }
                    )
                    ScreenDestination.PROFILE -> ProfileSettingsScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = ScreenDestination.HOME }
                    )
                    ScreenDestination.SUBSCRIPTION -> SubscriptionScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = ScreenDestination.HOME }
                    )
                    ScreenDestination.TIMER -> TimerScreen(viewModel = viewModel)
                    ScreenDestination.STATS -> StatsScreen(viewModel = viewModel)
                    ScreenDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }

                // Full-screen Interception Speedbump Overlay
                interceptionDetail?.let { detail ->
                    BlockedInterceptionOverlay(
                        detail = detail,
                        onDismiss = { viewModel.dismissInterceptionOverlay() }
                    )
                }

                // 5-Second Mindfulness Pause Overlay
                mindfulnessPauseApp?.let { detail ->
                    MindfulnessPauseDialog(
                        detail = detail,
                        onProceed = { viewModel.completeMindfulnessPause(detail.packageName) },
                        onCancel = { viewModel.dismissMindfulnessPause() }
                    )
                }

                // Anti-Bypass Strict Mode Challenge Dialog
                if (showBypassChallenge) {
                    BypassChallengeDialog(
                        emergencyPassesRemaining = emergencyPasses,
                        onUseEmergencyPass = { viewModel.useEmergencyPass(context) },
                        onChallengeCompleted = { viewModel.forceStopSession(context) },
                        onDismiss = { viewModel.dismissBypassDialog() }
                    )
                }

                // App Limit & Short Block Customizer Dialog
                editingLimit?.let { limit ->
                    AppLimitDialog(
                        limit = limit,
                        onSave = { updated -> viewModel.saveAppLimit(updated) },
                        onDismiss = { viewModel.closeAppLimitEditor() }
                    )
                }

                // Step-by-Step Permission Setup Wizard Dialog
                if (showPermissionWizard) {
                    com.example.ui.components.PermissionWizardDialog(
                        onDismiss = { viewModel.closePermissionWizard() }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    val theme = SentryTheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) theme.cardSecondary else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) theme.primary else theme.textMuted,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) theme.textMain else theme.textMuted
            )
        }

        badgeText?.let { badge ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(theme.primary.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primary
                )
            }
        }
    }
}

@Composable
fun SleekDarkNavigationBar(
    currentDestination: ScreenDestination,
    onNavigate: (ScreenDestination) -> Unit
) {
    val theme = SentryTheme
    Surface(
        color = theme.card,
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SleekDarkNavItem(
                label = "Home",
                selected = currentDestination == ScreenDestination.HOME,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                onClick = { onNavigate(ScreenDestination.HOME) },
                testTag = "nav_home"
            )

            SleekDarkNavItem(
                label = "Features",
                selected = currentDestination == ScreenDestination.ACTIVE_FEATURES || currentDestination == ScreenDestination.REELS_BLOCK,
                selectedIcon = Icons.Filled.Shield,
                unselectedIcon = Icons.Filled.Shield,
                onClick = { onNavigate(ScreenDestination.ACTIVE_FEATURES) },
                testTag = "nav_features"
            )

            SleekDarkNavItem(
                label = "Study",
                selected = currentDestination == ScreenDestination.TIMER,
                selectedIcon = Icons.Filled.HourglassBottom,
                unselectedIcon = Icons.Outlined.HourglassBottom,
                onClick = { onNavigate(ScreenDestination.TIMER) },
                testTag = "nav_timer"
            )

            SleekDarkNavItem(
                label = "Stats",
                selected = currentDestination == ScreenDestination.STATS,
                selectedIcon = Icons.Filled.BarChart,
                unselectedIcon = Icons.Outlined.BarChart,
                onClick = { onNavigate(ScreenDestination.STATS) },
                testTag = "nav_stats"
            )

            SleekDarkNavItem(
                label = "Settings",
                selected = currentDestination == ScreenDestination.SETTINGS,
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                onClick = { onNavigate(ScreenDestination.SETTINGS) },
                testTag = "nav_settings"
            )
        }
    }
}

@Composable
fun SleekDarkNavItem(
    label: String,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    val theme = SentryTheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(theme.iconBadgeBg)
                    .padding(horizontal = 14.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = selectedIcon,
                    contentDescription = label,
                    tint = theme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = unselectedIcon,
                    contentDescription = label,
                    tint = theme.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) theme.primary else theme.textMuted
        )
    }
}

private fun Color.isLight(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.5
}
