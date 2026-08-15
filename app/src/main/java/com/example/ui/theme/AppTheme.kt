package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppThemeData(
    val id: String,
    val name: String,
    val nameBn: String,
    val description: String,
    val badge: String,
    val themeIcon: String, // Decorative theme emoji icon
    val isDark: Boolean,
    
    // Core Backgrounds
    val canvas: Color,
    val card: Color,
    val cardSecondary: Color,
    val cardBorder: Color,
    
    // Brand & Accents
    val primary: Color,
    val primaryVariant: Color,
    val accent: Color,
    val accentSecondary: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    
    // Typography Colors
    val textMain: Color,
    val textMuted: Color,
    
    // Icon badge colors
    val iconBadgeBg: Color,
    val heroGradientStart: Color,
    val heroGradientEnd: Color
)

object AppThemes {
    // 1. Cyberpunk Neon (Default)
    val Cyberpunk = AppThemeData(
        id = "cyberpunk",
        name = "Cyberpunk Neon",
        nameBn = "সাইবারপাঙ্ক নিয়ন",
        description = "High-octane neon crimson & cyber gold with deep carbon canvas",
        badge = "DEFAULT",
        themeIcon = "⚡",
        isDark = true,
        canvas = Color(0xFF111315),
        card = Color(0xFF1C1E22),
        cardSecondary = Color(0xFF24272D),
        cardBorder = Color(0xFF2A2E35),
        primary = Color(0xFFFF4B72),
        primaryVariant = Color(0xFFE0365D),
        accent = Color(0xFFFFC226),
        accentSecondary = Color(0xFF8B5CF6),
        success = Color(0xFF22C55E),
        warning = Color(0xFFFFC226),
        error = Color(0xFFFF4B72),
        textMain = Color(0xFFFFFFFF),
        textMuted = Color(0xFF8E95A2),
        iconBadgeBg = Color(0xFF2C1924),
        heroGradientStart = Color(0xFFFF4B72),
        heroGradientEnd = Color(0xFFFFC226)
    )

    // 2. OLED Midnight Obsidian (Pure Black Battery Saver)
    val MidnightOled = AppThemeData(
        id = "midnight_oled",
        name = "Midnight Obsidian OLED",
        nameBn = "মিডনাইট ওলেড ব্ল্যাক",
        description = "Pure pitch-black OLED background with vivid electric cyan highlights",
        badge = "BATTERY SAVER",
        themeIcon = "🌑",
        isDark = true,
        canvas = Color(0xFF000000),
        card = Color(0xFF0D0E12),
        cardSecondary = Color(0xFF16181F),
        cardBorder = Color(0xFF222630),
        primary = Color(0xFF00E5FF),
        primaryVariant = Color(0xFF00B0FF),
        accent = Color(0xFF76FF03),
        accentSecondary = Color(0xFF00E5FF),
        success = Color(0xFF76FF03),
        warning = Color(0xFFFFD600),
        error = Color(0xFFFF1744),
        textMain = Color(0xFFFFFFFF),
        textMuted = Color(0xFF7E889B),
        iconBadgeBg = Color(0xFF0A2229),
        heroGradientStart = Color(0xFF00E5FF),
        heroGradientEnd = Color(0xFF76FF03)
    )

    // 3. Forest Zen Detox (Calming Emerald & Sage)
    val ForestZen = AppThemeData(
        id = "forest_zen",
        name = "Forest Zen Detox",
        nameBn = "ফরেস্ট জেন ডিটক্স",
        description = "Mindful organic emerald green & calm forest tones to soothe urges",
        badge = "CALM DETOX",
        themeIcon = "🌿",
        isDark = true,
        canvas = Color(0xFF0B1710),
        card = Color(0xFF13241A),
        cardSecondary = Color(0xFF1B3325),
        cardBorder = Color(0xFF284A37),
        primary = Color(0xFF10B981),
        primaryVariant = Color(0xFF059669),
        accent = Color(0xFFF59E0B),
        accentSecondary = Color(0xFF34D399),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        error = Color(0xFFF87171),
        textMain = Color(0xFFECFDF5),
        textMuted = Color(0xFF86A896),
        iconBadgeBg = Color(0xFF163826),
        heroGradientStart = Color(0xFF10B981),
        heroGradientEnd = Color(0xFF34D399)
    )

    // 4. Deep Space Nebula (Cosmic Violet & Star White)
    val SpaceNebula = AppThemeData(
        id = "space_nebula",
        name = "Cosmic Nebula",
        nameBn = "কসমিক নেবুলা স্পেস",
        description = "Mystical cosmic purple, galactic starlight magenta & astral blue",
        badge = "COSMIC",
        themeIcon = "🌌",
        isDark = true,
        canvas = Color(0xFF0D0E1E),
        card = Color(0xFF161830),
        cardSecondary = Color(0xFF202344),
        cardBorder = Color(0xFF303463),
        primary = Color(0xFFA855F7),
        primaryVariant = Color(0xFF9333EA),
        accent = Color(0xFFEC4899),
        accentSecondary = Color(0xFF6366F1),
        success = Color(0xFF38BDF8),
        warning = Color(0xFFFBBF24),
        error = Color(0xFFFB7185),
        textMain = Color(0xFFF8FAFC),
        textMuted = Color(0xFF94A3B8),
        iconBadgeBg = Color(0xFF2B1D45),
        heroGradientStart = Color(0xFFA855F7),
        heroGradientEnd = Color(0xFFEC4899)
    )

    // 5. Nordic Arctic Frost (Icy Titanium & Crisp Glacier Blue)
    val NordicFrost = AppThemeData(
        id = "nordic_frost",
        name = "Nordic Arctic Frost",
        nameBn = "নর্ডিক আর্কটিক ফ্রস্ট",
        description = "Cool Scandinavian glacier blue with sleek icy titanium dark slate",
        badge = "SHARP FOCUS",
        themeIcon = "❄️",
        isDark = true,
        canvas = Color(0xFF0A111E),
        card = Color(0xFF121E31),
        cardSecondary = Color(0xFF1A2A43),
        cardBorder = Color(0xFF263D60),
        primary = Color(0xFF38BDF8),
        primaryVariant = Color(0xFF0284C7),
        accent = Color(0xFF818CF8),
        accentSecondary = Color(0xFF67E8F9),
        success = Color(0xFF34D399),
        warning = Color(0xFFFBBF24),
        error = Color(0xFFF43F5E),
        textMain = Color(0xFFF0F9FF),
        textMuted = Color(0xFF7DD3FC).copy(alpha = 0.75f),
        iconBadgeBg = Color(0xFF102D47),
        heroGradientStart = Color(0xFF38BDF8),
        heroGradientEnd = Color(0xFF818CF8)
    )

    // 6. Solar Flare Amber (Warm Charcoal & Radiant Sunset)
    val SolarFlare = AppThemeData(
        id = "solar_flare",
        name = "Solar Flare Amber",
        nameBn = "সোলার অ্যাম্বার ফ্লেয়ার",
        description = "Warm energized amber sunset tones over velvety charcoal",
        badge = "WARM ENERGY",
        themeIcon = "☀️",
        isDark = true,
        canvas = Color(0xFF16110D),
        card = Color(0xFF231B15),
        cardSecondary = Color(0xFF30251C),
        cardBorder = Color(0xFF453427),
        primary = Color(0xFFF97316),
        primaryVariant = Color(0xFFEA580C),
        accent = Color(0xFFFBBF24),
        accentSecondary = Color(0xFFEF4444),
        success = Color(0xFF84CC16),
        warning = Color(0xFFFBBF24),
        error = Color(0xFFEF4444),
        textMain = Color(0xFFFFFBEB),
        textMuted = Color(0xFFA89682),
        iconBadgeBg = Color(0xFF3B2412),
        heroGradientStart = Color(0xFFF97316),
        heroGradientEnd = Color(0xFFFBBF24)
    )

    // 7. Tokyo Synthwave 80s (Retro Magenta & Laser Cyan)
    val TokyoSynthwave = AppThemeData(
        id = "synthwave_80s",
        name = "Tokyo Synthwave 80s",
        nameBn = "টোকিও সিন্থওয়েভ ৮০s",
        description = "Retro-futuristic hyper neon laser magenta, hot pink and ultraviolet",
        badge = "RETRO VIBE",
        themeIcon = "🕹️",
        isDark = true,
        canvas = Color(0xFF140B1E),
        card = Color(0xFF211333),
        cardSecondary = Color(0xFF2E1B46),
        cardBorder = Color(0xFF47286D),
        primary = Color(0xFFD946EF),
        primaryVariant = Color(0xFFC026D3),
        accent = Color(0xFF06B6D4),
        accentSecondary = Color(0xFFF43F5E),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        error = Color(0xFFF43F5E),
        textMain = Color(0xFFFDF4FF),
        textMuted = Color(0xFFB58DBF),
        iconBadgeBg = Color(0xFF3B184C),
        heroGradientStart = Color(0xFFD946EF),
        heroGradientEnd = Color(0xFF06B6D4)
    )

    // 8. Crimson Samurai (Deep Maroon & Imperial Gold)
    val CrimsonSamurai = AppThemeData(
        id = "crimson_samurai",
        name = "Crimson Samurai",
        nameBn = "ক্রিমসন সামুরাই",
        description = "Unbreakable willpower theme with imperial blood crimson and gold",
        badge = "WARRIOR",
        themeIcon = "⚔️",
        isDark = true,
        canvas = Color(0xFF170C0F),
        card = Color(0xFF241318),
        cardSecondary = Color(0xFF321A21),
        cardBorder = Color(0xFF4A2530),
        primary = Color(0xFFEF4444),
        primaryVariant = Color(0xFFDC2626),
        accent = Color(0xFFEAB308),
        accentSecondary = Color(0xFFF87171),
        success = Color(0xFF22C55E),
        warning = Color(0xFFEAB308),
        error = Color(0xFFEF4444),
        textMain = Color(0xFFFEF2F2),
        textMuted = Color(0xFFA6878D),
        iconBadgeBg = Color(0xFF38151D),
        heroGradientStart = Color(0xFFEF4444),
        heroGradientEnd = Color(0xFFEAB308)
    )

    // 9. Dracula Eclipse (Gothic Slate & Pastel Mint)
    val DraculaEclipse = AppThemeData(
        id = "dracula_eclipse",
        name = "Dracula Eclipse",
        nameBn = "ড্রাকুলা এক্লিপ্স",
        description = "Refined developer-loved gothic palette with pastel mint and soft lilac",
        badge = "PRO CODER",
        themeIcon = "🦇",
        isDark = true,
        canvas = Color(0xFF191A2A),
        card = Color(0xFF24273E),
        cardSecondary = Color(0xFF2E324E),
        cardBorder = Color(0xFF3F4469),
        primary = Color(0xFF50FA7B),
        primaryVariant = Color(0xFF5AF78E),
        accent = Color(0xFFFF79C6),
        accentSecondary = Color(0xFFBD93F9),
        success = Color(0xFF50FA7B),
        warning = Color(0xFFFFB86C),
        error = Color(0xFFFF5555),
        textMain = Color(0xFFF8F8F2),
        textMuted = Color(0xFF9AA0C2),
        iconBadgeBg = Color(0xFF1F3532),
        heroGradientStart = Color(0xFFBD93F9),
        heroGradientEnd = Color(0xFF50FA7B)
    )

    // 10. Clean Paper Light (Crisp Minimalist Daylight)
    val CleanPaperLight = AppThemeData(
        id = "paper_light",
        name = "Clean Paper Daylight",
        nameBn = "ক্লিন পেপার ডে-লাইট",
        description = "Crisp, airy high-contrast daylight theme with cobalt blue & amber",
        badge = "LIGHT MODE",
        themeIcon = "☀️",
        isDark = false,
        canvas = Color(0xFFF4F6F9),
        card = Color(0xFFFFFFFF),
        cardSecondary = Color(0xFFEBF0F6),
        cardBorder = Color(0xFFD9E2EC),
        primary = Color(0xFF2563EB),
        primaryVariant = Color(0xFF1D4ED8),
        accent = Color(0xFFD97706),
        accentSecondary = Color(0xFF7C3AED),
        success = Color(0xFF16A34A),
        warning = Color(0xFFD97706),
        error = Color(0xFFDC2626),
        textMain = Color(0xFF0F172A),
        textMuted = Color(0xFF64748B),
        iconBadgeBg = Color(0xFFDBEAFE),
        heroGradientStart = Color(0xFF2563EB),
        heroGradientEnd = Color(0xFF06B6D4)
    )

    val allThemes: List<AppThemeData> = listOf(
        Cyberpunk,
        MidnightOled,
        ForestZen,
        SpaceNebula,
        NordicFrost,
        SolarFlare,
        TokyoSynthwave,
        CrimsonSamurai,
        DraculaEclipse,
        CleanPaperLight
    )

    fun getThemeById(id: String): AppThemeData {
        return allThemes.find { it.id == id } ?: Cyberpunk
    }
}

val LocalAppTheme = staticCompositionLocalOf { AppThemes.Cyberpunk }

val SentryTheme: AppThemeData
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current
