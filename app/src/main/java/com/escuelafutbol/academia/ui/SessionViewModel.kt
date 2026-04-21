package com.escuelafutbol.academia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.data.local.AcademiaDatabase
import com.escuelafutbol.academia.data.local.entity.SessionCategoriaReciente
import com.escuelafutbol.academia.data.local.entity.SessionParentPortadaJugador
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Referencia mínima para reconciliar la portada de Inicio del padre (sin acoplar a Room/UI de Padres). */
data class HijoPortadaPadreRef(
    val jugadorRemoteId: String,
    val nombre: String,
)

class SessionViewModel(
    private val database: AcademiaDatabase,
    /** UUID de `auth.users` del usuario en sesión; vacío = no persistir. */
    private val authUserId: String,
) : ViewModel() {

    private val prefsDao get() = database.sessionCategoriaRecienteDao()
    private val portadaPadreDao get() = database.sessionParentPortadaJugadorDao()

    private val _filtroCategoria = MutableStateFlow<String?>(null)
    val filtroCategoria: StateFlow<String?> = _filtroCategoria.asStateFlow()

    /**
     * Si no es null: el usuario es coach en nube y solo puede operar en estas categorías (por nombre).
     * `null` = sin restricción por membresía coach.
     */
    private val _categoriasPermitidasOperacion = MutableStateFlow<Set<String>?>(null)
    val categoriasPermitidasOperacion: StateFlow<Set<String>?> =
        _categoriasPermitidasOperacion.asStateFlow()

    /**
     * true mientras hay academia en nube pero aún no llegó el rol a Room (evita mostrar todas las categorías un instante).
     */
    private val _esperandoMembresiaNubeParaSelector = MutableStateFlow(false)
    val esperandoMembresiaNubeParaSelector: StateFlow<Boolean> =
        _esperandoMembresiaNubeParaSelector.asStateFlow()

    /**
     * Padre en nube: `jugador_remote_id` del hijo cuya categoría determina la portada de Inicio.
     * Se hidrata desde Room en [init] y se persiste con [setParentInicioPortadaJugadorRemoteId] / [reconciliarPortadaPadreConHijos].
     */
    private val _parentInicioPortadaJugadorRemoteId = MutableStateFlow<String?>(null)
    val parentInicioPortadaJugadorRemoteId: StateFlow<String?> =
        _parentInicioPortadaJugadorRemoteId.asStateFlow()

    init {
        if (authUserId.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                val row = prefsDao.getForUser(authUserId)
                val saved = row?.categoriaNombre?.trim()?.takeIf { it.isNotEmpty() }
                val rowPortada = portadaPadreDao.getForUser(authUserId)
                val savedPortada =
                    normalizarJugadorRemoteIdParaPortada(rowPortada?.jugadorRemoteId)
                withContext(Dispatchers.Main) {
                    _filtroCategoria.value = saved
                    _parentInicioPortadaJugadorRemoteId.value = savedPortada
                }
            }
        }
    }

    /** true = menú principal e Inicio visibles al abrir la app; categoría por defecto “todas”. */
    private val _enMenuPrincipal = MutableStateFlow(true)
    val enMenuPrincipal: StateFlow<Boolean> = _enMenuPrincipal.asStateFlow()

    /**
     * Evita abrir el selector de categoría (p. ej. tras volver del gestor de archivos del alta de jugador,
     * donde un toque residual puede activar «Cambiar categoría» en la barra superior).
     */
    private val _impideVolverASeleccionCategoria = MutableStateFlow(false)
    val impideVolverASeleccionCategoria: StateFlow<Boolean> =
        _impideVolverASeleccionCategoria.asStateFlow()

    fun setImpideVolverASeleccionCategoria(impide: Boolean) {
        _impideVolverASeleccionCategoria.value = impide
    }

    private fun persistirCategoria(nombre: String?) {
        if (authUserId.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            prefsDao.upsert(SessionCategoriaReciente(userId = authUserId, categoriaNombre = nombre))
        }
    }

    fun confirmarSeleccion(categoria: String?) {
        val v = categoria?.trim()?.takeIf { it.isNotEmpty() }
        _filtroCategoria.value = v
        _enMenuPrincipal.value = true
        persistirCategoria(v)
    }

    fun volverASeleccionCategoria() {
        if (_impideVolverASeleccionCategoria.value) return
        _enMenuPrincipal.value = false
    }

    /** Cierra el selector de categoría y vuelve al menú sin cambiar la categoría activa (atrás del sistema / pestañas). */
    fun cerrarSelectorCategoria() {
        _enMenuPrincipal.value = true
    }

    private fun persistirPortadaPadreJugadorRemoteId(remoteId: String?) {
        if (authUserId.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            portadaPadreDao.upsert(
                SessionParentPortadaJugador(
                    userId = authUserId,
                    jugadorRemoteId = remoteId,
                ),
            )
        }
    }

    fun setParentInicioPortadaJugadorRemoteId(remoteId: String?) {
        val v = normalizarJugadorRemoteIdParaPortada(remoteId)
        _parentInicioPortadaJugadorRemoteId.value = v
        persistirPortadaPadreJugadorRemoteId(v)
    }

    /**
     * Alinea [parentInicioPortadaJugadorRemoteId] con la lista actual de hijos (orden por [HijoPortadaPadreRef.nombre]).
     * Si la selección actual no coincide con ningún hijo → primer hijo y **sustituye** el estado y la fila en Room.
     */
    fun reconciliarPortadaPadreConHijos(hijos: List<HijoPortadaPadreRef>) {
        val nuevo = reconciliarPortadaPadreConHijosLogica(
            seleccionActual = _parentInicioPortadaJugadorRemoteId.value,
            hijos = hijos,
        )
        _parentInicioPortadaJugadorRemoteId.value = nuevo
        persistirPortadaPadreJugadorRemoteId(nuevo)
    }

    fun actualizarRestriccionOperacionCoach(permitidas: Set<String>?, esperandoMembresiaNube: Boolean) {
        _esperandoMembresiaNubeParaSelector.value = esperandoMembresiaNube
        _categoriasPermitidasOperacion.value = permitidas
        val permitidasNorm = permitidas
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toSet()
            ?.takeIf { it.isNotEmpty() }
            ?: return
        val cur = _filtroCategoria.value?.trim()?.takeIf { it.isNotEmpty() } ?: return
        val exact = permitidasNorm.firstOrNull { it.equals(cur, ignoreCase = true) }
        when {
            exact == null -> {
                val replacement = permitidasNorm.minWith(String.CASE_INSENSITIVE_ORDER)
                _filtroCategoria.value = replacement
                persistirCategoria(replacement)
            }
            exact != cur -> {
                _filtroCategoria.value = exact
                persistirCategoria(exact)
            }
        }
    }
}

internal fun normalizarJugadorRemoteIdParaPortada(raw: String?): String? =
    raw?.trim()?.takeIf { it.isNotEmpty() }

/**
 * Lógica pura de reconciliación (tests en `SessionParentPortadaReconciliacionTest`).
 * Devuelve el `jugador_remote_id` que debe quedar tras evaluar la lista actual.
 */
internal fun reconciliarPortadaPadreConHijosLogica(
    seleccionActual: String?,
    hijos: List<HijoPortadaPadreRef>,
): String? {
    val sorted = hijos
        .mapNotNull { h ->
            val id = normalizarJugadorRemoteIdParaPortada(h.jugadorRemoteId) ?: return@mapNotNull null
            HijoPortadaPadreRef(jugadorRemoteId = id, nombre = h.nombre.trim().ifEmpty { "?" })
        }
        .sortedBy { it.nombre.lowercase(Locale.ROOT) }
    if (sorted.isEmpty()) return null
    val cur = normalizarJugadorRemoteIdParaPortada(seleccionActual) ?: return sorted.first().jugadorRemoteId
    val match = sorted.firstOrNull { it.jugadorRemoteId.equals(cur, ignoreCase = true) }
    return match?.jugadorRemoteId ?: sorted.first().jugadorRemoteId
}
