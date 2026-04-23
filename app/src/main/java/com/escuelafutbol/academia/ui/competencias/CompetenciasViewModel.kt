package com.escuelafutbol.academia.ui.competencias

import android.app.Application
import com.escuelafutbol.academia.R
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.data.local.AcademiaDatabase
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.data.local.model.esPadreMembresiaNube
import com.escuelafutbol.academia.data.local.model.normalizarClaveCategoriaNombre
import com.escuelafutbol.academia.data.remote.AcademiaCompetenciasRepository
import com.escuelafutbol.academia.data.remote.PadresAlumnosRepository
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
import com.escuelafutbol.academia.domain.competencias.calcularTablaPosiciones
import com.escuelafutbol.academia.domain.competencias.construirLideresOfensivosTabla
import com.escuelafutbol.academia.domain.competencias.resolverReglasPuntosTabla
import com.escuelafutbol.academia.data.remote.dto.AnotadorMarcadorLinea
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaCategoriaRow
import com.escuelafutbol.academia.data.remote.dto.CompetenciaPartidoEstado
import com.escuelafutbol.academia.data.remote.dto.CompetenciaPartidoLocalVisitante
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.jan.supabase.auth.auth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Tono del último resultado en tarjetas de lista (solo presentación). */
enum class CompetenciaListaTonResultado {
    Ninguno,
    Victoria,
    Empate,
    Derrota,
}

data class CompetenciaListaItemUi(
    val competencia: AcademiaCompetenciaRow,
    val deporteNombre: String,
    /** Padre con varias categorías: nombres de categoría (inscripciones) visibles en esta competencia. */
    val categoriasRelacionadas: List<String> = emptyList(),
    /** Partidos con marcador en categorías consideradas para la fila (lista staff/padre). */
    val partidosJugados: Int = 0,
    val numCategoriasInscritas: Int = 0,
    /** Staff: nombres de categoría inscritas (lista ordenada) para chips en la tarjeta de lista. */
    val categoriasInscritasNombres: List<String> = emptyList(),
    val proximoRival: String? = null,
    val proximoFechaCorta: String? = null,
    val padreUltimoRival: String? = null,
    val padreUltimoGolesPropio: Int? = null,
    val padreUltimoGolesRival: Int? = null,
    val padreUltimoTono: CompetenciaListaTonResultado = CompetenciaListaTonResultado.Ninguno,
    val padreCategoriaTexto: String? = null,
    val padreEquipoTexto: String? = null,
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

    /** `jugador_id` remotos vinculados al padre (Supabase); no usa session_categoria. */
    private val _vinculosJugadorRemoteIds = MutableStateFlow<Set<String>>(emptySet())

    /** Solo lectura: ids remotos devueltos por `academia_padres_alumnos` (última sync en [sincronizarVinculosPadreDesdeNube]). */
    val vinculosPadreJugadorRemoteIds: StateFlow<Set<String>> = _vinculosJugadorRemoteIds.asStateFlow()

    /** Nombres de categoría de hijos vinculados (Room), ordenados para chips. */
    private val _categoriasHijoPadre = MutableStateFlow<List<String>>(emptyList())
    val categoriasHijoPadre: StateFlow<List<String>> = _categoriasHijoPadre.asStateFlow()

    /** null = «Todas mis categorías»; nombre canónico de Room de una categoría hija. */
    private val _filtroLocalPadre = MutableStateFlow<String?>(null)
    val filtroLocalPadre: StateFlow<String?> = _filtroLocalPadre.asStateFlow()

    /**
     * Hijos vinculados al tutor en Room (activos, `remoteId` en [vinculosPadreJugadorRemoteIds]).
     * Sirve para la pestaña Inscripciones (padre): asociar cada inscripción al hijo por categoría normalizada.
     */
    val hijosPadreVinculados: StateFlow<List<Jugador>> = combine(
        database.jugadorDao().observeAll(),
        _vinculosJugadorRemoteIds,
    ) { jugadores, vinculoIds ->
        if (vinculoIds.isEmpty()) {
            emptyList()
        } else {
            jugadores
                .filter { j ->
                    j.activo && j.remoteId != null && j.remoteId!!.trim() in vinculoIds
                }
                .sortedBy { it.nombre.trim().lowercase(Locale.ROOT) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repo?.let { _catalogoDeportes.value = it.listarCatalogoDeportes() }
            combine(
                filtroCategoria,
                categoriasPermitidasOperacion,
                _categoriasHijoPadre,
                _filtroLocalPadre,
            ) { _, _, _, _ -> }
                .collect { refrescarListaInterno() }
        }
        viewModelScope.launch {
            database.academiaConfigDao().observe()
                .map { it?.esPadreMembresiaNube() == true }
                .distinctUntilChanged()
                .collect { esPadre ->
                    if (esPadre) {
                        sincronizarVinculosPadreDesdeNube()
                    } else {
                        _vinculosJugadorRemoteIds.value = emptySet()
                        _filtroLocalPadre.value = null
                    }
                }
        }
        viewModelScope.launch {
            combine(
                database.jugadorDao().observeAll(),
                _vinculosJugadorRemoteIds,
            ) { jugadores, vinculoIds ->
                jugadores to vinculoIds
            }.collect { (jugadores, vinculoIds) ->
                actualizarCategoriasHijoDesdeRoom(jugadores, vinculoIds)
            }
        }
    }

    fun setFiltroLocalPadreCategoria(nombreCategoria: String?) {
        val normSel = nombreCategoria?.trim()?.takeIf { it.isNotEmpty() }
            ?.let { normalizarClaveCategoriaNombre(it) }
        if (normSel == null) {
            _filtroLocalPadre.value = null
            return
        }
        val lista = _categoriasHijoPadre.value
        val match = lista.firstOrNull { normalizarClaveCategoriaNombre(it) == normSel }
        _filtroLocalPadre.value = match
    }

    private suspend fun sincronizarVinculosPadreDesdeNube() {
        val client = app.supabaseClient ?: run {
            _vinculosJugadorRemoteIds.value = emptySet()
            return
        }
        val uid = client.auth.currentUserOrNull()?.id?.toString() ?: run {
            _vinculosJugadorRemoteIds.value = emptySet()
            return
        }
        val aid = withContext(Dispatchers.IO) {
            database.academiaConfigDao().getActual()?.remoteAcademiaId?.trim()?.takeIf { it.isNotEmpty() }
        } ?: run {
            _vinculosJugadorRemoteIds.value = emptySet()
            return
        }
        val rows = withContext(Dispatchers.IO) {
            runCatching { PadresAlumnosRepository(client).listVinculos(aid, uid) }.getOrElse { emptyList() }
        }
        _vinculosJugadorRemoteIds.value = rows.map { it.jugadorId.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    private fun actualizarCategoriasHijoDesdeRoom(jugadores: List<Jugador>, vinculoIds: Set<String>) {
        if (vinculoIds.isEmpty()) {
            if (_categoriasHijoPadre.value.isNotEmpty()) {
                _categoriasHijoPadre.value = emptyList()
            }
            _filtroLocalPadre.value = null
            return
        }
        val ordenadas = jugadores
            .asSequence()
            .filter { it.activo && it.remoteId != null && it.remoteId!!.trim() in vinculoIds }
            .map { it.categoria.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sortedWith(compareBy { it.lowercase(Locale.ROOT) })
            .toList()
        val prevFiltro = _filtroLocalPadre.value
        if (ordenadas != _categoriasHijoPadre.value) {
            _categoriasHijoPadre.value = ordenadas
        }
        if (prevFiltro != null) {
            val ok = ordenadas.any { normalizarClaveCategoriaNombre(it) == normalizarClaveCategoriaNombre(prevFiltro) }
            if (!ok) _filtroLocalPadre.value = null
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
        viewModelScope.launch {
            val cfg = withContext(Dispatchers.IO) { database.academiaConfigDao().getActual() }
            if (cfg?.esPadreMembresiaNube() == true) {
                sincronizarVinculosPadreDesdeNube()
            }
            refrescarListaInterno()
        }
    }

    /** Recarga vínculos padre–alumno y lista (p. ej. al abrir Competencias como padre). */
    fun refrescarAmbitoPadreCompetencias() {
        viewModelScope.launch {
            sincronizarVinculosPadreDesdeNube()
            refrescarListaInterno()
        }
    }

    fun refrescarDetalle() {
        val id = detalleCompetenciaId ?: return
        viewModelScope.launch { refrescarDetalleInterno(id) }
    }

    private data class MetricasPartidosLista(
        val partidosJugados: Int,
        val proximo: AcademiaCompetenciaPartidoRow?,
        val ultimoJugado: AcademiaCompetenciaPartidoRow?,
    )

    private fun parseFechaPartidoValor(iso: String): LocalDate? {
        val s = iso.trim()
        if (s.length < 10) return null
        return runCatching { LocalDate.parse(s.substring(0, 10)) }.getOrNull()
    }

    private fun esPartidoCanceladoLista(p: AcademiaCompetenciaPartidoRow): Boolean =
        p.estado.equals(CompetenciaPartidoEstado.CANCELADO, ignoreCase = true)

    private fun esPartidoJugadoConMarcadorLista(p: AcademiaCompetenciaPartidoRow): Boolean =
        !esPartidoCanceladoLista(p) && p.jugado && p.scorePropio != null && p.scoreRival != null

    private fun calcularMetricasPartidosLista(partidos: List<AcademiaCompetenciaPartidoRow>): MetricasPartidosLista {
        val jugados = partidos.filter { esPartidoJugadoConMarcadorLista(it) }
        val ultimo = jugados.maxWithOrNull(
            compareBy<AcademiaCompetenciaPartidoRow>({ parseFechaPartidoValor(it.fecha) ?: LocalDate.MIN })
                .thenBy { it.jornada },
        )
        val hoy = LocalDate.now()
        val pendientes = partidos.filter { !esPartidoCanceladoLista(it) && !esPartidoJugadoConMarcadorLista(it) }
        val ord = pendientes.sortedWith(
            compareBy<AcademiaCompetenciaPartidoRow>({ parseFechaPartidoValor(it.fecha) ?: LocalDate.MAX })
                .thenBy { it.jornada },
        )
        val proximo = ord.firstOrNull { p ->
            val d = parseFechaPartidoValor(p.fecha)
            d == null || !d.isBefore(hoy)
        } ?: ord.firstOrNull()
        return MetricasPartidosLista(jugados.size, proximo, ultimo)
    }

    private fun tonoUltimoPartidoLista(p: AcademiaCompetenciaPartidoRow?): CompetenciaListaTonResultado {
        if (p == null || !esPartidoJugadoConMarcadorLista(p)) return CompetenciaListaTonResultado.Ninguno
        val a = p.scorePropio!!
        val b = p.scoreRival!!
        return when {
            a > b -> CompetenciaListaTonResultado.Victoria
            a < b -> CompetenciaListaTonResultado.Derrota
            else -> CompetenciaListaTonResultado.Empate
        }
    }

    private val formatterFechaLista = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES"))

    private fun formatearFechaListaCorta(fechaIso: String): String {
        val d = parseFechaPartidoValor(fechaIso)
        return if (d != null) d.format(formatterFechaLista) else fechaIso.trim()
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
        val cfgNube = withContext(Dispatchers.IO) { database.academiaConfigDao().getActual() }
        val esPadreNube = cfgNube?.esPadreMembresiaNube() == true
        val items = if (esPadreNube) {
            val hijosNorm = _categoriasHijoPadre.value.map { normalizarClaveCategoriaNombre(it) }.toSet()
            val localSel = _filtroLocalPadre.value?.trim()?.takeIf { it.isNotEmpty() }
            val efectivoNorm: Set<String> = when {
                hijosNorm.isEmpty() -> emptySet()
                localSel == null -> hijosNorm
                else -> {
                    val n = normalizarClaveCategoriaNombre(localSel)
                    if (n in hijosNorm) setOf(n) else hijosNorm
                }
            }
            raw.mapNotNull { comp ->
                val insc = runCatching { r.listarInscripciones(comp.id) }.getOrElse { emptyList() }
                comp to insc
            }
                .filter { (_, insc) ->
                    if (insc.isEmpty()) return@filter false
                    insc.any { row ->
                        normalizarClaveCategoriaNombre(row.categoriaNombre) in efectivoNorm
                    }
                }
                .map { (comp, insc) ->
                    val categoriasEtiqueta = insc
                        .asSequence()
                        .filter { normalizarClaveCategoriaNombre(it.categoriaNombre) in efectivoNorm }
                        .map { it.categoriaNombre.trim() }
                        .distinctBy { normalizarClaveCategoriaNombre(it) }
                        .sortedWith(compareBy { it.lowercase(Locale.ROOT) })
                        .toList()
                    val inscRows = insc.filter {
                        normalizarClaveCategoriaNombre(it.categoriaNombre) in efectivoNorm
                    }
                    val ids = inscRows.map { it.id }.toSet()
                    val partidos = runCatching { r.listarPartidos(comp.id) }.getOrElse { emptyList() }
                    val partidosVis = partidos.filter { it.categoriaEnCompetenciaId in ids }
                    val m = calcularMetricasPartidosLista(partidosVis)
                    val ultimo = m.ultimoJugado
                    val equipos = inscRows
                        .mapNotNull { it.nombreEquipoMostrado?.trim()?.takeIf { s -> s.isNotEmpty() } }
                        .distinct()
                    CompetenciaListaItemUi(
                        competencia = comp,
                        deporteNombre = deportesMap[comp.deporteId]?.nombre ?: "—",
                        categoriasRelacionadas = categoriasEtiqueta,
                        partidosJugados = m.partidosJugados,
                        numCategoriasInscritas = inscRows.distinctBy { it.id }.size,
                        proximoRival = m.proximo?.rival?.trim()?.takeIf { it.isNotEmpty() },
                        proximoFechaCorta = m.proximo?.let { formatearFechaListaCorta(it.fecha) },
                        padreUltimoRival = ultimo?.rival?.trim()?.takeIf { it.isNotEmpty() },
                        padreUltimoGolesPropio = ultimo?.scorePropio,
                        padreUltimoGolesRival = ultimo?.scoreRival,
                        padreUltimoTono = tonoUltimoPartidoLista(ultimo),
                        padreCategoriaTexto = categoriasEtiqueta.joinToString(" · ").takeIf { it.isNotBlank() },
                        padreEquipoTexto = when {
                            equipos.isEmpty() -> null
                            equipos.size == 1 -> equipos.first()
                            else -> equipos.joinToString(" · ")
                        },
                    )
                }
        } else {
            val catFiltro = filtroCategoria.value?.trim()?.takeIf { it.isNotEmpty() }
            val permitidas = categoriasPermitidasOperacion.value?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet()
            raw.mapNotNull { c ->
                val insc = runCatching { r.listarInscripciones(c.id) }.getOrElse { emptyList() }
                val pasaFiltroLista = if (insc.isEmpty()) {
                    // Quién ve borradores lo decide RLS (coach: solo sin inscripciones activas, etc.).
                    true
                } else {
                    insc.any { row ->
                        val okCat = catFiltro == null || row.categoriaNombre.trim().equals(catFiltro, ignoreCase = true)
                        val okCoach = permitidas == null || permitidas.any { p ->
                            row.categoriaNombre.trim().equals(p, ignoreCase = true)
                        }
                        okCat && okCoach
                    }
                }
                if (!pasaFiltroLista) return@mapNotNull null
                val inscFiltradas = insc.filter { row ->
                    val okCat = catFiltro == null || row.categoriaNombre.trim().equals(catFiltro, ignoreCase = true)
                    val okCoach = permitidas == null || permitidas.any { p ->
                        row.categoriaNombre.trim().equals(p, ignoreCase = true)
                    }
                    okCat && okCoach
                }
                val inscForMetrics = if (inscFiltradas.isNotEmpty()) inscFiltradas else insc
                val partidos = runCatching { r.listarPartidos(c.id) }.getOrElse { emptyList() }
                val ids = inscForMetrics.map { it.id }.toSet()
                val partidosVis = if (ids.isNotEmpty()) {
                    partidos.filter { it.categoriaEnCompetenciaId in ids }
                } else {
                    partidos
                }
                val m = calcularMetricasPartidosLista(partidosVis)
                val nombresCatsStaff = inscForMetrics
                    .asSequence()
                    .map { it.categoriaNombre.trim() }
                    .filter { it.isNotEmpty() }
                    .distinctBy { normalizarClaveCategoriaNombre(it) }
                    .sortedWith(compareBy { it.lowercase(Locale.ROOT) })
                    .toList()
                CompetenciaListaItemUi(
                    competencia = c,
                    deporteNombre = deportesMap[c.deporteId]?.nombre ?: "—",
                    partidosJugados = m.partidosJugados,
                    numCategoriasInscritas = inscForMetrics.distinctBy { it.id }.size,
                    categoriasInscritasNombres = nombresCatsStaff,
                    proximoRival = m.proximo?.rival?.trim()?.takeIf { it.isNotEmpty() },
                    proximoFechaCorta = m.proximo?.let { formatearFechaListaCorta(it.fecha) },
                )
            }
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
        val inscFull = r.listarInscripciones(competenciaId)
        val partidosFull = r.listarPartidos(competenciaId)
        val cfgDet = withContext(Dispatchers.IO) { database.academiaConfigDao().getActual() }
        val esPadreDet = cfgDet?.esPadreMembresiaNube() == true
        if (esPadreDet && deporte != null) {
            val hijosNorm = _categoriasHijoPadre.value.map { normalizarClaveCategoriaNombre(it) }.toSet()
            val localSel = _filtroLocalPadre.value?.trim()?.takeIf { it.isNotEmpty() }
            val efectivoNorm: Set<String> = when {
                hijosNorm.isEmpty() -> emptySet()
                localSel == null -> hijosNorm
                else -> {
                    val n = normalizarClaveCategoriaNombre(localSel)
                    if (n in hijosNorm) setOf(n) else hijosNorm
                }
            }
            val inscF = inscFull.filter { normalizarClaveCategoriaNombre(it.categoriaNombre) in efectivoNorm }
            val idsPermitidas = inscF.map { it.id }.toSet()
            val partF = partidosFull.filter { it.categoriaEnCompetenciaId in idsPermitidas }
            val reglas = resolverReglasPuntosTabla(deporte, comp)
            val tablaPadre = calcularTablaPosiciones(inscF, partF, deporte, reglas)
            val lideresPadre = construirLideresOfensivosTabla(partF, limite = 3)
            _detalleUi.value = CompetenciasDetalleUi(
                competencia = comp,
                deporte = deporte,
                inscripciones = inscF,
                partidos = partF,
                tabla = tablaPadre,
                lideresOfensivosTabla = lideresPadre,
                cargando = false,
                error = null,
            )
        } else {
            val tabla = casosUso?.calcularTablaPosiciones(aid, competenciaId)?.getOrNull() ?: emptyList()
            val lideresTabla = construirLideresOfensivosTabla(partidosFull, limite = 3)
            _detalleUi.value = CompetenciasDetalleUi(
                competencia = comp,
                deporte = deporte,
                inscripciones = inscFull,
                partidos = partidosFull,
                tabla = tabla,
                lideresOfensivosTabla = lideresTabla,
                cargando = false,
                error = null,
            )
        }
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
            val clave = normalizarClaveCategoriaNombre(categoriaNombre)
            database.jugadorDao().getAll()
                .filter { it.activo && normalizarClaveCategoriaNombre(it.categoria) == clave }
                .sortedBy { it.nombre.trim().lowercase(Locale.ROOT) }
        }

    /**
     * Jugadores para resolver fotos/nombres de anotadores: misma categoría (clave normalizada) y,
     * además, cualquier activo cuyo [Jugador.remoteId] coincida con [AnotadorMarcadorLinea.jugadorRemoteId]
     * (p. ej. padre con hijos en Room pero sin plantilla completa de la categoría).
     */
    suspend fun jugadoresParaAnotadoresPartido(
        categoriaNombre: String,
        lineasAnotadores: List<AnotadorMarcadorLinea>,
    ): List<Jugador> = withContext(Dispatchers.IO) {
        val clave = normalizarClaveCategoriaNombre(categoriaNombre)
        val todos = database.jugadorDao().getAll().filter { it.activo }
        val porCategoria = todos.filter { normalizarClaveCategoriaNombre(it.categoria) == clave }
        val porId = LinkedHashMap<Long, Jugador>()
        porCategoria.forEach { porId[it.id] = it }
        val rids = lineasAnotadores.mapNotNull { it.jugadorRemoteId?.trim()?.takeIf { r -> r.isNotEmpty() } }.distinct()
        for (rid in rids) {
            if (porCategoria.any { j -> j.remoteId?.trim()?.equals(rid, ignoreCase = true) == true }) {
                continue
            }
            val extra = todos.firstOrNull { j -> j.remoteId?.trim()?.equals(rid, ignoreCase = true) == true }
                ?: database.jugadorDao().getJugadorPorRemoteId(rid)
                ?: database.jugadorDao().getJugadorPorRemoteId(rid.trim().lowercase(Locale.ROOT))
            if (extra != null && extra.activo) {
                porId[extra.id] = extra
            }
        }
        porId.values.sortedBy { it.nombre.trim().lowercase(Locale.ROOT) }
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
