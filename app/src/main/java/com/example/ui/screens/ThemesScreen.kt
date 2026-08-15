package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SentryViewModel
import com.example.ui.theme.AppThemeData
import com.example.ui.theme.AppThemes
import com.example.ui.theme.SentryTheme

@Composable
fun ThemesScreen(
    viewModel: SentryViewModel,
    onBack: () -> Unit
) {
    val theme = SentryTheme
    val profile by viewModel.userProfile.collectAsState()
    val currentThemeId = profile.selectedThemeId

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.canvas)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textMain
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "THEMES & STYLES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primary,
                        letterSpacing = 1.2.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(theme.accent)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "10 THEMES",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
                Text(
                    text = "অ্যাপের থিম ও ভিজ্যুয়াল স্টাইল",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMain
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Active Theme Live Preview Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = theme.card,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
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
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(theme.iconBadgeBg)
                                        .border(1.dp, theme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = theme.themeIcon, fontSize = 22.sp)
                                }
                                Column {
                                    Text(
                                        text = "CURRENTLY ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.primary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = theme.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textMain
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.primary)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = theme.description,
                            fontSize = 13.sp,
                            color = theme.textMuted
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        // Mini preview palette bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PalettePill(label = "Canvas", color = theme.canvas, border = theme.cardBorder)
                            PalettePill(label = "Card", color = theme.card, border = theme.cardBorder)
                            PalettePill(label = "Primary", color = theme.primary, border = theme.primary)
                            PalettePill(label = "Accent", color = theme.accent, border = theme.accent)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "SELECT FROM 10 UNIQUE PALETTES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMuted,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // List all 10 themes
            items(AppThemes.allThemes, key = { it.id }) { themeItem ->
                ThemeCard(
                    themeItem = themeItem,
                    isActive = themeItem.id == currentThemeId,
                    onSelect = {
                        viewModel.setTheme(themeItem.id)
                        viewModel.playSoundTest("fahh")
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ThemeCard(
    themeItem: AppThemeData,
    isActive: Boolean,
    onSelect: () -> Unit
) {
    val currentTheme = SentryTheme
    val borderColor = if (isActive) themeItem.primary else currentTheme.cardBorder

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = currentTheme.card,
        border = androidx.compose.foundation.BorderStroke(if (isActive) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(themeItem.iconBadgeBg)
                            .border(1.dp, themeItem.primary.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = themeItem.themeIcon, fontSize = 22.sp)
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = themeItem.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentTheme.textMain
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(themeItem.primary.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = themeItem.badge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeItem.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = themeItem.nameBn,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = currentTheme.textMuted
                        )
                    }
                }

                // Check or select button
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(themeItem.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeItem.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(text = "Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = themeItem.description,
                fontSize = 12.sp,
                color = currentTheme.textMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Palette swatch preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(currentTheme.cardSecondary)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Colors:", fontSize = 11.sp, color = currentTheme.textMuted)
                    SwatchCircle(color = themeItem.canvas, label = "Canvas")
                    SwatchCircle(color = themeItem.card, label = "Card")
                    SwatchCircle(color = themeItem.primary, label = "Primary")
                    SwatchCircle(color = themeItem.accent, label = "Accent")
                }

                // Mini preview button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(themeItem.heroGradientStart, themeItem.heroGradientEnd)
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Sample Vibe",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SwatchCircle(color: Color, label: String) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
    )
}

@Composable
private fun PalettePill(
    label: String,
    color: Color,
    border: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color,
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        modifier = Modifier
            .height(28.dp)
            .width(68.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (color.isLight()) Color.Black else Color.White
            )
        }
    }
}

private fun Color.isLight(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.5
}
