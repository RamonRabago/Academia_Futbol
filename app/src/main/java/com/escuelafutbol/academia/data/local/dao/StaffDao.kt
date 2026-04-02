package com.escuelafutbol.academia.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.escuelafutbol.academia.data.local.entity.Staff
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {

    @Query("SELECT * FROM staff ORDER BY rol, nombre ASC")
    fun observeAll(): Flow<List<Staff>>

    @Query("SELECT * FROM staff")
    suspend fun getAll(): List<Staff>

    @Query("SELECT * FROM staff WHERE remoteId IS NULL")
    suspend fun getSinRemoto(): List<Staff>

    @Query("SELECT * FROM staff WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getPorRemoteId(remoteId: String): Staff?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(staff: Staff): Long

    @Update
    suspend fun update(staff: Staff)

    @Delete
    suspend fun delete(staff: Staff)
}
