package com.escuelafutbol.academia.data.local.entity

import androidx.room.Entity

/**
 * Preferencia de portada de Inicio (padre en nube): hijo elegido por `jugador_remote_id` en Supabase.
 * Una fila por usuario de Auth; independiente de [SessionCategoriaReciente].
 */
@Entity(
    tableName = "session_parent_portada_jugador",
    primaryKeys = ["userId"],
)
data class SessionParentPortadaJugador(
    val userId: String,
    val jugadorRemoteId: String? = null,
)
