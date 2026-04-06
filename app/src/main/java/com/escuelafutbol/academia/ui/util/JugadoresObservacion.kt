package com.escuelafutbol.academia.ui.util

import com.escuelafutbol.academia.data.local.dao.JugadorDao
import com.escuelafutbol.academia.data.local.entity.Jugador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Misma lógica que [jugadoresActivosFlow], en un snapshot para operaciones puntuales (p. ej. marcar todos en asistencia).
 */
suspend fun jugadoresActivosSnapshot(
    jugadorDao: JugadorDao,
    filtroCategoria: String?,
    categoriasPermitidasOperacion: Set<String>?,
): List<Jugador> {
    val filtroNorm = filtroCategoria?.trim()?.takeIf { it.isNotEmpty() }
    val permitidasNorm = categoriasPermitidasOperacion
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
    return when {
        permitidasNorm == null ->
            if (filtroNorm == null) jugadorDao.getAll()
            else jugadorDao.getByCategoria(filtroNorm)
        permitidasNorm.isEmpty() -> emptyList()
        filtroNorm == null ->
            jugadorDao.getByCategorias(permitidasNorm.toList())
        filtroNorm !in permitidasNorm -> emptyList()
        else -> jugadorDao.getByCategoria(filtroNorm)
    }
}

/**
 * Flujo de jugadores activos según filtro de categoría en sesión y, si aplica, restricción de coach en nube.
 */
fun jugadoresActivosFlow(
    jugadorDao: JugadorDao,
    filtroCategoria: String?,
    categoriasPermitidasOperacion: Set<String>?,
): Flow<List<Jugador>> {
    val filtroNorm = filtroCategoria?.trim()?.takeIf { it.isNotEmpty() }
    val permitidasNorm = categoriasPermitidasOperacion
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
    return when {
        permitidasNorm == null ->
            if (filtroNorm == null) {
                jugadorDao.observeAll()
            } else {
                jugadorDao.observeByCategoria(filtroNorm)
            }
        permitidasNorm.isEmpty() -> flowOf(emptyList())
        filtroNorm == null ->
            jugadorDao.observeByCategorias(permitidasNorm.toList())
        filtroNorm !in permitidasNorm -> flowOf(emptyList())
        else -> jugadorDao.observeByCategoria(filtroNorm)
    }
}
