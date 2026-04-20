package com.escuelafutbol.academia.domain.competencias

import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaPartidoRow
import com.escuelafutbol.academia.data.remote.dto.DetalleMarcadorJsonCodec
import com.escuelafutbol.academia.data.remote.dto.DetalleMarcadorPayload
import java.util.Locale

/**
 * Agregación multideporte de anotaciones individuales desde [AcademiaCompetenciaPartidoRow.detalleMarcadorJson].
 *
 * **Criterio de publicación del ranking en UI:** solo se expone un ranking cuando no hay partidos con
 * desglose **parcialmente informado e incoherente** (anotadores no vacíos pero que no suman el marcador
 * propio). Solo se agregan partidos con líneas de anotación que **sí cuadran** con [AcademiaCompetenciaPartidoRow.scorePropio].
 * Ver [construirLideresOfensivosTabla] y `docs/COMPETENCIAS_TABLA_EVOLUCION.md` (líderes por jornada, estadísticas completas, etc.).
 */
data class LiderOfensivoResumen(
    val claveAgrupacion: String,
    val nombreMostrado: String,
    val total: Int,
)

sealed class LideresOfensivosTablaResultado {
    /** Hay al menos un partido aportante y un ranking no vacío (hasta [limite] filas). */
    data class Ranking(val items: List<LiderOfensivoResumen>) : LideresOfensivosTablaResultado()

    /** Partidos jugados existen, pero ninguno aporta líneas de anotación coherentes con el marcador propio. */
    data object SinDesgloseCoherente : LideresOfensivosTablaResultado()

    /**
     * Al menos un partido jugado tiene anotadores en JSON que no suman [AcademiaCompetenciaPartidoRow.scorePropio];
     * no se muestra ranking acumulado para no inducir a conclusiones con datos incompletos.
     */
    data object InconsistenciaMarcadorIndividual : LideresOfensivosTablaResultado()
}

private fun claveAnotador(jugadorRemoteId: String?, nombreMostrado: String): String {
    val rid = jugadorRemoteId?.trim()?.takeIf { it.isNotEmpty() }
    if (rid != null) return "rid:$rid"
    return "nom:${nombreMostrado.trim().lowercase(Locale.ROOT)}"
}

private fun sumaCantidadesAnotadores(payload: DetalleMarcadorPayload): Int =
    payload.anotadores.sumOf { a -> a.cantidad.coerceAtLeast(0) }

/** Partido jugado con JSON de anotadores no vacío cuya suma no coincide con el marcador propio. */
private fun partidoConDetalleMarcadorInconsistente(p: AcademiaCompetenciaPartidoRow): Boolean {
    if (!partidoCuentaParaEstadistica(p)) return false
    val payload = DetalleMarcadorJsonCodec.decodeOrNull(p.detalleMarcadorJson) ?: return false
    if (payload.anotadores.isEmpty()) return false
    return sumaCantidadesAnotadores(payload) != p.scorePropio
}

/** Partido válido para estadística con anotadores que suman exactamente el marcador propio (puede aportar al ranking). */
private fun partidoAportaDesgloseCoherente(p: AcademiaCompetenciaPartidoRow): Boolean {
    if (!partidoCuentaParaEstadistica(p)) return false
    val payload = DetalleMarcadorJsonCodec.decodeOrNull(p.detalleMarcadorJson) ?: return false
    if (payload.anotadores.isEmpty()) return false
    if (sumaCantidadesAnotadores(payload) != p.scorePropio) return false
    return payload.anotadores.any { it.nombreMostrado.trim().isNotEmpty() && it.cantidad > 0 }
}

/**
 * Decide qué mostrar en la pestaña Tabla bajo la clasificación: ranking fiable, o estado vacío explicado.
 */
fun construirLideresOfensivosTabla(
    partidos: List<AcademiaCompetenciaPartidoRow>,
    limite: Int = 3,
): LideresOfensivosTablaResultado {
    if (limite <= 0) return LideresOfensivosTablaResultado.SinDesgloseCoherente
    if (partidos.any(::partidoConDetalleMarcadorInconsistente)) {
        return LideresOfensivosTablaResultado.InconsistenciaMarcadorIndividual
    }
    val partidosFuente = partidos.filter(::partidoAportaDesgloseCoherente)
    val items = calcularTopLideresOfensivos(partidosFuente, limite)
    return if (items.isEmpty()) {
        LideresOfensivosTablaResultado.SinDesgloseCoherente
    } else {
        LideresOfensivosTablaResultado.Ranking(items)
    }
}

/**
 * Suma anotaciones de los [partidos] indicados (deben estar ya filtrados por el llamador si aplica)
 * y devuelve los [limite] primeros por total descendente.
 */
fun calcularTopLideresOfensivos(
    partidos: List<AcademiaCompetenciaPartidoRow>,
    limite: Int = 3,
): List<LiderOfensivoResumen> {
    if (limite <= 0) return emptyList()
    val acum = linkedMapOf<String, Pair<String, Int>>()
    for (p in partidos) {
        if (!partidoCuentaParaEstadistica(p)) continue
        val payload = DetalleMarcadorJsonCodec.decodeOrNull(p.detalleMarcadorJson) ?: continue
        for (a in payload.anotadores) {
            val nombre = a.nombreMostrado.trim()
            if (nombre.isEmpty() || a.cantidad <= 0) continue
            val k = claveAnotador(a.jugadorRemoteId, nombre)
            val prev = acum[k]
            val suma = (prev?.second ?: 0) + a.cantidad
            acum[k] = nombre to suma
        }
    }
    return acum.entries
        .map { (k, v) -> LiderOfensivoResumen(claveAgrupacion = k, nombreMostrado = v.first, total = v.second) }
        .sortedWith(compareByDescending<LiderOfensivoResumen> { it.total }.thenBy { it.nombreMostrado.lowercase(Locale.ROOT) })
        .take(limite)
}
