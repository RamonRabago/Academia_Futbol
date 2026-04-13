package com.escuelafutbol.academia.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class FcmTokenRepository(
    private val client: SupabaseClient,
) {

    /** Registra o actualiza el token FCM del usuario autenticado (RPC `register_fcm_token`). */
    suspend fun registerToken(token: String) = withContext(Dispatchers.IO) {
        val trimmed = token.trim()
        if (trimmed.isEmpty()) return@withContext
        runCatching {
            val params = buildJsonObject {
                put("p_token", trimmed)
                put("p_platform", "android")
            }
            client.postgrest.rpc("register_fcm_token", params)
        }
    }
}
