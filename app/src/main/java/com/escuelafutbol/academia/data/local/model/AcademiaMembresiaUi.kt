package com.escuelafutbol.academia.data.local.model

import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import java.util.Locale
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
/**
 * La sesión de auth coincide con el dueño de cuenta de la academia enlazada en nube
 * ([AcademiaConfig.remoteAcademiaCuentaUserId], proveniente de `academias.user_id`).
 */
fun AcademiaConfig.esSesionDueñoCuentaAcademiaRemota(uidSesionAuth: String?): Boolean {
    val cuenta = remoteAcademiaCuentaUserId?.trim()?.lowercase(Locale.ROOT) ?: return false
    val uid = uidSesionAuth?.trim()?.lowercase(Locale.ROOT) ?: return false
    return uid == cuenta
}

/**
 * Academia enlazada en nube pero aún no tenemos rol de membresía en Room (p. ej. tras cambiar de cuenta).
 * Hasta que no se resuelva, el selector de categorías no debe mostrar «todas» como si fuera dueño/admin.
 *
 * Si la sesión es el **dueño de cuenta** (`remoteAcademiaCuentaUserId`), no se considera pendiente: evita
 * bloquear el selector mientras llega `cloudMembresiaRol` del sync.
 */
fun AcademiaConfig.membresiaNubeAunNoResuelta(uidSesionAuth: String? = null): Boolean {
    if (remoteAcademiaId == null || cloudMembresiaRol != null) return false
    if (esSesionDueñoCuentaAcademiaRemota(uidSesionAuth)) return false
    return true
}

/** Membresía en nube con rol padre/tutor (rutas y sync restringidos). */
fun AcademiaConfig.esPadreMembresiaNube(): Boolean =
    remoteAcademiaId != null &&
        cloudMembresiaRol?.equals("parent", ignoreCase = true) == true

/**
 * Puede editar el día límite de pago mensual: sin academia en nube, o sesión actual = dueño de cuenta
 * (`academias.user_id`, cacheado en [AcademiaConfig.remoteAcademiaCuentaUserId]).
 */
fun AcademiaConfig.puedeMutarDiaLimitePagoMes(uidSesionAuth: String?): Boolean {
    if (remoteAcademiaId == null) return true
    return esSesionDueñoCuentaAcademiaRemota(uidSesionAuth)
}

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
