package com.escuelafutbol.academia.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.escuelafutbol.academia.data.local.entity.Asistencia
import kotlinx.coroutines.flow.Flow

@Dao
interface AsistenciaDao {

    @Query("SELECT * FROM asistencias")
    fun observeAll(): Flow<List<Asistencia>>

    @Query("SELECT * FROM asistencias WHERE fechaDia = :fechaDia")
    fun observeForDay(fechaDia: Long): Flow<List<Asistencia>>

    @Query(
        "SELECT * FROM asistencias WHERE fechaDia >= :desdeMillis AND fechaDia <= :hastaMillis " +
            "ORDER BY fechaDia ASC, jugadorId ASC",
    )
    fun observeBetween(desdeMillis: Long, hastaMillis: Long): Flow<List<Asistencia>>

    @Query("SELECT * FROM asistencias WHERE fechaDia = :fechaDia")
    suspend fun getForDay(fechaDia: Long): List<Asistencia>

    @Query("SELECT * FROM asistencias")
    suspend fun getAll(): List<Asistencia>

    @Query("SELECT * FROM asistencias WHERE remoteId IS NULL")
    suspend fun getSinRemoto(): List<Asistencia>

    @Query(
        "SELECT * FROM asistencias WHERE remoteId IS NOT NULL AND needsCloudPush = 1",
    )
    suspend fun getRemotasPendientesPush(): List<Asistencia>

    @Query(
        "SELECT * FROM asistencias WHERE jugadorId = :jugadorId AND fechaDia = :fechaDia LIMIT 1",
    )
    suspend fun getPorJugadorYDia(jugadorId: Long, fechaDia: Long): Asistencia?

    @Query("SELECT * FROM asistencias WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getPorRemoteId(remoteId: String): Asistencia?

    @Upsert
    suspend fun upsert(registro: Asistencia)
}
