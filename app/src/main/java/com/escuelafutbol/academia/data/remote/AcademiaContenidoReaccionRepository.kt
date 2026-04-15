package com.escuelafutbol.academia.data.remote

import com.escuelafutbol.academia.data.remote.dto.AcademiaContenidoReaccionInsert
import com.escuelafutbol.academia.data.remote.dto.AcademiaContenidoReaccionRow
import com.escuelafutbol.academia.data.remote.dto.ContenidoReaccionTipo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AcademiaContenidoReaccionRepository(
    private val client: SupabaseClient,
) {

    suspend fun listarPorAcademia(academiaId: String): List<AcademiaContenidoReaccionRow> = withContext(Dispatchers.IO) {
        runCatching {
            client.from(TABLE).select {
                filter { eq("academia_id", academiaId) }
            }.decodeList<AcademiaContenidoReaccionRow>()
        }.getOrElse { emptyList() }
    }

    /** Sustituye la reacción del usuario (una fila por par contenido/usuario). */
    suspend fun ponerReaccion(
        academiaId: String,
        contenidoId: String,
        userId: String,
        tipo: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (tipo !in ContenidoReaccionTipo.todosWire) {
            return@withContext Result.failure(IllegalArgumentException("Tipo de reacción no válido"))
        }
        runCatching {
            client.from(TABLE).delete {
                filter {
                    eq("contenido_id", contenidoId)
                    eq("user_id", userId)
                }
            }
            client.from(TABLE).insert(
                AcademiaContenidoReaccionInsert(
                    academiaId = academiaId,
                    contenidoId = contenidoId,
                    userId = userId,
                    tipo = tipo,
                ),
            )
            Unit
        }
    }

    suspend fun quitarReaccion(contenidoId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.from(TABLE).delete {
                filter {
                    eq("contenido_id", contenidoId)
                    eq("user_id", userId)
                }
            }
            Unit
        }
    }

    companion object {
        private const val TABLE = "academia_contenido_reaccion"
    }
}
