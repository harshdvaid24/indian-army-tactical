package com.watchforge.companion.midnightcarrier

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

/**
 * ★ FACTORY CONFIG — This is the ONLY file to edit per watch face. ★
 *
 * Change these values + swap drawable assets to create a companion for any watch face.
 */
object WatchFaceConfig {

    // ── Identity ──────────────────────────────────────────────────────────
    const val APP_NAME = "Midnight Carrier"
    const val TAGLINE = "Dark matte watch face inspired by aircraft carriers under the night sky — precision operations in low light."
    const val DESCRIPTION = "Runway-style markers, glowing indicators, and authentic DSEG7 LCD digits. " +
            "Animated carrier silhouette floating over sea waves. " +
            "Dedicated to the Indian Navy."

    // ── Play Store ────────────────────────────────────────────────────────
    const val WATCH_FACE_PACKAGE_ID = "com.watchforge.midnightcarrier"

    // ── Theme Colors ──────────────────────────────────────────────────────
    val accentColor = Color(0xFF00E5FF)       // Carrier cyan
    val accentColorDim = Color(0xFF00B8CC)
    val backgroundColor = Color(0xFF050810)
    val surfaceColor = Color(0xFF0A1020)
    val borderColor = Color(0xFF1A2540)
    val textColor = Color(0xFFCCDDEE)
    val mutedColor = Color(0xFF667788)

    // ── Special Accent (e.g. tricolor) ────────────────────────────────────
    val hasTricolor = false
    val tricolorSaffron = Color(0xFFFF9933)
    val tricolorWhite = Color(0xFFFFFFFF)
    val tricolorGreen = Color(0xFF138808)

    // ── Hero ──────────────────────────────────────────────────────────────
    @DrawableRes val heroScreenshot = R.drawable.wf_preview
    @DrawableRes val appIcon = R.drawable.wf_icon

    // ── Themes ────────────────────────────────────────────────────────────
    val themes = listOf(
        ThemeItem("Carrier Cyan", Color(0xFF00E5FF), R.drawable.theme_carrier_cyan),
        ThemeItem("Runway Amber", Color(0xFFFFAA00), R.drawable.theme_runway_amber),
        ThemeItem("Radar Green", Color(0xFF00FF7F), R.drawable.theme_radar_green),
        ThemeItem("Silent Running", Color(0xFFCCDDEE), R.drawable.theme_silent_running),
        ThemeItem("Battle Stations", Color(0xFFFF2222), R.drawable.theme_battle_stations),
    )

    // ── Features ──────────────────────────────────────────────────────────
    val features = listOf(
        FeatureItem(
            label = "LCD DISPLAY",
            description = "Authentic DSEG7 7-segment LCD font with ghost segment effect — green glow on dark"
        ),
        FeatureItem(
            label = "ANIMATED CARRIER",
            description = "Aircraft carrier silhouette bobbing over animated sea waves — 60-second cycle"
        ),
        FeatureItem(
            label = "FLIGHT DECK",
            description = "Battery bar, percentage readout, and 'FLIGHT DECK OPS' status indicator"
        ),
        FeatureItem(
            label = "BEZEL MARKS",
            description = "Amber cardinal marks at 12/3/6/9, cyan hour marks and half-hour sub-marks"
        ),
        FeatureItem(
            label = "AMBIENT MODE",
            description = "Dim monochrome HH:MM display — OLED-safe with minimal pixel burn-in"
        ),
        FeatureItem(
            label = "LIGHTWEIGHT",
            description = "Under 120 KB. No phone companion required. No permissions. No data collection."
        ),
    )

    // ── Footer ────────────────────────────────────────────────────────────
    const val FOOTER_TEXT = "⚓ MIDNIGHT CARRIER ⚓"
    const val COPYRIGHT = "© 2026 Midnight Carrier. Dedicated to the Indian Navy and all who serve at sea."
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
