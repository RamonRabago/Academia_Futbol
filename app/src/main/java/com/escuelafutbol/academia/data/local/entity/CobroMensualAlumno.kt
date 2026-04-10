package com.escuelafutbol.academia.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cobros_mensuales_alumno",
    indices = [
        Index(value = ["jugadorId", "periodoYyyyMm"], unique = true),
    ],
)
data class CobroMensualAlumno(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jugadorId: Long,
    /** Formato `YYYY-MM`. */
    val periodoYyyyMm: String,
    val importeEsperado: Double,
    val importePagado: Double,
    val notas: String? = null,
    val remoteId: String? = null,
    val needsCloudPush: Boolean = false,
)
