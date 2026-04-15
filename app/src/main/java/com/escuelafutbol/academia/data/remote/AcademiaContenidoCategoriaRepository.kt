package com.escuelafutbol.academia.data.remote

import com.escuelafutbol.academia.data.remote.dto.AcademiaContenidoCategoriaArchivarPatch
import com.escuelafutbol.academia.data.remote.dto.AcademiaContenidoCategoriaInsert
import com.escuelafutbol.academia.data.remote.dto.AcademiaContenidoCategoriaRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaContenidoEstadoPublicacionPatch
import com.escuelafutbol.academia.data.remote.dto.encodeContenidoCuerpoImagenesUrls
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant

class AcademiaContenidoCategoriaRepository(
    private val client: SupabaseClient,
) {

    suspend fun listar(academiaId: String): List<AcademiaContenidoCategoriaRow> = withContext(Dispatchers.IO) {
        runCatching {
            client.from(TABLE).select {
                filter { eq("academia_id", academiaId) }
            }.decodeList<AcademiaContenidoCategoriaRow>()
                .filter { it.archivedAt == null }
                .sortedByDescending { it.createdAt }
                .take(LIMIT)
        }.getOrElse { emptyList() }
    }

    suspend fun insertar(
        academiaId: String,
        categoriaNombre: String,
        tema: String,
        titulo: String,
        cuerpo: String,
        authorUserId: String,
        imagenUrl: String? = null,
        cuerpoImagenesUrls: List<String> = emptyList(),
        estadoPublicacion: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.from(TABLE).insert(
                AcademiaContenidoCategoriaInsert(
                    academiaId = academiaId,
                    categoriaNombre = categoriaNombre.trim(),
                    tema = tema,
                    titulo = titulo.trim(),
                    cuerpo = cuerpo.trim(),
                    imagenUrl = imagenUrl?.trim()?.takeIf { it.isNotEmpty() },
                    cuerpoImagenesUrlsJson = encodeContenidoCuerpoImagenesUrls(cuerpoImagenesUrls),
                    authorUserId = authorUserId,
                    estadoPublicacion = estadoPublicacion,
                ),
            )
            Unit
        }
    }

    suspend fun actualizarEstadoPublicacion(
        academiaId: String,
        id: String,
        estadoPublicacion: String,
        approvedAtIso: String?,
        approvedByUserId: String?,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.from(TABLE).update(
                AcademiaContenidoEstadoPublicacionPatch(
                    estadoPublicacion = estadoPublicacion,
                    approvedAt = approvedAtIso,
                    approvedByUserId = approvedByUserId,
                ),
            ) {
                filter {
                    eq("academia_id", academiaId)
                    eq("id", id)
                }
            }
            Unit
        }
    }

    suspend fun archivar(academiaId: String, id: String): Result<Unit> = withContext(Dispatchers.IO) {
        val rpc = runCatching {
            client.postgrest.rpc(
                "archivar_academia_contenido_categoria",
                buildJsonObject {
                    put("p_id", id)
                    put("p_academia_id", academiaId)
                },
            )
            Unit
        }
        if (rpc.isSuccess) return@withContext rpc
        runCatching {
            val iso = Instant.now().toString()
            client.from(TABLE).update(AcademiaContenidoCategoriaArchivarPatch(archivedAt = iso)) {
                filter {
                    eq("academia_id", academiaId)
                    eq("id", id)
                }
            }
            Unit
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { second ->
                val first = rpc.exceptionOrNull()
                Result.failure(
                    if (first != null) {
                        Exception("${first.message ?: first::class.simpleName}\n${second.message ?: ""}".trim())
                    } else {
                        second
                    },
                )
            },
        )
    }

    companion object {
        private const val TABLE = "academia_contenido_categoria"
        private const val LIMIT = 200
    }
}
