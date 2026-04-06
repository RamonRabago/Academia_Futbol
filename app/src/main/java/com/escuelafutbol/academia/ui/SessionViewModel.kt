package com.escuelafutbol.academia.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionViewModel : ViewModel() {

    private val _filtroCategoria = MutableStateFlow<String?>(null)
    val filtroCategoria: StateFlow<String?> = _filtroCategoria.asStateFlow()

    /**
     * Si no es null: el usuario es coach en nube y solo puede operar en estas categorías (por nombre).
     * `null` = sin restricción por membresía coach.
     */
    private val _categoriasPermitidasOperacion = MutableStateFlow<Set<String>?>(null)
    val categoriasPermitidasOperacion: StateFlow<Set<String>?> =
        _categoriasPermitidasOperacion.asStateFlow()

    /**
     * true mientras hay academia en nube pero aún no llegó el rol a Room (evita mostrar todas las categorías un instante).
     */
    private val _esperandoMembresiaNubeParaSelector = MutableStateFlow(false)
    val esperandoMembresiaNubeParaSelector: StateFlow<Boolean> =
        _esperandoMembresiaNubeParaSelector.asStateFlow()

    fun actualizarRestriccionOperacionCoach(permitidas: Set<String>?, esperandoMembresiaNube: Boolean) {
        _esperandoMembresiaNubeParaSelector.value = esperandoMembresiaNube
        _categoriasPermitidasOperacion.value = permitidas
    }

    /** true = menú principal e Inicio visibles al abrir la app; categoría por defecto “todas”. */
    private val _enMenuPrincipal = MutableStateFlow(true)
    val enMenuPrincipal: StateFlow<Boolean> = _enMenuPrincipal.asStateFlow()

    /**
     * Evita abrir el selector de categoría (p. ej. tras volver del gestor de archivos del alta de jugador,
     * donde un toque residual puede activar «Cambiar categoría» en la barra superior).
     */
    private val _impideVolverASeleccionCategoria = MutableStateFlow(false)
    val impideVolverASeleccionCategoria: StateFlow<Boolean> =
        _impideVolverASeleccionCategoria.asStateFlow()

    fun setImpideVolverASeleccionCategoria(impide: Boolean) {
        _impideVolverASeleccionCategoria.value = impide
    }

    fun confirmarSeleccion(categoria: String?) {
        _filtroCategoria.value = categoria?.trim()?.takeIf { it.isNotEmpty() }
        _enMenuPrincipal.value = true
    }

    fun volverASeleccionCategoria() {
        if (_impideVolverASeleccionCategoria.value) return
        _enMenuPrincipal.value = false
    }

    /** Cierra el selector de categoría y vuelve al menú sin cambiar la categoría activa (atrás del sistema / pestañas). */
    fun cerrarSelectorCategoria() {
        _enMenuPrincipal.value = true
    }
}
