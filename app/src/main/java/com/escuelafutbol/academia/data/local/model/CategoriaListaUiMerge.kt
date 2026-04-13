package com.escuelafutbol.academia.data.local.model

import com.escuelafutbol.academia.data.local.entity.Categoria
import java.io.File
import java.util.Locale

/**
 * Lista unificada categorías tabla + nombres en jugadores, misma lógica que el selector:
 * una entrada por [normalizarClaveCategoriaNombre], prioridad a la fila con mejor portada,
 * y enriquecimiento si hay otra fila en tabla equivalente con foto/URL.
 */
fun mergeCategoriasParaUi(
    desdeTabla: List<Categoria>,
    desdeJugadores: List<String>,
): List<Categoria> {
    fun Categoria.portadaScore(): Int = when {
        !portadaUrlSupabase.isNullOrBlank() -> 2
        portadaRutaAbsoluta != null && File(portadaRutaAbsoluta).exists() -> 1
        else -> 0
    }
    fun Categoria.tienePortadaVisual(): Boolean = portadaScore() > 0

    val map = mutableMapOf<String, Categoria>()
    for (c in desdeTabla) {
        val key = normalizarClaveCategoriaNombre(c.nombre)
        if (key.isEmpty()) continue
        val prev = map[key]
        if (prev == null || c.portadaScore() > prev.portadaScore()) {
            map[key] = c
        }
    }
    for (jn in desdeJugadores) {
        val key = normalizarClaveCategoriaNombre(jn)
        if (key.isEmpty()) continue
        if (!map.containsKey(key)) {
            val display = jn.trim()
            map[key] = Categoria(nombre = display)
        }
    }
    return map.values.map { cat ->
        if (cat.tienePortadaVisual()) {
            cat
        } else {
            val donor = desdeTabla.find { t ->
                normalizarClaveCategoriaNombre(t.nombre) == normalizarClaveCategoriaNombre(cat.nombre) &&
                    t.tienePortadaVisual()
            }
            if (donor != null) {
                cat.copy(
                    portadaUrlSupabase = cat.portadaUrlSupabase ?: donor.portadaUrlSupabase,
                    portadaRutaAbsoluta = cat.portadaRutaAbsoluta
                        ?: donor.portadaRutaAbsoluta?.takeIf { File(it).exists() },
                    remoteId = cat.remoteId ?: donor.remoteId,
                )
            } else {
                cat
            }
        }
    }.sortedWith(compareBy { it.nombre.lowercase(Locale.ROOT) })
}

/** Resuelve la categoría activa como en el selector (clave normalizada). */
fun categoriaPortadaParaFiltro(
    filtroNombre: String?,
    desdeTabla: List<Categoria>,
    desdeJugadores: List<String>,
): Categoria? {
    val filtro = filtroNombre?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val keyFiltro = normalizarClaveCategoriaNombre(filtro)
    if (keyFiltro.isEmpty()) return null
    return mergeCategoriasParaUi(desdeTabla, desdeJugadores)
        .find { normalizarClaveCategoriaNombre(it.nombre) == keyFiltro }
}
