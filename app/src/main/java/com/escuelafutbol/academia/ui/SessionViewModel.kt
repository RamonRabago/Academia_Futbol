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

    fun actualizarRestriccionOperacionCoach(permitidas: Set<String>?) {
        _categoriasPermitidasOperacion.value = permitidas
    }

    /** true = menú principal e Inicio visibles al abrir la app; categoría por defecto “todas”. */
    private val _enMenuPrincipal = MutableStateFlow(true)
    val enMenuPrincipal: StateFlow<Boolean> = _enMenuPrincipal.asStateFlow()

    fun confirmarSeleccion(categoria: String?) {
        _filtroCategoria.value = categoria
        _enMenuPrincipal.value = true
    }

    fun volverASeleccionCategoria() {
        _enMenuPrincipal.value = false
    }
}
