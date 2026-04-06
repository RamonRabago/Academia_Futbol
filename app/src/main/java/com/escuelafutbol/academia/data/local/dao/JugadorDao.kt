package com.escuelafutbol.academia.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.data.local.entity.JugadorHistorial
import com.escuelafutbol.academia.data.local.model.JugadorHistorialTipo
import kotlinx.coroutines.flow.Flow

@Dao
interface JugadorDao {

    @Query("SELECT * FROM jugadores WHERE activo = 1 ORDER BY categoria, nombre")
    fun observeAll(): Flow<List<Jugador>>

    @Query("SELECT DISTINCT categoria FROM jugadores ORDER BY categoria ASC")
    fun observeCategorias(): Flow<List<String>>

    @Query(
        "SELECT * FROM jugadores WHERE activo = 1 AND trim(categoria) = trim(:categoria) ORDER BY nombre ASC",
    )
    fun observeByCategoria(categoria: String): Flow<List<Jugador>>

    @Query(
        "SELECT * FROM jugadores WHERE activo = 1 AND trim(categoria) IN (:nombres) ORDER BY categoria, nombre ASC",
    )
    fun observeByCategorias(nombres: List<String>): Flow<List<Jugador>>

    @Query(
        "SELECT * FROM jugadores WHERE activo = 1 AND trim(categoria) IN (:nombres) ORDER BY categoria, nombre ASC",
    )
    suspend fun getByCategorias(nombres: List<String>): List<Jugador>

    @Query(
        "SELECT * FROM jugadores WHERE activo = 1 AND trim(categoria) = trim(:categoria) ORDER BY nombre ASC",
    )
    suspend fun getByCategoria(categoria: String): List<Jugador>

    @Query("SELECT * FROM jugadores WHERE activo = 1 ORDER BY categoria, nombre")
    suspend fun getAll(): List<Jugador>

    @Query("SELECT * FROM jugadores")
    suspend fun getAllIncludingInactive(): List<Jugador>

    @Query("SELECT * FROM jugadores WHERE remoteId IS NULL")
    suspend fun getJugadoresSinRemoto(): List<Jugador>

    @Query("SELECT * FROM jugadores WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getJugadorPorRemoteId(remoteId: String): Jugador?

    @Query("SELECT * FROM jugadores WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Jugador?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jugador: Jugador): Long

    @Insert
    suspend fun insertHistorial(h: JugadorHistorial): Long

    @Update
    suspend fun update(jugador: Jugador)

    @Delete
    suspend fun delete(jugador: Jugador)

    @Query(
        "SELECT * FROM jugador_historial WHERE jugadorId = :jugadorId ORDER BY fechaMillis DESC, id DESC",
    )
    fun observeHistorial(jugadorId: Long): Flow<List<JugadorHistorial>>

    @Query("SELECT * FROM jugador_historial WHERE remoteId IS NULL")
    suspend fun getHistorialSinRemoto(): List<JugadorHistorial>

    @Query("SELECT * FROM jugador_historial")
    suspend fun getAllHistorial(): List<JugadorHistorial>

    @Query("SELECT * FROM jugador_historial WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getHistorialPorRemoteId(remoteId: String): JugadorHistorial?

    @Update
    suspend fun updateHistorial(h: JugadorHistorial)

    @Transaction
    suspend fun insertJugadorConAlta(jugador: Jugador): Long {
        val id = insert(jugador)
        insertHistorial(
            JugadorHistorial(
                jugadorId = id,
                tipo = JugadorHistorialTipo.ALTA.name,
                fechaMillis = jugador.fechaAltaMillis,
                detalle = null,
            ),
        )
        return id
    }

    @Transaction
    suspend fun darBaja(jugador: Jugador, fechaMillis: Long, detalle: String?) {
        update(
            jugador.copy(
                activo = false,
                fechaBajaMillis = fechaMillis,
            ),
        )
        insertHistorial(
            JugadorHistorial(
                jugadorId = jugador.id,
                tipo = JugadorHistorialTipo.BAJA.name,
                fechaMillis = fechaMillis,
                detalle = detalle,
            ),
        )
    }
}
