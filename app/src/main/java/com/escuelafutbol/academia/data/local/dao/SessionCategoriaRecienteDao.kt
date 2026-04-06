package com.escuelafutbol.academia.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.escuelafutbol.academia.data.local.entity.SessionCategoriaReciente

@Dao
interface SessionCategoriaRecienteDao {

    @Query("SELECT * FROM session_categoria_reciente WHERE userId = :userId LIMIT 1")
    suspend fun getForUser(userId: String): SessionCategoriaReciente?

    @Upsert
    suspend fun upsert(row: SessionCategoriaReciente)

    @Query("DELETE FROM session_categoria_reciente WHERE userId = :userId")
    suspend fun deleteForUser(userId: String)
}
