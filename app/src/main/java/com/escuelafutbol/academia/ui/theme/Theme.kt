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

private val NeutralOnSurface = Color(0xFF1A1C1E)
private val NeutralOnSurfaceVariant = Color(0xFF44474E)
private val NeutralSurfaceVariant = Color(0xFFE1E2E6)
private val NeutralOutline = Color(0xFF74777F)
private val NeutralOutlineVariant = Color(0xFFC4C6CF)

private fun brandLightScheme(primary: Color, secondary: Color) = lightColorScheme(
    primary = primary,
    onPrimary = onContentForBackground(primary),
    primaryContainer = lightenTowardWhite(primary, 0.78f),
    onPrimaryContainer = primary,
    secondary = secondary,
    onSecondary = onContentForBackground(secondary),
    secondaryContainer = lightenTowardWhite(secondary, 0.82f),
    onSecondaryContainer = secondary,
    tertiary = secondary,
    onTertiary = onContentForBackground(secondary),
    tertiaryContainer = Color(0xFFF0F0F2),
    onTertiaryContainer = NeutralOnSurfaceVariant,
    background = SurfaceLight,
    onBackground = NeutralOnSurface,
    surface = SurfaceLight,
    onSurface = NeutralOnSurface,
    surfaceVariant = NeutralSurfaceVariant,
    onSurfaceVariant = NeutralOnSurfaceVariant,
    surfaceContainerLowest = SurfaceLight,
    surfaceContainerLow = Color(0xFFF7F7F9),
    surfaceContainer = Color(0xFFF0F0F2),
    surfaceContainerHigh = Color(0xFFEAEAED),
    surfaceContainerHighest = Color(0xFFE4E4E8),
    outline = NeutralOutline,
    outlineVariant = NeutralOutlineVariant,
    scrim = Color.Black,
)

private fun brandDarkScheme(primary: Color, secondary: Color): ColorScheme {
    val p = lightenTowardWhite(primary, 0.35f)
    val darkSurface = Color(0xFF121212)
    val darkOnSurface = Color(0xFFE3E3E3)
    return darkColorScheme(
        primary = p,
        onPrimary = onContentForBackground(p),
        primaryContainer = Color(0xFF0F3D2A),
        onPrimaryContainer = Color(0xFFB8E8D0),
        secondary = secondary,
        onSecondary = onContentForBackground(secondary),
        background = Color(0xFF101010),
        onBackground = darkOnSurface,
        surface = darkSurface,
        onSurface = darkOnSurface,
        surfaceVariant = Color(0xFF2C2C2C),
        onSurfaceVariant = Color(0xFFC4C4C8),
        surfaceContainerLowest = Color(0xFF0D0D0D),
        surfaceContainerLow = Color(0xFF181818),
        surfaceContainer = Color(0xFF1C1C1C),
        surfaceContainerHigh = Color(0xFF222222),
        surfaceContainerHighest = Color(0xFF282828),
        outline = Color(0xFF8E8E93),
        outlineVariant = Color(0xFF3A3A3C),
        scrim = Color.Black,
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
