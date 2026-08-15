package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockedEvent
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: SentryViewModel
) {
    val allEvents by viewModel.allBlockedEvents.collectAsState()
    val allSessions by viewModel.allFocusSessions.collectAsState()
    val realScreenTime by viewModel.realTotalScreenTimeMinutes.collectAsState()

    val totalBlocked = allEvents.size
    val totalSecondsSaved = totalBlocked * 108
    val savedHours = totalSecondsSaved / 3600
    val savedMins = (totalSecondsSaved % 3600) / 60
    val savedFormatted = if (savedHours > 0) "${savedHours}h ${savedMins}m" else "${savedMins}m"

    // Real dynamic 7-day calculation from database
    val startOfToday = viewModel.getStartOfTodayMillis()
    val dayMillis = 86_400_000L

    val weeklyData = remember(allEvents) {
        val list = mutableListOf<Pair<String, Int>>()
        val dayFormat = SimpleDateFormat("E", Locale.ENGLISH)
        for (i in 6 downTo 0) {
            val dayStart = startOfToday - (i * dayMillis)
            val dayEnd = dayStart + dayMillis
            val count = allEvents.count { it.timestamp in dayStart until dayEnd }
            val label = dayFormat.format(Date(dayStart))
            list.add(label to count)
        }
        list
    }

    val maxCount = (weeklyData.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1).toFloat()

    // Platform distribution percentages (strictly computed from allEvents)
    val igCount = allEvents.count { it.appName.contains("Instagram", ignoreCase = true) }
    val ytCount = allEvents.count { it.appName.contains("YouTube", ignoreCase = true) }
    val fbCount = allEvents.count { it.appName.contains("Facebook", ignoreCase = true) }
    val ttCount = allEvents.count { it.appName.contains("TikTok", ignoreCase = true) }

    val safeTotal = totalBlocked.coerceAtLeast(1)
    val igPercent = (igCount * 100) / safeTotal
    val ytPercent = (ytCount * 100) / safeTotal
    val fbPercent = (fbCount * 100) / safeTotal
    val ttPercent = (ttCount * 100) / safeTotal

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SentryDarkCanvas)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "REAL-TIME ANALYTICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SentryGoldCTA,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Discipline Stats",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SentryDarkTextMain
                )
            }
        }

        // Summary Metric Tiles (Real Screen Time & Real Blocked)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SentryDarkCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SentryDarkCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TOTAL BLOCKED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SentryDarkTextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalBlocked", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SentryDarkTextMain)
                        Text("Reels & Shorts", fontSize = 11.sp, color = SentrySuccessGreen, fontWeight = FontWeight.Medium)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SentryDarkCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SentryDarkCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("RECLAIMED TIME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SentryDarkTextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(savedFormatted, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SentryDarkTextMain)
                        Text("Productive Hours", fontSize = 11.sp, color = SentryGoldCTA, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Weekly Block Frequency Bar Chart (Real dynamic Room values)
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SentryDarkCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SentryDarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "7-Day Interception Trend",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SentryDarkTextMain
                        )
                        Text(
                            text = "Real Room DB Data",
                            fontSize = 11.sp,
                            color = SentryDarkTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Custom Canvas Bar Chart
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val barWidth = 24.dp.toPx()
                            val spacing = (size.width - (barWidth * weeklyData.size)) / (weeklyData.size + 1)
                            val canvasHeight = size.height - 24.dp.toPx()

                            weeklyData.forEachIndexed { index, pair ->
                                val x = spacing + index * (barWidth + spacing)
                                val barHeight = if (maxCount > 0) (pair.second / maxCount) * (canvasHeight - 10f) else 0f
                                val y = canvasHeight - barHeight

                                // Draw bar background slot
                                drawRoundRect(
                                    color = Color(0xFF22262C),
                                    topLeft = Offset(x, 0f),
                                    size = Size(barWidth, canvasHeight),
                                    cornerRadius = CornerRadius(10f, 10f)
                                )

                                // Draw active bar value if > 0
                                if (pair.second > 0) {
                                    drawRoundRect(
                                        color = if (pair.second >= 5) SentryGoldCTA else SentryBrandRed,
                                        topLeft = Offset(x, y),
                                        size = Size(barWidth, barHeight.coerceAtLeast(8f)),
                                        cornerRadius = CornerRadius(10f, 10f)
                                    )
                                }
                            }
                        }

                        // Day Labels Row below
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            weeklyData.forEach { (day, count) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = day.take(3),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SentryDarkTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Platform Breakdown Card (Real dynamically computed)
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
                        text = "Distribution by Social Feed",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SentryDarkTextMain
                    )

                    PlatformBarItem(name = "Instagram Reels", count = igCount, percent = igPercent, color = Color(0xFFE4405F), emoji = "📸")
                    PlatformBarItem(name = "YouTube Shorts", count = ytCount, percent = ytPercent, color = Color(0xFFFF0000), emoji = "▶️")
                    PlatformBarItem(name = "Facebook Reels", count = fbCount, percent = fbPercent, color = Color(0xFF1877F2), emoji = "📘")
                    PlatformBarItem(name = "TikTok Feeds", count = ttCount, percent = ttPercent, color = Color(0xFF00F2FE), emoji = "🎵")
                }
            }
        }

        // Real Recent Intercept Logs Title & Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live On-Device Log (${allEvents.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SentryDarkTextMain
                )

                Text(
                    text = "Strictly Real Data",
                    fontSize = 11.sp,
                    color = SentrySuccessGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Event logs (Real database items)
        if (allEvents.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SentryDarkCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SentryDarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "No distractions intercepted yet today.",
                            fontSize = 13.sp,
                            color = SentryDarkTextMuted
                        )
                        Button(
                            onClick = { viewModel.testSimulateInterception("Instagram", "Instagram Reels") },
                            colors = ButtonDefaults.buttonColors(containerColor = SentryGoldCTA)
                        ) {
                            Text("Simulate Interception Event", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(allEvents.take(12)) { event ->
                BlockedEventLogItem(event)
            }
        }
    }
}

@Composable
fun PlatformBarItem(name: String, count: Int, percent: Int, color: Color, emoji: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(name, fontSize = 12.sp, color = SentryDarkTextMain, fontWeight = FontWeight.Medium)
            }
            Text("$count ($percent%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SentryDarkTextMain)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFF22262C), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((percent / 100f).coerceIn(0f, 1f))
                    .height(6.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun BlockedEventLogItem(event: BlockedEvent) {
    val dateStr = SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date(event.timestamp))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SentryDarkCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, SentryDarkCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF262930), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (event.appName) {
                            "Instagram" -> "📸"
                            "YouTube" -> "▶️"
                            "Facebook" -> "📘"
                            "TikTok" -> "🎵"
                            else -> "📱"
                        },
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${event.appName} (${event.feedType})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SentryDarkTextMain
                    )
                    Text(
                        text = "${event.actionTaken} • Saved ~1.8m",
                        fontSize = 11.sp,
                        color = SentryDarkTextMuted
                    )
                }
            }

            Text(
                text = dateStr,
                fontSize = 11.sp,
                color = SentryDarkTextMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
