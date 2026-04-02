package com.escuelafutbol.academia.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object DayMillis {

    fun today(zone: ZoneId = ZoneId.systemDefault()): Long =
        startOfDay(LocalDate.now(zone), zone)

    fun fromLocalDate(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
        startOfDay(date, zone)

    fun startOfDay(instantMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long {
        val localDate = Instant.ofEpochMilli(instantMillis).atZone(zone).toLocalDate()
        return startOfDay(localDate, zone)
    }

    private fun startOfDay(date: LocalDate, zone: ZoneId): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()
}
