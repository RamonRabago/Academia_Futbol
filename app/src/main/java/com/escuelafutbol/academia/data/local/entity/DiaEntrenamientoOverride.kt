package com.escuelafutbol.academia.data.local.entity

import androidx.room.Entity

/**
 * Forzado manual del interruptor «día de entrenamiento» para un día de calendario y [scopeKey] de asistencia.
 * Si no hay fila, el valor efectivo sale de la detección automática (días de la semana en [AcademiaConfig.diasEntrenoSemanaIsoJson]).
 */
@Entity(
    tableName = "dias_entreno_override",
    primaryKeys = ["fechaDia", "scopeKey"],
)
data class DiaEntrenamientoOverride(
    val fechaDia: Long,
    val scopeKey: String,
    val valorForzado: Boolean,
)
