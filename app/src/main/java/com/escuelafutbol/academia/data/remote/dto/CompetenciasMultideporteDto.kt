package com.escuelafutbol.academia.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Estados de partido en almacén (neutro, no ligado a un deporte concreto). */
object CompetenciaPartidoEstado {
    const val PROGRAMADO = "programado"
    const val JUGADO = "jugado"
    const val POSPUESTO = "pospuesto"
    const val CANCELADO = "cancelado"

    val todosWire: Set<String> = setOf(PROGRAMADO, JUGADO, POSPUESTO, CANCELADO)
}

object CompetenciaPartidoLocalVisitante {
    const val LOCAL = "local"
    const val VISITANTE = "visitante"
    const val NEUTRAL = "neutral"
}

@Serializable
data class CatalogoDeporteRow(
    val id: String,
    val codigo: String,
    val nombre: String,
    @SerialName("etiqueta_score_singular") val etiquetaScoreSingular: String,
    @SerialName("etiqueta_score_plural") val etiquetaScorePlural: String,
    @SerialName("etiqueta_score_favor") val etiquetaScoreFavor: String? = null,
    @SerialName("etiqueta_score_contra") val etiquetaScoreContra: String? = null,
    @SerialName("permite_empate") val permiteEmpate: Boolean = true,
    @SerialName("puntos_por_victoria") val puntosPorVictoria: Int = 3,
    @SerialName("puntos_por_empate") val puntosPorEmpate: Int = 1,
    @SerialName("puntos_por_derrota") val puntosPorDerrota: Int = 0,
    @SerialName("maneja_sets") val manejaSets: Boolean = false,
    @SerialName("maneja_periodos") val manejaPeriodos: Boolean = false,
    @SerialName("maneja_entradas") val manejaEntradas: Boolean = false,
    val activo: Boolean = true,
)

@Serializable
data class AcademiaCompetenciaRow(
    val id: String,
    @SerialName("academia_id") val academiaId: String,
    @SerialName("deporte_id") val deporteId: String,
    val nombre: String,
    val temporada: String? = null,
    val descripcion: String? = null,
    @SerialName("sede_o_zona") val sedeOZona: String? = null,
    @SerialName("tipo_competencia") val tipoCompetencia: String,
    @SerialName("fecha_inicio") val fechaInicio: String? = null,
    @SerialName("fecha_fin") val fechaFin: String? = null,
    @SerialName("puntos_por_victoria") val puntosPorVictoria: Int? = null,
    @SerialName("puntos_por_empate") val puntosPorEmpate: Int? = null,
    @SerialName("puntos_por_derrota") val puntosPorDerrota: Int? = null,
    val activa: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class AcademiaCompetenciaInsert(
    @SerialName("academia_id") val academiaId: String,
    @SerialName("deporte_id") val deporteId: String,
    val nombre: String,
    val temporada: String? = null,
    val descripcion: String? = null,
    @SerialName("sede_o_zona") val sedeOZona: String? = null,
    @SerialName("tipo_competencia") val tipoCompetencia: String,
    @SerialName("fecha_inicio") val fechaInicio: String? = null,
    @SerialName("fecha_fin") val fechaFin: String? = null,
    @SerialName("puntos_por_victoria") val puntosPorVictoria: Int? = null,
    @SerialName("puntos_por_empate") val puntosPorEmpate: Int? = null,
    @SerialName("puntos_por_derrota") val puntosPorDerrota: Int? = null,
    val activa: Boolean = true,
)

@Serializable
data class AcademiaCompetenciaCategoriaRow(
    val id: String,
    @SerialName("competencia_id") val competenciaId: String,
    @SerialName("categoria_nombre") val categoriaNombre: String,
    @SerialName("nombre_equipo_mostrado") val nombreEquipoMostrado: String? = null,
    val grupo: String? = null,
    val activo: Boolean = true,
)

@Serializable
data class AcademiaCompetenciaCategoriaInsert(
    @SerialName("competencia_id") val competenciaId: String,
    @SerialName("categoria_nombre") val categoriaNombre: String,
    @SerialName("nombre_equipo_mostrado") val nombreEquipoMostrado: String? = null,
    val grupo: String? = null,
    val activo: Boolean = true,
)

@Serializable
data class AcademiaCompetenciaPartidoRow(
    val id: String,
    @SerialName("competencia_id") val competenciaId: String,
    @SerialName("categoria_en_competencia_id") val categoriaEnCompetenciaId: String,
    @SerialName("categoria_nombre") val categoriaNombre: String,
    val jornada: Int = 1,
    /** ISO date `yyyy-MM-dd`. */
    val fecha: String,
    /** `HH:mm:ss` o null desde PostgREST `time`. */
    val hora: String? = null,
    val sede: String? = null,
    val rival: String = "",
    @SerialName("local_visitante") val localVisitante: String = CompetenciaPartidoLocalVisitante.NEUTRAL,
    @SerialName("score_propio") val scorePropio: Int? = null,
    @SerialName("score_rival") val scoreRival: Int? = null,
    val jugado: Boolean = false,
    val estado: String = CompetenciaPartidoEstado.PROGRAMADO,
    val notas: String? = null,
    @SerialName("detalle_marcador_json") val detalleMarcadorJson: String? = null,
)

@Serializable
data class AcademiaCompetenciaPartidoUpdatePatch(
    val jornada: Int? = null,
    val fecha: String? = null,
    val hora: String? = null,
    val sede: String? = null,
    val rival: String? = null,
    @SerialName("local_visitante") val localVisitante: String? = null,
    @SerialName("score_propio") val scorePropio: Int? = null,
    @SerialName("score_rival") val scoreRival: Int? = null,
    val jugado: Boolean? = null,
    val estado: String? = null,
    val notas: String? = null,
    @SerialName("detalle_marcador_json") val detalleMarcadorJson: String? = null,
)

@Serializable
data class AcademiaCompetenciaPartidoInsert(
    @SerialName("competencia_id") val competenciaId: String,
    @SerialName("categoria_en_competencia_id") val categoriaEnCompetenciaId: String,
    @SerialName("categoria_nombre") val categoriaNombre: String,
    val jornada: Int = 1,
    val fecha: String,
    val hora: String? = null,
    val sede: String? = null,
    val rival: String = "",
    @SerialName("local_visitante") val localVisitante: String = CompetenciaPartidoLocalVisitante.NEUTRAL,
    @SerialName("score_propio") val scorePropio: Int? = null,
    @SerialName("score_rival") val scoreRival: Int? = null,
    val jugado: Boolean = false,
    val estado: String = CompetenciaPartidoEstado.PROGRAMADO,
    val notas: String? = null,
    @SerialName("detalle_marcador_json") val detalleMarcadorJson: String? = null,
)
