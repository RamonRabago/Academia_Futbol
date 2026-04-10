package com.escuelafutbol.academia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staff")
data class Staff(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    /** [com.escuelafutbol.academia.data.local.model.RolStaff.name] */
    val rol: String,
    val telefono: String?,
    val email: String?,
    val fotoRutaAbsoluta: String?,
    val fotoUrlSupabase: String? = null,
    val remoteId: String? = null,
    /** Sueldo mensual de referencia (solo gestión interna). */
    val sueldoMensual: Double? = null,
)
