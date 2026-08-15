package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

enum class SubscriptionPlan(
    val title: String,
    val priceText: String,
    val billingCycle: String,
    val discountBadge: String?,
    val isBestValue: Boolean
) {
    MONTHLY(
        title = "Monthly Discipline",
        priceText = "$4.99",
        billingCycle = "/ month",
        discountBadge = null,
        isBestValue = false
    ),
    ANNUAL(
        title = "Annual Freedom (Best Value)",
        priceText = "$24.99",
        billingCycle = "/ year ($2.08/mo)",
        discountBadge = "SAVE 70%",
        isBestValue = true
    ),
    LIFETIME(
        title = "Lifetime Dopamine Master",
        priceText = "$49.99",
        billingCycle = "one-time payment",
        discountBadge = "FOREVER",
        isBestValue = false
    )
}

@Composable
fun SubscriptionScreen(
    viewModel: SentryViewModel,
    onBack: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    var selectedPlan by remember { mutableStateOf(SubscriptionPlan.ANNUAL) }
    var showPlanDialog by remember { mutableStateOf(false) }

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
                text = "Pro Subscription Hub",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SentryDarkTextMain
            )

            Box(modifier = Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SentryDarkCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SentryDarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(if (profile.isPro) SentryGoldCTA else SentrySuccessGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.name.take(1).uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (profile.isPro) Color.Black else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = profile.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SentryDarkTextMain
                    )

                    Text(
                        text = profile.email,
                        fontSize = 13.sp,
                        color = SentryDarkTextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Plan Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (profile.isPro) Color(0xFF261842) else Color(0xFF2A2E35))
                            .border(1.dp, if (profile.isPro) SentryGoldCTA else SentryDarkCardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (profile.isPro) {
                                Icon(Icons.Default.Star, contentDescription = "Pro", tint = SentryGoldCTA, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = if (profile.isPro) "PRO MEMBER (UNLIMITED)" else "FREE TIER",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (profile.isPro) SentryGoldCTA else SentryDarkTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🔥 CURRENT STREAK", fontSize = 10.sp, color = SentryDarkTextMuted, fontWeight = FontWeight.Bold)
                            Text(text = "${profile.streakDays} Days", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SentryDarkTextMain)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "⭐ KARMA XP", fontSize = 10.sp, color = SentryDarkTextMuted, fontWeight = FontWeight.Bold)
                            Text(text = "${profile.karmaXp} XP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SentryGoldCTA)
                        }
                    }
                }
            }

            // Subscription Plan Selector Cards
            Text(
                text = "CHOOSE YOUR FREEDOM PLAN",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SentryDarkTextMuted,
                letterSpacing = 1.sp
            )

            SubscriptionPlan.values().forEach { plan ->
                val isSelected = selectedPlan == plan
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) Color(0xFF261D1A) else SentryDarkCard,
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) SentryGoldCTA else SentryDarkCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPlan = plan }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = plan.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SentryDarkTextMain
                                )
                                plan.discountBadge?.let { badge ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (plan.isBestValue) SentryGoldCTA else SentryBrandRed)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = badge,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = plan.billingCycle,
                                fontSize = 12.sp,
                                color = SentryDarkTextMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = plan.priceText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) SentryGoldCTA else SentryDarkTextMain
                            )
                        }
                    }
                }
            }

            // Feature Comparison Table
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SentryDarkCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SentryDarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Features & Addictions Guard", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SentryDarkTextMuted, modifier = Modifier.weight(1f))
                        Text(text = "Free", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SentryDarkTextMuted, modifier = Modifier.width(48.dp))
                        Text(text = "Pro", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SentryGoldCTA, modifier = Modifier.width(48.dp))
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SentryDarkCardBorder))

                    // Comparison rows
                    ComparisonRow("Reels & Shorts Blocker", free = true, pro = true)
                    ComparisonRow("Study Mode Timer", free = true, pro = true)
                    ComparisonRow("Daily App Limits", free = true, pro = true)
                    ComparisonRow("Audio Mods (Fahh & Rag)", free = true, pro = true)
                    ComparisonRow("5-Second Mindful Pause", free = true, pro = true)
                    ComparisonRow("Website Blocker (Basic)", free = true, pro = true)
                    ComparisonRow("Adult Content Shield", free = true, pro = true)
                    ComparisonRow("Prime Commitment Lock (Hardcore)", free = false, pro = true)
                    ComparisonRow("Uninstall Protection Shield", free = false, pro = true)
                    ComparisonRow("Unlimited Smart Schedules", free = false, pro = true)
                    ComparisonRow("Endless Scroll Limiter", free = false, pro = true)
                    ComparisonRow("Loud Siren & Shame Sound Alarm", free = false, pro = true)
                    ComparisonRow("AI Relapse & Trigger Predictor", free = false, pro = true)
                    ComparisonRow("Unlimited Custom Web Blacklist", free = false, pro = true)
                    ComparisonRow("Weekly PDF Detox Analytics", free = false, pro = true)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Bottom Yellow Action Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SentryDarkCanvas)
                .padding(16.dp)
        ) {
            Button(
                onClick = { showPlanDialog = true },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SentryGoldCTA,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (profile.isPro) "Manage Active Subscription" else "Start 7-Day Free Trial (${selectedPlan.priceText})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }

    if (showPlanDialog) {
        AlertDialog(
            onDismissRequest = { showPlanDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Star, contentDescription = "Pro", tint = SentryGoldCTA)
                    Text("Social Addiction PRO", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Plan: ${selectedPlan.title} (${selectedPlan.priceText} ${selectedPlan.billingCycle})",
                        color = SentryGoldCTA,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Gain instant access to Hardcore Prime Mode, Anti-Uninstall Shield, Smart Schedules, Audible Sirens, and Unlimited Blacklists.",
                        color = SentryDarkTextMuted,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleProAccount()
                        showPlanDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SentryGoldCTA)
                ) {
                    Text(
                        if (profile.isPro) "Downgrade to Free" else "Confirm & Unlock PRO",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPlanDialog = false }) {
                    Text("Cancel", color = SentryDarkTextMuted)
                }
            },
            containerColor = SentryDarkCard
        )
    }
}

@Composable
fun ComparisonRow(
    title: String,
    free: Boolean,
    pro: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                color = SentryDarkTextMain,
                modifier = Modifier.weight(1f)
            )

            Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.Center) {
                if (free) {
                    Icon(Icons.Default.Check, contentDescription = "Yes", tint = SentrySuccessGreen, modifier = Modifier.size(16.dp))
                } else {
                    Text("-", color = SentryDarkTextMuted, fontSize = 16.sp)
                }
            }

            Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.Center) {
                if (pro) {
                    Icon(Icons.Default.Check, contentDescription = "Yes", tint = SentryGoldCTA, modifier = Modifier.size(16.dp))
                } else {
                    Text("-", color = SentryDarkTextMuted, fontSize = 16.sp)
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SentryDarkCardBorder.copy(alpha = 0.5f)))
    }
}
