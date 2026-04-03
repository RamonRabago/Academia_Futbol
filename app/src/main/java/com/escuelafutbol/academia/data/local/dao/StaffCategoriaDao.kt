package com.escuelafutbol.academia.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.escuelafutbol.academia.data.local.entity.StaffCategoria
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffCategoriaDao {

    @Query(
        "SELECT categoriaNombre FROM staff_categorias WHERE staffId = :staffId ORDER BY categoriaNombre ASC",
    )
    fun observeNombresForStaff(staffId: Long): Flow<List<String>>

    @Query(
        "SELECT categoriaNombre FROM staff_categorias WHERE staffId = :staffId ORDER BY categoriaNombre ASC",
    )
    suspend fun getNombresForStaff(staffId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: StaffCategoria)

    @Query("DELETE FROM staff_categorias WHERE staffId = :staffId")
    suspend fun deleteForStaff(staffId: Long)

    @Transaction
    suspend fun setCategoriasForStaff(staffId: Long, nombres: Collection<String>) {
        deleteForStaff(staffId)
        for (n in nombres) {
            val t = n.trim()
            if (t.isNotEmpty()) {
                insert(StaffCategoria(staffId = staffId, categoriaNombre = t))
            }
        }
    }
}
