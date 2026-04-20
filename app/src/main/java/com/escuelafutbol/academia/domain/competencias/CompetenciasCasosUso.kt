package com.escuelafutbol.academia.domain.competencias

import com.escuelafutbol.academia.data.remote.AcademiaCompetenciasRepository

/**
 * Orquestación de lecturas + cálculo de tabla (Fase 1, sin UI).
 *
 * La semántica de la tabla y de la columna **Var.** (variación de posición) y el bloque de líderes
 * ofensivos en la app se documentan en dominio (`CompetenciaTablaCalculator`, `CompetenciaLideresOfensivos`)
 * y en `docs/COMPETENCIAS_TABLA_EVOLUCION.md` para evolución futura (por fecha, por jornada, grupos, etc.).
 */
class CompetenciasCasosUso(
    private val repository: AcademiaCompetenciasRepository,
) {

    suspend fun listarDeportes() = repository.listarCatalogoDeportes()

    suspend fun listarCompetencias(academiaId: String) = repository.listarCompetencias(academiaId)

    suspend fun listarInscripciones(competenciaId: String) = repository.listarInscripciones(competenciaId)

    suspend fun listarPartidos(competenciaId: String) = repository.listarPartidos(competenciaId)

    /**
     * Devuelve líneas de tabla ordenadas o falla si no se encuentra la competencia o el deporte asociado.
     */
    suspend fun calcularTablaPosiciones(
        academiaId: String,
        competenciaId: String,
    ): Result<List<LineaTablaPosicion>> {
        val competencias = repository.listarCompetencias(academiaId).getOrElse { return Result.failure(it) }
        val competencia = competencias.find { it.id == competenciaId }
            ?: return Result.failure(IllegalArgumentException("Competencia no encontrada"))
        val deportes = repository.listarCatalogoDeportes()
        val deporte = deportes.find { it.id == competencia.deporteId }
            ?: return Result.failure(IllegalStateException("Deporte de catálogo no encontrado"))
        val inscripciones = repository.listarInscripciones(competenciaId)
        val partidos = repository.listarPartidos(competenciaId)
        val reglas = resolverReglasPuntosTabla(deporte, competencia)
        return Result.success(
            calcularTablaPosiciones(inscripciones, partidos, deporte, reglas),
        )
    }
}
