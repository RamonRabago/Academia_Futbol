package com.escuelafutbol.academia.ui.contenido

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.data.local.AcademiaDatabase
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.model.esSesionDueñoCuentaAcademiaRemota
import com.escuelafutbol.academia.data.remote.AcademiaContenidoCategoriaRepository
import com.escuelafutbol.academia.data.remote.AcademiaContenidoReaccionRepository
import com.escuelafutbol.academia.data.remote.dto.AcademiaContenidoCategoriaRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaContenidoReaccionRow
import com.escuelafutbol.academia.data.remote.dto.ContenidoReaccionTipo
import com.escuelafutbol.academia.data.remote.dto.ContenidoEstadoPublicacion
import com.escuelafutbol.academia.data.remote.dto.decodeContenidoCuerpoImagenesUrls
import com.escuelafutbol.academia.data.sync.uploadAcademiaPublicImage
import io.github.jan.supabase.auth.auth
import java.io.File
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ContenidoUiState(
    val cargando: Boolean = false,
    val error: String? = null,
)

data class ContenidoReaccionesUi(
    val like: Int = 0,
    val celebrate: Int = 0,
    val thanks: Int = 0,
    val strong: Int = 0,
    /** Tipo de la sesión actual, o null si no reaccionó. */
    val miTipo: String? = null,
)

data class ContenidoItemUi(
    val id: String,
    val categoriaNombre: String,
    val tema: String,
    val titulo: String,
    val cuerpo: String,
    /** UUID Auth del autor (Supabase). */
    val authorUserId: String = "",
    /** URL pública en Storage; null = sin portada. */
    val imagenUrl: String? = null,
    /** Fotos dentro del artículo (orden de inserción). */
    val cuerpoImagenesUrls: List<String> = emptyList(),
    val createdAtMillis: Long,
    val reacciones: ContenidoReaccionesUi = ContenidoReaccionesUi(),
    val estadoPublicacion: String = ContenidoEstadoPublicacion.PUBLISHED,
    /**
     * Filas distintas en `academia_contenido_categoria` que corresponden a la misma publicación
     * (mismo envío en varias categorías). Vacío = solo [id].
     */
    val idsFilasMismaPublicacion: List<String> = emptyList(),
    /** Categorías de esas filas (permisos y moderación). Vacío = inferir de [categoriaNombre]. */
    val categoriasDeFilas: List<String> = emptyList(),
)

object ContenidoTema {
    const val NOTICIA = "noticia"
    const val ENTRENAMIENTO = "entrenamiento"
    const val NUTRICION = "nutricion"
    const val EJERCICIO = "ejercicio"
    const val BIENESTAR = "bienestar"
    const val OTRO = "otro"

    val todosWire: List<String> = listOf(
        NOTICIA,
        ENTRENAMIENTO,
        NUTRICION,
        EJERCICIO,
        BIENESTAR,
        OTRO,
    )
}

class ContenidoViewModel(
    application: Application,
    private val database: AcademiaDatabase,
    private val filtroCategoria: StateFlow<String?>,
    private val categoriasPermitidasOperacion: StateFlow<Set<String>?>,
) : AndroidViewModel(application) {

    companion object {
        const val MAX_FOTOS_CUERPO = 12
        const val MAX_CHARS_CUERPO_SOCIAL = 500
        /** Valor interno del selector «publicar en todas las categorías permitidas». */
        const val CATEGORIA_TODAS_MAGIC = "__TODAS__"
    }

    private val app: AcademiaApplication get() = getApplication()

    private val _rawItems = MutableStateFlow<List<ContenidoItemUi>>(emptyList())
    private val _uidSesion = MutableStateFlow<String?>(null)
    private val _reaccionesPorContenido = MutableStateFlow<Map<String, ContenidoReaccionesUi>>(emptyMap())
    private val filtroTema = MutableStateFlow<String?>(null)
    /** null = todos; [ContenidoEstadoPublicacion.PENDING], [PUBLISHED]. */
    private val filtroEstadoPublicacion = MutableStateFlow<String?>(null)
    private val _ui = MutableStateFlow(ContenidoUiState())
    val uiState: StateFlow<ContenidoUiState> = _ui.asStateFlow()

    val filtroTemaSeleccionado: StateFlow<String?> = filtroTema.asStateFlow()

    val filtroEstadoPublicacionSeleccionado: StateFlow<String?> = filtroEstadoPublicacion.asStateFlow()

    val categoriasParaPublicar = combine(
        categoriasPermitidasOperacion,
        database.categoriaDao().observeAllOrdered(),
    ) { permitidas, cats ->
        val nombres = cats.map { it.nombre }.sorted()
        val p = permitidas?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet()
        if (p == null) nombres else nombres.filter { n -> p.any { it.equals(n, ignoreCase = true) } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Publicaciones con `created_at` posterior a la última visita a Recursos, excluyendo las del propio usuario.
     * Se usa para el badge en la barra / menú (sin push).
     */
    val recursosNoLeidosCount: StateFlow<Int> = combine(
        _rawItems,
        database.academiaConfigDao().observe(),
        _uidSesion,
    ) { items, cfg, uid ->
        val c = cfg ?: return@combine 0
        if (c.remoteAcademiaId.isNullOrBlank()) return@combine 0
        val last = c.recursosUltimaVistaAtMillis
        val u = uid?.trim()?.takeIf { it.isNotEmpty() }
        items
            .asSequence()
            .filter { item ->
                item.createdAtMillis > last &&
                    (u == null || !item.authorUserId.trim().equals(u, ignoreCase = true))
            }
            .distinctBy { huellaPublicacionMulticategoria(it) }
            .count()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val itemsPresentados: StateFlow<List<ContenidoItemUi>> = combine(
        _rawItems,
        filtroCategoria,
        filtroTema,
        filtroEstadoPublicacion,
        _reaccionesPorContenido,
    ) { raw, catFiltro, temaFiltro, estadoFiltro, reaccMap ->
        val conReacciones = raw
            .asSequence()
            .filter {
                catFiltro == null ||
                    it.categoriaNombre.trim().equals(catFiltro.trim(), ignoreCase = true)
            }
            .filter { temaFiltro == null || it.tema == temaFiltro }
            .filter { estadoFiltro == null || it.estadoPublicacion == estadoFiltro }
            .map { item ->
                item.copy(reacciones = reaccMap[item.id] ?: ContenidoReaccionesUi())
            }
            .toList()
        if (catFiltro == null) {
            fusionarItemsMulticategoriaMismaPublicacion(conReacciones, reaccMap)
        } else {
            conReacciones
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { refrescarSuspend() }
    }

    fun setFiltroTema(tema: String?) {
        filtroTema.value = tema
    }

    fun setFiltroEstadoPublicacion(estado: String?) {
        filtroEstadoPublicacion.value = estado
    }

    fun refrescar() {
        viewModelScope.launch { refrescarSuspend() }
    }

    private suspend fun refrescarSuspend() {
        val client = app.supabaseClient
        val cfg = database.academiaConfigDao().getActual()
        val uid = client?.auth?.currentUserOrNull()?.id?.toString()
        _uidSesion.value = uid
        val aid = cfg?.remoteAcademiaId?.takeIf { it.isNotBlank() }
        if (client == null || aid == null) {
            _rawItems.value = emptyList()
            _reaccionesPorContenido.value = emptyMap()
            _ui.value = ContenidoUiState(cargando = false, error = null)
            return
        }
        _ui.update { it.copy(cargando = true, error = null) }
        val result = runCatching {
            val list = AcademiaContenidoCategoriaRepository(client).listar(aid).map { it.toUi() }
            val reaccRows = AcademiaContenidoReaccionRepository(client).listarPorAcademia(aid)
            Triple(list, reaccRows, uid)
        }
        result.onSuccess { (list, reaccRows, uidOk) ->
            _rawItems.value = list
            _reaccionesPorContenido.value = agregarReaccionesMap(reaccRows, uidOk)
            _ui.value = ContenidoUiState(cargando = false, error = null)
            val c = database.academiaConfigDao().getActual()
            if (c != null && c.recursosUltimaVistaAtMillis == 0L) {
                database.academiaConfigDao().upsert(
                    c.copy(recursosUltimaVistaAtMillis = System.currentTimeMillis()),
                )
            }
        }.onFailure { e ->
            _rawItems.value = emptyList()
            _reaccionesPorContenido.value = emptyMap()
            _ui.value = ContenidoUiState(cargando = false, error = e.message)
        }
    }

    /** Marca Recursos como visitado (reinicia el contador de no leídos en barra/menú). */
    fun marcarRecursosVistos() {
        viewModelScope.launch(Dispatchers.IO) {
            val c = database.academiaConfigDao().getActual() ?: return@launch
            database.academiaConfigDao().upsert(
                c.copy(recursosUltimaVistaAtMillis = System.currentTimeMillis()),
            )
        }
    }

    fun alternarReaccionDesdeUi(contenidoId: String, tipo: String) {
        viewModelScope.launch {
            alternarReaccionSuspend(contenidoId, tipo)
        }
    }

    private suspend fun alternarReaccionSuspend(contenidoId: String, tipo: String) {
        val client = app.supabaseClient ?: return
        val uid = client.auth.currentUserOrNull()?.id?.toString()?.takeIf { it.isNotBlank() } ?: return
        val aid = database.academiaConfigDao().getActual()?.remoteAcademiaId?.takeIf { it.isNotBlank() } ?: return
        if (tipo !in ContenidoReaccionTipo.todosWire) return
        val repo = AcademiaContenidoReaccionRepository(client)
        val actual = _reaccionesPorContenido.value[contenidoId]?.miTipo
        val res = if (actual == tipo) {
            repo.quitarReaccion(contenidoId = contenidoId, userId = uid)
        } else {
            repo.ponerReaccion(academiaId = aid, contenidoId = contenidoId, userId = uid, tipo = tipo)
        }
        if (res.isSuccess) {
            val rows = repo.listarPorAcademia(aid)
            _reaccionesPorContenido.value = agregarReaccionesMap(rows, uid)
        }
    }

    /** Una categoría concreta (no [CATEGORIA_TODAS_MAGIC]). */
    private fun puedePublicarUnaCategoria(config: AcademiaConfig, categoriaNombre: String): Boolean {
        if (config.remoteAcademiaId.isNullOrBlank()) return false
        if (config.cloudMembresiaRol?.equals("parent", ignoreCase = true) == true) return false
        val cat = categoriaNombre.trim()
        if (cat.isEmpty() || cat == CATEGORIA_TODAS_MAGIC) return false
        return when (val rol = config.cloudMembresiaRol?.lowercase()) {
            "coach" -> {
                val permitidas = categoriasPermitidasOperacion.value
                permitidas?.any { it.equals(cat, ignoreCase = true) } == true
            }
            "owner", "admin", "coordinator" -> true
            null -> true
            else -> rol != "parent"
        }
    }

    fun puedePublicar(config: AcademiaConfig, categoriaElegida: String): Boolean {
        val cat = categoriaElegida.trim()
        if (cat == CATEGORIA_TODAS_MAGIC) return false
        return puedePublicarUnaCategoria(config, cat)
    }

    /**
     * @param seleccion nombre de categoría o [CATEGORIA_TODAS_MAGIC] (todas las de [categoriasDisponibles]).
     */
    fun puedePublicarSeleccion(
        config: AcademiaConfig,
        seleccion: String,
        categoriasDisponibles: List<String>,
    ): Boolean {
        val s = seleccion.trim()
        if (s.isEmpty()) return false
        if (s == CATEGORIA_TODAS_MAGIC) {
            val lista = categoriasDisponibles.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
            return lista.isNotEmpty() && lista.all { puedePublicarUnaCategoria(config, it) }
        }
        return puedePublicarUnaCategoria(config, s)
    }

    fun categoriasDestinoPublicacion(seleccion: String, categoriasDisponibles: List<String>): List<String> {
        val s = seleccion.trim()
        if (s == CATEGORIA_TODAS_MAGIC) {
            return categoriasDisponibles.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        }
        return listOf(s).filter { it.isNotEmpty() }
    }

    /** Dueño de cuenta o admin de nube pueden publicar ya visible para familias. */
    private fun puedePublicarVisibleDirectoInterno(config: AcademiaConfig, uidSesion: String?): Boolean {
        if (config.remoteAcademiaId.isNullOrBlank()) return false
        val r = config.cloudMembresiaRol?.lowercase()
        if (r == "owner" || r == "admin") return true
        if (config.esSesionDueñoCuentaAcademiaRemota(uidSesion)) return true
        return false
    }

    fun puedeElegirVisibleYaParaFamilias(config: AcademiaConfig): Boolean =
        puedePublicarVisibleDirectoInterno(
            config,
            app.supabaseClient?.auth?.currentUserOrNull()?.id?.toString(),
        )

    fun puedeModerarPublicacion(config: AcademiaConfig, item: ContenidoItemUi): Boolean {
        if (item.estadoPublicacion != ContenidoEstadoPublicacion.PENDING) return false
        if (config.remoteAcademiaId.isNullOrBlank()) return false
        if (config.cloudMembresiaRol?.equals("parent", ignoreCase = true) == true) return false
        val uid = app.supabaseClient?.auth?.currentUserOrNull()?.id?.toString()
        val catsItem = item.categoriasParaPermisos()
        return when (val rol = config.cloudMembresiaRol?.lowercase()) {
            "owner", "admin", "coordinator" -> true
            "coach" -> {
                val permitidas = categoriasPermitidasOperacion.value
                permitidas?.any { p -> catsItem.any { it.equals(p, ignoreCase = true) } } == true
            }
            null -> config.esSesionDueñoCuentaAcademiaRemota(uid)
            else -> false
        }
    }

    /** Archivar / menú: basta con permiso en alguna categoría de la fila (o grupo fusionado). */
    fun puedeGestionarRecursosPublicacion(config: AcademiaConfig, item: ContenidoItemUi): Boolean =
        item.categoriasParaPermisos().any { puedePublicar(config, it) }

    private fun resolverEstadoInicialPublicacion(
        config: AcademiaConfig,
        uidSesion: String?,
        visibleYaParaFamilias: Boolean,
    ): String {
        if (puedePublicarVisibleDirectoInterno(config, uidSesion) && visibleYaParaFamilias) {
            return ContenidoEstadoPublicacion.PUBLISHED
        }
        // Entrenador (categorías asignadas) y coordinador publican ya visibles para familias; sin cola de aprobación del dueño.
        val r = config.cloudMembresiaRol?.lowercase()
        if (r == "coach" || r == "coordinator") {
            return ContenidoEstadoPublicacion.PUBLISHED
        }
        return ContenidoEstadoPublicacion.PENDING
    }

    suspend fun publicar(
        categoriasNombres: List<String>,
        tema: String,
        titulo: String,
        cuerpo: String,
        imagenLocal: File?,
        imagenesCuerpoLocales: List<File> = emptyList(),
        visibleYaParaFamilias: Boolean,
    ): Result<Unit> {
        val client = app.supabaseClient ?: return Result.failure(IllegalStateException("Sin conexión a la nube"))
        val uid = client.auth.currentUserOrNull()?.id?.toString()?.takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Sin sesión"))
        val cfg = database.academiaConfigDao().getActual()
            ?: return Result.failure(IllegalStateException("Sin configuración"))
        val aid = cfg.remoteAcademiaId?.takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Academia no vinculada a la nube"))
        val cats = categoriasNombres.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (cats.isEmpty()) {
            return Result.failure(IllegalStateException("Elige al menos una categoría"))
        }
        for (c in cats) {
            if (!puedePublicarUnaCategoria(cfg, c)) {
                return Result.failure(IllegalStateException("Sin permiso para publicar en: $c"))
            }
        }
        if (tema !in ContenidoTema.todosWire) {
            return Result.failure(IllegalStateException("Tema no válido"))
        }
        if (titulo.isBlank() || cuerpo.isBlank()) {
            return Result.failure(IllegalStateException("Título y texto son obligatorios"))
        }
        if (cuerpo.length > MAX_CHARS_CUERPO_SOCIAL) {
            return Result.failure(
                IllegalStateException("El texto no puede superar $MAX_CHARS_CUERPO_SOCIAL caracteres"),
            )
        }
        if (imagenesCuerpoLocales.size > MAX_FOTOS_CUERPO) {
            return Result.failure(
                IllegalStateException("Máximo $MAX_FOTOS_CUERPO fotos en el artículo"),
            )
        }
        val estado = resolverEstadoInicialPublicacion(cfg, uid, visibleYaParaFamilias)
        val archivosCuerpo = imagenesCuerpoLocales.mapNotNull { it.takeIf { f -> f.isFile } }
        val archivo = imagenLocal?.takeIf { it.isFile }
        var imagenUrlRemota: String? = null
        val urlsCuerpo = mutableListOf<String>()
        try {
            if (archivo != null) {
                val ext = when (archivo.extension.lowercase()) {
                    "png" -> "png"
                    "webp" -> "webp"
                    "jpeg", "jpg" -> "jpg"
                    else -> "jpg"
                }
                val objectPath = "$uid/$aid/contenido/${UUID.randomUUID()}.$ext"
                imagenUrlRemota = withContext(Dispatchers.IO) {
                    client.uploadAcademiaPublicImage(objectPath, archivo)
                }
            }
            for (img in archivosCuerpo) {
                val ext = when (img.extension.lowercase()) {
                    "png" -> "png"
                    "webp" -> "webp"
                    "jpeg", "jpg" -> "jpg"
                    else -> "jpg"
                }
                val objectPath = "$uid/$aid/contenido/cuerpo/${UUID.randomUUID()}.$ext"
                val url = withContext(Dispatchers.IO) {
                    client.uploadAcademiaPublicImage(objectPath, img)
                }
                urlsCuerpo.add(url)
            }
            val repo = AcademiaContenidoCategoriaRepository(client)
            for (cat in cats) {
                val res = repo.insertar(
                    academiaId = aid,
                    categoriaNombre = cat,
                    tema = tema,
                    titulo = titulo,
                    cuerpo = cuerpo,
                    authorUserId = uid,
                    imagenUrl = imagenUrlRemota,
                    cuerpoImagenesUrls = urlsCuerpo,
                    estadoPublicacion = estado,
                )
                if (res.isFailure) return res
            }
            refrescarSuspend()
            return Result.success(Unit)
        } finally {
            archivo?.let { runCatching { it.delete() } }
            archivosCuerpo.forEach { runCatching { it.delete() } }
        }
    }

    private suspend fun aprobarPublicacionUna(id: String): Result<Unit> {
        val client = app.supabaseClient ?: return Result.failure(IllegalStateException("Sin conexión a la nube"))
        val uid = client.auth.currentUserOrNull()?.id?.toString()?.takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Sin sesión"))
        val aid = database.academiaConfigDao().getActual()?.remoteAcademiaId?.takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Academia no vinculada a la nube"))
        val iso = Instant.now().toString()
        return AcademiaContenidoCategoriaRepository(client).actualizarEstadoPublicacion(
            academiaId = aid,
            id = id,
            estadoPublicacion = ContenidoEstadoPublicacion.PUBLISHED,
            approvedAtIso = iso,
            approvedByUserId = uid,
        )
    }

    suspend fun aprobarPublicacionesPorIds(ids: Collection<String>): Result<Unit> {
        val distinct = ids.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (distinct.isEmpty()) {
            return Result.failure(IllegalStateException("Sin publicación"))
        }
        for (id in distinct) {
            val r = aprobarPublicacionUna(id)
            if (r.isFailure) return r
        }
        refrescarSuspend()
        return Result.success(Unit)
    }

    private suspend fun rechazarPublicacionUna(id: String): Result<Unit> {
        val client = app.supabaseClient ?: return Result.failure(IllegalStateException("Sin conexión a la nube"))
        val aid = database.academiaConfigDao().getActual()?.remoteAcademiaId?.takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Academia no vinculada a la nube"))
        return AcademiaContenidoCategoriaRepository(client).actualizarEstadoPublicacion(
            academiaId = aid,
            id = id,
            estadoPublicacion = ContenidoEstadoPublicacion.REJECTED,
            approvedAtIso = null,
            approvedByUserId = null,
        )
    }

    suspend fun rechazarPublicacionesPorIds(ids: Collection<String>): Result<Unit> {
        val distinct = ids.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (distinct.isEmpty()) {
            return Result.failure(IllegalStateException("Sin publicación"))
        }
        for (id in distinct) {
            val r = rechazarPublicacionUna(id)
            if (r.isFailure) return r
        }
        refrescarSuspend()
        return Result.success(Unit)
    }

    fun aprobarDesdeUi(ids: Collection<String>, onDone: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch { onDone(aprobarPublicacionesPorIds(ids)) }
    }

    fun rechazarDesdeUi(ids: Collection<String>, onDone: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch { onDone(rechazarPublicacionesPorIds(ids)) }
    }

    private suspend fun archivarUna(id: String): Result<Unit> {
        val client = app.supabaseClient ?: return Result.failure(IllegalStateException("Sin conexión a la nube"))
        val aid = database.academiaConfigDao().getActual()?.remoteAcademiaId?.takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Academia no vinculada a la nube"))
        return AcademiaContenidoCategoriaRepository(client).archivar(academiaId = aid, id = id)
    }

    suspend fun archivarPorIds(ids: Collection<String>): Result<Unit> {
        val distinct = ids.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (distinct.isEmpty()) {
            return Result.failure(IllegalStateException("Sin publicación"))
        }
        for (id in distinct) {
            val r = archivarUna(id)
            if (r.isFailure) return r
        }
        refrescarSuspend()
        return Result.success(Unit)
    }

    fun archivarDesdeUi(ids: Collection<String>, onDone: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            onDone(archivarPorIds(ids))
        }
    }

    fun publicarDesdeUi(
        categoriasNombres: List<String>,
        tema: String,
        titulo: String,
        cuerpo: String,
        imagenLocal: File?,
        imagenesCuerpoLocales: List<File>,
        visibleYaParaFamilias: Boolean,
        onResult: (Result<Unit>) -> Unit,
    ) {
        viewModelScope.launch {
            onResult(
                publicar(
                    categoriasNombres,
                    tema,
                    titulo,
                    cuerpo,
                    imagenLocal,
                    imagenesCuerpoLocales,
                    visibleYaParaFamilias,
                ),
            )
        }
    }
}

private fun parseInstantMs(iso: String?): Long =
    iso?.takeIf { it.isNotBlank() }?.let {
        runCatching { Instant.parse(it).toEpochMilli() }.getOrNull()
    } ?: 0L

private fun agregarReaccionesMap(
    rows: List<AcademiaContenidoReaccionRow>,
    uidSesion: String?,
): Map<String, ContenidoReaccionesUi> {
    val porContenido = rows.groupBy { it.contenidoId }
    return porContenido.mapValues { (_, list) ->
        val counts = list.groupingBy { it.tipo }.eachCount()
        val mine = list.firstOrNull { it.userId == uidSesion }?.tipo
        ContenidoReaccionesUi(
            like = counts[ContenidoReaccionTipo.LIKE] ?: 0,
            celebrate = counts[ContenidoReaccionTipo.CELEBRATE] ?: 0,
            thanks = counts[ContenidoReaccionTipo.THANKS] ?: 0,
            strong = counts[ContenidoReaccionTipo.STRONG] ?: 0,
            miTipo = mine,
        )
    }
}

private fun AcademiaContenidoCategoriaRow.toUi(): ContenidoItemUi {
    val cat = categoriaNombre.trim()
    return ContenidoItemUi(
        id = id,
        categoriaNombre = cat,
        tema = tema,
        titulo = titulo,
        cuerpo = cuerpo,
        authorUserId = authorUserId.trim(),
        imagenUrl = imagenUrl?.trim()?.takeIf { it.isNotEmpty() },
        cuerpoImagenesUrls = decodeContenidoCuerpoImagenesUrls(cuerpoImagenesUrlsJson),
        createdAtMillis = parseInstantMs(createdAt),
        estadoPublicacion = estadoPublicacion.trim().ifEmpty { ContenidoEstadoPublicacion.PUBLISHED },
        idsFilasMismaPublicacion = listOf(id),
        categoriasDeFilas = listOf(cat).filter { it.isNotEmpty() },
    )
}

/** Ids de filas Supabase a aprobar, rechazar o archivar (incluye multicategoría). */
fun ContenidoItemUi.idsFilasParaAccionesRemotas(): List<String> {
    val xs = idsFilasMismaPublicacion.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    return if (xs.isEmpty()) listOf(id) else xs.sorted()
}

internal fun ContenidoItemUi.categoriasParaPermisos(): List<String> {
    val xs = categoriasDeFilas.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    return if (xs.isEmpty()) {
        listOf(categoriaNombre.trim()).filter { it.isNotEmpty() }
    } else {
        xs
    }
}

private fun huellaPublicacionMulticategoria(item: ContenidoItemUi): String =
    listOf(
        item.authorUserId.lowercase(),
        item.tema,
        item.titulo.trim(),
        item.cuerpo.trim(),
        item.imagenUrl ?: "",
        item.cuerpoImagenesUrls.joinToString("\u0001"),
        item.estadoPublicacion,
    ).joinToString("\u0000")

private fun fusionarReaccionesMultiplesFilas(
    ids: List<String>,
    reaccMap: Map<String, ContenidoReaccionesUi>,
): ContenidoReaccionesUi {
    val partes = ids.map { reaccMap[it] ?: ContenidoReaccionesUi() }
    return ContenidoReaccionesUi(
        like = partes.sumOf { it.like },
        celebrate = partes.sumOf { it.celebrate },
        thanks = partes.sumOf { it.thanks },
        strong = partes.sumOf { it.strong },
        miTipo = partes.firstOrNull { !it.miTipo.isNullOrBlank() }?.miTipo,
    )
}

private fun fusionarItemsMulticategoriaMismaPublicacion(
    items: List<ContenidoItemUi>,
    reaccMap: Map<String, ContenidoReaccionesUi>,
): List<ContenidoItemUi> {
    if (items.size <= 1) return items
    return items
        .groupBy { huellaPublicacionMulticategoria(it) }
        .values
        .map { grupo ->
            if (grupo.size == 1) {
                grupo.first()
            } else {
                val orden = grupo.sortedBy { it.id }
                val base = orden.first()
                val ids = orden.flatMap { it.idsFilasParaAccionesRemotas() }.distinct().sorted()
                val cats = orden.flatMap { it.categoriasParaPermisos() }.distinct().sorted()
                base.copy(
                    id = ids.first(),
                    categoriaNombre = if (cats.size == 1) cats.first() else cats.joinToString(", "),
                    categoriasDeFilas = cats,
                    idsFilasMismaPublicacion = ids,
                    reacciones = fusionarReaccionesMultiplesFilas(ids, reaccMap),
                )
            }
        }
        .sortedByDescending { it.createdAtMillis }
}
