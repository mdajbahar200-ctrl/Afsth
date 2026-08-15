package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SentryViewModel
import com.example.ui.theme.SentryBrandRed
import com.example.ui.theme.SentryDarkCanvas
import com.example.ui.theme.SentryDarkCard
import com.example.ui.theme.SentryDarkCardBorder
import com.example.ui.theme.SentryDarkTextMain
import com.example.ui.theme.SentryDarkTextMuted
import com.example.ui.theme.SentryGoldCTA
import com.example.ui.theme.SentryProPurple
import com.example.ui.theme.SentrySuccessGreen

@Composable
fun TimerScreen(
    viewModel: SentryViewModel
) {
    val context = LocalContext.current
    val isStudyModeActive by viewModel.isStudyModeActive.collectAsState()
    val focusRemainingSeconds by viewModel.focusRemainingSeconds.collectAsState()
    val selectedDurationMinutes by viewModel.selectedDurationMinutes.collectAsState()
    val isStrictMode by viewModel.isStrictMode.collectAsState()
    val emergencyPassesRemaining by viewModel.emergencyPassesRemaining.collectAsState()

    val totalSeconds = (selectedDurationMinutes * 60).coerceAtLeast(1)
    val progress = if (isStudyModeActive) {
        (focusRemainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else {
        1f
    }
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    val mins = if (isStudyModeActive) focusRemainingSeconds / 60 else selectedDurationMinutes
    val secs = if (isStudyModeActive) focusRemainingSeconds % 60 else 0
    val timeFormatted = String.format("%02d:%02d", mins, secs)

    val lockedApps = remember {
        mutableStateListOf("Instagram", "YouTube", "TikTok", "Facebook")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentryDarkCanvas)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "FOCUS SHIELD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SentryBrandRed,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "Study Mode",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SentryDarkTextMain
            )
        }

        // Circular Timer Dial
        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background ring
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF22262C),
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round,
                trackColor = Color.Transparent,
            )

            // Animated active progress ring
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = if (isStudyModeActive) SentryBrandRed else SentryGoldCTA,
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round,
                trackColor = Color.Transparent,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timeFormatted,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = SentryDarkTextMain,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = if (isStudyModeActive) "Active Deep Block" else "Pomodoro Session",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SentryDarkTextMuted
                )
                if (isStrictMode) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF3B1E28)
                    ) {
                        Text(
                            text = "STRICT LOCK",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SentryBrandRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Duration Presets
        if (!isStudyModeActive) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(15, 25, 45, 60).forEach { dur ->
                    val isSelected = selectedDurationMinutes == dur
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF2E1A23) else SentryDarkCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) SentryBrandRed else SentryDarkCardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .clickable { viewModel.setDuration(dur) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${dur}m",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) SentryBrandRed else SentryDarkTextMain
                            )
                            Text(
                                text = when (dur) {
                                    15 -> "Quick"
                                    25 -> "Pomo"
                                    45 -> "Deep"
                                    else -> "Power"
                                },
                                fontSize = 10.sp,
                                color = SentryDarkTextMuted
                            )
                        }
                    }
                }
            }
        }

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.pauseOrResumeSession(context) },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isStudyModeActive) SentryBrandRed else SentryGoldCTA,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = if (isStudyModeActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Control",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isStudyModeActive) "Give Up Session" else "Start Deep Work",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            if (isStudyModeActive) {
                OutlinedButton(
                    onClick = { viewModel.useEmergencyPass(context) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SentryGoldCTA
                    ),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Pass",
                        tint = SentryGoldCTA,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pass ($emergencyPassesRemaining)", color = SentryGoldCTA)
                }
            }
        }

        // Strict Mode Toggle
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
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = SentryBrandRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Strict Anti-Bypass Mode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SentryDarkTextMain
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Requires math friction challenge to exit session early",
                        fontSize = 11.sp,
                        color = SentryDarkTextMuted
                    )
                }

                Switch(
                    checked = isStrictMode,
                    onCheckedChange = { viewModel.toggleStrictMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SentryBrandRed,
                        uncheckedThumbColor = SentryDarkTextMuted,
                        uncheckedTrackColor = SentryDarkCardBorder
                    )
                )
            }
        }

        // Locked Apps Matrix
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SentryDarkCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, SentryDarkCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Distracting Apps Shielded (${lockedApps.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SentryDarkTextMain
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Instagram", "YouTube", "TikTok", "Facebook").forEach { app ->
                        val isLocked = lockedApps.contains(app)
                        FilterChip(
                            selected = isLocked,
                            onClick = {
                                if (isLocked) lockedApps.remove(app) else lockedApps.add(app)
                            },
                            label = { Text(app, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3B1E28),
                                selectedLabelColor = SentryBrandRed,
                                containerColor = Color(0xFF22262C),
                                labelColor = SentryDarkTextMuted
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
