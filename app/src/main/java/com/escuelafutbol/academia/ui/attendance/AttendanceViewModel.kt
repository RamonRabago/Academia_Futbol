package com.escuelafutbol.academia.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.data.local.dao.AsistenciaDao
import com.escuelafutbol.academia.data.local.dao.DiaEntrenamientoDao
import com.escuelafutbol.academia.data.local.dao.JugadorDao
import com.escuelafutbol.academia.data.local.entity.Asistencia
import com.escuelafutbol.academia.data.local.entity.DiaEntrenamiento
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.ui.util.DayMillis
import com.escuelafutbol.academia.ui.util.jugadoresActivosFlow
import com.escuelafutbol.academia.ui.util.jugadoresActivosSnapshot
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
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
    private val diaEntrenoDao: DiaEntrenamientoDao,
    private val filtroCategoria: StateFlow<String?>,
    private val categoriasPermitidasOperacion: StateFlow<Set<String>?>,
) : ViewModel() {

    val fechaDia = MutableStateFlow(DayMillis.today())

    private val zonaResumen = ZoneId.systemDefault()

    private val periodoResumenFlow = MutableStateFlow(AsistenciaPeriodoResumen.MesVista)
    private val anioResumenFlow = MutableStateFlow(
        YearMonth.from(LocalDate.now(zonaResumen)).year,
    )

    /** null = resumen de todo el equipo en el filtro; id = solo ese jugador. */
    private val _focoResumenJugadorId = MutableStateFlow<Long?>(null)
    val focoResumenJugadorId: StateFlow<Long?> get() = _focoResumenJugadorId

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

    private val rangoResumenParams = combine(
        periodoResumenFlow,
        fechaDia,
        anioResumenFlow,
    ) { modo, diaMs, anio ->
        Triple(modo, diaMs, anio)
    }

    private val asistenciasRangoResumen = rangoResumenParams.flatMapLatest { (modo, diaMs, anio) ->
        val (desde, hasta) = when (modo) {
            AsistenciaPeriodoResumen.MesVista -> rangoMesDelDia(diaMs, zonaResumen)
            AsistenciaPeriodoResumen.AnioCompleto -> rangoAnio(anio, zonaResumen)
        }
        asistenciaDao.observeBetween(desde, hasta)
    }

    private val diasEntrenoRangoResumen = rangoResumenParams.flatMapLatest { (modo, diaMs, anio) ->
        val (desde, hasta) = when (modo) {
            AsistenciaPeriodoResumen.MesVista -> rangoMesDelDia(diaMs, zonaResumen)
            AsistenciaPeriodoResumen.AnioCompleto -> rangoAnio(anio, zonaResumen)
        }
        diaEntrenoDao.observeBetween(desde, hasta)
    }

    private data class ResumenBloque(
        val asistencias: List<Asistencia>,
        val diasEntreno: List<DiaEntrenamiento>,
        val jugadores: List<Jugador>,
        val modo: AsistenciaPeriodoResumen,
        val diaMs: Long,
        val anio: Int,
    )

    private data class ResumenBaseParams(
        val asistencias: List<Asistencia>,
        val diasEntreno: List<DiaEntrenamiento>,
        val jugadores: List<Jugador>,
        val modo: AsistenciaPeriodoResumen,
        val diaMs: Long,
        val anio: Int,
        val scopeKey: String,
    )

    private val resumenBloque = combine(
        combine(
            asistenciasRangoResumen,
            diasEntrenoRangoResumen,
            jugadoresFiltrados,
        ) { asistencias, diasEn, jugadores ->
            Triple(asistencias, diasEn, jugadores)
        },
        combine(
            periodoResumenFlow,
            fechaDia,
            anioResumenFlow,
        ) { modo, diaMs, anio ->
            Triple(modo, diaMs, anio)
        },
    ) { adJ, mda ->
        ResumenBloque(
            asistencias = adJ.first,
            diasEntreno = adJ.second,
            jugadores = adJ.third,
            modo = mda.first,
            diaMs = mda.second,
            anio = mda.third,
        )
    }

    private val resumenBaseParams = combine(
        resumenBloque,
        filtroCategoria,
    ) { bloque, cat ->
        ResumenBaseParams(
            asistencias = bloque.asistencias,
            diasEntreno = bloque.diasEntreno,
            jugadores = bloque.jugadores,
            modo = bloque.modo,
            diaMs = bloque.diaMs,
            anio = bloque.anio,
            scopeKey = scopeKeyAsistencia(cat),
        )
    }

    private val marcasDiaActualLista = fechaDia.flatMapLatest { dia ->
        diaEntrenoDao.observeForDay(dia)
    }

    val esDiaEntrenamientoMarcado = combine(
        marcasDiaActualLista,
        filtroCategoria,
    ) { list, cat ->
        val scope = scopeKeyAsistencia(cat)
        list.any { d ->
            when {
                scope.isEmpty() -> d.scopeKey.isEmpty()
                else -> d.scopeKey == scope || d.scopeKey.isEmpty()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val resumenAsistencia = combine(
        resumenBaseParams,
        _focoResumenJugadorId,
    ) { base, focoId ->
        val ids = base.jugadores.map { it.id }.toSet()
        val focoValido = focoId?.takeIf { it in ids }
        val filtradasCrudas = when (focoValido) {
            null -> base.asistencias.filter { it.jugadorId in ids }
            else -> base.asistencias.filter { it.jugadorId == focoValido }
        }
        val filtradas = filtradasCrudas.filter {
            diaMarcadoComoEntrenamiento(it.fechaDia, base.scopeKey, base.diasEntreno)
        }
        val hayMarcasFueraDeDiasEntreno =
            filtradasCrudas.isNotEmpty() && filtradas.isEmpty()
        val jugadoresIds = when (focoValido) {
            null -> ids
            else -> setOf(focoValido)
        }
        val cupos = contarPresentesAusentesEntrenamientoImplicitos(
            jugadoresIds,
            base.diasEntreno,
            filtradasCrudas,
            base.scopeKey,
        )
        val total = cupos.totalCupos
        val pres = cupos.presentes
        val aus = cupos.ausentes
        val pct = if (total == 0) null else (100f * pres / total)
        val etiqueta = when (base.modo) {
            AsistenciaPeriodoResumen.MesVista -> {
                val ld = Instant.ofEpochMilli(base.diaMs).atZone(zonaResumen).toLocalDate()
                val ym = YearMonth.from(ld)
                ym.month.getDisplayName(TextStyle.FULL, Locale("es", "MX")) + " " + ym.year
            }
            AsistenciaPeriodoResumen.AnioCompleto -> base.anio.toString()
        }
        val diasDistintos = filtradas.map { it.fechaDia }.distinct().size
        val nombreFoco = focoValido?.let { id -> base.jugadores.find { it.id == id }?.nombre }
        AsistenciaResumenUi(
            periodo = base.modo,
            etiqueta = etiqueta,
            nombreAlumnoFoco = nombreFoco,
            porcentaje = pct,
            presentes = pres,
            ausentes = aus,
            totalRegistros = total,
            diasConRegistro = diasDistintos,
            hayMarcasFueraDeDiasEntreno = hayMarcasFueraDeDiasEntreno,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AsistenciaResumenUi(
            periodo = AsistenciaPeriodoResumen.MesVista,
            etiqueta = "",
            nombreAlumnoFoco = null,
            porcentaje = null,
            presentes = 0,
            ausentes = 0,
            totalRegistros = 0,
            diasConRegistro = 0,
            hayMarcasFueraDeDiasEntreno = false,
        ),
    )

    fun setDiaEntrenamientoMarcado(marcado: Boolean) {
        val día = fechaDia.value
        val scope = scopeKeyAsistencia(filtroCategoria.value)
        viewModelScope.launch {
            if (marcado) {
                diaEntrenoDao.upsert(DiaEntrenamiento(fechaDia = día, scopeKey = scope))
            } else {
                // Quitar todas las marcas del día (p. ej. "" de la migración + una categoría).
                diaEntrenoDao.deleteAllForDay(día)
            }
        }
    }

    fun setResumenFocoJugador(jugadorId: Long?) {
        _focoResumenJugadorId.value = jugadorId
    }

    fun setPeriodoResumen(periodo: AsistenciaPeriodoResumen) {
        if (periodo == AsistenciaPeriodoResumen.AnioCompleto &&
            periodoResumenFlow.value != AsistenciaPeriodoResumen.AnioCompleto
        ) {
            val y = Instant.ofEpochMilli(fechaDia.value).atZone(zonaResumen).year
            anioResumenFlow.value = y
        }
        periodoResumenFlow.value = periodo
    }

    fun resumenAnioAnterior() {
        anioResumenFlow.value = (anioResumenFlow.value - 1).coerceAtLeast(1970)
    }

    fun resumenAnioSiguiente() {
        anioResumenFlow.value = anioResumenFlow.value + 1
    }

    fun cambiarDía(nuevoEpochDia: Long) {
        fechaDia.value = nuevoEpochDia
    }

    fun diaAnterior(zone: ZoneId = ZoneId.systemDefault()) {
        fechaDia.value = shiftDias(fechaDia.value, -1, zone)
    }

    fun diaSiguiente(zone: ZoneId = ZoneId.systemDefault()) {
        fechaDia.value = shiftDias(fechaDia.value, 1, zone)
    }

    /** Ajusta el día de trabajo al inicio de día local del instante elegido en el calendario. */
    fun seleccionarFechaCalendario(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()) {
        fechaDia.value = DayMillis.startOfDay(epochMillis, zone)
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

    private companion object {
        fun rangoMesDelDia(diaMillis: Long, zone: ZoneId): Pair<Long, Long> {
            val ld = Instant.ofEpochMilli(diaMillis).atZone(zone).toLocalDate()
            val ym = YearMonth.from(ld)
            val inicio = ym.atDay(1)
            val fin = ym.atEndOfMonth()
            return DayMillis.fromLocalDate(inicio, zone) to DayMillis.fromLocalDate(fin, zone)
        }

        fun rangoAnio(anio: Int, zone: ZoneId): Pair<Long, Long> {
            val inicio = LocalDate.of(anio, 1, 1)
            val fin = LocalDate.of(anio, 12, 31)
            return DayMillis.fromLocalDate(inicio, zone) to DayMillis.fromLocalDate(fin, zone)
        }
    }
}
