package com.escuelafutbol.academia.ui.parents

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ParentsViewModel : ViewModel() {

    private val _mensaje = MutableStateFlow(
        "Estimadas familias:\n\n" +
            "Recordatorio de entrenamiento este fin de semana. " +
            "Por favor, confirmar asistencia con el cuerpo técnico.\n\n" +
            "— Academia",
    )
    val mensaje: StateFlow<String> = _mensaje.asStateFlow()

    fun actualizarMensaje(texto: String) {
        _mensaje.update { texto }
    }
}
