package com.escuelafutbol.academia.ui.players

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.data.local.dao.AcademiaConfigDao
import com.escuelafutbol.academia.data.local.dao.CobroMensualDao
import com.escuelafutbol.academia.data.local.dao.JugadorDao
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.data.remote.AcademiaMiembrosRepository
import com.escuelafutbol.academia.data.remote.JugadorRemoteRepository
import com.escuelafutbol.academia.data.remote.pushCobroMensualSiNube
import com.escuelafutbol.academia.ui.util.etiquetaVisibleDesdeAuthMetadata
import com.escuelafutbol.academia.ui.util.jugadoresActivosFlow
import com.escuelafutbol.academia.util.anioDesdeMillisUtcDia
import io.github.jan.supabase.auth.auth
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject

sealed class FormularioJugadorUi {
    data object Oculto : FormularioJugadorUi()
    data object Alta : FormularioJugadorUi()
    data class Edicion(val jugador: Jugador) : FormularioJugadorUi()
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlayersViewModel(
    application: Application,
    private val jugadorDao: JugadorDao,
    private val cobroMensualDao: CobroMensualDao,
    private val academiaConfigDao: AcademiaConfigDao,
    private val filtroCategoria: StateFlow<String?>,
    categoriasPermitidasOperacion: StateFlow<Set<String>?>,
) : AndroidViewModel(application) {

    private val jugadoresFlow = combine(filtroCategoria, categoriasPermitidasOperacion) { cat, permitidas ->
        Pair(cat, permitidas)
    }
        .flatMapLatest { (cat, permitidas) ->
            jugadoresActivosFlow(jugadorDao, cat, permitidas)
        }

    private val _formularioJugador = MutableStateFlow<FormularioJugadorUi>(FormularioJugadorUi.Oculto)
    val formularioJugador: StateFlow<FormularioJugadorUi> = _formularioJugador.asStateFlow()

    private val _formularioSession = MutableStateFlow(0)
    val formularioSession: StateFlow<Int> = _formularioSession.asStateFlow()

    private val _formCurpDocPath = MutableStateFlow<String?>(null)
    val formCurpDocPath: StateFlow<String?> = _formCurpDocPath.asStateFlow()

    private val _formActaPath = MutableStateFlow<String?>(null)
    val formActaPath: StateFlow<String?> = _formActaPath.asStateFlow()

    fun abrirAltaJugador() {
        _formularioSession.update { it + 1 }
        _formCurpDocPath.value = null
        _formActaPath.value = null
        _formularioJugador.value = FormularioJugadorUi.Alta
    }

    fun abrirEdicionJugador(jugador: Jugador) {
        _formularioSession.update { it + 1 }
        _formCurpDocPath.value = jugador.curpDocumentoRutaAbsoluta?.takeIf { File(it).exists() }
        _formActaPath.value = jugador.actaNacimientoRutaAbsoluta?.takeIf { File(it).exists() }
        _formularioJugador.value = FormularioJugadorUi.Edicion(jugador)
    }

    fun cerrarFormularioJugador() {
        _formularioJugador.value = FormularioJugadorUi.Oculto
    }

    fun setFormCurpDocPath(path: String?) {
        _formCurpDocPath.value = path
    }

    fun setFormActaPath(path: String?) {
        _formActaPath.value = path
    }

    fun aplicarRutaCurpCopiada(nuevaRutaAbsoluta: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _formCurpDocPath.value?.let { runCatching { File(it).delete() } }
            _formCurpDocPath.value = nuevaRutaAbsoluta
        }
    }

    fun aplicarRutaActaCopiada(nuevaRutaAbsoluta: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _formActaPath.value?.let { runCatching { File(it).delete() } }
            _formActaPath.value = nuevaRutaAbsoluta
        }
    }

    val jugadores = jugadoresFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _etiquetasAltaPorUid = MutableStateFlow<Map<String, String>>(emptyMap())
    val etiquetasAltaPorUid: StateFlow<Map<String, String>> = _etiquetasAltaPorUid.asStateFlow()

    init {
        viewModelScope.launch {
            jugadoresFlow.collect { lista ->
                refreshEtiquetasAltaDesdeNube(lista)
            }
        }
    }

    private suspend fun refreshEtiquetasAltaDesdeNube(lista: List<Jugador>) {
        if (lista.none { j ->
                !j.altaPorUserId.isNullOrBlank() && j.altaPorNombre.isNullOrBlank()
            }
        ) {
            _etiquetasAltaPorUid.value = emptyMap()
            return
        }
        val map = withContext(Dispatchers.IO) {
            val app = getApplication<AcademiaApplication>()
            val client = app.supabaseClient ?: return@withContext emptyMap()
            val academiaId = academiaConfigDao.getActual()?.remoteAcademiaId
                ?: return@withContext emptyMap()
            runCatching {
                AcademiaMiembrosRepository(client, app.database).etiquetasAltaPorUsuario(academiaId)
            }.getOrNull().orEmpty()
        }
        _etiquetasAltaPorUid.value = map
    }

    fun historialFlow(jugadorId: Long) = jugadorDao.observeHistorial(jugadorId)

    fun guardarJugador(
        nombre: String,
        categoria: String,
        fechaNacimientoMillis: Long?,
        telefonoTutor: String?,
        emailTutor: String?,
        notas: String?,
        fotoRutaAbsoluta: String?,
        curp: String?,
        curpDocumentoRutaAbsoluta: String?,
        actaNacimientoRutaAbsoluta: String?,
        becado: Boolean,
        mensualidad: Double?,
    ) {
        viewModelScope.launch {
            val ahora = System.currentTimeMillis()
            val anio = fechaNacimientoMillis?.let { anioDesdeMillisUtcDia(it) }
            val cuota = if (becado) null else mensualidad
            val user = getApplication<AcademiaApplication>().supabaseClient
                ?.auth
                ?.currentUserOrNull()
            val altaPor = user?.id?.toString()?.takeIf { it.isNotBlank() }
            val altaPorNombre = etiquetaVisibleDesdeAuthMetadata(
                user?.userMetadata as? JsonObject,
                user?.email,
            )
            withContext(Dispatchers.IO) {
                jugadorDao.insertJugadorConAlta(
                    Jugador(
                        nombre = nombre.trim(),
                        categoria = categoria.trim(),
                        fechaNacimientoMillis = fechaNacimientoMillis,
                        anioNacimiento = anio,
                        telefonoTutor = telefonoTutor?.trim()?.takeIf { it.isNotEmpty() },
                        emailTutor = emailTutor?.trim()?.takeIf { it.isNotEmpty() },
                        notas = notas?.trim()?.takeIf { it.isNotEmpty() },
                        fotoRutaAbsoluta = fotoRutaAbsoluta,
                        curp = curp?.trim()?.uppercase()?.takeIf { it.isNotEmpty() },
                        curpDocumentoRutaAbsoluta = curpDocumentoRutaAbsoluta,
                        actaNacimientoRutaAbsoluta = actaNacimientoRutaAbsoluta,
                        fechaAltaMillis = ahora,
                        activo = true,
                        fechaBajaMillis = null,
                        mensualidad = cuota,
                        becado = becado,
                        altaPorUserId = altaPor,
                        altaPorNombre = altaPorNombre,
                    ),
                )
            }
            cerrarFormularioJugador()
        }
    }

    fun actualizarJugador(
        base: Jugador,
        puedeVerMensualidad: Boolean,
        nombre: String,
        categoria: String,
        fechaNacimientoMillis: Long?,
        telefonoTutor: String?,
        emailTutor: String?,
        notas: String?,
        fotoRutaAbsoluta: String?,
        curp: String?,
        curpDocumentoRutaAbsoluta: String?,
        actaNacimientoRutaAbsoluta: String?,
        becado: Boolean,
        mensualidad: Double?,
    ) {
        viewModelScope.launch {
            val anio = fechaNacimientoMillis?.let { anioDesdeMillisUtcDia(it) }
            withContext(Dispatchers.IO) {
                val current = jugadorDao.getById(base.id) ?: base
                val becadoFinal = if (puedeVerMensualidad) becado else current.becado
                val cuota = if (!puedeVerMensualidad) {
                    current.mensualidad
                } else if (becadoFinal) {
                    null
                } else {
                    mensualidad
                }
                val updated = current.copy(
                    nombre = nombre.trim(),
                    categoria = categoria.trim(),
                    fechaNacimientoMillis = fechaNacimientoMillis,
                    anioNacimiento = anio,
                    telefonoTutor = telefonoTutor?.trim()?.takeIf { it.isNotEmpty() },
                    emailTutor = emailTutor?.trim()?.takeIf { it.isNotEmpty() },
                    notas = notas?.trim()?.takeIf { it.isNotEmpty() },
                    fotoRutaAbsoluta = fotoRutaAbsoluta,
                    curp = curp?.trim()?.uppercase()?.takeIf { it.isNotEmpty() },
                    curpDocumentoRutaAbsoluta = curpDocumentoRutaAbsoluta,
                    actaNacimientoRutaAbsoluta = actaNacimientoRutaAbsoluta,
                    mensualidad = cuota,
                    becado = becadoFinal,
                )
                jugadorDao.update(updated)
                val rid = updated.remoteId
                val client = getApplication<AcademiaApplication>().supabaseClient
                if (!rid.isNullOrBlank() && client != null) {
                    runCatching {
                        JugadorRemoteRepository(client).actualizarCamposJugador(rid, updated)
                    }
                }
                if (becadoFinal && !current.becado) {
                    liquidarPendientesCobrosAlMarcarBecado(updated.id)
                }
            }
            cerrarFormularioJugador()
        }
    }

    /**
     * Al pasar a becado, el esperado de cada mes no puede quedar por encima de lo ya cobrado:
     * se iguala [CobroMensualAlumno.importeEsperado] a [importePagado] para anular pendientes
     * (becado no debe dejar deuda mensual registrada).
     */
    private suspend fun liquidarPendientesCobrosAlMarcarBecado(jugadorId: Long) {
        val app = getApplication<AcademiaApplication>()
        val cobros = cobroMensualDao.getByJugadorId(jugadorId)
        for (cob in cobros) {
            if (cob.importeEsperado <= cob.importePagado) continue
            val ajustado = cob.copy(
                importeEsperado = cob.importePagado,
                needsCloudPush = true,
            )
            cobroMensualDao.update(ajustado)
            pushCobroMensualSiNube(app, academiaConfigDao, jugadorDao, cobroMensualDao, ajustado)
        }
    }

    fun darBaja(jugador: Jugador) {
        viewModelScope.launch {
            jugadorDao.darBaja(jugador, System.currentTimeMillis(), null)
        }
    }
}
