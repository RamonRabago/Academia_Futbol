package com.escuelafutbol.academia.ui.stats

import com.escuelafutbol.academia.data.local.entity.CobroMensualAlumno
import com.escuelafutbol.academia.data.local.entity.Jugador
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

data class EconomiaPorCategoriaUi(
    val categoria: String,
    val alumnosActivos: Int,
    val becados: Int,
    val alumnosConCuota: Int,
    val sinCuotaDefinida: Int,
    /** Suma de mensualidad en ficha (no becados con cuota mayor que cero). No es ingreso neto ni ganancia. */
    val ingresoMensualEstimado: Double,
    /** Suma de `importePagado` en el periodo; null si aún no hay cobros en base. */
    val ingresoCobradoPeriodo: Double?,
    /** Suma de `max(0, esperado − pagado)` en el periodo; null si aún no hay cobros en base. */
    val adeudoPendientePeriodo: Double?,
)

data class StatsEconomiaResumenUi(
    val periodoYyyyMm: String,
    val etiquetaPeriodoHumano: String,
    /** Hay al menos una fila en `cobros_mensuales_alumno` (cualquier mes). */
    val hayCobrosRegistradosEnSistema: Boolean,
    val filasPorCategoria: List<EconomiaPorCategoriaUi>,
    val topCategoriaIngresoEstimado: String?,
    val topCategoriaIngresoCobrado: String?,
    val topCategoriaAdeudoPendiente: String?,
    val topCategoriaMasBecados: String?,
)

internal fun periodoYyyyMm(ym: YearMonth): String =
    String.format(Locale.ROOT, "%04d-%02d", ym.year, ym.monthValue)

fun etiquetaMesAnio(ym: YearMonth): String {
    val mes = ym.month.getDisplayName(TextStyle.FULL, Locale("es", "MX"))
    return "$mes ${ym.year}"
}

private fun adeudoLinea(c: CobroMensualAlumno): Double =
    (c.importeEsperado - c.importePagado).coerceAtLeast(0.0)

/**
 * Agrega cobros del [periodoYyyyMm] por categoría **actual** del jugador en [jugadoresPorId].
 */
private fun agregarCobrosPorCategoriaActual(
    cobrosDelPeriodo: List<CobroMensualAlumno>,
    jugadoresPorId: Map<Long, Jugador>,
): Map<String, Pair<Double, Double>> {
    val out = mutableMapOf<String, Pair<Double, Double>>()
    for (c in cobrosDelPeriodo) {
        val j = jugadoresPorId[c.jugadorId] ?: continue
        val cat = j.categoria.trim().ifBlank { "—" }
        val pag = c.importePagado
        val ade = adeudoLinea(c)
        val prev = out[cat] ?: (0.0 to 0.0)
        out[cat] = prev.first + pag to prev.second + ade
    }
    return out
}

fun calcularEconomiaResumen(
    jugadores: List<Jugador>,
    todosLosCobros: List<CobroMensualAlumno>,
    ymReferencia: YearMonth = YearMonth.now(),
): StatsEconomiaResumenUi {
    val periodo = periodoYyyyMm(ymReferencia)
    val hayCobros = todosLosCobros.isNotEmpty()
    val cobrosMes = todosLosCobros.filter { it.periodoYyyyMm == periodo }
    val porId = jugadores.associateBy { it.id }
    val cobrosPorCat = if (hayCobros) {
        agregarCobrosPorCategoriaActual(cobrosMes, porId)
    } else {
        emptyMap()
    }

    val sorted = jugadores.sortedWith(compareBy({ it.categoria }, { it.nombre }))
    val filas = sorted.groupBy { it.categoria.trim().ifBlank { "—" } }.map { (cat, list) ->
        val bec = list.count { it.becado }
        val con = list.count { !it.becado && it.mensualidad != null && it.mensualidad > 0 }
        val sin = list.count {
            !it.becado && (it.mensualidad == null || it.mensualidad == 0.0)
        }
        val estimado = list
            .filter { !it.becado && it.mensualidad != null && it.mensualidad > 0 }
            .sumOf { it.mensualidad!! }
        val (pag, ade) = cobrosPorCat[cat] ?: (0.0 to 0.0)
        EconomiaPorCategoriaUi(
            categoria = cat,
            alumnosActivos = list.size,
            becados = bec,
            alumnosConCuota = con,
            sinCuotaDefinida = sin,
            ingresoMensualEstimado = estimado,
            ingresoCobradoPeriodo = if (hayCobros) pag else null,
            adeudoPendientePeriodo = if (hayCobros) ade else null,
        )
    }.sortedWith(
        compareByDescending<EconomiaPorCategoriaUi> { it.ingresoMensualEstimado }
            .thenBy { it.categoria },
    )

    fun topNombre(
        selector: (EconomiaPorCategoriaUi) -> Double,
        minStrictlyPositive: Boolean,
    ): String? {
        if (filas.isEmpty()) return null
        val cand = filas
            .map { it.categoria to selector(it) }
            .sortedWith(compareByDescending<Pair<String, Double>> { it.second }.thenBy { it.first })
        val best = cand.first()
        if (minStrictlyPositive && best.second <= 0.0) return null
        return best.first
    }

    fun topBecados(): String? {
        val cand = filas
            .map { it.categoria to it.becados }
            .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
        val best = cand.firstOrNull() ?: return null
        if (best.second == 0) return null
        return best.first
    }

    return StatsEconomiaResumenUi(
        periodoYyyyMm = periodo,
        etiquetaPeriodoHumano = etiquetaMesAnio(ymReferencia),
        hayCobrosRegistradosEnSistema = hayCobros,
        filasPorCategoria = filas,
        topCategoriaIngresoEstimado = topNombre({ it.ingresoMensualEstimado }, minStrictlyPositive = false),
        topCategoriaIngresoCobrado = if (hayCobros) {
            topNombre({ it.ingresoCobradoPeriodo ?: 0.0 }, minStrictlyPositive = true)
        } else {
            null
        },
        topCategoriaAdeudoPendiente = if (hayCobros) {
            topNombre({ it.adeudoPendientePeriodo ?: 0.0 }, minStrictlyPositive = true)
        } else {
            null
        },
        topCategoriaMasBecados = topBecados(),
    )
}
