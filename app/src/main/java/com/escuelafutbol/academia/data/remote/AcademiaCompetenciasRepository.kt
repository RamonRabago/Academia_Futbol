package com.escuelafutbol.academia.data.remote

import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaCategoriaInsert
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaCategoriaRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaInsert
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaPartidoInsert
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaPartidoRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaPartidoUpdatePatch
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaRow
import com.escuelafutbol.academia.data.remote.dto.CatalogoDeporteRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Competencias / inscripciones / partidos en Supabase (multideporte, marcador neutro).
 * Fase 1: lecturas y altas básicas; la UI (Fase 2) orquestará estos métodos.
 */
class AcademiaCompetenciasRepository(
    private val client: SupabaseClient,
) {

    suspend fun listarCatalogoDeportes(): List<CatalogoDeporteRow> = withContext(Dispatchers.IO) {
        runCatching {
            client.from(TABLE_DEPORTE).select()
                .decodeList<CatalogoDeporteRow>()
                .sortedBy { it.nombre.lowercase() }
        }.getOrElse { emptyList() }
    }

    suspend fun listarCompetencias(academiaId: String): Result<List<AcademiaCompetenciaRow>> = withContext(Dispatchers.IO) {
        runCatching {
            client.from(TABLE_COMPETENCIA).select {
                filter { eq("academia_id", academiaId) }
            }.decodeList<AcademiaCompetenciaRow>()
                .filter { it.activa }
                .sortedByDescending { it.createdAt ?: it.nombre }
        }
    }

    suspend fun insertarCompetencia(row: AcademiaCompetenciaInsert): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.from(TABLE_COMPETENCIA).insert(row)
            Unit
        }
    }

    suspend fun listarInscripciones(competenciaId: String): List<AcademiaCompetenciaCategoriaRow> =
        withContext(Dispatchers.IO) {
            runCatching {
                client.from(TABLE_INSCRIPCION).select {
                    filter { eq("competencia_id", competenciaId) }
                }.decodeList<AcademiaCompetenciaCategoriaRow>()
                    .filter { it.activo }
                    .sortedBy { it.categoriaNombre.lowercase() }
            }.getOrElse { emptyList() }
        }

    suspend fun insertarInscripcion(row: AcademiaCompetenciaCategoriaInsert): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                client.from(TABLE_INSCRIPCION).insert(row)
                Unit
            }
        }

    suspend fun listarPartidos(competenciaId: String): List<AcademiaCompetenciaPartidoRow> =
        withContext(Dispatchers.IO) {
            runCatching {
                client.from(TABLE_PARTIDO).select {
                    filter { eq("competencia_id", competenciaId) }
                }.decodeList<AcademiaCompetenciaPartidoRow>()
                    .sortedWith(
                        compareBy<AcademiaCompetenciaPartidoRow> { it.jornada }
                            .thenBy { it.fecha }
                            .thenBy { it.categoriaNombre.lowercase() },
                    )
            }.getOrElse { emptyList() }
        }

    suspend fun insertarPartido(row: AcademiaCompetenciaPartidoInsert): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                client.from(TABLE_PARTIDO).insert(row)
                Unit
            }
        }

    suspend fun actualizarPartido(
        partidoId: String,
        patch: AcademiaCompetenciaPartidoUpdatePatch,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.from(TABLE_PARTIDO).update(patch) {
                filter { eq("id", partidoId) }
            }
            Unit
        }
    }

    private companion object {
        const val TABLE_DEPORTE = "catalogo_deporte"
        const val TABLE_COMPETENCIA = "academia_competencia"
        const val TABLE_INSCRIPCION = "academia_competencia_categoria"
        const val TABLE_PARTIDO = "academia_competencia_partido"
    }
}
