package com.classisland.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Color(0xFF2196F3), onPrimary = Color.White,
    background = Color(0xFFFFFBFE), onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE), onSurface = Color(0xFF1C1B1F)
)
private val Dark = darkColorScheme(
    primary = Color(0xFF90CAF9), onPrimary = Color(0xFF003258),
    background = Color(0xFF1C1B1F), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F), onSurface = Color(0xFFE6E1E5)
)

@Composable
fun ClassIslandTheme(mode: String = "system", content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = when (mode) { "dark" -> Dark; "light" -> Light; else -> if (isSystemInDarkTheme()) Dark else Light },
        content = content
    )
}