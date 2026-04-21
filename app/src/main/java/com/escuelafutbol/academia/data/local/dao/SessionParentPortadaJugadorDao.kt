package com.escuelafutbol.academia.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.escuelafutbol.academia.data.local.entity.SessionParentPortadaJugador

@Dao
interface SessionParentPortadaJugadorDao {

    @Query("SELECT * FROM session_parent_portada_jugador WHERE userId = :userId LIMIT 1")
    suspend fun getForUser(userId: String): SessionParentPortadaJugador?

    @Upsert
    suspend fun upsert(row: SessionParentPortadaJugador)

    @Query("DELETE FROM session_parent_portada_jugador WHERE userId = :userId")
    suspend fun deleteForUser(userId: String)
}
