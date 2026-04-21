package com.escuelafutbol.academia.data.local.model

import com.escuelafutbol.academia.data.local.entity.Categoria

/**
 * Portada de Inicio para padre/tutor en nube: misma resolución que staff ([categoriaPortadaParaFiltro]),
 * pero la categoría activa sale de los hijos vinculados (no del selector global de staff).
 *
 * @param hijosOrdenados pares (`jugador_remote_id`, nombre de categoría), orden estable (p. ej. por nombre de hijo).
 * @param jugadorRemoteIdPreferido null o desconocido = primer par de la lista.
 */
fun categoriaPortadaParaPadreInicio(
    hijosOrdenados: List<Pair<String, String>>,
    jugadorRemoteIdPreferido: String?,
    desdeTabla: List<Categoria>,
    desdeJugadores: List<String>,
): Categoria? {
    if (hijosOrdenados.isEmpty()) return null
    val want = jugadorRemoteIdPreferido?.trim()?.takeIf { it.isNotEmpty() }
    val par = want?.let { id ->
        hijosOrdenados.find { it.first.equals(id, ignoreCase = true) }
    } ?: hijosOrdenados.first()
    val nombre = par.second.trim().takeIf { it.isNotEmpty() } ?: return null
    return categoriaPortadaParaFiltro(nombre, desdeTabla, desdeJugadores)
}
