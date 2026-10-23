package com.example.clutch_ai.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ElectricCyanGlow,
    onPrimary = MidnightChassis,
    secondary = IrisIndigo,
    onSecondary = CardSurface,
    background = CanvasBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = CanvasBackground,
    onSurfaceVariant = TextMuted,
    outline = ClayBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = MidnightChassis,
    secondary = IrisIndigoLight,
    onSecondary = CardSurface,
    background = MidnightChassis,
    onBackground = TextOnDark,
    surface = MidnightChassisCard,
    onSurface = TextOnDark,
    surfaceVariant = MidnightChassisSurface,
    onSurfaceVariant = TextLightSlate,
    outline = MidnightChassisBorder
)

@Composable
fun ClutchaiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ClutchTypography,
        content = content
    )
}
