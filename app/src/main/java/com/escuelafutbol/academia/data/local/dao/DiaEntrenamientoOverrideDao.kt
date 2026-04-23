package com.escuelafutbol.academia.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.escuelafutbol.academia.data.local.entity.DiaEntrenamientoOverride
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaEntrenamientoOverrideDao {

    @Query("SELECT * FROM dias_entreno_override WHERE fechaDia = :fechaDia")
    fun observeForDay(fechaDia: Long): Flow<List<DiaEntrenamientoOverride>>

    @Upsert
    suspend fun upsert(row: DiaEntrenamientoOverride)

    @Query(
        "DELETE FROM dias_entreno_override WHERE fechaDia = :fechaDia AND scopeKey = :scopeKey",
    )
    suspend fun deleteByFechaYScope(fechaDia: Long, scopeKey: String)

    @Query("DELETE FROM dias_entreno_override")
    suspend fun deleteAll()
}
