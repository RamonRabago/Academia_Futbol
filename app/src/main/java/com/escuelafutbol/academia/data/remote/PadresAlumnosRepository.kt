package com.escuelafutbol.academia.data.remote

import com.escuelafutbol.academia.data.remote.dto.AcademiaPadresAlumnoInsert
import com.escuelafutbol.academia.data.remote.dto.AcademiaPadresAlumnoRow
import com.escuelafutbol.academia.data.remote.dto.JugadorListaVinculoStaffRow
import com.escuelafutbol.academia.data.remote.dto.JugadorRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class PadresAlumnosRepository(private val client: SupabaseClient) {

    /** Staff ve todos los vínculos del club; si se pasa [parentUserId], filtra a ese tutor. */
    /**
     * Jugadores visibles para el usuario actual (RLS).
     * En cuenta padre devuelve hijos ya vinculados y candidatos cuyo [JugadorRow.emailTutor] coincide con el JWT.
     */
    suspend fun listJugadoresAcademia(academiaId: String): List<JugadorRow> =
        withContext(Dispatchers.IO) {
            client.from("jugadores").select {
                filter {
                    eq("academia_id", academiaId)
                    eq("activo", true)
                }
            }.decodeList<JugadorRow>()
        }

    /**
     * Candidatos a vincular tutor ↔ alumno desde gestión de miembros (owner/admin/coordinador).
     * Preferencia: RPC en nube (lista completa); si falla (migración no aplicada), mismo criterio vía SELECT + RLS.
     */
    suspend fun listJugadoresDisponiblesParaVinculoPadreStaff(
        academiaId: String,
        parentUserId: String,
    ): List<JugadorListaVinculoStaffRow> =
        withContext(Dispatchers.IO) {
            val params = buildJsonObject {
                put("p_academia_id", academiaId)
                put("p_parent_user_id", parentUserId)
            }
            runCatching {
                client.postgrest.rpc("list_jugadores_para_vinculo_padre_staff", params)
                    .decodeList<JugadorListaVinculoStaffRow>()
            }.getOrElse {
                val linked = listVinculos(academiaId, parentUserId).map { it.jugadorId }.toSet()
                listJugadoresAcademia(academiaId)
                    .filter { row -> row.activo && row.id !in linked }
                    .map { row ->
                        JugadorListaVinculoStaffRow(
                            id = row.id,
                            nombre = row.nombre,
                            categoria = row.categoria,
                        )
                    }
            }
        }

    suspend fun listVinculos(academiaId: String, parentUserId: String? = null): List<AcademiaPadresAlumnoRow> =
        withContext(Dispatchers.IO) {
            if (parentUserId != null) {
                client.from("academia_padres_alumnos").select {
                    filter {
                        eq("academia_id", academiaId)
                        eq("parent_user_id", parentUserId)
                    }
                }.decodeList<AcademiaPadresAlumnoRow>()
            } else {
                client.from("academia_padres_alumnos").select {
                    filter { eq("academia_id", academiaId) }
                }.decodeList<AcademiaPadresAlumnoRow>()
            }
        }

    suspend fun insertVinculo(academiaId: String, parentUserId: String, jugadorRemoteId: String) =
        withContext(Dispatchers.IO) {
            client.from("academia_padres_alumnos").insert(
                AcademiaPadresAlumnoInsert(
                    academiaId = academiaId,
                    parentUserId = parentUserId,
                    jugadorId = jugadorRemoteId,
                ),
            )
            Unit
        }

    /** Staff (RLS) o padre sobre su propia fila (política `padres_alumnos_delete_parent_own`). */
    suspend fun deleteVinculo(linkId: String) = withContext(Dispatchers.IO) {
        client.from("academia_padres_alumnos").delete {
            filter { eq("id", linkId) }
        }
        Unit
    }
}
