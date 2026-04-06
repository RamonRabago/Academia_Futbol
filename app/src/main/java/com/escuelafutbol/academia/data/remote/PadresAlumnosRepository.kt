package com.escuelafutbol.academia.data.remote

import com.escuelafutbol.academia.data.remote.dto.AcademiaPadresAlumnoInsert
import com.escuelafutbol.academia.data.remote.dto.AcademiaPadresAlumnoRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PadresAlumnosRepository(private val client: SupabaseClient) {

    /** Staff ve todos los vínculos del club; si se pasa [parentUserId], filtra a ese tutor. */
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

    suspend fun deleteVinculo(linkId: String) = withContext(Dispatchers.IO) {
        client.from("academia_padres_alumnos").delete {
            filter { eq("id", linkId) }
        }
        Unit
    }
}
