package com.escuelafutbol.academia.domain.competencias

import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaCategoriaRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaPartidoRow
import com.escuelafutbol.academia.data.remote.dto.CatalogoDeporteRow
import com.escuelafutbol.academia.data.remote.dto.CompetenciaPartidoEstado

/**
 * Cálculo de tabla de posiciones y columna de **variación** respecto a un “snapshot” anterior.
 *
 * **Criterio actual de variación (por jornada numérica, no por fecha de calendario):**
 * - Se toma `J = max(jornada)` entre los partidos que **sí cuentan** para estadística
 *   ([partidoCuentaParaEstadistica]): jugados, no cancelados, marcador completo.
 * - La tabla **actual** usa todos esos partidos.
 * - La tabla **de referencia** usa solo partidos con `jornada < J`.
 * - [LineaTablaPosicion.variacionPosicion] = `rank_en_referencia − rank_actual` (positivo = subió en la tabla).
 * - Si `J ≤ 1` o no hay partidos con `jornada < J`, no hay referencia útil → `variacionPosicion = null`.
 *
 * **Evolución prevista** (ver `docs/COMPETENCIAS_TABLA_EVOLUCION.md`): variación por fecha de encuentro,
 * por ventana móvil de N partidos, o por jornada “oficial” distinta del entero almacenado.
 *
 * ---
 *
 * Estadísticas acumuladas y puntos de tabla por inscripción ([AcademiaCompetenciaCategoriaRow]).
 * Solo entran partidos con [AcademiaCompetenciaPartidoRow.jugado] == true, estado distinto de cancelado,
 * y marcador completo (scores no nulos).
 *
 * [variacionPosicion]: ver criterio arriba. Valores: `null` = sin dato; `0` = sin cambio;
 * `>0` = subió tantas posiciones; `<0` = bajó tantas posiciones.
 */
data class LineaTablaPosicion(
    val categoriaEnCompetenciaId: String,
    val nombreEquipoEnTabla: String,
    val grupo: String?,
    val partidosJugados: Int,
    val ganados: Int,
    val empatados: Int,
    val perdidos: Int,
    val scoreFavor: Int,
    val scoreContra: Int,
    val diferenciaScore: Int,
    val puntosTabla: Int,
    val variacionPosicion: Int? = null,
)

private fun nombreMostradoInscripcion(row: AcademiaCompetenciaCategoriaRow): String {
    val eq = row.nombreEquipoMostrado?.trim().orEmpty()
    if (eq.isNotEmpty()) return eq
    return row.categoriaNombre.trim()
}

internal fun partidoCuentaParaEstadistica(p: AcademiaCompetenciaPartidoRow): Boolean {
    if (!p.jugado) return false
    if (p.estado == CompetenciaPartidoEstado.CANCELADO) return false
    val sp = p.scorePropio
    val sr = p.scoreRival
    if (sp == null || sr == null) return false
    return true
}

private val comparadorTabla = compareByDescending<LineaTablaPosicion> { it.puntosTabla }
    .thenByDescending { it.diferenciaScore }
    .thenByDescending { it.scoreFavor }
    .thenBy { it.nombreEquipoEnTabla.lowercase() }

private fun construirTablaOrdenada(
    inscripciones: List<AcademiaCompetenciaCategoriaRow>,
    partidosFiltrados: List<AcademiaCompetenciaPartidoRow>,
    deporte: CatalogoDeporteRow,
    reglas: ReglasPuntosTabla,
): List<LineaTablaPosicion> {
    val inscActivas = inscripciones.filter { it.activo }
    val ids = inscActivas.map { it.id }.toSet()
    val partidosValidos = partidosFiltrados.filter { it.categoriaEnCompetenciaId in ids && partidoCuentaParaEstadistica(it) }

    val porInscripcion = inscActivas.associate { it.id to nombreMostradoInscripcion(it) }

    data class Acum(
        var pj: Int = 0,
        var g: Int = 0,
        var e: Int = 0,
        var p: Int = 0,
        var sf: Int = 0,
        var sc: Int = 0,
    )

    val acum = inscActivas.associate { it.id to Acum() }.toMutableMap()

    for (par in partidosValidos) {
        val a = acum.getOrPut(par.categoriaEnCompetenciaId) { Acum() }
        val sp = par.scorePropio!!
        val sr = par.scoreRival!!
        a.pj++
        a.sf += sp
        a.sc += sr
        when {
            sp > sr -> a.g++
            sp < sr -> a.p++
            else -> a.e++
        }
    }

    val lineas = inscActivas.map { row ->
        val a = acum[row.id] ?: Acum()
        val diff = a.sf - a.sc
        val puntos = a.g * reglas.puntosVictoria +
            a.e * reglas.puntosEmpate +
            a.p * reglas.puntosDerrota
        LineaTablaPosicion(
            categoriaEnCompetenciaId = row.id,
            nombreEquipoEnTabla = porInscripcion[row.id] ?: row.categoriaNombre,
            grupo = row.grupo?.trim()?.takeIf { it.isNotEmpty() },
            partidosJugados = a.pj,
            ganados = a.g,
            empatados = a.e,
            perdidos = a.p,
            scoreFavor = a.sf,
            scoreContra = a.sc,
            diferenciaScore = diff,
            puntosTabla = puntos,
            variacionPosicion = null,
        )
    }

    return lineas.sortedWith(comparadorTabla)
}

/**
 * Añade [LineaTablaPosicion.variacionPosicion] comparando [tablaActual] con una tabla construida **sin**
 * los partidos de la **última jornada numérica** presente entre los partidos válidos (ver KDoc de archivo).
 */
private fun enriquecerVariacionJornadaAnterior(
    inscripciones: List<AcademiaCompetenciaCategoriaRow>,
    partidosTodos: List<AcademiaCompetenciaPartidoRow>,
    deporte: CatalogoDeporteRow,
    reglas: ReglasPuntosTabla,
    tablaActual: List<LineaTablaPosicion>,
): List<LineaTablaPosicion> {
    val inscActivas = inscripciones.filter { it.activo }
    val ids = inscActivas.map { it.id }.toSet()
    val partidosValidos = partidosTodos.filter { it.categoriaEnCompetenciaId in ids && partidoCuentaParaEstadistica(it) }
    val maxJornada = partidosValidos.maxOfOrNull { it.jornada } ?: 0
    if (maxJornada <= 1) {
        return tablaActual.map { it.copy(variacionPosicion = null) }
    }
    val partidosPrevios = partidosValidos.filter { it.jornada < maxJornada }
    if (partidosPrevios.isEmpty()) {
        return tablaActual.map { it.copy(variacionPosicion = null) }
    }
    val tablaPrev = construirTablaOrdenada(inscripciones, partidosPrevios, deporte, reglas)
    val rankPrev = tablaPrev.mapIndexed { idx, l -> l.categoriaEnCompetenciaId to (idx + 1) }.toMap()
    return tablaActual.mapIndexed { idx, linea ->
        val rankActual = idx + 1
        val rankAnterior = rankPrev[linea.categoriaEnCompetenciaId] ?: (tablaPrev.size + 1)
        val variacion = rankAnterior - rankActual
        linea.copy(variacionPosicion = variacion)
    }
}

fun calcularTablaPosiciones(
    inscripciones: List<AcademiaCompetenciaCategoriaRow>,
    partidos: List<AcademiaCompetenciaPartidoRow>,
    deporte: CatalogoDeporteRow,
    reglas: ReglasPuntosTabla,
): List<LineaTablaPosicion> {
    val tablaBase = construirTablaOrdenada(inscripciones, partidos, deporte, reglas)
    return enriquecerVariacionJornadaAnterior(inscripciones, partidos, deporte, reglas, tablaBase)
}
