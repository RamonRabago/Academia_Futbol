package com.escuelafutbol.academia.data.remote

import com.escuelafutbol.academia.data.local.entity.CobroMensualAlumno
import com.escuelafutbol.academia.data.remote.dto.CobroMensualPatch
import com.escuelafutbol.academia.data.remote.dto.CobroMensualRow
import com.escuelafutbol.academia.data.remote.dto.toCloudInsert
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CobroMensualRemoteRepository(
    private val client: SupabaseClient,
) {

    suspend fun insertar(academiaId: String, jugadorRemoteId: String, cobro: CobroMensualAlumno): String =
        withContext(Dispatchers.IO) {
            val row = client.from("jugador_cobros_mensual").insert(
                cobro.toCloudInsert(academiaId, jugadorRemoteId),
            ) { select() }.decodeSingle<CobroMensualRow>()
            row.id
        }

    suspend fun actualizar(remoteId: String, cobro: CobroMensualAlumno) = withContext(Dispatchers.IO) {
        client.from("jugador_cobros_mensual").update(
            CobroMensualPatch(
                importeEsperado = cobro.importeEsperado,
                importePagado = cobro.importePagado,
                notas = cobro.notas,
            ),
        ) {
            filter { eq("id", remoteId) }
        }
    }
}
