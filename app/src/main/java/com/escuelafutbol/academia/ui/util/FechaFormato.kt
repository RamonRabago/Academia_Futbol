package com.escuelafutbol.academia.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Locale fijo para fechas visibles en la app (coherencia con textos en español). */
val AcademiaLocaleEs: Locale = Locale("es", "ES")

private val formatoSoloDia =
    DateTimeFormatter.ofPattern("dd/MM/yyyy", AcademiaLocaleEs)

private val formatoDiaHora =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", AcademiaLocaleEs)

/** Título asistencia: día de la semana + misma base numérica dd/MM/yyyy. */
private val formatoDiaConSemana =
    DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", AcademiaLocaleEs)

/** Momentos reales (alta, historial, etc.) en zona del dispositivo. */
fun formatearFechaDiaLocal(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().format(formatoSoloDia)

fun formatearFechaHoraLocal(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(millis).atZone(zone).format(formatoDiaHora)

/**
 * Fecha de nacimiento guardada como medianoche UTC del día elegido en el DatePicker
 * (convención de Material 3).
 */
fun formatearFechaCalendarioUtc(millisUtcDia: Long): String =
    Instant.ofEpochMilli(millisUtcDia).atZone(ZoneOffset.UTC).toLocalDate().format(formatoSoloDia)

fun formatearFechaAsistenciaTitulo(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().format(formatoDiaConSemana)
