package com.escuelafutbol.academia.ui.attendance

import com.escuelafutbol.academia.data.local.entity.Asistencia
import com.escuelafutbol.academia.data.local.entity.DiaEntrenamiento
import java.time.Instant
import java.time.ZoneId
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

fun scopeKeyAsistencia(categoriaFiltro: String?): String =
    categoriaFiltro?.trim().orEmpty()

private val jsonDiasParser = Json { ignoreUnknownKeys = true }

/** Martes y jueves (ISO `DayOfWeek`, lunes=1). */
private val diasEntrenoSemanaPorDefecto: Set<Int> = setOf(2, 4)

/**
 * Interpreta [raw] como JSON array de enteros 1–7 (lunes a domingo). Valores inválidos se ignoran.
 */
fun parseDiasEntrenoSemanaIsoJson(raw: String): Set<Int> {
    val s = raw.trim()
    if (s.isEmpty()) return diasEntrenoSemanaPorDefecto
    return try {
        jsonDiasParser.parseToJsonElement(s).jsonArray
            .mapNotNull { el ->
                el.jsonPrimitive.intOrNull?.takeIf { it in 1..7 }
            }
            .toSet()
            .takeIf { it.isNotEmpty() }
            ?: diasEntrenoSemanaPorDefecto
    } catch (_: Exception) {
        diasEntrenoSemanaPorDefecto
    }
}

fun esDiaSemanaHabitualEntreno(
    fechaDiaMillis: Long,
    zone: ZoneId,
    diasEntrenoSemanaIsoJson: String,
): Boolean {
    val isoDow = Instant.ofEpochMilli(fechaDiaMillis).atZone(zone).dayOfWeek.value
    return isoDow in parseDiasEntrenoSemanaIsoJson(diasEntrenoSemanaIsoJson)
}

/**
 * Indica si [fechaDia] está marcado como día de entrenamiento para la vista actual ([scopeKeyVista]).
 *
 * - Vista **«todas las categorías»** (`scopeKeyVista` vacío): cuenta si existe **cualquier** marca
 *   para ese día de calendario (p. ej. se guardó con una categoría concreta y luego el menú volvió a «todas»).
 * - Vista con **categoría concreta**: coincide la misma clave, o una marca universal (`scopeKey` vacío).
 */
fun diaMarcadoComoEntrenamiento(
    fechaDia: Long,
    scopeKeyVista: String,
    marcas: Collection<DiaEntrenamiento>,
): Boolean {
    val delDia = marcas.filter { it.fechaDia == fechaDia }
    if (delDia.isEmpty()) return false
    if (scopeKeyVista.isEmpty()) {
        return true
    }
    return delDia.any { d ->
        d.scopeKey == scopeKeyVista || d.scopeKey.isEmpty()
    }
}

/**
 * Cuenta presentes y ausentes en la rejilla (jugador × día de entrenamiento marcado).
 * Si no hay fila de [Asistencia] para un cupo, cuenta como **ausente** (igual que el interruptor por defecto en la UI).
 */
fun contarPresentesAusentesEntrenamientoImplicitos(
    jugadorIds: Collection<Long>,
    diasEntreno: Collection<DiaEntrenamiento>,
    asistencias: Collection<Asistencia>,
    scopeKeyVista: String,
): PresentesAusentesEntrenoConteo {
    val ids = jugadorIds.toSet()
    if (ids.isEmpty()) {
        return PresentesAusentesEntrenoConteo(presentes = 0, ausentes = 0, totalCupos = 0)
    }
    val fechasEntreno = diasEntreno
        .map { it.fechaDia }
        .distinct()
        .filter { diaMarcadoComoEntrenamiento(it, scopeKeyVista, diasEntreno) }
        .toSet()
    if (fechasEntreno.isEmpty()) {
        return PresentesAusentesEntrenoConteo(presentes = 0, ausentes = 0, totalCupos = 0)
    }
    val porJugadorDia = asistencias
        .filter { it.jugadorId in ids && it.fechaDia in fechasEntreno }
        .associateBy { it.jugadorId to it.fechaDia }
    var pres = 0
    var aus = 0
    for (dia in fechasEntreno) {
        for (jid in ids) {
            if (porJugadorDia[jid to dia]?.presente == true) {
                pres++
            } else {
                aus++
            }
        }
    }
    return PresentesAusentesEntrenoConteo(
        presentes = pres,
        ausentes = aus,
        totalCupos = pres + aus,
    )
}

data class PresentesAusentesEntrenoConteo(
    val presentes: Int,
    val ausentes: Int,
    val totalCupos: Int,
)
