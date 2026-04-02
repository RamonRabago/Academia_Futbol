package com.escuelafutbol.academia.ui.academia

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.data.local.AcademiaDatabase
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.model.RolDispositivo
import com.escuelafutbol.academia.data.local.security.StaffPinHasher
import com.escuelafutbol.academia.ui.theme.normalizeBrandColorHex
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AcademiaConfigViewModel(
    application: Application,
    private val database: AcademiaDatabase,
) : AndroidViewModel(application) {

    val config = database.academiaConfigDao().observe()
        .map { it ?: AcademiaConfig.DEFAULT }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AcademiaConfig.DEFAULT)

    fun guardarColoresTema(primarioInput: String?, secundarioInput: String?) {
        viewModelScope.launch {
            val dao = database.academiaConfigDao()
            val actual = dao.getActual() ?: AcademiaConfig.DEFAULT
            val pRaw = primarioInput?.trim()
            val sRaw = secundarioInput?.trim()
            val p = pRaw?.takeIf { it.isNotEmpty() }?.let { normalizeBrandColorHex(it) }
            val s = sRaw?.takeIf { it.isNotEmpty() }?.let { normalizeBrandColorHex(it) }
            if (!pRaw.isNullOrEmpty() && p == null) return@launch
            if (!sRaw.isNullOrEmpty() && s == null) return@launch
            dao.upsert(
                actual.copy(
                    temaColorPrimarioHex = p,
                    temaColorSecundarioHex = s,
                ),
            )
        }
    }

    fun restaurarColoresTemaPorDefecto() {
        viewModelScope.launch {
            val dao = database.academiaConfigDao()
            val actual = dao.getActual() ?: AcademiaConfig.DEFAULT
            dao.upsert(
                actual.copy(
                    temaColorPrimarioHex = null,
                    temaColorSecundarioHex = null,
                ),
            )
        }
    }

    fun guardarNombre(nombre: String) {
        viewModelScope.launch {
            val dao = database.academiaConfigDao()
            val actual = dao.getActual() ?: AcademiaConfig.DEFAULT
            dao.upsert(actual.copy(nombreAcademia = nombre.trim()))
        }
    }

    fun guardarLogo(uri: Uri) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val dest = File(app.filesDir, "academy_logo")
            withContext(Dispatchers.IO) {
                runCatching {
                    app.contentResolver.openInputStream(uri)?.use { input ->
                        dest.outputStream().use { out -> input.copyTo(out) }
                    }
                }
            }
            val dao = database.academiaConfigDao()
            val actual = dao.getActual() ?: AcademiaConfig.DEFAULT
            dao.upsert(actual.copy(logoRutaAbsoluta = dest.absolutePath))
        }
    }

    fun quitarLogo() {
        viewModelScope.launch {
            val dao = database.academiaConfigDao()
            val actual = dao.getActual() ?: AcademiaConfig.DEFAULT
            actual.logoRutaAbsoluta?.let { runCatching { File(it).delete() } }
            dao.upsert(actual.copy(logoRutaAbsoluta = null, logoUrlSupabase = null))
        }
    }

    fun guardarPortada(uri: Uri) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val dest = File(app.filesDir, "academy_portada")
            withContext(Dispatchers.IO) {
                runCatching {
                    app.contentResolver.openInputStream(uri)?.use { input ->
                        dest.outputStream().use { out -> input.copyTo(out) }
                    }
                }
            }
            val dao = database.academiaConfigDao()
            val actual = dao.getActual() ?: AcademiaConfig.DEFAULT
            dao.upsert(actual.copy(portadaRutaAbsoluta = dest.absolutePath))
        }
    }

    fun quitarPortada() {
        viewModelScope.launch {
            val dao = database.academiaConfigDao()
            val actual = dao.getActual() ?: AcademiaConfig.DEFAULT
            actual.portadaRutaAbsoluta?.let { runCatching { File(it).delete() } }
            dao.upsert(actual.copy(portadaRutaAbsoluta = null, portadaUrlSupabase = null))
        }
    }

    fun guardarPermisosMensualidad(
        visibleProfesor: Boolean,
        visibleCoordinador: Boolean,
        visibleDueno: Boolean,
    ) {
        viewModelScope.launch {
            val dao = database.academiaConfigDao()
            val actual = dao.getActual() ?: AcademiaConfig.DEFAULT
            dao.upsert(
                actual.copy(
                    mensualidadVisibleProfesor = visibleProfesor,
                    mensualidadVisibleCoordinador = visibleCoordinador,
                    mensualidadVisibleDueno = visibleDueno,
                ),
            )
        }
    }

    fun guardarRolDispositivo(rol: RolDispositivo) {
        viewModelScope.launch {
            val dao = database.academiaConfigDao()
            val actual = dao.getActual() ?: AcademiaConfig.DEFAULT
            dao.upsert(actual.copy(rolDispositivo = rol.name))
        }
    }

    suspend fun intentarGuardarPinNuevo(primero: String, segundo: String): Boolean {
        if (primero != segundo || !StaffPinHasher.pinValido(primero)) return false
        return withContext(Dispatchers.IO) {
            val dao = database.academiaConfigDao()
            val actual = dao.getActual() ?: AcademiaConfig.DEFAULT
            dao.upsert(actual.copy(pinStaffHash = StaffPinHasher.hash(primero)))
            true
        }
    }

    suspend fun intentarVerificarPin(pin: String): Boolean =
        withContext(Dispatchers.IO) {
            val actual = database.academiaConfigDao().getActual() ?: return@withContext false
            val h = actual.pinStaffHash ?: return@withContext false
            h == StaffPinHasher.hash(pin)
        }

    suspend fun intentarCambiarPin(viejo: String, nuevo: String, nuevo2: String): Boolean {
        if (nuevo != nuevo2 || !StaffPinHasher.pinValido(nuevo)) return false
        return withContext(Dispatchers.IO) {
            val dao = database.academiaConfigDao()
            val actual = dao.getActual() ?: return@withContext false
            val h = actual.pinStaffHash ?: return@withContext false
            if (h != StaffPinHasher.hash(viejo)) return@withContext false
            dao.upsert(actual.copy(pinStaffHash = StaffPinHasher.hash(nuevo)))
            true
        }
    }
}
