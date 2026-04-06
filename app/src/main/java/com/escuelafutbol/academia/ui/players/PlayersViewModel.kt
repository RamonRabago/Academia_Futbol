package com.escuelafutbol.academia.ui.players

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.data.local.dao.JugadorDao
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.ui.util.jugadoresActivosFlow
import com.escuelafutbol.academia.util.anioDesdeMillisUtcDia
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlayersViewModel(
    application: Application,
    private val jugadorDao: JugadorDao,
    private val filtroCategoria: StateFlow<String?>,
    categoriasPermitidasOperacion: StateFlow<Set<String>?>,
) : AndroidViewModel(application) {

    /**
     * Estado del formulario de alta en el ViewModel (no en `rememberSaveable`) para que sobreviva
     * a recreación de actividad / recomposiciones al volver del selector de archivos (CURP, acta).
     */
    private val _mostrarAltaJugador = MutableStateFlow(false)
    val mostrarAltaJugador: StateFlow<Boolean> = _mostrarAltaJugador.asStateFlow()

    private val _altaFormSession = MutableStateFlow(0)
    val altaFormSession: StateFlow<Int> = _altaFormSession.asStateFlow()

    private val _altaCurpDocPath = MutableStateFlow<String?>(null)
    val altaCurpDocPath: StateFlow<String?> = _altaCurpDocPath.asStateFlow()

    private val _altaActaPath = MutableStateFlow<String?>(null)
    val altaActaPath: StateFlow<String?> = _altaActaPath.asStateFlow()

    fun abrirAltaJugador() {
        _altaFormSession.update { it + 1 }
        _altaCurpDocPath.value = null
        _altaActaPath.value = null
        _mostrarAltaJugador.value = true
    }

    fun cerrarAltaJugador() {
        _mostrarAltaJugador.value = false
    }

    fun setAltaCurpDocPath(path: String?) {
        _altaCurpDocPath.value = path
    }

    fun setAltaActaPath(path: String?) {
        _altaActaPath.value = path
    }

    /** Tras copiar desde un Uri: borra el archivo anterior si había y asigna la nueva ruta (en IO). */
    fun aplicarRutaCurpCopiada(nuevaRutaAbsoluta: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _altaCurpDocPath.value?.let { runCatching { File(it).delete() } }
            _altaCurpDocPath.value = nuevaRutaAbsoluta
        }
    }

    fun aplicarRutaActaCopiada(nuevaRutaAbsoluta: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _altaActaPath.value?.let { runCatching { File(it).delete() } }
            _altaActaPath.value = nuevaRutaAbsoluta
        }
    }

    val jugadores = combine(filtroCategoria, categoriasPermitidasOperacion) { cat, permitidas ->
        Pair(cat, permitidas)
    }
        .flatMapLatest { (cat, permitidas) ->
            jugadoresActivosFlow(jugadorDao, cat, permitidas)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
                ),
            )
            cerrarAltaJugador()
        }
    }

    fun darBaja(jugador: Jugador) {
        viewModelScope.launch {
            jugadorDao.darBaja(jugador, System.currentTimeMillis(), null)
        }
    }
}
