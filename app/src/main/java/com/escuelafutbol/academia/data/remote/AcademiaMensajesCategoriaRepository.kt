package com.escuelafutbol.academia.data.remote

import com.escuelafutbol.academia.data.remote.dto.AcademiaMensajeCategoriaInsert
import com.escuelafutbol.academia.data.remote.dto.AcademiaMensajeCategoriaRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AcademiaMensajesCategoriaRepository(
    private val client: SupabaseClient,
) {

    suspend fun listar(academiaId: String): List<AcademiaMensajeCategoriaRow> = withContext(Dispatchers.IO) {
        runCatching {
            client.from(TABLE).select {
                filter { eq("academia_id", academiaId) }
            }.decodeList<AcademiaMensajeCategoriaRow>()
                .filter { it.archivedAt == null }
                .sortedByDescending { it.createdAt }
                .take(LIMIT)
        }.getOrElse { emptyList() }
    }

    suspend fun insertar(
        academiaId: String,
        categoriaNombre: String,
        tipo: String,
        titulo: String,
        cuerpo: String,
        authorUserId: String,
        eventAtIso: String? = null,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.from(TABLE).insert(
                AcademiaMensajeCategoriaInsert(
                    academiaId = academiaId,
                    categoriaNombre = categoriaNombre.trim(),
                    tipo = tipo,
                    titulo = titulo.trim(),
                    cuerpo = cuerpo.trim(),
                    authorUserId = authorUserId,
                    eventAt = eventAtIso,
                ),
            )
            Unit
        }
    }

    companion object {
        private const val TABLE = "academia_mensajes_categoria"
        private const val LIMIT = 120
    }
}
