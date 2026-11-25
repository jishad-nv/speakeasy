package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.model.ThemeMode

private val SpeakUpLightColors = lightColorScheme(
    primary = DeepGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = DeepGreenContainerLight,
    onPrimaryContainer = Color(0xFF063D28),
    secondary = LeoOrangeAccent,
    onSecondary = Color(0xFF2E1B00),
    secondaryContainer = SoftYellowHighlight,
    onSecondaryContainer = Color(0xFF422C00),
    tertiary = LeoOrangeAccent,
    onTertiary = Color(0xFF2E1E00),
    tertiaryContainer = SoftYellowHighlight,
    onTertiaryContainer = Color(0xFF3E2900),
    background = CreamBackground,
    onBackground = InkPrimary,
    surface = CardWhite,
    onSurface = InkPrimary,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = OutlineWarm,
    error = CoralError,
    onError = Color.White,
    errorContainer = CoralContainerLight,
    onErrorContainer = Color(0xFF410002)
)

private val SpeakUpDarkColors = darkColorScheme(
    primary = DeepGreenLight,
    onPrimary = Color(0xFF002114),
    primaryContainer = DeepGreenContainerDark,
    onPrimaryContainer = Color(0xFFB6F2D7),
    secondary = LeoOrangeLight,
    onSecondary = Color(0xFF332100),
    secondaryContainer = SoftYellowDark,
    onSecondaryContainer = SoftYellowHighlight,
    tertiary = LeoOrangeLight,
    onTertiary = Color(0xFF332200),
    tertiaryContainer = SoftYellowDark,
    onTertiaryContainer = SoftYellowHighlight,
    background = NightBackground,
    onBackground = NightTextPrimary,
    surface = NightSurface,
    onSurface = NightTextPrimary,
    surfaceVariant = NightSurfaceElevated,
    onSurfaceVariant = NightTextSecondary,
    outline = OutlineNight,
    error = Color(0xFFFF8987),
    onError = Color(0xFF690005),
    errorContainer = CoralContainerDark,
    onErrorContainer = Color(0xFFFFDAD6)
)

val SpeakUpShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun SpeakUpTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) SpeakUpDarkColors else SpeakUpLightColors,
        typography = SpeakUpTypography,
        shapes = SpeakUpShapes,
        content = content
    )
}
