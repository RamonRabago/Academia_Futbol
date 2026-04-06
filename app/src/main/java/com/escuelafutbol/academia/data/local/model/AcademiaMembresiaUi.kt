package com.escuelafutbol.academia.data.local.model

import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val jsonCategoriasCoach = Json { ignoreUnknownKeys = true }
private val stringListSer = ListSerializer(String.serializer())

/**
 * Si el usuario es miembro **coach** en nube con academia enlazada, conjunto de nombres de categoría
 * permitidas para operación (jugadores, asistencia, stats). `null` = sin restricción por coach.
 * Conjunto vacío = coach sin categorías asignadas en `academia_miembro_categorias`.
 */
/** Membresía en nube con rol padre/tutor (rutas y sync restringidos). */
fun AcademiaConfig.esPadreMembresiaNube(): Boolean =
    remoteAcademiaId != null &&
        cloudMembresiaRol?.equals("parent", ignoreCase = true) == true

fun AcademiaConfig.cloudCoachCategoriasPermitidasOperacion(): Set<String>? {
    if (remoteAcademiaId == null) return null
    if (cloudMembresiaRol?.equals("coach", ignoreCase = true) != true) return null
    val raw = cloudCoachCategoriasJson ?: return emptySet()
    if (raw.isBlank()) return emptySet()
    return runCatching {
        jsonCategoriasCoach.decodeFromString(stringListSer, raw)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }.getOrNull() ?: emptySet()
}

/** Alta de categorías / portadas en el selector: `academiaGestionNubePermitida` (owner/admin/coordinator en nube; o sin enlace remoto). */
fun AcademiaConfig.puedeEditarCategoriasEnSelector(): Boolean {
    if (remoteAcademiaId == null) return true
    return academiaGestionNubePermitida
}
