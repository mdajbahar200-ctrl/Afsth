package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SentryViewModel
import com.example.ui.theme.SentryTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReelsBlockV5Screen(
    viewModel: SentryViewModel,
    onBack: () -> Unit,
    onOpenHelp: () -> Unit
) {
    val context = LocalContext.current
    val theme = SentryTheme

    val allEvents by viewModel.allBlockedEvents.collectAsState()
    val isMasterActive by viewModel.isMasterShieldActive.collectAsState()
    val allFeatures by viewModel.allFeatureItems.collectAsState()
    val appLimits by viewModel.allAppLimits.collectAsState()

    val reelsFeature = allFeatures.find { it.key == "reels_block" }
    val isReelsBlockActive = reelsFeature?.isEnabled ?: isMasterActive

    val startOfToday = viewModel.getStartOfTodayMillis()

    // Real platform-specific counts for today
    val todayEvents = allEvents.filter { it.timestamp >= startOfToday }
    val todayTotalReelsScrolled = todayEvents.size
    val fbCount = todayEvents.count { it.appName.contains("Facebook", ignoreCase = true) }
    val ytCount = todayEvents.count { it.appName.contains("YouTube", ignoreCase = true) }
    val igCount = todayEvents.count { it.appName.contains("Instagram", ignoreCase = true) }
    val ttCount = todayEvents.count { it.appName.contains("TikTok", ignoreCase = true) }

    // Dynamic 7-day trend calculation
    val dayMillis = 86_400_000L
    val past7DaysCounts = remember(allEvents) {
        val list = mutableListOf<Pair<String, Int>>()
        val dayFormat = SimpleDateFormat("E", Locale.ENGLISH)
        for (i in 6 downTo 0) {
            val dayStart = startOfToday - (i * dayMillis)
            val dayEnd = dayStart + dayMillis
            val count = allEvents.count { it.timestamp in dayStart until dayEnd }
            val label = dayFormat.format(Date(dayStart)).take(1) // "S", "M", "T"...
            list.add(label to count)
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.canvas)
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
                    tint = theme.textMain
                )
            }

            // "Social Sentry" brand title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Reels & ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primary
                )
                Text(
                    text = "Shorts Blocker",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMain
                )
            }

            // Help button with Info icon
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.card)
                    .border(1.dp, theme.cardBorder, RoundedCornerShape(20.dp))
                    .clickable { onOpenHelp() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Help",
                    tint = theme.textMuted,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Help",
                    fontSize = 12.sp,
                    color = theme.textMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Card: Reels Scrolled Today + Breakdown Pills
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = theme.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Downward trend",
                            tint = theme.textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "🧠 $todayTotalReelsScrolled",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textMain
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Reels Blocked & Intercepted Today",
                        fontSize = 14.sp,
                        color = theme.textMuted,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Social Apps Breakdown Pills (Facebook, YouTube, Instagram, TikTok)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SocialPlatformPill(iconText = "F", name = "Facebook", count = fbCount, color = Color(0xFF1877F2))
                        SocialPlatformPill(iconText = "▶", name = "YouTube", count = ytCount, color = Color(0xFFFF0000))
                        SocialPlatformPill(iconText = "I", name = "Instagram", count = igCount, color = Color(0xFFE4405F))
                        SocialPlatformPill(iconText = "🎵", name = "TikTok", count = ttCount, color = Color(0xFF00F2FE))
                    }
                }
            }

            // Distraction Trend Line Chart Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = theme.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DISTRACTION TREND",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textMuted,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Last 7 Days",
                            fontSize = 12.sp,
                            color = theme.textMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 7-day Real Line Graph
                    TrendLineChart(data = past7DaysCounts)
                }
            }

            // Reels Blocker Master Switch Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = theme.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
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
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(theme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = theme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Surgical Feed Blocker",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.textMain
                            )
                            Text(
                                text = "Auto-intercept Reels, Shorts & Doomscrolling",
                                fontSize = 12.sp,
                                color = theme.textMuted
                            )
                        }
                    }

                    Switch(
                        checked = isReelsBlockActive,
                        onCheckedChange = { viewModel.toggleFeature("reels_block") },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = theme.primary,
                            uncheckedThumbColor = theme.textMuted,
                            uncheckedTrackColor = theme.cardBorder
                        )
                    )
                }
            }

            // All Phone Social Apps Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ফোনের সোশ্যাল অ্যাপসমূহ (${appLimits.size}টি অ্যাপ)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMain
                    )
                    Text(
                        text = "প্রত্যেকটি অ্যাপের রিলস ও লিমিট আলাদা কনফিগার করুন",
                        fontSize = 11.sp,
                        color = theme.textMuted
                    )
                }

                Button(
                    onClick = { viewModel.scanAndSyncInstalledApps(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.cardSecondary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan",
                        tint = theme.textMain,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("স্ক্যান", fontSize = 12.sp, color = theme.textMain)
                }
            }

            // List of detected Social Media Apps
            appLimits.forEach { appLimit ->
                val appBlockedCount = todayEvents.count { it.packageName == appLimit.packageName || it.appName.equals(appLimit.appName, ignoreCase = true) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                    modifier = Modifier.fillMaxWidth()
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(theme.cardSecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = appLimit.iconEmoji.ifBlank { "📱" }, fontSize = 20.sp)
                            }

                            Column {
                                Text(
                                    text = appLimit.appName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textMain
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (appLimit.isShortsBlocked) "🚫 রিলস ব্লক চালু" else "রিলস অনুমতিপ্রাপ্ত",
                                        fontSize = 11.sp,
                                        color = if (appLimit.isShortsBlocked) theme.primary else theme.textMuted
                                    )
                                    if (appBlockedCount > 0) {
                                        Text(
                                            text = "• $appBlockedCount বার থামানো হয়েছে",
                                            fontSize = 11.sp,
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.openAppLimitEditor(appLimit) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit limit",
                                    tint = theme.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Switch(
                                checked = appLimit.isEnabled && appLimit.isShortsBlocked,
                                onCheckedChange = { isChecked ->
                                    viewModel.saveAppLimit(
                                        appLimit.copy(
                                            isEnabled = isChecked,
                                            isShortsBlocked = isChecked
                                        )
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = theme.primary,
                                    uncheckedThumbColor = theme.textMuted,
                                    uncheckedTrackColor = theme.cardBorder
                                )
                            )
                        }
                    }
                }
            }

            // Quick Interception Simulation Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = theme.cardSecondary,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🧪 টেস্ট ইন্টারসেপশন (সরাসরি টেস্ট করুন)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMain
                    )
                    Text(
                        text = "নিচের যেকোনো প্ল্যাটফর্মে ট্যাপ করে ব্লকিং ও স্পিডবাম্প সঠিকভাবে কাজ করছে কিনা পরীক্ষা করুন:",
                        fontSize = 11.sp,
                        color = theme.textMuted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.testSimulateInterception("YouTube", "YouTube Shorts") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000).copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                        ) {
                            Text("▶ YouTube", color = Color(0xFFFF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.testSimulateInterception("Instagram", "Instagram Reels") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE4405F).copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                        ) {
                            Text("📷 Insta Reels", color = Color(0xFFFF6699), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.testSimulateInterception("Facebook", "Facebook Reels") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2).copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                        ) {
                            Text("📘 Facebook", color = Color(0xFF3B82F6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Prominent Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.canvas)
                .padding(16.dp)
        ) {
            Button(
                onClick = { viewModel.saveMeFromReels() },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.primary,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = "Save me from reels (সব রিলস ব্লক চালু করুন)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun SocialPlatformPill(
    iconText: String,
    name: String,
    count: Int,
    color: Color
) {
    val theme = SentryTheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(theme.card)
            .border(1.dp, theme.cardBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = iconText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = "$count",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.textMain
        )
    }
}

@Composable
fun TrendLineChart(data: List<Pair<String, Int>>) {
    val theme = SentryTheme
    val maxCount = (data.maxOfOrNull { it.second } ?: 1).coerceAtLeast(4)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val stepX = width / (data.size - 1).coerceAtLeast(1)

                val points = data.mapIndexed { index, pair ->
                    val x = index * stepX
                    val yFraction = pair.second.toFloat() / maxCount.toFloat()
                    val y = height - (yFraction * (height - 20f)) - 10f
                    Offset(x, y)
                }

                // Draw connecting path
                val path = Path()
                if (points.isNotEmpty()) {
                    path.moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        path.lineTo(points[i].x, points[i].y)
                    }
                    drawPath(
                        path = path,
                        color = theme.primary,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Draw point dots
                points.forEach { point ->
                    drawCircle(
                        color = theme.primary,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = 2.dp.toPx(),
                        center = point
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Day Labels (S, S, M, T, W, T, F)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { (day, _) ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = theme.textMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

