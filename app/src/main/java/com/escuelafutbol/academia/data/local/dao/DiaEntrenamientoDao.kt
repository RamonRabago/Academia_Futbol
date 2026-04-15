package com.escuelafutbol.academia.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.escuelafutbol.academia.data.local.entity.DiaEntrenamiento
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaEntrenamientoDao {

    @Query("SELECT * FROM dias_entrenamiento")
    fun observeAll(): Flow<List<DiaEntrenamiento>>

    @Query(
        "SELECT * FROM dias_entrenamiento WHERE fechaDia >= :desdeMillis AND fechaDia <= :hastaMillis",
    )
    fun observeBetween(desdeMillis: Long, hastaMillis: Long): Flow<List<DiaEntrenamiento>>

    @Query("SELECT * FROM dias_entrenamiento WHERE fechaDia = :fechaDia")
    fun observeForDay(fechaDia: Long): Flow<List<DiaEntrenamiento>>

    @Upsert
    suspend fun upsert(registro: DiaEntrenamiento)

    /** Quita cualquier marca de entreno para ese día (global y por categoría). */
    @Query("DELETE FROM dias_entrenamiento WHERE fechaDia = :fechaDia")
    suspend fun deleteAllForDay(fechaDia: Long)
}
