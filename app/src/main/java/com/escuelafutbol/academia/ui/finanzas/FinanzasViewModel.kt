package com.escuelafutbol.academia.ui.finanzas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.data.local.dao.AcademiaConfigDao
import com.escuelafutbol.academia.data.local.dao.CobroMensualDao
import com.escuelafutbol.academia.data.local.dao.JugadorDao
import com.escuelafutbol.academia.data.local.dao.StaffDao
import com.escuelafutbol.academia.data.local.entity.CobroMensualAlumno
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.data.local.entity.Staff
import com.escuelafutbol.academia.data.remote.pushCobroMensualSiNube
import com.escuelafutbol.academia.ui.util.jugadoresActivosFlow
import com.escuelafutbol.academia.ui.util.jugadoresActivosSnapshot
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Alcance de la vista de finanzas: toda la academia o una sola categoría de alumnos. */
sealed class FinanzasAlcance {
    data object GeneralAcademia : FinanzasAlcance()
    data class SoloCategoria(val nombre: String) : FinanzasAlcance()
}

data class FinanzaLineaAlumno(
    val jugador: Jugador,
    val cobro: CobroMensualAlumno?,
)

data class FinanzaResumenCategoria(
    val categoria: String,
    val esperado: Double,
    val pagado: Double,
    val pendiente: Double,
    val conRegistro: Int,
)

private data class DatosMesYStaff(
    val jugadores: List<Jugador>,
    val cobros: List<CobroMensualAlumno>,
    val cobrosTodos: List<CobroMensualAlumno>,
    val staff: List<Staff>,
)

data class FinanzasUiState(
    val periodoYyyyMm: String,
    /** Etiqueta legible del mes (p. ej. «abril de 2026»). */
    val periodoTitulo: String,
    val alcance: FinanzasAlcance,
    val categoriasDisponibles: List<String>,
    val lineas: List<FinanzaLineaAlumno>,
    val totalEsperadoMes: Double,
    val totalPagadoMes: Double,
    val pendienteMes: Double,
    val adeudoHistorico: Double,
    val totalSueldosStaff: Double,
    val staffOrdenado: List<Staff>,
    val porCategoria: List<FinanzaResumenCategoria>,
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class FinanzasViewModel(
    application: Application,
    private val jugadorDao: JugadorDao,
    private val cobroMensualDao: CobroMensualDao,
    private val staffDao: StaffDao,
    private val academiaConfigDao: AcademiaConfigDao,
    private val categoriasPermitidasOperacion: StateFlow<Set<String>?>,
) : AndroidViewModel(application) {

    private val app: AcademiaApplication get() = getApplication()

    private val periodoFlow = MutableStateFlow(YearMonth.now())
    private val alcanceFlow = MutableStateFlow<FinanzasAlcance>(FinanzasAlcance.GeneralAcademia)

    /** Evita dos prellenados concurrentes (automático + botón). */
    private val prellenarMutex = Mutex()

    private val jugadoresFlow = combine(alcanceFlow, categoriasPermitidasOperacion) { alcance, permitidas ->
        val filtroCategoria = when (alcance) {
            FinanzasAlcance.GeneralAcademia -> null
            is FinanzasAlcance.SoloCategoria -> alcance.nombre
        }
        Pair(filtroCategoria, permitidas)
    }.flatMapLatest { (filtro, permitidas) ->
        jugadoresActivosFlow(jugadorDao, filtro, permitidas)
    }

    private val categoriasDisponiblesFlow = combine(
        categoriasPermitidasOperacion,
        jugadorDao.observeAll(),
    ) { permitidas, jugadores ->
        val cats = jugadores
            .map { it.categoria.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
        val p = permitidas
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toSet()
        if (p == null) cats else cats.filter { it in p }
    }

    private val cobrosDelMes = periodoFlow.flatMapLatest { ym ->
        cobroMensualDao.observeByPeriodo(ym.format(PERIODO_FMT))
    }

    private val datosMesYStaff = combine(
        jugadoresFlow,
        cobrosDelMes,
        cobroMensualDao.observeTodos(),
        staffDao.observeAll(),
    ) { jugadores, cobros, cobrosTodos, staff ->
        DatosMesYStaff(jugadores, cobros, cobrosTodos, staff)
    }

    private val periodoYAlcance = combine(
        periodoFlow,
        alcanceFlow,
        categoriasDisponiblesFlow,
    ) { periodo, alcance, categoriasDisponibles ->
        Triple(periodo, alcance, categoriasDisponibles)
    }

    val uiState = combine(datosMesYStaff, periodoYAlcance) { datos, pac ->
        val (periodo, alcance, categoriasDisponibles) = pac
        val jugadores = datos.jugadores
        val cobros = datos.cobros
        val cobrosTodos = datos.cobrosTodos
        val staff = datos.staff
        val periodoStr = periodo.format(PERIODO_FMT)
        val periodoTitulo = periodo.month.getDisplayName(TextStyle.FULL, Locale("es", "MX")) +
            " " + periodo.year
        val cobroByJug = cobros.associateBy { it.jugadorId }
        val sorted = jugadores.sortedWith(compareBy({ it.categoria }, { it.nombre }))
        val lineas = sorted.map { j -> FinanzaLineaAlumno(j, cobroByJug[j.id]) }

        var esp = 0.0
        var pag = 0.0
        for (ln in lineas) {
            val c = ln.cobro ?: continue
            esp += c.importeEsperado
            pag += c.importePagado
        }
        val pendienteMes = (esp - pag).coerceAtLeast(0.0)

        val idsVisibles = jugadores.map { it.id }.toSet()
        val adeudoHistorico = cobrosTodos
            .filter { it.jugadorId in idsVisibles }
            .sumOf { cobroRow ->
                (cobroRow.importeEsperado - cobroRow.importePagado).coerceAtLeast(0.0)
            }

        val sueldos = staff.mapNotNull { it.sueldoMensual }.filter { it > 0 }.sum()
        val staffOrd = staff.sortedWith(compareBy({ it.rol }, { it.nombre }))
        val porCat = lineas
            .filter { it.cobro != null }
            .groupBy { it.jugador.categoria }
            .map { (cat, list) ->
                val e = list.sumOf { line -> line.cobro!!.importeEsperado }
                val p = list.sumOf { line -> line.cobro!!.importePagado }
                FinanzaResumenCategoria(
                    categoria = cat,
                    esperado = e,
                    pagado = p,
                    pendiente = (e - p).coerceAtLeast(0.0),
                    conRegistro = list.size,
                )
            }
            .sortedBy { it.categoria }
        FinanzasUiState(
            periodoYyyyMm = periodoStr,
            periodoTitulo = periodoTitulo,
            alcance = alcance,
            categoriasDisponibles = categoriasDisponibles,
            lineas = lineas,
            totalEsperadoMes = esp,
            totalPagadoMes = pag,
            pendienteMes = pendienteMes,
            adeudoHistorico = adeudoHistorico,
            totalSueldosStaff = sueldos,
            staffOrdenado = staffOrd,
            porCategoria = porCat,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FinanzasUiState(
            periodoYyyyMm = YearMonth.now().format(PERIODO_FMT),
            periodoTitulo = "",
            alcance = FinanzasAlcance.GeneralAcademia,
            categoriasDisponibles = emptyList(),
            lineas = emptyList(),
            totalEsperadoMes = 0.0,
            totalPagadoMes = 0.0,
            pendienteMes = 0.0,
            adeudoHistorico = 0.0,
            totalSueldosStaff = 0.0,
            staffOrdenado = emptyList(),
            porCategoria = emptyList(),
        ),
    )

    init {
        viewModelScope.launch {
            combine(periodoFlow, alcanceFlow, jugadoresFlow) { p, a, js ->
                Triple(
                    p,
                    a,
                    js.map { j -> "${j.id},${j.becado},${j.mensualidad}" }.sorted().joinToString("|"),
                )
            }
                .distinctUntilChanged()
                .debounce(AUTO_PRELLENAR_DEBOUNCE_MS)
                .collectLatest {
                    rellenarHuecosCobrosDelMesSeleccionado()
                }
        }
    }

    fun setAlcanceGeneral() {
        alcanceFlow.value = FinanzasAlcance.GeneralAcademia
    }

    fun setAlcanceCategoria(nombre: String) {
        val n = nombre.trim()
        if (n.isNotEmpty()) alcanceFlow.value = FinanzasAlcance.SoloCategoria(n)
    }

    fun periodoAnterior() {
        periodoFlow.value = periodoFlow.value.minusMonths(1)
    }

    fun periodoSiguiente() {
        periodoFlow.value = periodoFlow.value.plusMonths(1)
    }

    fun prellenarMesConCuotasAlumnos() {
        viewModelScope.launch { rellenarHuecosCobrosDelMesSeleccionado() }
    }

    /**
     * Crea filas de cobro del mes en pantalla para alumnos activos con cuota en ficha que aún no tengan registro.
     * Misma regla que el botón manual; se invoca también al cambiar mes, alcance o datos relevantes de jugadores.
     */
    private suspend fun rellenarHuecosCobrosDelMesSeleccionado() {
        prellenarMutex.withLock {
            val periodoStr = periodoFlow.value.format(PERIODO_FMT)
            val filtro = when (val a = alcanceFlow.value) {
                FinanzasAlcance.GeneralAcademia -> null
                is FinanzasAlcance.SoloCategoria -> a.nombre
            }
            val jugadores = jugadoresActivosSnapshot(
                jugadorDao,
                filtro,
                categoriasPermitidasOperacion.value,
            )
            for (j in jugadores) {
                if (j.becado) continue
                val men = j.mensualidad ?: continue
                if (men <= 0) continue
                if (cobroMensualDao.getByJugadorYPeriodo(j.id, periodoStr) != null) continue
                val nuevo = CobroMensualAlumno(
                    jugadorId = j.id,
                    periodoYyyyMm = periodoStr,
                    importeEsperado = men,
                    importePagado = 0.0,
                    notas = null,
                    remoteId = null,
                    needsCloudPush = true,
                )
                cobroMensualDao.insert(nuevo)
                val guardado = cobroMensualDao.getByJugadorYPeriodo(j.id, periodoStr)!!
                pushCobroSiNube(guardado)
            }
        }
    }

    fun registrarCobroManual(jugadorId: Long, esperado: Double, pagado: Double, notas: String?) {
        viewModelScope.launch {
            val periodoStr = periodoFlow.value.format(PERIODO_FMT)
            val existente = cobroMensualDao.getByJugadorYPeriodo(jugadorId, periodoStr)
            if (existente != null) return@launch
            val esp = esperado.coerceAtLeast(0.0)
            val nuevo = CobroMensualAlumno(
                jugadorId = jugadorId,
                periodoYyyyMm = periodoStr,
                importeEsperado = esp,
                importePagado = pagado.coerceAtLeast(0.0).coerceAtMost(esp),
                notas = notas?.trim()?.takeIf { it.isNotEmpty() },
                remoteId = null,
                needsCloudPush = true,
            )
            cobroMensualDao.insert(nuevo)
            val guardado = cobroMensualDao.getByJugadorYPeriodo(jugadorId, periodoStr)!!
            pushCobroSiNube(guardado)
        }
    }

    fun actualizarCobro(cobro: CobroMensualAlumno, esperado: Double, pagado: Double, notas: String?) {
        viewModelScope.launch {
            val esp = esperado.coerceAtLeast(0.0)
            val pag = pagado.coerceAtLeast(0.0).coerceAtMost(esp)
            val actualizado = cobro.copy(
                importeEsperado = esp,
                importePagado = pag,
                notas = notas?.trim()?.takeIf { it.isNotEmpty() },
                needsCloudPush = true,
            )
            cobroMensualDao.update(actualizado)
            pushCobroSiNube(actualizado)
        }
    }

    private suspend fun pushCobroSiNube(c: CobroMensualAlumno) {
        pushCobroMensualSiNube(app, academiaConfigDao, jugadorDao, cobroMensualDao, c)
    }

    companion object {
        private val PERIODO_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
        private const val AUTO_PRELLENAR_DEBOUNCE_MS = 400L
    }
}
