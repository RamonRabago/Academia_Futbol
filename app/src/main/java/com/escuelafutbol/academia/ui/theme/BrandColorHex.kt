package com.escuelafutbol.academia.ui.theme

import androidx.compose.ui.graphics.Color

private val HEX6 = Regex("^#?([0-9A-Fa-f]{6})$")

/** Devuelve `#RRGGBB` en mayúsculas o null si no es válido. */
fun normalizeBrandColorHex(input: String?): String? {
    val t = input?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val m = HEX6.matchEntire(t) ?: return null
    return "#" + m.groupValues[1].uppercase()
}

fun parseBrandColorOrNull(hex: String?): Color? {
    val n = normalizeBrandColorHex(hex) ?: return null
    return runCatching {
        val argb = android.graphics.Color.parseColor(n)
        Color(argb)
    }.getOrNull()
}
