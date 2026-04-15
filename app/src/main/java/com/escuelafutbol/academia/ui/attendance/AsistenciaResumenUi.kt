package com.escuelafutbol.academia.ui.attendance

/** Qué rango de fechas usa el resumen visual respecto al día que se está tomando asistencia. */
enum class AsistenciaPeriodoResumen {
    /** Mes calendario del día seleccionado en la cabecera. */
    MesVista,

    /** 1 ene – 31 dic del año elegido en el resumen. */
    AnioCompleto,
}

data class AsistenciaResumenUi(
    val periodo: AsistenciaPeriodoResumen,
    val etiqueta: String,
    /** Si no es null, el resumen es solo de ese alumno (nombre para mostrar). */
    val nombreAlumnoFoco: String?,
    /** 0–100 o null si no hay registros en el período. */
    val porcentaje: Float?,
    val presentes: Int,
    val ausentes: Int,
    val totalRegistros: Int,
    /** Días distintos marcados como entrenamiento con al menos una marca en el período. */
    val diasConRegistro: Int,
    /** Hay marcas en el período pero ninguna cae en un día marcado como entrenamiento. */
    val hayMarcasFueraDeDiasEntreno: Boolean = false,
)
