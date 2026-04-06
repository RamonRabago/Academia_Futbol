package com.escuelafutbol.academia.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.data.local.dao.AsistenciaDao
import com.escuelafutbol.academia.data.local.dao.JugadorDao
import com.escuelafutbol.academia.data.local.entity.Asistencia
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.ui.util.DayMillis
import com.escuelafutbol.academia.ui.util.jugadoresActivosFlow
import com.escuelafutbol.academia.ui.util.jugadoresActivosSnapshot
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class JugadorAsistenciaUi(
    val jugador: Jugador,
    val presente: Boolean,
)

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModel(
    private val jugadorDao: JugadorDao,
    private val asistenciaDao: AsistenciaDao,
    private val filtroCategoria: StateFlow<String?>,
    private val categoriasPermitidasOperacion: StateFlow<Set<String>?>,
) : ViewModel() {

    val fechaDia = MutableStateFlow(DayMillis.today())

    private val jugadoresFiltrados = combine(
        filtroCategoria,
        categoriasPermitidasOperacion,
    ) { cat, permitidas -> Pair(cat, permitidas) }
        .flatMapLatest { (cat, permitidas) ->
            jugadoresActivosFlow(jugadorDao, cat, permitidas)
        }

    private val asistenciasDelDia = fechaDia.flatMapLatest { día ->
        asistenciaDao.observeForDay(día)
    }

    val filas = combine(jugadoresFiltrados, asistenciasDelDia) { jugadores, registros ->
        val porJugador = registros.associateBy { it.jugadorId }
        jugadores.map { j ->
            JugadorAsistenciaUi(
                jugador = j,
                presente = porJugador[j.id]?.presente ?: false,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun cambiarDía(nuevoEpochDia: Long) {
        fechaDia.value = nuevoEpochDia
    }

    fun diaAnterior(zone: ZoneId = ZoneId.systemDefault()) {
        fechaDia.value = shiftDias(fechaDia.value, -1, zone)
    }

    fun diaSiguiente(zone: ZoneId = ZoneId.systemDefault()) {
        fechaDia.value = shiftDias(fechaDia.value, 1, zone)
    }

    private fun shiftDias(inicioDiaMillis: Long, delta: Long, zone: ZoneId): Long {
        val d = Instant.ofEpochMilli(inicioDiaMillis).atZone(zone).toLocalDate().plusDays(delta)
        return DayMillis.fromLocalDate(d, zone)
    }

    fun marcarAsistencia(jugadorId: Long, presente: Boolean) {
        val día = fechaDia.value
        viewModelScope.launch {
            val prev = asistenciaDao.getPorJugadorYDia(jugadorId, día)
            val next = if (prev != null) {
                prev.copy(presente = presente, needsCloudPush = true)
            } else {
                Asistencia(
                    jugadorId = jugadorId,
                    fechaDia = día,
                    presente = presente,
                    needsCloudPush = true,
                )
            }
            asistenciaDao.upsert(next)
        }
    }

    fun marcarTodosPresentes() {
        val día = fechaDia.value
        viewModelScope.launch {
            val lista = jugadoresActivosSnapshot(
                jugadorDao,
                filtroCategoria.value,
                categoriasPermitidasOperacion.value,
            )
            for (j in lista) {
                val prev = asistenciaDao.getPorJugadorYDia(j.id, día)
                val next = if (prev != null) {
                    prev.copy(presente = true, needsCloudPush = true)
                } else {
                    Asistencia(
                        jugadorId = j.id,
                        fechaDia = día,
                        presente = true,
                        needsCloudPush = true,
                    )
                }
                asistenciaDao.upsert(next)
            }
        }
    }
}
