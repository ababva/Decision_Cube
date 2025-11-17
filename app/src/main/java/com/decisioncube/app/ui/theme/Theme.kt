package com.decisioncube.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF424242),
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = Color(0xFF212121),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF616161),
    outline = Color(0xFFBDBDBD)
)

@Composable
fun FitnessCubeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}

