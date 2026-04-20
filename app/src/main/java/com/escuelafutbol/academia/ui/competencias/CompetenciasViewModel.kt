package com.escuelafutbol.academia.ui.competencias

import android.app.Application
import com.escuelafutbol.academia.R
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.data.local.AcademiaDatabase
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.data.remote.AcademiaCompetenciasRepository
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaCategoriaInsert
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaInsert
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaPartidoInsert
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaPartidoRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaPartidoUpdatePatch
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaRow
import com.escuelafutbol.academia.data.remote.dto.CatalogoDeporteRow
import com.escuelafutbol.academia.domain.competencias.CompetenciasCasosUso
import com.escuelafutbol.academia.domain.competencias.LideresOfensivosTablaResultado
import com.escuelafutbol.academia.domain.competencias.LineaTablaPosicion
import com.escuelafutbol.academia.domain.competencias.construirLideresOfensivosTabla
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaCategoriaRow
import com.escuelafutbol.academia.data.remote.dto.CompetenciaPartidoEstado
import com.escuelafutbol.academia.data.remote.dto.CompetenciaPartidoLocalVisitante
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class CompetenciaListaItemUi(
    val competencia: AcademiaCompetenciaRow,
    val deporteNombre: String,
)

data class CompetenciasListaUi(
    val items: List<CompetenciaListaItemUi>,
    val cargando: Boolean,
    val error: String?,
)

data class CompetenciasDetalleUi(
    val competencia: AcademiaCompetenciaRow?,
    val deporte: CatalogoDeporteRow?,
    val inscripciones: List<AcademiaCompetenciaCategoriaRow>,
    val partidos: List<AcademiaCompetenciaPartidoRow>,
    val tabla: List<LineaTablaPosicion>,
    /** Ranking de anotaciones en Tabla o estado vacío/inconsistente (ver dominio). */
    val lideresOfensivosTabla: LideresOfensivosTablaResultado,
    val cargando: Boolean,
    val error: String?,
)

class CompetenciasViewModel(
    application: Application,
    private val database: AcademiaDatabase,
    private val filtroCategoria: StateFlow<String?>,
    private val categoriasPermitidasOperacion: StateFlow<Set<String>?>,
) : AndroidViewModel(application) {

    private val app: AcademiaApplication get() = getApplication()
    private val repo by lazy {
        app.supabaseClient?.let { AcademiaCompetenciasRepository(it) }
    }
    private val casosUso by lazy {
        repo?.let { CompetenciasCasosUso(it) }
    }

    private val _catalogoDeportes = MutableStateFlow<List<CatalogoDeporteRow>>(emptyList())
    val catalogoDeportes: StateFlow<List<CatalogoDeporteRow>> = _catalogoDeportes.asStateFlow()

    /** Nombres de categoría locales (Room + jugadores) para inscripciones. */
    val nombresCategoriasLocales = combine(
        database.categoriaDao().observeAllOrdered(),
        database.jugadorDao().observeCategorias(),
    ) { tabla, desdeJugadores ->
        val desdeTabla = tabla.map { it.nombre.trim() }.filter { it.isNotEmpty() }
        (desdeTabla + desdeJugadores).distinct().sortedBy { it.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _listaUi = MutableStateFlow(
        CompetenciasListaUi(items = emptyList(), cargando = false, error = null),
    )
    val listaUi: StateFlow<CompetenciasListaUi> = _listaUi.asStateFlow()

    private val _detalleUi = MutableStateFlow(
        CompetenciasDetalleUi(
            competencia = null,
            deporte = null,
            inscripciones = emptyList(),
            partidos = emptyList(),
            tabla = emptyList(),
            lideresOfensivosTabla = LideresOfensivosTablaResultado.SinDesgloseCoherente,
            cargando = false,
            error = null,
        ),
    )
    val detalleUi: StateFlow<CompetenciasDetalleUi> = _detalleUi.asStateFlow()

    private var detalleCompetenciaId: String? = null

    init {
        viewModelScope.launch {
            repo?.let { _catalogoDeportes.value = it.listarCatalogoDeportes() }
            combine(filtroCategoria, categoriasPermitidasOperacion) { _, _ -> }
                .collect { refrescarListaInterno() }
        }
    }

    fun limpiarDetalle() {
        detalleCompetenciaId = null
        _detalleUi.value = CompetenciasDetalleUi(
            competencia = null,
            deporte = null,
            inscripciones = emptyList(),
            partidos = emptyList(),
            tabla = emptyList(),
            lideresOfensivosTabla = LideresOfensivosTablaResultado.SinDesgloseCoherente,
            cargando = false,
            error = null,
        )
    }

    fun cargarDetalle(competenciaId: String) {
        detalleCompetenciaId = competenciaId
        viewModelScope.launch { refrescarDetalleInterno(competenciaId) }
    }

    fun refrescarLista() {
        viewModelScope.launch { refrescarListaInterno() }
    }

    fun refrescarDetalle() {
        val id = detalleCompetenciaId ?: return
        viewModelScope.launch { refrescarDetalleInterno(id) }
    }

    private suspend fun refrescarListaInterno() {
        val r = repo ?: run {
            _listaUi.value = CompetenciasListaUi(
                items = emptyList(),
                cargando = false,
                error = "Sin cliente Supabase",
            )
            return
        }
        val aid = withContext(Dispatchers.IO) {
            database.academiaConfigDao().getActual()?.remoteAcademiaId?.trim()?.takeIf { it.isNotEmpty() }
        } ?: run {
            _listaUi.value = CompetenciasListaUi(emptyList(), false, "Academia no vinculada a la nube")
            return
        }
        _listaUi.value = _listaUi.value.copy(cargando = true, error = null)
        val deportes = _catalogoDeportes.value.takeIf { it.isNotEmpty() }
            ?: r.listarCatalogoDeportes().also { _catalogoDeportes.value = it }
        val deportesMap = deportes.associateBy { it.id }
        val listado = r.listarCompetencias(aid)
        if (listado.isFailure) {
            _listaUi.value = CompetenciasListaUi(
                items = emptyList(),
                cargando = false,
                error = listado.exceptionOrNull()?.message
                    ?: "No se pudieron cargar las competencias (revisa permisos RLS en Supabase).",
            )
            return
        }
        val raw = listado.getOrThrow()
        val catFiltro = filtroCategoria.value?.trim()?.takeIf { it.isNotEmpty() }
        val permitidas = categoriasPermitidasOperacion.value?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet()
        val filtradas = raw.filter { comp ->
            val insc = runCatching { r.listarInscripciones(comp.id) }.getOrElse { emptyList() }
            if (insc.isEmpty()) {
                // Quién ve borradores lo decide RLS (coach: solo sin inscripciones activas, etc.).
                return@filter true
            }
            insc.any { row ->
                val okCat = catFiltro == null || row.categoriaNombre.trim().equals(catFiltro, ignoreCase = true)
                val okCoach = permitidas == null || permitidas.any { p ->
                    row.categoriaNombre.trim().equals(p, ignoreCase = true)
                }
                okCat && okCoach
            }
        }
        val items = filtradas.map { c ->
            CompetenciaListaItemUi(
                competencia = c,
                deporteNombre = deportesMap[c.deporteId]?.nombre ?: "—",
            )
        }
        _listaUi.value = CompetenciasListaUi(items = items, cargando = false, error = null)
    }

    private suspend fun refrescarDetalleInterno(competenciaId: String) {
        val r = repo ?: run {
            _detalleUi.value = _detalleUi.value.copy(cargando = false, error = "Sin cliente Supabase")
            return
        }
        val aid = withContext(Dispatchers.IO) {
            database.academiaConfigDao().getActual()?.remoteAcademiaId?.trim()?.takeIf { it.isNotEmpty() }
        } ?: run {
            _detalleUi.value = _detalleUi.value.copy(cargando = false, error = "Academia no vinculada")
            return
        }
        _detalleUi.value = _detalleUi.value.copy(cargando = true, error = null)
        val listado = r.listarCompetencias(aid)
        if (listado.isFailure) {
            _detalleUi.value = _detalleUi.value.copy(
                cargando = false,
                error = listado.exceptionOrNull()?.message
                    ?: "No se pudo cargar la competencia.",
            )
            return
        }
        val comp = listado.getOrThrow().find { it.id == competenciaId }
        if (comp == null) {
            _detalleUi.value = CompetenciasDetalleUi(
                competencia = null,
                deporte = null,
                inscripciones = emptyList(),
                partidos = emptyList(),
                tabla = emptyList(),
                lideresOfensivosTabla = LideresOfensivosTablaResultado.SinDesgloseCoherente,
                cargando = false,
                error = "Competencia no disponible",
            )
            return
        }
        val deporte = r.listarCatalogoDeportes().find { it.id == comp.deporteId }
        val insc = r.listarInscripciones(competenciaId)
        val partidos = r.listarPartidos(competenciaId)
        val tabla = casosUso?.calcularTablaPosiciones(aid, competenciaId)?.getOrNull() ?: emptyList()
        val lideresTabla = construirLideresOfensivosTabla(partidos, limite = 3)
        _detalleUi.value = CompetenciasDetalleUi(
            competencia = comp,
            deporte = deporte,
            inscripciones = insc,
            partidos = partidos,
            tabla = tabla,
            lideresOfensivosTabla = lideresTabla,
            cargando = false,
            error = null,
        )
    }

    fun puedeCrearCompetencia(config: AcademiaConfig): Boolean {
        if (config.remoteAcademiaId.isNullOrBlank()) return false
        if (config.cloudMembresiaRol?.equals("parent", ignoreCase = true) == true) return false
        return true
    }

    fun puedeAgregarInscripcionOPartido(config: AcademiaConfig): Boolean {
        if (config.remoteAcademiaId.isNullOrBlank()) return false
        if (config.cloudMembresiaRol?.equals("parent", ignoreCase = true) == true) return false
        return true
    }

    fun crearCompetencia(
        nombre: String,
        deporteId: String,
        tipoCompetencia: String,
        temporada: String?,
        onResult: (Result<Unit>) -> Unit,
    ) {
        val r = repo ?: run {
            onResult(Result.failure(IllegalStateException("Sin Supabase")))
            return
        }
        viewModelScope.launch {
            val aid = database.academiaConfigDao().getActual()?.remoteAcademiaId?.trim()?.takeIf { it.isNotEmpty() }
                ?: run {
                    onResult(Result.failure(IllegalStateException("Sin academia")))
                    return@launch
                }
            val tipoWire = normalizarTipoCompetenciaSupabase(tipoCompetencia)
            val res = r.insertarCompetencia(
                AcademiaCompetenciaInsert(
                    academiaId = aid,
                    deporteId = deporteId,
                    nombre = nombre.trim(),
                    temporada = temporada?.trim()?.takeIf { it.isNotEmpty() },
                    tipoCompetencia = tipoWire,
                ),
            )
            if (res.isSuccess) refrescarListaInterno()
            onResult(res)
        }
    }

    fun agregarInscripcion(
        competenciaId: String,
        categoriaNombre: String,
        nombreEquipo: String?,
        onResult: (Result<Unit>) -> Unit,
    ) {
        val r = repo ?: run {
            onResult(Result.failure(IllegalStateException("Sin Supabase")))
            return
        }
        viewModelScope.launch {
            val res = r.insertarInscripcion(
                AcademiaCompetenciaCategoriaInsert(
                    competenciaId = competenciaId,
                    categoriaNombre = categoriaNombre.trim(),
                    nombreEquipoMostrado = nombreEquipo?.trim()?.takeIf { it.isNotEmpty() },
                ),
            )
            if (res.isSuccess) refrescarDetalleInterno(competenciaId)
            onResult(res)
        }
    }

    fun agregarPartido(
        competenciaId: String,
        categoriaEnCompetenciaId: String,
        categoriaNombre: String,
        jornada: Int,
        fechaIso: String,
        rival: String,
        onResult: (Result<Unit>) -> Unit,
    ) {
        val r = repo ?: run {
            onResult(Result.failure(IllegalStateException("Sin Supabase")))
            return
        }
        viewModelScope.launch {
            val j = jornada.coerceAtLeast(1)
            val partidosActuales = r.listarPartidos(competenciaId)
            if (partidosActuales.any { it.categoriaEnCompetenciaId == categoriaEnCompetenciaId && it.jornada == j }) {
                onResult(
                    Result.failure(
                        IllegalArgumentException(
                            getApplication<Application>().getString(R.string.competitions_error_duplicate_matchday),
                        ),
                    ),
                )
                return@launch
            }
            val res = r.insertarPartido(
                AcademiaCompetenciaPartidoInsert(
                    competenciaId = competenciaId,
                    categoriaEnCompetenciaId = categoriaEnCompetenciaId,
                    categoriaNombre = categoriaNombre.trim(),
                    jornada = j,
                    fecha = fechaIso.trim(),
                    rival = rival.trim(),
                    localVisitante = CompetenciaPartidoLocalVisitante.NEUTRAL,
                    jugado = false,
                    estado = CompetenciaPartidoEstado.PROGRAMADO,
                ),
            )
            if (res.isSuccess) refrescarDetalleInterno(competenciaId)
            onResult(res)
        }
    }

    /** Jugadores activos de Room para la categoría del partido (foto, nombre, ids para anotadores). */
    suspend fun jugadoresParaCategoria(categoriaNombre: String): List<Jugador> =
        withContext(Dispatchers.IO) {
            database.jugadorDao()
                .getByCategoria(categoriaNombre.trim())
                .sortedBy { it.nombre.trim().lowercase(Locale.ROOT) }
        }

    fun guardarResultadoPartido(
        competenciaId: String,
        partidoId: String,
        fechaIso: String,
        scorePropio: Int,
        scoreRival: Int,
        jugado: Boolean,
        estado: String,
        detalleMarcadorJson: String?,
        onResult: (Result<Unit>) -> Unit,
    ) {
        val r = repo ?: run {
            onResult(Result.failure(IllegalStateException("Sin Supabase")))
            return
        }
        viewModelScope.launch {
            val res = r.actualizarPartido(
                partidoId,
                AcademiaCompetenciaPartidoUpdatePatch(
                    fecha = fechaIso.trim(),
                    scorePropio = scorePropio,
                    scoreRival = scoreRival,
                    jugado = jugado,
                    estado = estado,
                    detalleMarcadorJson = detalleMarcadorJson,
                ),
            )
            if (res.isSuccess) refrescarDetalleInterno(competenciaId)
            onResult(res)
        }
    }

    private companion object {
        private val TiposCompetenciaSupabase = setOf("liga", "copa", "torneo", "amistoso", "otro")

        /** Alinea con el `check` de `academia_competencia.tipo_competencia` en Postgres. */
        private fun normalizarTipoCompetenciaSupabase(entrada: String): String {
            val s = entrada.trim().lowercase(Locale.ROOT)
            if (s.isEmpty()) return "liga"
            return if (s in TiposCompetenciaSupabase) s else "otro"
        }
    }
}
