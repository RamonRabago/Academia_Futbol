package com.escuelafutbol.academia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.core.graphics.ColorUtils

private fun onContentForBackground(background: Color): Color =
    if (background.luminance() > 0.5f) Color(0xFF1C1B1F) else Color.White

private fun lightenTowardWhite(c: Color, ratio: Float = 0.82f): Color {
    val blended = ColorUtils.blendARGB(c.toArgb(), android.graphics.Color.WHITE, ratio)
    return Color(blended)
}

private fun brandLightScheme(primary: Color, secondary: Color) = lightColorScheme(
    primary = primary,
    onPrimary = onContentForBackground(primary),
    primaryContainer = lightenTowardWhite(primary, 0.78f),
    onPrimaryContainer = primary,
    secondary = secondary,
    onSecondary = onContentForBackground(secondary),
    tertiary = secondary,
    surface = SurfaceLight,
)

private fun brandDarkScheme(primary: Color, secondary: Color): ColorScheme {
    val p = lightenTowardWhite(primary, 0.35f)
    return darkColorScheme(
        primary = p,
        onPrimary = onContentForBackground(p),
        primaryContainer = Color(0xFF0F3D2A),
        onPrimaryContainer = Color(0xFFB8E8D0),
        secondary = secondary,
        onSecondary = onContentForBackground(secondary),
    )
}

@Composable
fun AcademiaFutbolTheme(
    colorPrimarioHex: String? = null,
    colorSecundarioHex: String? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val primary = remember(colorPrimarioHex) {
        parseBrandColorOrNull(colorPrimarioHex) ?: PitchGreen
    }
    val secondary = remember(colorSecundarioHex) {
        parseBrandColorOrNull(colorSecundarioHex) ?: AccentGold
    }
    val colors = remember(darkTheme, primary, secondary) {
        if (darkTheme) brandDarkScheme(primary, secondary) else brandLightScheme(primary, secondary)
    }
    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}
