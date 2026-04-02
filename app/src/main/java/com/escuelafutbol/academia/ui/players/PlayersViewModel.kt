package com.escuelafutbol.academia.ui.players

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.data.local.dao.JugadorDao
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.util.anioDesdeMillisUtcDia
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlayersViewModel(
    application: Application,
    private val jugadorDao: JugadorDao,
    private val filtroCategoria: StateFlow<String?>,
) : AndroidViewModel(application) {

    val jugadores = filtroCategoria
        .flatMapLatest { cat ->
            if (cat == null) jugadorDao.observeAll() else jugadorDao.observeByCategoria(cat)
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
        }
    }

    fun darBaja(jugador: Jugador) {
        viewModelScope.launch {
            jugadorDao.darBaja(jugador, System.currentTimeMillis(), null)
        }
    }
}
