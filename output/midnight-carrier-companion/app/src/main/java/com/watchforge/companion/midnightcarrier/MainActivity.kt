package com.watchforge.companion.midnightcarrier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.watchforge.companion.midnightcarrier.ui.screens.HomeScreen
import com.watchforge.companion.midnightcarrier.ui.theme.CompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CompanionTheme {
                Surface(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                    HomeScreen()
                }
            }
        }
    }
}
