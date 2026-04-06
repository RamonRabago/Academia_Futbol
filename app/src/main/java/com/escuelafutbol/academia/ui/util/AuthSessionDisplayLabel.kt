package com.escuelafutbol.academia.ui.util

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Misma lógica que la barra de sesión: given+family, full_name/name, o correo.
 * Para persistir en [com.escuelafutbol.academia.data.local.entity.Jugador.altaPorNombre] al dar de alta.
 */
fun etiquetaVisibleDesdeAuthMetadata(
    meta: JsonObject?,
    email: String?,
): String? {
    val given = metaString(meta, "given_name")
    val family = metaString(meta, "family_name")
    if (!given.isNullOrBlank() && !family.isNullOrBlank()) {
        return "${given.trim()} ${family.trim()}".trim().takeIf { it.isNotEmpty() }
    }
    val full = metaString(meta, "full_name") ?: metaString(meta, "name")
    if (!full.isNullOrBlank()) return full.trim()
    return email?.trim()?.takeIf { it.isNotEmpty() }
}

private fun metaString(meta: JsonObject?, key: String): String? {
    val el = meta?.get(key) ?: return null
    return (el as? JsonPrimitive)?.contentOrNull
}
