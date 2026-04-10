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
import com.escuelafutbol.academia.data.remote.CobroMensualRemoteRepository
import com.escuelafutbol.academia.ui.util.jugadoresActivosFlow
import com.escuelafutbol.academia.ui.util.jugadoresActivosSnapshot
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

data class FinanzasUiState(
    val periodoYyyyMm: String,
    /** Etiqueta legible del mes (p. ej. «abril de 2026»). */
    val periodoTitulo: String,
    val lineas: List<FinanzaLineaAlumno>,
    val totalEsperadoMes: Double,
    val totalPagadoMes: Double,
    val pendienteMes: Double,
    val adeudoHistorico: Double,
    val totalSueldosStaff: Double,
    val staffOrdenado: List<Staff>,
    val porCategoria: List<FinanzaResumenCategoria>,
)

@OptIn(ExperimentalCoroutinesApi::class)
class FinanzasViewModel(
    application: Application,
    private val jugadorDao: JugadorDao,
    private val cobroMensualDao: CobroMensualDao,
    private val staffDao: StaffDao,
    private val academiaConfigDao: AcademiaConfigDao,
    private val filtroCategoria: StateFlow<String?>,
    private val categoriasPermitidasOperacion: StateFlow<Set<String>?>,
) : AndroidViewModel(application) {

    private val app: AcademiaApplication get() = getApplication()

    private val periodoFlow = MutableStateFlow(YearMonth.now())

    private val jugadoresFlow = combine(filtroCategoria, categoriasPermitidasOperacion) { c, p ->
        Pair(c, p)
    }.flatMapLatest { (cat, permitidas) ->
        jugadoresActivosFlow(jugadorDao, cat, permitidas)
    }

    private val cobrosDelMes = periodoFlow.flatMapLatest { ym ->
        cobroMensualDao.observeByPeriodo(ym.format(PERIODO_FMT))
    }

    val uiState = combine(
        jugadoresFlow,
        cobrosDelMes,
        cobroMensualDao.observeAdeudoHistorico(),
        staffDao.observeAll(),
        periodoFlow,
    ) { jugadores, cobros, adeudoHist, staff, periodo ->
        val periodoStr = periodo.format(PERIODO_FMT)
        val periodoTitulo = periodo.month.getDisplayName(TextStyle.FULL, Locale("es", "MX")) +
            " " + periodo.year
        val cobroByJug = cobros.associateBy { it.jugadorId }
        val sorted = jugadores.sortedWith(compareBy({ it.categoria }, { it.nombre }))
        val lineas = sorted.map { j -> FinanzaLineaAlumno(j, cobroByJug[j.id]) }
        var esp = 0.0
        var pag = 0.0
        for (c in cobros) {
            esp += c.importeEsperado
            pag += c.importePagado
        }
        val pendienteMes = (esp - pag).coerceAtLeast(0.0)
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
            lineas = lineas,
            totalEsperadoMes = esp,
            totalPagadoMes = pag,
            pendienteMes = pendienteMes,
            adeudoHistorico = adeudoHist,
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

    fun periodoAnterior() {
        periodoFlow.value = periodoFlow.value.minusMonths(1)
    }

    fun periodoSiguiente() {
        periodoFlow.value = periodoFlow.value.plusMonths(1)
    }

    fun prellenarMesConCuotasAlumnos() {
        viewModelScope.launch {
            val periodoStr = periodoFlow.value.format(PERIODO_FMT)
            val jugadores = jugadoresActivosSnapshot(
                jugadorDao,
                filtroCategoria.value,
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
        val client = app.supabaseClient ?: return
        val cfg = academiaConfigDao.getActual() ?: return
        val academiaId = cfg.remoteAcademiaId ?: return
        val j = jugadorDao.getById(c.jugadorId) ?: return
        val jr = j.remoteId ?: return
        val repo = CobroMensualRemoteRepository(client)
        runCatching {
            if (c.remoteId == null) {
                val rid = repo.insertar(academiaId, jr, c)
                val local = cobroMensualDao.getByJugadorYPeriodo(c.jugadorId, c.periodoYyyyMm) ?: return
                cobroMensualDao.update(local.copy(remoteId = rid, needsCloudPush = false))
            } else {
                repo.actualizar(c.remoteId, c.copy(needsCloudPush = false))
                cobroMensualDao.update(c.copy(needsCloudPush = false))
            }
        }
    }

    companion object {
        private val PERIODO_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    }
}
