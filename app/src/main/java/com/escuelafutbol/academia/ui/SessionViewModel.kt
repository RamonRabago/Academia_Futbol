package com.escuelafutbol.academia.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionViewModel : ViewModel() {

    private val _filtroCategoria = MutableStateFlow<String?>(null)
    val filtroCategoria: StateFlow<String?> = _filtroCategoria.asStateFlow()

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
