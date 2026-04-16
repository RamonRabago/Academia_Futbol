package com.escuelafutbol.academia.ui.parents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.data.local.AcademiaDatabase
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.Asistencia
import com.escuelafutbol.academia.data.local.entity.CobroMensualAlumno
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.data.local.model.esPadreMembresiaNube
import com.escuelafutbol.academia.data.remote.AcademiaMensajesCategoriaRepository
import com.escuelafutbol.academia.data.remote.PadresAlumnosRepository
import com.escuelafutbol.academia.data.remote.dto.AcademiaMensajeCategoriaRow
import com.escuelafutbol.academia.data.sync.AcademiaCloudSync
import com.escuelafutbol.academia.ui.util.PagoPlazoUtil
import io.github.jan.supabase.auth.auth
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

data class ParentsMensajesUiState(
    val cargando: Boolean = false,
    val items: List<MensajeCategoriaUi> = emptyList(),
    val error: String? = null,
)

data class MensajeCategoriaUi(
    val id: String,
    val categoriaNombre: String,
    val tipo: String,
    val titulo: String,
    val cuerpo: String,
    val createdAtMillis: Long,
    val eventAtMillis: Long?,
)

/** Alumno candidato a auto-vínculo (correo tutor = correo de la sesión; RLS en Supabase). */
data class ParentVinculoCandidatoUi(
    val remoteId: String,
    val nombre: String,
    val categoria: String,
)

object MensajeCategoriaTipo {
    const val PARTIDO_EVENTO = "partido_evento"
    const val CONVIVIO_LOGISTICA = "convivio_logistica"
    const val ADMINISTRATIVO = "administrativo"
    const val OTRO = "otro"

    val todosWire: List<String> = listOf(
        PARTIDO_EVENTO,
        CONVIVIO_LOGISTICA,
        ADMINISTRATIVO,
        OTRO,
    )
}

class ParentsViewModel(
    application: Application,
    private val database: AcademiaDatabase,
    private val categoriasPermitidasOperacion: StateFlow<Set<String>?>,
) : AndroidViewModel(application) {

    private val app: AcademiaApplication get() = getApplication()

    private val _mensajesNube = MutableStateFlow(ParentsMensajesUiState())
    val mensajesNube: StateFlow<ParentsMensajesUiState> = _mensajesNube.asStateFlow()

    private val _enviandoMensaje = MutableStateFlow(false)
    val enviandoMensaje: StateFlow<Boolean> = _enviandoMensaje.asStateFlow()

    private val _candidatosVinculo = MutableStateFlow<List<ParentVinculoCandidatoUi>>(emptyList())
    val candidatosVinculo: StateFlow<List<ParentVinculoCandidatoUi>> = _candidatosVinculo.asStateFlow()

    private val _candidatosVinculoCargando = MutableStateFlow(false)
    val candidatosVinculoCargando: StateFlow<Boolean> = _candidatosVinculoCargando.asStateFlow()

    private val _candidatosVinculoError = MutableStateFlow<String?>(null)
    val candidatosVinculoError: StateFlow<String?> = _candidatosVinculoError.asStateFlow()

    val categoriasParaMensajesStaff: StateFlow<List<String>> = combine(
        categoriasPermitidasOperacion,
        database.categoriaDao().observeAllOrdered(),
    ) { permitidas, cats ->
        val nombres = cats.map { it.nombre }.sorted()
        val p = permitidas?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet()
        when {
            p == null -> nombres
            p.isEmpty() -> emptyList()
            else -> nombres.filter { it.trim() in p }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    fun refrescarMensajes() {
        viewModelScope.launch { refrescarMensajesSuspend() }
    }

    /** Carga desde la nube alumnos que el padre puede vincular (mismo criterio que RLS: email tutor). */
    fun cargarCandidatosVinculo() {
        viewModelScope.launch {
            _candidatosVinculoCargando.value = true
            _candidatosVinculoError.value = null
            runCatching {
                val client = app.supabaseClient ?: error("Sin nube")
                val cfg = database.academiaConfigDao().getActual() ?: error("Sin configuración")
                val aid = cfg.remoteAcademiaId?.takeIf { it.isNotBlank() } ?: error("Sin academia en la nube")
                val uid = client.auth.currentUserOrNull()?.id?.toString() ?: error("Sin sesión")
                val p = PadresAlumnosRepository(client)
                val vinc = withContext(Dispatchers.IO) { p.listVinculos(aid, uid) }.map { it.jugadorId }.toSet()
                val rows = withContext(Dispatchers.IO) { p.listJugadoresAcademia(aid) }
                rows
                    .filter { it.id !in vinc }
                    .map { ParentVinculoCandidatoUi(it.id, it.nombre, it.categoria) }
                    .sortedWith(compareBy({ it.categoria }, { it.nombre }))
            }.onSuccess { _candidatosVinculo.value = it }
                .onFailure { e -> _candidatosVinculoError.value = e.message ?: e.toString() }
            _candidatosVinculoCargando.value = false
        }
    }

    fun vincularMiHijo(remotoId: String, onDone: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val client = app.supabaseClient
            val cfg = database.academiaConfigDao().getActual()
            val aid = cfg?.remoteAcademiaId?.takeIf { it.isNotBlank() }
            val uid = client?.auth?.currentUserOrNull()?.id?.toString()
            if (client == null || aid == null || uid.isNullOrBlank()) {
                onDone(Result.failure(IllegalStateException("Sin sesión o academia")))
                return@launch
            }
            val res = runCatching {
                withContext(Dispatchers.IO) {
                    PadresAlumnosRepository(client).insertVinculo(aid, uid, remotoId)
                }
                AcademiaCloudSync(client, database).syncAll(skipPush = true).getOrThrow()
            }
            if (res.isSuccess) {
                cargarCandidatosVinculo()
            }
            onDone(res)
        }
    }

    private suspend fun refrescarMensajesSuspend() {
        val client = app.supabaseClient
        val cfg = database.academiaConfigDao().getActual()
        val aid = cfg?.remoteAcademiaId?.takeIf { it.isNotBlank() }
        if (client == null || aid == null) {
            _mensajesNube.value = ParentsMensajesUiState()
            return
        }
        _mensajesNube.update { it.copy(cargando = true, error = null) }
        val repo = AcademiaMensajesCategoriaRepository(client)
        runCatching { repo.listar(aid) }
            .onSuccess { rows ->
                _mensajesNube.value = ParentsMensajesUiState(
                    cargando = false,
                    items = rows.map { it.toUi() },
                    error = null,
                )
            }
            .onFailure { e ->
                _mensajesNube.update {
                    it.copy(cargando = false, error = e.message ?: e.toString())
                }
            }
    }

    fun enviarMensajeCategoria(
        categoriaNombre: String,
        tipo: String,
        titulo: String,
        cuerpo: String,
        onFinished: (Result<Unit>) -> Unit,
    ) {
        viewModelScope.launch {
            _enviandoMensaje.value = true
            try {
                val r = enviarMensajeInterno(categoriaNombre, tipo, titulo, cuerpo)
                if (r.isSuccess) refrescarMensajesSuspend()
                onFinished(r)
            } finally {
                _enviandoMensaje.value = false
            }
        }
    }

    private suspend fun enviarMensajeInterno(
        categoriaNombre: String,
        tipo: String,
        titulo: String,
        cuerpo: String,
    ): Result<Unit> {
        val client = app.supabaseClient ?: return Result.failure(IllegalStateException("Sin conexión a la nube"))
        val uid = client.auth.currentUserOrNull()?.id?.toString()?.takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Sin sesión"))
        val cfg = database.academiaConfigDao().getActual()
            ?: return Result.failure(IllegalStateException("Sin configuración"))
        val aid = cfg.remoteAcademiaId?.takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Academia no vinculada a la nube"))
        val cat = categoriaNombre.trim()
        if (cat.isEmpty()) return Result.failure(IllegalStateException("Elige categoría"))
        if (titulo.isBlank() || cuerpo.isBlank()) {
            return Result.failure(IllegalStateException("Título y mensaje son obligatorios"))
        }
        if (tipo !in MensajeCategoriaTipo.todosWire) {
            return Result.failure(IllegalStateException("Tipo no válido"))
        }
        return AcademiaMensajesCategoriaRepository(client).insertar(
            academiaId = aid,
            categoriaNombre = cat,
            tipo = tipo,
            titulo = titulo,
            cuerpo = cuerpo,
            authorUserId = uid,
        )
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

private fun parseInstantMs(iso: String?): Long? =
    iso?.takeIf { it.isNotBlank() }?.let {
        runCatching { Instant.parse(it).toEpochMilli() }.getOrNull()
    }

private fun AcademiaMensajeCategoriaRow.toUi(): MensajeCategoriaUi =
    MensajeCategoriaUi(
        id = id,
        categoriaNombre = categoriaNombre,
        tipo = tipo,
        titulo = titulo,
        cuerpo = cuerpo,
        createdAtMillis = parseInstantMs(createdAt) ?: 0L,
        eventAtMillis = parseInstantMs(eventAt),
    )
