package com.escuelafutbol.academia.data.remote

import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.data.remote.dto.JugadorRemoteUpdatePatch
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JugadorRemoteRepository(
    private val client: SupabaseClient,
) {

    suspend fun actualizarCamposJugador(remoteId: String, jugador: Jugador) = withContext(Dispatchers.IO) {
        val patch = JugadorRemoteUpdatePatch.fromJugador(jugador)
        client.from("jugadores").update(patch) {
            filter { eq("id", remoteId) }
        }
    }
}
