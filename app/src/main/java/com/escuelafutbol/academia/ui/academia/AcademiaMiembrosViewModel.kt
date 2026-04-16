package com.escuelafutbol.academia.ui.academia

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.data.local.AcademiaDatabase
import com.escuelafutbol.academia.data.remote.AcademiaMiembrosRepository
import com.escuelafutbol.academia.data.remote.PadresAlumnosRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Locale

data class PadresVinculoUi(
    val linkId: String,
    val jugadorRemoteId: String,
    val jugadorNombre: String,
)

/** Opción para vincular tutor ↔ alumno (gestión de miembros). */
data class JugadorOpcionVinculoUi(
    val remoteId: String,
    val nombre: String,
    val categoria: String,
)

data class MiembroAdminUi(
    val id: String,
    val userId: String,
    /** Últimos 8 caracteres del UUID para identificar sin email. */
    val userIdCorto: String,
    /** Nombre (metadata Auth) o correo; null si solo hay fallback de tabla. */
    val displayLabel: String? = null,
    val memberEmail: String? = null,
    val rol: String,
    val activo: Boolean,
    val esDueñoCuentaAcademia: Boolean,
    val nombresCategoriasCoach: List<String>,
    val categoriaRemoteIds: List<String>,
    /** Categorías de alumnos vinculados (solo rol `parent`), desde Room por `remoteId` del jugador. */
    val categoriasDesdeHijos: List<String> = emptyList(),
    /** Alta en el club (`academia_miembros.created_at`), millis UTC. */
    val fechaAltaClubMillis: Long? = null,
)

class AcademiaMiembrosViewModel(
    application: Application,
    private val database: AcademiaDatabase,
) : AndroidViewModel(application) {

    private val repo: AcademiaMiembrosRepository?
        get() {
            val c = (getApplication<Application>() as AcademiaApplication).supabaseClient ?: return null
            return AcademiaMiembrosRepository(c, database)
        }

    private val padresRepo: PadresAlumnosRepository?
        get() {
            val c = (getApplication<Application>() as AcademiaApplication).supabaseClient ?: return null
            return PadresAlumnosRepository(c)
        }

    private val _uiState = MutableStateFlow<MiembrosAdminState>(MiembrosAdminState.Idle)
    val uiState: StateFlow<MiembrosAdminState> = _uiState.asStateFlow()

    private val _items = MutableStateFlow<List<MiembroAdminUi>>(emptyList())
    val items: StateFlow<List<MiembroAdminUi>> = _items.asStateFlow()

    fun cargar(academiaId: String) {
        val r = repo ?: run {
            _uiState.value = MiembrosAdminState.Error("Supabase no configurado.")
            return
        }
        viewModelScope.launch {
            _uiState.value = MiembrosAdminState.Cargando
            runCatching {
                val ownerUid = r.getAcademiaOwnerUserId(academiaId)
                val rows = r.listMiembros(academiaId)
                val dao = database.jugadorDao()
                val pRepo = padresRepo
                val ui = rows.map { row ->
                    val catIds = if (row.rol.trim().equals("coach", ignoreCase = true)) {
                        r.getCategoriaIdsForMiembro(row.id)
                    } else {
                        emptyList()
                    }
                    val nombres = if (catIds.isEmpty()) {
                        emptyList()
                    } else {
                        val map = r.nombresCategoriasPorIds(academiaId, catIds)
                        catIds.mapNotNull { map[it] }.sorted()
                    }
                    MiembroAdminUi(
                        id = row.id,
                        userId = row.userId,
                        userIdCorto = row.userId.takeLast(8).uppercase(Locale.ROOT),
                        displayLabel = row.displayLabel?.trim()?.takeIf { it.isNotEmpty() },
                        memberEmail = row.memberEmail?.trim()?.takeIf { it.isNotEmpty() },
                        rol = row.rol.trim().lowercase(Locale.ROOT),
                        activo = row.activo,
                        esDueñoCuentaAcademia = ownerUid != null && row.userId == ownerUid,
                        nombresCategoriasCoach = nombres,
                        categoriaRemoteIds = catIds,
                        categoriasDesdeHijos = emptyList(),
                        fechaAltaClubMillis = millisDesdeCreatedAtIso(row.createdAt),
                    )
                }
                val soloPadres = ui.filter { it.rol == "parent" }
                val conCategoriasHijos = withContext(Dispatchers.IO) {
                    soloPadres.map { m ->
                        val cats = if (pRepo != null) {
                            runCatching {
                                val links = pRepo.listVinculos(academiaId, m.userId)
                                links.flatMap { link ->
                                    val j = dao.getJugadorPorRemoteId(link.jugadorId)
                                    listOfNotNull(j?.categoria?.trim()?.takeIf { it.isNotEmpty() })
                                }
                                    .distinctBy { it.lowercase(Locale.getDefault()) }
                                    .sortedBy { it.lowercase(Locale.getDefault()) }
                            }.getOrElse { emptyList() }
                        } else {
                            emptyList()
                        }
                        m.copy(categoriasDesdeHijos = cats)
                    }
                }
                _items.value = conCategoriasHijos
                _uiState.value = MiembrosAdminState.Listo
            }.onFailure { e ->
                _uiState.value = MiembrosAdminState.Error(e.message ?: e.toString())
            }
        }
    }

    fun setActivo(miembroId: String, activo: Boolean, academiaId: String, onDone: (Result<Unit>) -> Unit) {
        val r = repo ?: run {
            onDone(Result.failure(Exception("Supabase no configurado.")))
            return
        }
        viewModelScope.launch {
            val res = runCatching {
                r.setMiembroActivo(miembroId, activo)
                Unit
            }
            onDone(res)
            if (res.isSuccess) cargar(academiaId)
        }
    }

    fun setRol(miembroId: String, rol: String, academiaId: String, onDone: (Result<Unit>) -> Unit) {
        val r = repo ?: run {
            onDone(Result.failure(Exception("Supabase no configurado.")))
            return
        }
        val norm = rol.trim().lowercase(Locale.ROOT)
        if (norm !in ROLES_ASIGNABLES) {
            onDone(Result.failure(Exception("Rol no permitido.")))
            return
        }
        viewModelScope.launch {
            val res = runCatching {
                r.setMiembroRol(miembroId, norm)
                Unit
            }
            onDone(res)
            if (res.isSuccess) cargar(academiaId)
        }
    }

    fun guardarCategoriasCoach(
        miembroId: String,
        academiaId: String,
        categoriaRemoteIds: List<String>,
        onDone: (Result<Unit>) -> Unit,
    ) {
        val r = repo ?: run {
            onDone(Result.failure(Exception("Supabase no configurado.")))
            return
        }
        viewModelScope.launch {
            val res = runCatching {
                r.replaceMiembroCategorias(miembroId, categoriaRemoteIds)
                Unit
            }
            onDone(res)
            if (res.isSuccess) cargar(academiaId)
        }
    }

    fun eliminarMiembro(miembroId: String, academiaId: String, onDone: (Result<Unit>) -> Unit) {
        val r = repo ?: run {
            onDone(Result.failure(Exception("Supabase no configurado.")))
            return
        }
        viewModelScope.launch {
            val res = runCatching {
                r.deleteMiembro(miembroId)
                Unit
            }
            onDone(res)
            if (res.isSuccess) cargar(academiaId)
        }
    }

    suspend fun categoriasSeleccionables(): List<Pair<String, String>> {
        return repo?.categoriasConRemotoParaUi().orEmpty()
    }

    suspend fun listarVinculosPadre(academiaId: String, parentUserId: String): List<PadresVinculoUi> {
        val p = padresRepo ?: return emptyList()
        val dao = database.jugadorDao()
        return p.listVinculos(academiaId, parentUserId).map { row ->
            val nombre = dao.getJugadorPorRemoteId(row.jugadorId)?.nombre
                ?: row.jugadorId.takeLast(8).uppercase(Locale.ROOT)
            PadresVinculoUi(row.id, row.jugadorId, nombre)
        }
    }

    suspend fun jugadoresDisponiblesParaVincular(academiaId: String, parentUserId: String): List<JugadorOpcionVinculoUi> {
        val p = padresRepo ?: return emptyList()
        return p.listJugadoresDisponiblesParaVinculoPadreStaff(academiaId, parentUserId)
            .sortedWith(compareBy({ it.categoria }, { it.nombre }))
            .map { row ->
                JugadorOpcionVinculoUi(
                    remoteId = row.id,
                    nombre = row.nombre,
                    categoria = row.categoria,
                )
            }
    }

    fun agregarVinculoPadre(
        academiaId: String,
        parentUserId: String,
        jugadorRemoteId: String,
        onDone: (Result<Unit>) -> Unit,
    ) {
        val p = padresRepo ?: run {
            onDone(Result.failure(Exception("Supabase no configurado.")))
            return
        }
        viewModelScope.launch {
            val res = runCatching {
                p.insertVinculo(academiaId, parentUserId, jugadorRemoteId)
                Unit
            }
            onDone(res)
        }
    }

    fun quitarVinculoPadre(linkId: String, onDone: (Result<Unit>) -> Unit) {
        val p = padresRepo ?: run {
            onDone(Result.failure(Exception("Supabase no configurado.")))
            return
        }
        viewModelScope.launch {
            val res = runCatching {
                p.deleteVinculo(linkId)
                Unit
            }
            onDone(res)
        }
    }

    companion object {
        /** Roles editables desde la app (no `owner`: reservado / tabla dueño). */
        val ROLES_ASIGNABLES = setOf("coach", "coordinator", "parent", "admin")
    }
}

sealed class MiembrosAdminState {
    data object Idle : MiembrosAdminState()
    data object Cargando : MiembrosAdminState()
    data object Listo : MiembrosAdminState()
    data class Error(val mensaje: String) : MiembrosAdminState()
}

/** ISO 8601 desde PostgREST (`timestamptz`). */
private fun millisDesdeCreatedAtIso(iso: String?): Long? {
    val s = iso?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return runCatching { Instant.parse(s).toEpochMilli() }.getOrNull()
}
