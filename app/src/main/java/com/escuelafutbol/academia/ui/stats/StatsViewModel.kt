package com.escuelafutbol.academia.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.data.local.dao.AsistenciaDao
import com.escuelafutbol.academia.data.local.dao.JugadorDao
import com.escuelafutbol.academia.data.local.entity.Jugador
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class LineaCuotaAlumno(
    val jugadorId: Long,
    val nombre: String,
    val categoria: String,
    val becado: Boolean,
    val mensualidad: Double?,
)

data class CuotaPorCategoria(
    val categoria: String,
    val alumnosConCuota: Int,
    val becados: Int,
    val sinCuotaDefinida: Int,
    val totalMensual: Double,
)

data class CuotasResumenUi(
    val lineas: List<LineaCuotaAlumno>,
    val porCategoria: List<CuotaPorCategoria>,
    /** Suma de mensualidades de quienes pagan (no becados, importe mayor que 0). */
    val totalMensual: Double,
    val nBecados: Int,
    val nConCuota: Int,
    val nSinCuota: Int,
)

data class StatsUi(
    val totalJugadores: Int,
    val porcentajeAsistenciaGlobal: Double?,
    val diasConRegistro: Int,
    val cuotasResumen: CuotasResumenUi,
)

private fun construirCuotasResumen(jugadores: List<Jugador>): CuotasResumenUi {
    val sorted = jugadores.sortedWith(compareBy({ it.categoria }, { it.nombre }))
    val lineas = sorted.map { j ->
        LineaCuotaAlumno(
            jugadorId = j.id,
            nombre = j.nombre,
            categoria = j.categoria,
            becado = j.becado,
            mensualidad = j.mensualidad,
        )
    }
    val porCategoria = sorted.groupBy { it.categoria }.map { (cat, list) ->
        val bec = list.count { it.becado }
        val con = list.count { !it.becado && it.mensualidad != null && it.mensualidad > 0 }
        val sin = list.count {
            !it.becado && (it.mensualidad == null || it.mensualidad == 0.0)
        }
        val total = list
            .filter { !it.becado && it.mensualidad != null && it.mensualidad > 0 }
            .sumOf { it.mensualidad!! }
        CuotaPorCategoria(cat, con, bec, sin, total)
    }.sortedBy { it.categoria }
    val totalM = sorted
        .filter { !it.becado && it.mensualidad != null && it.mensualidad > 0 }
        .sumOf { it.mensualidad!! }
    val nBec = sorted.count { it.becado }
    val nCon = sorted.count { !it.becado && it.mensualidad != null && it.mensualidad > 0 }
    val nSin = sorted.count { !it.becado && (it.mensualidad == null || it.mensualidad == 0.0) }
    return CuotasResumenUi(lineas, porCategoria, totalM, nBec, nCon, nSin)
}

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    private val jugadorDao: JugadorDao,
    private val asistenciaDao: AsistenciaDao,
    private val filtroCategoria: StateFlow<String?>,
) : ViewModel() {

    private val jugadoresFiltrados = filtroCategoria.flatMapLatest { cat ->
        if (cat == null) jugadorDao.observeAll() else jugadorDao.observeByCategoria(cat)
    }

    val stats = combine(
        jugadoresFiltrados,
        asistenciaDao.observeAll(),
    ) { jugadores, todasAsistencias ->
        val ids = jugadores.map { it.id }.toSet()
        val asistencias = todasAsistencias.filter { it.jugadorId in ids }
        val totalFilas = asistencias.size
        val presentes = asistencias.count { it.presente }
        val porcentaje = if (totalFilas == 0) null else presentes * 100.0 / totalFilas
        val dias = asistencias.map { it.fechaDia }.distinct().size
        StatsUi(
            totalJugadores = jugadores.size,
            porcentajeAsistenciaGlobal = porcentaje,
            diasConRegistro = dias,
            cuotasResumen = construirCuotasResumen(jugadores),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        StatsUi(
            totalJugadores = 0,
            porcentajeAsistenciaGlobal = null,
            diasConRegistro = 0,
            cuotasResumen = construirCuotasResumen(emptyList()),
        ),
    )
}
