package com.escuelafutbol.academia.ui.attendance

/**
 * Estado del interruptor «Es día de entrenamiento» y metadatos para textos de detección automática.
 */
data class DiaEntrenoBarraUi(
    val interruptorOn: Boolean,
    val hayOverrideManual: Boolean,
    val esDiaSemanaHabitual: Boolean,
)
