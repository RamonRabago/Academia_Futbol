package com.escuelafutbol.academia.ui.util

import com.escuelafutbol.academia.data.local.dao.JugadorDao
import com.escuelafutbol.academia.data.local.entity.Jugador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Flujo de jugadores activos según filtro de categoría en sesión y, si aplica, restricción de coach en nube.
 */
fun jugadoresActivosFlow(
    jugadorDao: JugadorDao,
    filtroCategoria: String?,
    categoriasPermitidasOperacion: Set<String>?,
): Flow<List<Jugador>> =
    when {
        categoriasPermitidasOperacion == null ->
            if (filtroCategoria == null) {
                jugadorDao.observeAll()
            } else {
                jugadorDao.observeByCategoria(filtroCategoria)
            }
        categoriasPermitidasOperacion.isEmpty() -> flowOf(emptyList())
        filtroCategoria == null ->
            jugadorDao.observeByCategorias(categoriasPermitidasOperacion.toList())
        filtroCategoria !in categoriasPermitidasOperacion -> flowOf(emptyList())
        else -> jugadorDao.observeByCategoria(filtroCategoria)
    }
