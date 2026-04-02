package com.escuelafutbol.academia.util

import java.time.Instant
import java.time.ZoneOffset

/** Año civil del día representado por medianoche UTC (convención DatePicker / Supabase). */
fun anioDesdeMillisUtcDia(millisUtcDia: Long): Int =
    Instant.ofEpochMilli(millisUtcDia).atZone(ZoneOffset.UTC).year
