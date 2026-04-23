package com.escuelafutbol.academia.data.local.entity

import androidx.room.Entity

/**
 * Día de calendario que el staff declara como **sesión de entrenamiento** para el resumen.
 *
 * [scopeKey] vacío = marcado con el filtro «todas las categorías» (marca «universal» para esa categoría al ver una sola categoría).
 * Valor concreto = se guardó con ese filtro de categoría en el menú; la vista «todas» también reconoce el día si hay cualquier marca.
 */
@Entity(
    tableName = "dias_entrenamiento",
    primaryKeys = ["fechaDia", "scopeKey"],
)
data class DiaEntrenamiento(
    val fechaDia: Long,
    val scopeKey: String,
)
