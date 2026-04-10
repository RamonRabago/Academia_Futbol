package com.escuelafutbol.academia.ui.parents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.escuelafutbol.academia.data.local.AcademiaDatabase
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.Asistencia
import com.escuelafutbol.academia.data.local.entity.CobroMensualAlumno
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.data.local.model.esPadreMembresiaNube
import com.escuelafutbol.academia.ui.util.PagoPlazoUtil
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

data class LineaAsistenciaPadreUi(val fechaDia: Long, val presente: Boolean)

data class MesAdeudoPadreUi(val etiquetaMes: String, val saldoPendiente: Double)

data class HijoResumenUi(
    val nombre: String,
    val categoria: String,
    val ultimasAsistencias: List<LineaAsistenciaPadreUi>,
    val mesesVencidos: List<MesAdeudoPadreUi> = emptyList(),
    val totalAdeudoHijo: Double = 0.0,
)

sealed class ParentsTabContent {
    data object StaffComunicaciones : ParentsTabContent()
    data object PadreSinHijos : ParentsTabContent()
    data class PadreConHijos(
        val hijos: List<HijoResumenUi>,
        val totalAdeudoVencido: Double,
        val reglaLimitePagoActiva: Boolean,
    ) : ParentsTabContent()
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

    fun contenidoSegunMembresia(): Flow<ParentsTabContent> {
        return combine(
            database.academiaConfigDao().observe(),
            database.jugadorDao().observeAll(),
            database.asistenciaDao().observeAll(),
            database.cobroMensualDao().observeTodos(),
        ) { cfgNullable: AcademiaConfig?, jugadores: List<Jugador>, asistencias: List<Asistencia>, cobros: List<CobroMensualAlumno> ->
            val cfg = cfgNullable ?: AcademiaConfig.DEFAULT
            if (!cfg.esPadreMembresiaNube()) {
                ParentsTabContent.StaffComunicaciones
            } else if (jugadores.isEmpty()) {
                ParentsTabContent.PadreSinHijos
            } else {
                construirPadreConHijos(cfg, jugadores, asistencias, cobros)
            }
        }
    }

    private fun construirPadreConHijos(
        cfg: AcademiaConfig,
        jugadores: List<Jugador>,
        asistencias: List<Asistencia>,
        cobros: List<CobroMensualAlumno>,
    ): ParentsTabContent.PadreConHijos {
        val hoy = LocalDate.now()
        val diaLimite = cfg.diaLimitePagoMes
        val reglaActiva = diaLimite != null && diaLimite in 1..28
        val jugIds = jugadores.map { it.id }.toSet()
        val cobrosPorJugador = cobros.filter { it.jugadorId in jugIds }.groupBy { it.jugadorId }
        val porJug = asistencias.groupBy { it.jugadorId }
        val hijos = jugadores.sortedBy { it.nombre }.map { j ->
            val lineas = (porJug[j.id] ?: emptyList())
                .sortedByDescending { it.fechaDia }
                .take(8)
                .map { a -> LineaAsistenciaPadreUi(a.fechaDia, a.presente) }
            val mesesVencidos = if (!reglaActiva) {
                emptyList()
            } else {
                (cobrosPorJugador[j.id] ?: emptyList())
                    .filter { PagoPlazoUtil.cobroVencidoConSaldo(it, diaLimite, hoy) }
                    .sortedByDescending { it.periodoYyyyMm }
                    .map { c ->
                        MesAdeudoPadreUi(
                            PagoPlazoUtil.etiquetaMesPeriodo(c.periodoYyyyMm),
                            c.importeEsperado - c.importePagado,
                        )
                    }
            }
            val totalHijo = mesesVencidos.sumOf { it.saldoPendiente }
            HijoResumenUi(j.nombre, j.categoria, lineas, mesesVencidos, totalHijo)
        }
        val totalGlobal = hijos.sumOf { it.totalAdeudoHijo }
        return ParentsTabContent.PadreConHijos(hijos, totalGlobal, reglaActiva)
    }
}
