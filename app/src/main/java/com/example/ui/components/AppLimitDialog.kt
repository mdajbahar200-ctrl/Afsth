package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLimit
import com.example.ui.theme.SentryLightPurple
import com.example.ui.theme.SentryPurple
import com.example.ui.theme.SentryTextPrimary
import com.example.ui.theme.SentryTextSecondary

@Composable
fun AppLimitDialog(
    limit: AppLimit,
    onSave: (AppLimit) -> Unit,
    onDismiss: () -> Unit
) {
    var limitMinutes by remember { mutableFloatStateOf(limit.dailyLimitMinutes.toFloat()) }
    var isShortsBlocked by remember { mutableStateOf(limit.isShortsBlocked) }
    var isHardLocked by remember { mutableStateOf(limit.isHardLocked) }
    var isEnabled by remember { mutableStateOf(limit.isEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF3F3F7),
                    modifier = Modifier.size(40.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(limit.iconEmoji, fontSize = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = limit.appName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SentryTextPrimary
                    )
                    Text(
                        text = limit.category,
                        fontSize = 12.sp,
                        color = SentryTextSecondary
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Daily limit slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Screen Time Limit",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SentryTextPrimary
                    )
                    Text(
                        text = "${limitMinutes.toInt()} mins",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SentryPurple
                    )
                }

                Slider(
                    value = limitMinutes,
                    onValueChange = { limitMinutes = it },
                    valueRange = 5f..180f,
                    steps = 34, // 5 min increments
                    colors = SliderDefaults.colors(
                        thumbColor = SentryPurple,
                        activeTrackColor = SentryPurple,
                        inactiveTrackColor = SentryLightPurple.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Short form blocker toggle
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF3F3F7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Block Shorts / Reels Feed",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SentryTextPrimary
                            )
                            Text(
                                text = "Surgically intercepts infinite swipe video tabs",
                                fontSize = 11.sp,
                                color = SentryTextSecondary
                            )
                        }
                        Switch(
                            checked = isShortsBlocked,
                            onCheckedChange = { isShortsBlocked = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SentryPurple
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hard lock toggle
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF3F3F7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hard Lock Overlay",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SentryTextPrimary
                            )
                            Text(
                                text = "Locks full app once limit is exhausted",
                                fontSize = 11.sp,
                                color = SentryTextSecondary
                            )
                        }
                        Switch(
                            checked = isHardLocked,
                            onCheckedChange = { isHardLocked = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SentryPurple
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        limit.copy(
                            dailyLimitMinutes = limitMinutes.toInt(),
                            isShortsBlocked = isShortsBlocked,
                            isHardLocked = isHardLocked,
                            isEnabled = isEnabled
                        )
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SentryPurple,
                    contentColor = Color.White
                )
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancel", color = SentryTextSecondary)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
