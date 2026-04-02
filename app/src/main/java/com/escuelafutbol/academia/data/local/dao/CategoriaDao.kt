package com.escuelafutbol.academia.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.escuelafutbol.academia.data.local.entity.Categoria
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    @Query("SELECT nombre FROM categorias ORDER BY nombre ASC")
    fun observeNombres(): Flow<List<String>>

    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    fun observeAllOrdered(): Flow<List<Categoria>>

    @Query("SELECT * FROM categorias WHERE nombre = :nombre LIMIT 1")
    fun observeByNombre(nombre: String): Flow<Categoria?>

    @Query("SELECT * FROM categorias WHERE nombre = :nombre LIMIT 1")
    suspend fun getByNombre(nombre: String): Categoria?

    @Query("SELECT * FROM categorias")
    suspend fun getAll(): List<Categoria>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(categoria: Categoria)

    @Update
    suspend fun update(categoria: Categoria)
}
