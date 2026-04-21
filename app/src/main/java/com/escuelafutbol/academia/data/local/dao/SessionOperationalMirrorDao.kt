package com.escuelafutbol.academia.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

/**
 * Limpieza de tablas espejo locales (sync operativo). No toca [academia_config] ni sesión de categoría por usuario.
 * Se usa al cambiar de cuenta para no mezclar jugadores/categorías/asistencias entre usuarios.
 */
@Dao
interface SessionOperationalMirrorDao {

    @Query("DELETE FROM jugador_historial")
    suspend fun deleteAllJugadorHistorial()

    @Query("DELETE FROM asistencias")
    suspend fun deleteAllAsistencias()

    @Query("DELETE FROM cobros_mensuales_alumno")
    suspend fun deleteAllCobrosMensuales()

    @Query("DELETE FROM dias_entrenamiento")
    suspend fun deleteAllDiasEntrenamiento()

    @Query("DELETE FROM jugadores")
    suspend fun deleteAllJugadores()

    @Query("DELETE FROM staff_categorias")
    suspend fun deleteAllStaffCategorias()

    @Query("DELETE FROM staff")
    suspend fun deleteAllStaff()

    @Query("DELETE FROM categorias")
    suspend fun deleteAllCategorias()

    @Transaction
    suspend fun clearOperationalMirrorTables() {
        deleteAllJugadorHistorial()
        deleteAllAsistencias()
        deleteAllCobrosMensuales()
        deleteAllDiasEntrenamiento()
        deleteAllJugadores()
        deleteAllStaffCategorias()
        deleteAllStaff()
        deleteAllCategorias()
    }
}
