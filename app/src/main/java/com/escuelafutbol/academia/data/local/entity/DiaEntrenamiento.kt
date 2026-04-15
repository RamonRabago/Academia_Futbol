package com.escuelafutbol.academia.data.local.entity

import androidx.room.Entity

/**
 * Día de calendario que el staff declara como **sesión de entrenamiento** para el resumen.
 *
 * [scopeKey] vacío = marcado con el filtro «todas las categorías» (vale para cualquier vista de categoría).
 * Valor concreto = solo aplica con ese filtro de categoría activo.
 */
@Entity(
    tableName = "dias_entrenamiento",
    primaryKeys = ["fechaDia", "scopeKey"],
)
data class DiaEntrenamiento(
    val fechaDia: Long,
    val scopeKey: String,
)
