package com.watchforge.companion.midnightcarrier.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.watchforge.companion.midnightcarrier.WatchFaceConfig

fun openPlayStoreListing(context: Context) {
    val packageId = WatchFaceConfig.WATCH_FACE_PACKAGE_ID
    // Try Play Store app first
    val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageId")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(storeIntent)
    } catch (_: android.content.ActivityNotFoundException) {
        // Fallback to browser
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageId")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)
    }
}
