package com.escuelafutbol.academia.data.local.model

import java.text.Normalizer
import java.util.Locale

/**
 * Clave estable para comparar nombres de categoría (tabla `categorias`, jugadores, asignación coach).
 * NFC + minúsculas + guiones tipográficos a ASCII + espacios colapsados.
 */
fun normalizarClaveCategoriaNombre(nombre: String): String {
    var s = Normalizer.normalize(nombre.trim(), Normalizer.Form.NFC)
        .lowercase(Locale.ROOT)
    s = s.replace('\u2013', '-') // en dash
        .replace('\u2014', '-') // em dash
        .replace('\u2212', '-') // minus sign
    s = s.replace(Regex("\\s+"), " ")
    return s.trim()
}
