package com.escuelafutbol.academia.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.escuelafutbol.academia.data.local.entity.CobroMensualAlumno
import kotlinx.coroutines.flow.Flow

@Dao
interface CobroMensualDao {

    @Query(
        "SELECT * FROM cobros_mensuales_alumno WHERE periodoYyyyMm = :periodo ORDER BY jugadorId ASC",
    )
    fun observeByPeriodo(periodo: String): Flow<List<CobroMensualAlumno>>

    @Query(
        """
        SELECT COALESCE(SUM(
            CASE WHEN importeEsperado > importePagado
            THEN importeEsperado - importePagado ELSE 0 END
        ), 0) FROM cobros_mensuales_alumno
        """,
    )
    fun observeAdeudoHistorico(): Flow<Double>

    @Query("SELECT * FROM cobros_mensuales_alumno ORDER BY periodoYyyyMm DESC, jugadorId ASC")
    fun observeTodos(): Flow<List<CobroMensualAlumno>>

    @Query("SELECT * FROM cobros_mensuales_alumno")
    suspend fun getAll(): List<CobroMensualAlumno>

    @Query("SELECT * FROM cobros_mensuales_alumno WHERE remoteId IS NULL")
    suspend fun getSinRemoto(): List<CobroMensualAlumno>

    @Query("SELECT * FROM cobros_mensuales_alumno WHERE needsCloudPush = 1 AND remoteId IS NOT NULL")
    suspend fun getRemotosPendientesPush(): List<CobroMensualAlumno>

    @Query(
        "SELECT * FROM cobros_mensuales_alumno WHERE jugadorId = :jugadorId AND periodoYyyyMm = :periodo LIMIT 1",
    )
    suspend fun getByJugadorYPeriodo(jugadorId: Long, periodo: String): CobroMensualAlumno?

    @Query("SELECT * FROM cobros_mensuales_alumno WHERE jugadorId = :jugadorId ORDER BY periodoYyyyMm ASC")
    suspend fun getByJugadorId(jugadorId: Long): List<CobroMensualAlumno>

    @Query("SELECT * FROM cobros_mensuales_alumno WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getPorRemoteId(remoteId: String): CobroMensualAlumno?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: CobroMensualAlumno): Long

    @Update
    suspend fun update(entity: CobroMensualAlumno)

    @Query("DELETE FROM cobros_mensuales_alumno WHERE id = :id")
    suspend fun deleteById(id: Long)
}
