package com.escuelafutbol.academia.ui.parents.components

import com.escuelafutbol.academia.ui.parents.HijoResumenUi

/** Clave estable para lista y expansión exclusiva de una tarjeta de hijo. */
fun HijoResumenUi.linkedChildStableKey(): String =
    listOf(nombre, categoria, vinculoId.orEmpty(), fotoUrlSupabase.orEmpty(), fotoRutaAbsoluta.orEmpty())
        .joinToString("|")
