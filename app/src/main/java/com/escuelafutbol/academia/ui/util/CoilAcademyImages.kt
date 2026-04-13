package com.escuelafutbol.academia.ui.util

import android.content.Context
import coil.request.ImageRequest
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.Categoria
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.data.local.entity.Staff
import java.io.File

fun AcademiaConfig.coilLogoModel(context: Context): Any? {
    logoUrlSupabase?.trim()?.takeIf { it.isNotEmpty() }?.let { url ->
        return ImageRequest.Builder(context).data(url).crossfade(true).build()
    }
    val path = logoRutaAbsoluta ?: return null
    val f = File(path)
    if (!f.exists()) return null
    return ImageRequest.Builder(context).data(f).crossfade(true).build()
}

fun Categoria.coilPortadaCategoriaModel(context: Context): Any? {
    portadaUrlSupabase?.trim()?.takeIf { it.isNotEmpty() }?.let { url ->
        return ImageRequest.Builder(context).data(url).crossfade(true).build()
    }
    val path = portadaRutaAbsoluta ?: return null
    val f = File(path)
    if (!f.exists()) return null
    return ImageRequest.Builder(context).data(f).crossfade(true).build()
}

fun AcademiaConfig.coilPortadaModel(context: Context): Any? {
    portadaUrlSupabase?.trim()?.takeIf { it.isNotEmpty() }?.let { url ->
        return ImageRequest.Builder(context).data(url).crossfade(true).build()
    }
    val path = portadaRutaAbsoluta ?: return null
    val f = File(path)
    if (!f.exists()) return null
    return ImageRequest.Builder(context).data(f).crossfade(true).build()
}

fun Jugador.coilFotoModel(context: Context): Any? {
    fotoUrlSupabase?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    val path = fotoRutaAbsoluta ?: return null
    val f = File(path)
    if (!f.exists()) return null
    return ImageRequest.Builder(context).data(f).crossfade(true).build()
}

fun Staff.coilFotoModel(context: Context): Any? {
    fotoUrlSupabase?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    val path = fotoRutaAbsoluta ?: return null
    val f = File(path)
    if (!f.exists()) return null
    return ImageRequest.Builder(context).data(f).crossfade(true).build()
}
