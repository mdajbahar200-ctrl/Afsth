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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.SentryTheme

@Composable
fun ProfileSettingsScreen(
    viewModel: SentryViewModel,
    onBack: () -> Unit
) {
    val theme = SentryTheme
    val profile by viewModel.userProfile.collectAsState()
    val allEvents by viewModel.allBlockedEvents.collectAsState()
    val allSessions by viewModel.allFocusSessions.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(profile.name) }
    var editUsername by remember { mutableStateOf(profile.username) }
    var editEmail by remember { mutableStateOf(profile.email) }

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

            Text(
                text = "Profile Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textMain
            )

            IconButton(onClick = {
                editName = profile.name
                editUsername = profile.username
                editEmail = profile.email
                showEditProfileDialog = true
            }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = theme.primary
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
            // Profile Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = theme.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(theme.accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.name.take(1).uppercase(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (theme.accent.isLight()) Color.Black else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = profile.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMain
                    )

                    Text(
                        text = profile.username,
                        fontSize = 13.sp,
                        color = theme.textMuted
                    )

                    Text(
                        text = profile.email,
                        fontSize = 12.sp,
                        color = theme.textMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (profile.isPro) theme.primary.copy(alpha = 0.2f) else theme.cardSecondary)
                            .border(1.dp, if (profile.isPro) theme.primary else theme.cardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (profile.isPro) "PRO ACCOUNT" else "FREE TIER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (profile.isPro) theme.primary else theme.textMuted
                        )
                    }
                }
            }

            // Theme Active Card
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.iconBadgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = theme.themeIcon, fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                text = "Active Theme: ${theme.name}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textMain
                            )
                            Text(
                                text = theme.nameBn,
                                fontSize = 12.sp,
                                color = theme.textMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.primary)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = theme.badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Discipline Milestones Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = theme.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "DISCIPLINE MILESTONES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMuted,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "🔥 STREAK", fontSize = 11.sp, color = theme.textMuted, fontWeight = FontWeight.Bold)
                            Text(text = "${profile.streakDays} Days", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
                        }
                        Column {
                            Text(text = "⭐ KARMA XP", fontSize = 11.sp, color = theme.textMuted, fontWeight = FontWeight.Bold)
                            Text(text = "${profile.karmaXp} XP", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = theme.accent)
                        }
                        Column {
                            Text(text = "📈 BEST STREAK", fontSize = 11.sp, color = theme.textMuted, fontWeight = FontWeight.Bold)
                            Text(text = "${profile.bestStreakDays} Days", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = theme.success)
                        }
                    }
                }
            }

            // Lifetime Stats Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = theme.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "REAL LIFETIME METRICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMuted,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Distractions Intercepted", fontSize = 12.sp, color = theme.textMuted)
                            Text(text = "${allEvents.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
                        }
                        Column {
                            Text(text = "Completed Deep Sessions", fontSize = 12.sp, color = theme.textMuted)
                            Text(text = "${allSessions.count { it.completed }}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = theme.textMain)
                        }
                    }
                }
            }

            // Cloud Sync & Web Admin Card
            val cloudStatus by viewModel.cloudSyncStatus.collectAsState()
            val remoteAnnouncement by viewModel.remoteAnnouncement.collectAsState()

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = theme.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CLOUD ADMIN & BACKUP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = cloudStatus,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primary
                            )
                        }
                        Button(
                            onClick = { viewModel.triggerCloudSync() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary.copy(alpha = 0.15f))
                        ) {
                            Text("Sync Now", color = theme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = theme.cardSecondary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Firebase Project: social-sentry-78d1a",
                                fontSize = 11.sp,
                                color = theme.textMuted
                            )
                            Text(
                                text = "Device ID: ${viewModel.getCloudDeviceId()}",
                                fontSize = 11.sp,
                                color = theme.textMuted
                            )
                            Text(
                                text = "Status: ${if (profile.isPro) "PRO / LIFETIME ACTIVE" else "FREE TIER (Upgrade in Admin Panel)"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (profile.isPro) theme.accent else theme.textMain
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(theme.cardBorder)
                                    .padding(vertical = 2.dp)
                            )

                            Text(
                                text = "🌐 Super Admin Web Dashboard Link:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primary
                            )
                            Text(
                                text = "প্রজেক্টের 'admin_dashboard/index.html' ফাইলটি সরাসরি যেকোনো ব্রাউজারে খুলুন অথবা Firebase Hosting এ হোস্ট করুন।",
                                fontSize = 11.sp,
                                color = theme.textMuted
                            )
                        }
                    }

                    if (!remoteAnnouncement.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = theme.accent.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.accent.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("📢 Admin Broadcast", color = theme.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(remoteAnnouncement ?: "", color = theme.textMain, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsState()

            // Google Account Sync Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = theme.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            Column {
                                Text(
                                    text = "Google Account Sync",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textMain
                                )
                                Text(
                                    text = if (isGoogleSignedIn) "Connected: ${profile.email}" else "Not signed in (Local only)",
                                    fontSize = 11.sp,
                                    color = if (isGoogleSignedIn) theme.accent else theme.textMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isGoogleSignedIn) theme.accent.copy(alpha = 0.2f) else theme.cardSecondary)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isGoogleSignedIn) "SYNCED" else "OFFLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGoogleSignedIn) theme.accent else theme.textMuted
                            )
                        }
                    }

                    if (isGoogleSignedIn) {
                        Button(
                            onClick = { viewModel.signOutGoogle() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.cardSecondary),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Text("Sign Out from Google", color = theme.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.performGoogleSignIn() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Text("Sign In with Google", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Account Actions
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = theme.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            editName = profile.name
                            editUsername = profile.username
                            editEmail = profile.email
                            showEditProfileDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.cardSecondary),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Edit Name & Username", color = theme.textMain, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { viewModel.toggleProAccount() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (profile.isPro) theme.primary.copy(alpha = 0.2f) else theme.primary
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = if (profile.isPro) "Revert to Free Account" else "Upgrade to Pro Tier",
                            color = if (profile.isPro) theme.primary else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", color = theme.textMain, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name", color = theme.textMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textMain,
                            unfocusedTextColor = theme.textMain,
                            focusedBorderColor = theme.primary,
                            unfocusedBorderColor = theme.cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Username", color = theme.textMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textMain,
                            unfocusedTextColor = theme.textMain,
                            focusedBorderColor = theme.primary,
                            unfocusedBorderColor = theme.cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email", color = theme.textMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textMain,
                            unfocusedTextColor = theme.textMain,
                            focusedBorderColor = theme.primary,
                            unfocusedBorderColor = theme.cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserProfile(editName, editUsername, editEmail)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = theme.textMuted)
                }
            },
            containerColor = theme.card
        )
    }
}

private fun Color.isLight(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.5
}
