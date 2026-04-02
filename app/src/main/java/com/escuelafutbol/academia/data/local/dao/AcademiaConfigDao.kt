package com.escuelafutbol.academia.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademiaConfigDao {

    @Query("SELECT * FROM academia_config WHERE id = 1 LIMIT 1")
    fun observe(): Flow<AcademiaConfig?>

    @Query("SELECT * FROM academia_config WHERE id = 1 LIMIT 1")
    suspend fun getActual(): AcademiaConfig?

    @Upsert
    suspend fun upsert(config: AcademiaConfig)
}
