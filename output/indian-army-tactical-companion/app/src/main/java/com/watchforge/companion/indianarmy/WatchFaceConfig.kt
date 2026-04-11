package com.watchforge.companion.indianarmy

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

/**
 * ★ FACTORY CONFIG — This is the ONLY file to edit per watch face. ★
 *
 * Change these values + swap drawable assets to create a companion for any watch face.
 */
object WatchFaceConfig {

    // ── Identity ──────────────────────────────────────────────────────────
    const val APP_NAME = "Indian Army Tactical"
    const val TAGLINE = "Rugged 7-segment LCD tactical watch face for Wear OS — inspired by Indian Army field-issue timepieces."
    const val DESCRIPTION = "Authentic DSEG7 7-segment LCD digits, military-grade layout, " +
            "and full customization. Designed with pride for the soldiers and veterans " +
            "of the Indian Armed Forces."

    // ── Play Store ────────────────────────────────────────────────────────
    const val WATCH_FACE_PACKAGE_ID = "com.army.tectical"

    // ── Theme Colors ──────────────────────────────────────────────────────
    val accentColor = Color(0xFFFF8C00)       // Tactical amber
    val accentColorDim = Color(0xFFCC6A00)
    val backgroundColor = Color(0xFF0A0A0A)
    val surfaceColor = Color(0xFF0D1A0D)
    val borderColor = Color(0xFF4A5D23)
    val textColor = Color(0xFFE8E8E8)
    val mutedColor = Color(0xFF888888)

    // ── Special Accent (e.g. tricolor) ────────────────────────────────────
    val hasTricolor = true
    val tricolorSaffron = Color(0xFFFF9933)
    val tricolorWhite = Color(0xFFFFFFFF)
    val tricolorGreen = Color(0xFF138808)

    // ── Hero ──────────────────────────────────────────────────────────────
    @DrawableRes val heroScreenshot = R.drawable.wf_preview
    @DrawableRes val appIcon = R.drawable.wf_icon

    // ── Themes ────────────────────────────────────────────────────────────
    val themes = listOf(
        ThemeItem("Tactical Amber", Color(0xFFFF8C00), R.drawable.theme_tactical_amber),
        ThemeItem("Tactical Green", Color(0xFF4CAF50), R.drawable.theme_tactical_green),
        ThemeItem("Urban Blue", Color(0xFF42A5F5), R.drawable.theme_urban_blue),
        ThemeItem("Stealth Black", Color(0xFFAAAAAA), R.drawable.theme_stealth_black),
        ThemeItem("Red Alert", Color(0xFFFF4444), R.drawable.theme_red_alert),
    )

    // ── Features ──────────────────────────────────────────────────────────
    val features = listOf(
        FeatureItem(
            label = "DISPLAY",
            description = "Authentic DSEG7 7-segment LCD font with ghost segment effect for a real LCD look"
        ),
        FeatureItem(
            label = "COMPLICATIONS",
            description = "Battery bar, heart rate (BPM), daily step count, and a configurable weather/info slot"
        ),
        FeatureItem(
            label = "MILITARY TIME",
            description = "Auto-follows device format — 24H or 12H with AM/PM indicator. Day-of-year counter included"
        ),
        FeatureItem(
            label = "INDIAN TRICOLOR",
            description = "Saffron, white, and green accent lines honoring the Indian national flag"
        ),
        FeatureItem(
            label = "AMBIENT MODE",
            description = "Monochrome HH:MM display in ambient — OLED-safe, burn-in protected"
        ),
        FeatureItem(
            label = "LIGHTWEIGHT",
            description = "Under 110 KB. No phone companion required. No permissions. No data collection."
        ),
    )

    // ── Footer ────────────────────────────────────────────────────────────
    const val FOOTER_TEXT = "=== TACTICAL ==="
    const val COPYRIGHT = "© 2026 Indian Army Tactical. Designed with pride for the soldiers and veterans of the Indian Armed Forces."
}

data class ThemeItem(
    val name: String,
    val color: Color,
    @DrawableRes val previewRes: Int,
)

data class FeatureItem(
    val label: String,
    val description: String,
)
