package com.watchforge.companion.indianarmy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.watchforge.companion.indianarmy.WatchFaceConfig
import com.watchforge.companion.indianarmy.ui.components.FeatureGrid
import com.watchforge.companion.indianarmy.ui.components.HeroSection
import com.watchforge.companion.indianarmy.ui.components.InstallButton
import com.watchforge.companion.indianarmy.ui.components.ThemeCarousel
import com.watchforge.companion.indianarmy.ui.components.WatchPreview
import com.watchforge.companion.indianarmy.util.openPlayStoreListing

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Tricolor bar (if enabled in config)
        if (WatchFaceConfig.hasTricolor) {
            TricolorBar()
        }

        Spacer(Modifier.height(40.dp))

        // Hero: Icon + Name + Tagline
        HeroSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(32.dp))

        // Circular watch preview
        WatchPreview()

        Spacer(Modifier.height(32.dp))

        // Play Store button
        InstallButton(onClick = { openPlayStoreListing(context) })

        Spacer(Modifier.height(8.dp))

        // "Customize on Watch" hint
        Text(
            text = "Customize colors & themes on your watch",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outlineVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        // Divider
        @Suppress("DEPRECATION")
        Divider(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 36.dp),
            color = MaterialTheme.colorScheme.outline,
        )

        // Theme carousel
        ThemeCarousel()

        Spacer(Modifier.height(36.dp))

        // Feature grid
        FeatureGrid()

        Spacer(Modifier.height(36.dp))

        // Description
        Text(
            text = WatchFaceConfig.DESCRIPTION,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outlineVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )

        // Divider
        @Suppress("DEPRECATION")
        Divider(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 36.dp),
            color = MaterialTheme.colorScheme.outline,
        )

        // Footer
        Text(
            text = WatchFaceConfig.FOOTER_TEXT,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = WatchFaceConfig.COPYRIGHT,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outlineVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun TricolorBar() {
    Row(modifier = Modifier.fillMaxWidth().height(5.dp)) {
        Box(Modifier.weight(1f).height(5.dp).background(WatchFaceConfig.tricolorSaffron))
        Box(Modifier.weight(1f).height(5.dp).background(WatchFaceConfig.tricolorWhite))
        Box(Modifier.weight(1f).height(5.dp).background(WatchFaceConfig.tricolorGreen))
    }
}
