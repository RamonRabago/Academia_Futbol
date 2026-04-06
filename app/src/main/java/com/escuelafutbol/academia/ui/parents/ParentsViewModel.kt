package com.escuelafutbol.academia.ui.parents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.escuelafutbol.academia.data.local.AcademiaDatabase
import com.escuelafutbol.academia.data.local.model.esPadreMembresiaNube
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.Asistencia
import com.escuelafutbol.academia.data.local.entity.Jugador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LineaAsistenciaPadreUi(val fechaDia: Long, val presente: Boolean)

data class HijoResumenUi(
    val nombre: String,
    val categoria: String,
    val ultimasAsistencias: List<LineaAsistenciaPadreUi>,
)

sealed class ParentsTabContent {
    data object StaffComunicaciones : ParentsTabContent()
    data object PadreSinHijos : ParentsTabContent()
    data class PadreConHijos(val hijos: List<HijoResumenUi>) : ParentsTabContent()
}

class ParentsViewModel(
    application: Application,
    private val database: AcademiaDatabase,
) : AndroidViewModel(application) {

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

    fun contenidoSegunMembresia(config: AcademiaConfig): Flow<ParentsTabContent> {
        if (!config.esPadreMembresiaNube()) {
            return flowOf(ParentsTabContent.StaffComunicaciones)
        }
        return combine(
            database.jugadorDao().observeAll(),
            database.asistenciaDao().observeAll(),
        ) { jugadores: List<Jugador>, asistencias: List<Asistencia> ->
            if (jugadores.isEmpty()) {
                ParentsTabContent.PadreSinHijos
            } else {
                val porJug = asistencias.groupBy { it.jugadorId }
                val hijos = jugadores.sortedBy { it.nombre }.map { j ->
                    val lineas = (porJug[j.id] ?: emptyList())
                        .sortedByDescending { it.fechaDia }
                        .take(8)
                        .map { a -> LineaAsistenciaPadreUi(a.fechaDia, a.presente) }
                    HijoResumenUi(j.nombre, j.categoria, lineas)
                }
                ParentsTabContent.PadreConHijos(hijos)
            }
        }
    }
}
