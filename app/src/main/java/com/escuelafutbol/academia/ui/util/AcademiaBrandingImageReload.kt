package com.escuelafutbol.academia.ui.util

import java.util.concurrent.atomic.AtomicInteger

/**
 * Tras resolver la academia en Supabase (a veces con varios reintentos), la primera petición Coil
 * del logo puede fallar por red saturada o quedar sin reintento si la solicitud se considera
 * equivalente a una anterior. Cada binding exitoso incrementa la generación para armar otra
 * [coil.request.ImageRequest] (misma URL en [data], otra clave de memoria) sin pull manual.
 */
object AcademiaBrandingImageReload {
    private val generation = AtomicInteger(0)

    fun generation(): Int = generation.get()

    fun bumpAfterBindingSuccess() {
        generation.incrementAndGet()
    }
}
