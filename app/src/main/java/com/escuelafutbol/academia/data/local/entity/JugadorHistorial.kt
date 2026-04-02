package com.escuelafutbol.academia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jugador_historial")
data class JugadorHistorial(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jugadorId: Long,
    /** ALTA, BAJA */
    val tipo: String,
    val fechaMillis: Long,
    val detalle: String?,
    val remoteId: String? = null,
)
