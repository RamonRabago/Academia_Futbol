package com.escuelafutbol.academia.domain.competencias

import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaRow
import com.escuelafutbol.academia.data.remote.dto.CatalogoDeporteRow

/**
 * Puntos de tabla por resultado: primero la competencia (override), luego el deporte base.
 */
data class ReglasPuntosTabla(
    val puntosVictoria: Int,
    val puntosEmpate: Int,
    val puntosDerrota: Int,
)

fun resolverReglasPuntosTabla(
    deporte: CatalogoDeporteRow,
    competencia: AcademiaCompetenciaRow?,
): ReglasPuntosTabla {
    val c = competencia
    return ReglasPuntosTabla(
        puntosVictoria = c?.puntosPorVictoria ?: deporte.puntosPorVictoria,
        puntosEmpate = c?.puntosPorEmpate ?: deporte.puntosPorEmpate,
        puntosDerrota = c?.puntosPorDerrota ?: deporte.puntosPorDerrota,
    )
}
