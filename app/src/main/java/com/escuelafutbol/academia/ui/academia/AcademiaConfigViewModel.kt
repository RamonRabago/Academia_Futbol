package com.escuelafutbol.academia.ui.academia

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.data.local.AcademiaDatabase
import com.escuelafutbol.academia.data.remote.dto.RegenerateInviteCodesResult
import com.escuelafutbol.academia.data.sync.AcademiaCloudSync
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.model.puedeMutarDiaLimitePagoMes
import com.escuelafutbol.academia.data.local.security.StaffPinHasher
import com.escuelafutbol.academia.ui.theme.normalizeBrandColorHex
import io.github.jan.supabase.auth.auth
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

    private suspend fun puedeMutarConfigAcademiaAdmin(): Boolean {
        val actual = database.academiaConfigDao().getActual() ?: return true
        if (actual.remoteAcademiaId == null) return true
        return actual.academiaGestionNubePermitida
    }

    val config = database.academiaConfigDao().observe()
        .map { it ?: AcademiaConfig.DEFAULT }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AcademiaConfig.DEFAULT)

    fun guardarColoresTema(primarioInput: String?, secundarioInput: String?) {
        viewModelScope.launch {
            if (!puedeMutarConfigAcademiaAdmin()) return@launch
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
            if (!puedeMutarConfigAcademiaAdmin()) return@launch
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

    fun guardarNombre(nombre: String, onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            if (!puedeMutarConfigAcademiaAdmin()) {
                onResult?.invoke(false)
                return@launch
            }
            val dao = database.academiaConfigDao()
            val actual = dao.getActual() ?: AcademiaConfig.DEFAULT
            val trimmed = nombre.trim()
            dao.upsert(actual.copy(nombreAcademia = trimmed))
            val aid = actual.remoteAcademiaId
            if (aid != null) {
                val app = getApplication<Application>() as AcademiaApplication
                app.supabaseClient?.let { client ->
                    AcademiaCloudSync(client, database).pushAcademiaNombre(aid)
                }
            }
            onResult?.invoke(true)
        }
    }

    /** Genera o renueva los tres códigos de invitación (entrenador, coordinador, padre) en Supabase. */
    fun regenerarCodigosInvitacion(onResult: (Result<RegenerateInviteCodesResult>) -> Unit = {}) {
        viewModelScope.launch {
            if (!puedeMutarConfigAcademiaAdmin()) {
                onResult(Result.failure(Exception("Sin permiso para administrar la academia.")))
                return@launch
            }
            val app = getApplication<Application>() as AcademiaApplication
            val client = app.supabaseClient ?: run {
                onResult(Result.failure(Exception("Supabase no configurado.")))
                return@launch
            }
            val aid = database.academiaConfigDao().getActual()?.remoteAcademiaId ?: run {
                onResult(Result.failure(Exception("Sincroniza primero con la nube.")))
                return@launch
            }
            onResult(AcademiaCloudSync(client, database).regenerateAcademiaInviteCodes(aid))
        }
    }

    fun guardarLogo(uri: Uri) {
        viewModelScope.launch {
            if (!puedeMutarConfigAcademiaAdmin()) return@launch
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
            if (!puedeMutarConfigAcademiaAdmin()) return@launch
            val dao = database.academiaConfigDao()
            val actual = dao.getActual() ?: AcademiaConfig.DEFAULT
            actual.logoRutaAbsoluta?.let { runCatching { File(it).delete() } }
            dao.upsert(actual.copy(logoRutaAbsoluta = null, logoUrlSupabase = null))
        }
    }

    fun guardarPortada(uri: Uri) {
        viewModelScope.launch {
            if (!puedeMutarConfigAcademiaAdmin()) return@launch
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
            if (!puedeMutarConfigAcademiaAdmin()) return@launch
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
            if (!puedeMutarConfigAcademiaAdmin()) return@launch
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

    /**
     * [diaDelMes] en 1..28 o null para quitar la regla. Valores fuera de rango se ignoran.
     * [onResult] true si se persistió en Room; false si no hay permiso de administración.
     */
    fun guardarDiaLimitePagoMes(diaDelMes: Int?, onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val dao = database.academiaConfigDao()
            val actual = dao.getActual() ?: AcademiaConfig.DEFAULT
            val uid = (getApplication<Application>() as AcademiaApplication).supabaseClient
                ?.auth?.currentUserOrNull()?.id?.toString()
            if (!actual.puedeMutarDiaLimitePagoMes(uid)) {
                onResult?.invoke(false)
                return@launch
            }
            val normalizado = diaDelMes?.takeIf { it in 1..28 }
            dao.upsert(actual.copy(diaLimitePagoMes = normalizado))
            val aid = actual.remoteAcademiaId
            if (aid != null) {
                val app = getApplication<Application>() as AcademiaApplication
                app.supabaseClient?.let { client ->
                    AcademiaCloudSync(client, database).pushAcademiaDiaLimitePago(aid)
                }
            }
            onResult?.invoke(true)
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
