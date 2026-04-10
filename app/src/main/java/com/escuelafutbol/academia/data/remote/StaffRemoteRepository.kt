package com.escuelafutbol.academia.data.remote

import com.escuelafutbol.academia.data.local.entity.Staff
import com.escuelafutbol.academia.data.remote.dto.StaffCamposPatch
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StaffRemoteRepository(
    private val client: SupabaseClient,
) {

    suspend fun actualizarCamposStaff(remoteId: String, staff: Staff) = withContext(Dispatchers.IO) {
        client.from("equipo_staff").update(
            StaffCamposPatch(
                nombre = staff.nombre,
                rol = staff.rol,
                telefono = staff.telefono,
                email = staff.email,
                sueldoMensual = staff.sueldoMensual,
            ),
        ) {
            filter { eq("id", remoteId) }
        }
    }
}
