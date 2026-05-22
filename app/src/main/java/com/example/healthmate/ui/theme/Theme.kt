package com.example.healthmate.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary         = OceanBlue,
    onPrimary       = White,
    primaryContainer = OceanBlueLight,
    secondary       = MintGreen,
    onSecondary     = White,
    secondaryContainer = MintGreenLight,
    tertiary        = CoralAccent,
    background      = Background,
    onBackground    = TextPrimary,
    surface         = White,
    onSurface       = TextPrimary,
    surfaceVariant  = CardBackground,
    onSurfaceVariant = TextSecondary,
    outline         = Divider
)

@Composable
fun HealthMateTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography,
        content     = content
    )
}
