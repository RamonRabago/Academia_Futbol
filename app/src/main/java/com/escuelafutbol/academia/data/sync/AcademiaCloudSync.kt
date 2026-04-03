package com.escuelafutbol.academia.data.sync

import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.Asistencia
import com.escuelafutbol.academia.data.local.entity.Categoria
import com.escuelafutbol.academia.data.local.entity.StaffCategoria
import com.escuelafutbol.academia.data.local.entity.JugadorHistorial
import com.escuelafutbol.academia.data.local.AcademiaDatabase
import com.escuelafutbol.academia.data.local.dao.AcademiaConfigDao
import com.escuelafutbol.academia.data.remote.dto.AcademiaCodigoClubPatch
import com.escuelafutbol.academia.data.remote.dto.AcademiaColoresPatch
import com.escuelafutbol.academia.data.remote.dto.AcademiaInsert
import com.escuelafutbol.academia.data.remote.dto.AcademiaLogoUrlPatch
import com.escuelafutbol.academia.data.remote.dto.AcademiaMiembroRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaNombrePatch
import com.escuelafutbol.academia.data.remote.dto.AcademiaPortadaUrlPatch
import com.escuelafutbol.academia.data.remote.dto.AcademiaRow
import com.escuelafutbol.academia.data.remote.dto.AsistenciaRow
import com.escuelafutbol.academia.data.remote.dto.CategoriaInsert
import com.escuelafutbol.academia.data.remote.dto.CategoriaPortadaUrlPatch
import com.escuelafutbol.academia.data.remote.dto.CategoriaRow
import com.escuelafutbol.academia.data.remote.dto.HistorialRow
import com.escuelafutbol.academia.data.remote.dto.JugadorActaUrlPatch
import com.escuelafutbol.academia.data.remote.dto.JugadorCurpDocUrlPatch
import com.escuelafutbol.academia.data.remote.dto.JugadorFotoUrlPatch
import com.escuelafutbol.academia.data.remote.dto.JugadorRow
import com.escuelafutbol.academia.data.remote.dto.StaffCategoriaInsert
import com.escuelafutbol.academia.data.remote.dto.StaffCategoriaRow
import com.escuelafutbol.academia.data.remote.dto.StaffFotoUrlPatch
import com.escuelafutbol.academia.data.remote.dto.StaffRow
import com.escuelafutbol.academia.data.remote.dto.toCloudInsert
import com.escuelafutbol.academia.data.remote.dto.toLocalMerged
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import java.io.File
import java.util.Locale
import kotlin.random.Random
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Sincronización: sube filas sin [remoteId], sube imágenes locales al bucket Storage `academia-media`,
 * actualiza `foto_url` / `logo_url` / `portada_url` en PostgREST y descarga cambios.
 */
class AcademiaCloudSync(
    private val client: SupabaseClient,
    private val db: AcademiaDatabase,
) {

    suspend fun syncAll(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val uid = client.auth.currentUserOrNull()?.id?.toString()
                ?: error("No hay sesión. Inicia sesión antes de sincronizar.")
            val academiaId = ensureAcademiaIdForSync(uid)
            pushAcademiaNombre(academiaId)
            pushAcademiaThemeColors(academiaId)
            pushAcademiaMedia(uid, academiaId)
            pushCategorias(academiaId)
            pushCategoriaPortadas(uid, academiaId)
            pushJugadores(academiaId)
            pushJugadorFotosLocales(uid, academiaId)
            pushHistorial(academiaId)
            pushAsistencias(academiaId)
            pushStaff(academiaId)
            pushStaffFotosLocales(uid, academiaId)
            pushStaffCategorias(academiaId)
            pullCategorias(academiaId)
            pullJugadores(academiaId)
            val jugadorMap = buildJugadorRemoteMap()
            pullHistorial(academiaId, jugadorMap)
            pullAsistencias(academiaId, jugadorMap)
            pullStaff(academiaId)
            pullStaffCategorias(academiaId)
            pullAcademiaConfig(academiaId)
        }
    }

    /**
     * Resuelve dueño, caché válida o membresías. No crea academia ni filas de miembro.
     */
    suspend fun resolveAcademiaBinding(uid: String): AcademiaBindingResult {
        val dao = db.academiaConfigDao()
        var cfg = dao.getActual() ?: AcademiaConfig.DEFAULT

        val cached = cfg.remoteAcademiaId
        if (cached != null) {
            if (userCanAccessAcademia(uid, cached)) {
                return AcademiaBindingResult.Ok(cached)
            }
            dao.upsert(
                cfg.copy(
                    remoteAcademiaId = null,
                    codigoClubRemoto = null,
                    academiaGestionNubePermitida = true,
                ),
            )
            cfg = dao.getActual() ?: AcademiaConfig.DEFAULT
        }

        val owned = client.from("academias").select {
            filter { eq("user_id", uid) }
        }.decodeList<AcademiaRow>()

        if (owned.isNotEmpty()) {
            val row = owned.first()
            mergeAcademiaRowIntoLocal(dao, cfg, row)
            return AcademiaBindingResult.Ok(row.id)
        }

        val members = client.from("academia_miembros").select {
            filter {
                eq("user_id", uid)
                eq("activo", true)
            }
        }.decodeList<AcademiaMiembroRow>()

        return when {
            members.isEmpty() -> AcademiaBindingResult.NeedsOnboarding
            members.size == 1 -> {
                val aid = members.first().academiaId
                val row = client.from("academias").select {
                    filter { eq("id", aid) }
                }.decodeSingle<AcademiaRow>()
                mergeAcademiaRowIntoLocal(dao, cfg, row)
                AcademiaBindingResult.Ok(aid)
            }
            else -> {
                val options = members.mapNotNull { m ->
                    runCatching {
                        val a = client.from("academias").select {
                            filter { eq("id", m.academiaId) }
                        }.decodeSingle<AcademiaRow>()
                        AcademiaBindingOption(m.academiaId, a.nombre, m.rol)
                    }.getOrNull()
                }
                AcademiaBindingResult.PickAcademy(options)
            }
        }
    }

    suspend fun createOwnedAcademia(uid: String): Result<String> = runCatching {
        val dao = db.academiaConfigDao()
        var cfg = dao.getActual() ?: AcademiaConfig.DEFAULT
        val existing = client.from("academias").select {
            filter { eq("user_id", uid) }
        }.decodeList<AcademiaRow>()
        if (existing.isNotEmpty()) {
            val row = existing.first()
            mergeAcademiaRowIntoLocal(dao, cfg, row)
            return@runCatching row.id
        }
        val insert = AcademiaInsert(
            userId = uid,
            nombre = cfg.nombreAcademia,
            logoUrl = null,
            portadaUrl = null,
            mensualidadVisibleProfesor = cfg.mensualidadVisibleProfesor,
            mensualidadVisibleCoordinador = cfg.mensualidadVisibleCoordinador,
            mensualidadVisibleDueno = cfg.mensualidadVisibleDueno,
            rolDispositivo = cfg.rolDispositivo,
            pinStaffHash = cfg.pinStaffHash,
            colorPrimarioHex = cfg.temaColorPrimarioHex,
            colorSecundarioHex = cfg.temaColorSecundarioHex,
        )
        val row = client.from("academias").insert(insert) {
            select()
        }.decodeSingle<AcademiaRow>()
        mergeAcademiaRowIntoLocal(dao, cfg, row)
        row.id
    }

    suspend fun joinAcademiaByCode(code: String, rolMembresia: String): Result<String> = runCatching {
        val normalizedRol = rolMembresia.trim().lowercase()
        val params = buildJsonObject {
            put("p_codigo", code.trim().uppercase())
            put("p_rol", normalizedRol)
        }
        val result = client.postgrest.rpc("join_academia_by_code", params)
        val aid = result.decodeAs<String>()
        bindAcademiaIdAndPullConfig(aid)
        aid
    }

    suspend fun bindAcademiaIdAndPullConfig(academiaId: String) {
        val dao = db.academiaConfigDao()
        val cfg = dao.getActual() ?: AcademiaConfig.DEFAULT
        val row = client.from("academias").select {
            filter { eq("id", academiaId) }
        }.decodeSingle<AcademiaRow>()
        mergeAcademiaRowIntoLocal(dao, cfg, row)
    }

    suspend fun regenerateClubCode(academiaId: String): Result<String> = runCatching {
        val charset = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        repeat(15) {
            val code = buildString(6) {
                repeat(6) {
                    append(charset[Random.nextInt(charset.length)])
                }
            }
            runCatching {
                client.from("academias").update(AcademiaCodigoClubPatch(code)) {
                    filter { eq("id", academiaId) }
                }
            }.onSuccess {
                val dao = db.academiaConfigDao()
                val cfg = dao.getActual() ?: AcademiaConfig.DEFAULT
                dao.upsert(cfg.copy(codigoClubRemoto = code))
                return@runCatching code
            }
        }
        error("no_unique_club_code")
    }

    /** Dueño de la fila `academias` o miembro activo con rol owner/admin (PostgREST). */
    private suspend fun computeAcademiaGestionNubePermitida(uid: String, row: AcademiaRow): Boolean {
        if (row.userId == uid) return true
        val members = client.from("academia_miembros").select {
            filter {
                eq("academia_id", row.id)
                eq("user_id", uid)
                eq("activo", true)
            }
        }.decodeList<AcademiaMiembroRow>()
        val r = members.firstOrNull()?.rol?.trim()?.lowercase(Locale.ROOT) ?: return false
        return r == "owner" || r == "admin"
    }

    private suspend fun userCanAccessAcademia(uid: String, academiaId: String): Boolean {
        val asOwner = client.from("academias").select {
            filter {
                eq("id", academiaId)
                eq("user_id", uid)
            }
        }.decodeList<AcademiaRow>().isNotEmpty()
        if (asOwner) return true
        return client.from("academia_miembros").select {
            filter {
                eq("academia_id", academiaId)
                eq("user_id", uid)
                eq("activo", true)
            }
        }.decodeList<AcademiaMiembroRow>().isNotEmpty()
    }

    private suspend fun mergeAcademiaRowIntoLocal(
        dao: AcademiaConfigDao,
        cfg: AcademiaConfig,
        row: AcademiaRow,
    ) {
        val uid = client.auth.currentUserOrNull()?.id?.toString()
        val puedeGestionar = uid?.let { computeAcademiaGestionNubePermitida(it, row) }
            ?: cfg.academiaGestionNubePermitida
        dao.upsert(
            cfg.copy(
                remoteAcademiaId = row.id,
                nombreAcademia = row.nombre.ifBlank { cfg.nombreAcademia },
                logoUrlSupabase = row.logoUrl?.takeIf { it.isNotBlank() } ?: cfg.logoUrlSupabase,
                portadaUrlSupabase = row.portadaUrl?.takeIf { it.isNotBlank() } ?: cfg.portadaUrlSupabase,
                mensualidadVisibleProfesor = row.mensualidadVisibleProfesor,
                mensualidadVisibleCoordinador = row.mensualidadVisibleCoordinador,
                mensualidadVisibleDueno = row.mensualidadVisibleDueno,
                rolDispositivo = row.rolDispositivo.ifBlank { cfg.rolDispositivo },
                pinStaffHash = row.pinStaffHash ?: cfg.pinStaffHash,
                temaColorPrimarioHex = row.colorPrimarioHex?.takeIf { it.isNotBlank() }
                    ?: cfg.temaColorPrimarioHex,
                temaColorSecundarioHex = row.colorSecundarioHex?.takeIf { it.isNotBlank() }
                    ?: cfg.temaColorSecundarioHex,
                codigoClubRemoto = row.codigoClub?.takeIf { it.isNotBlank() } ?: cfg.codigoClubRemoto,
                academiaGestionNubePermitida = puedeGestionar,
            ),
        )
    }

    private suspend fun ensureAcademiaIdForSync(uid: String): String {
        when (val r = resolveAcademiaBinding(uid)) {
            is AcademiaBindingResult.Ok -> return r.academiaId
            AcademiaBindingResult.NeedsOnboarding -> error("NEEDS_ACADEMY_ONBOARDING")
            is AcademiaBindingResult.PickAcademy -> error("NEEDS_ACADEMY_PICK")
        }
    }

    /**
     * Persiste el nombre local en `academias` para que el pull posterior no lo pise con el valor antiguo.
     */
    suspend fun pushAcademiaNombre(academiaId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cfg = db.academiaConfigDao().getActual() ?: error("no config")
            if (cfg.remoteAcademiaId != null && !cfg.academiaGestionNubePermitida) return@runCatching
            val n = cfg.nombreAcademia.trim()
            if (n.isEmpty()) return@runCatching
            client.from("academias").update(AcademiaNombrePatch(nombre = n)) {
                filter { eq("id", academiaId) }
            }
        }
    }

    private suspend fun pushAcademiaThemeColors(academiaId: String) {
        val cfg = db.academiaConfigDao().getActual() ?: return
        if (cfg.remoteAcademiaId != null && !cfg.academiaGestionNubePermitida) return
        client.from("academias").update(
            AcademiaColoresPatch(
                colorPrimarioHex = cfg.temaColorPrimarioHex,
                colorSecundarioHex = cfg.temaColorSecundarioHex,
            ),
        ) {
            filter { eq("id", academiaId) }
        }
    }

    private fun normalizeImageExt(raw: String): String {
        val e = raw.lowercase().ifEmpty { "jpg" }
        return if (e == "jpeg") "jpg" else e
    }

    private fun normalizeActaExt(raw: String): String {
        val e = raw.lowercase().ifEmpty { "pdf" }
        return when (e) {
            "jpeg" -> "jpg"
            "jpg", "png", "webp", "pdf" -> e
            else -> "pdf"
        }
    }

    private suspend fun pushAcademiaMedia(uid: String, academiaId: String) {
        val dao = db.academiaConfigDao()
        var cfg = dao.getActual() ?: return
        if (cfg.remoteAcademiaId != null && !cfg.academiaGestionNubePermitida) return
        var changed = false

        cfg.logoRutaAbsoluta?.let { p ->
            val f = File(p)
            if (f.isFile) {
                val ext = normalizeImageExt(f.extension)
                val objectPath = "$uid/$academiaId/logo.$ext"
                val url = client.uploadAcademiaPublicImage(objectPath, f)
                client.from("academias").update(AcademiaLogoUrlPatch(url)) {
                    filter { eq("id", academiaId) }
                }
                cfg = cfg.copy(logoUrlSupabase = url)
                changed = true
            }
        }
        cfg.portadaRutaAbsoluta?.let { p ->
            val f = File(p)
            if (f.isFile) {
                val ext = normalizeImageExt(f.extension)
                val objectPath = "$uid/$academiaId/portada.$ext"
                val url = client.uploadAcademiaPublicImage(objectPath, f)
                client.from("academias").update(AcademiaPortadaUrlPatch(url)) {
                    filter { eq("id", academiaId) }
                }
                cfg = cfg.copy(portadaUrlSupabase = url)
                changed = true
            }
        }
        if (changed) dao.upsert(cfg)
    }

    private suspend fun pushCategorias(academiaId: String) {
        val dao = db.categoriaDao()
        for (cat in dao.getAll().filter { it.remoteId == null }) {
            val row = client.from("categorias").insert(
                CategoriaInsert(
                    academiaId = academiaId,
                    nombre = cat.nombre,
                    portadaUrl = cat.portadaUrlSupabase,
                ),
            ) { select() }.decodeSingle<CategoriaRow>()
            dao.update(
                cat.copy(
                    remoteId = row.id,
                    portadaUrlSupabase = row.portadaUrl?.takeIf { it.isNotBlank() }
                        ?: cat.portadaUrlSupabase,
                ),
            )
        }
    }

    private suspend fun pushCategoriaPortadas(uid: String, academiaId: String) {
        val dao = db.categoriaDao()
        for (c in dao.getAll()) {
            val remote = c.remoteId ?: continue
            val p = c.portadaRutaAbsoluta ?: continue
            val f = File(p)
            if (!f.isFile) continue
            val ext = normalizeImageExt(f.extension)
            val objectPath = "$uid/$academiaId/categorias/$remote/portada.$ext"
            val url = client.uploadAcademiaPublicImage(objectPath, f)
            client.from("categorias").update(CategoriaPortadaUrlPatch(url)) {
                filter { eq("id", remote) }
            }
            if (c.portadaUrlSupabase != url) dao.update(c.copy(portadaUrlSupabase = url))
        }
    }

    private suspend fun pushJugadores(academiaId: String) {
        val dao = db.jugadorDao()
        for (j in dao.getJugadoresSinRemoto()) {
            val row = client.from("jugadores").insert(j.toCloudInsert(academiaId)) {
                select()
            }.decodeSingle<JugadorRow>()
            dao.update(j.copy(remoteId = row.id))
        }
    }

    private suspend fun pushJugadorFotosLocales(uid: String, academiaId: String) {
        val dao = db.jugadorDao()
        for (j in dao.getAllIncludingInactive()) {
            val remote = j.remoteId ?: continue
            var patch = j
            j.fotoRutaAbsoluta?.let { p ->
                val f = File(p)
                if (!f.isFile) return@let
                val ext = normalizeImageExt(f.extension)
                val objectPath = "$uid/$academiaId/jugadores/$remote.$ext"
                val url = client.uploadAcademiaPublicImage(objectPath, f)
                client.from("jugadores").update(JugadorFotoUrlPatch(url)) {
                    filter { eq("id", remote) }
                }
                if (patch.fotoUrlSupabase != url) {
                    patch = patch.copy(fotoUrlSupabase = url)
                    dao.update(patch)
                }
            }
            j.actaNacimientoRutaAbsoluta?.let { p ->
                val f = File(p)
                if (!f.isFile) return@let
                val ext = normalizeActaExt(f.extension)
                val objectPath = "$uid/$academiaId/jugadores/${remote}_acta.$ext"
                val url = client.uploadAcademiaPublicBinary(objectPath, f)
                client.from("jugadores").update(JugadorActaUrlPatch(url)) {
                    filter { eq("id", remote) }
                }
                if (patch.actaNacimientoUrlSupabase != url) {
                    patch = patch.copy(actaNacimientoUrlSupabase = url)
                    dao.update(patch)
                }
            }
            j.curpDocumentoRutaAbsoluta?.let { p ->
                val f = File(p)
                if (!f.isFile) return@let
                val ext = normalizeActaExt(f.extension)
                val objectPath = "$uid/$academiaId/jugadores/${remote}_curp_doc.$ext"
                val url = client.uploadAcademiaPublicBinary(objectPath, f)
                client.from("jugadores").update(JugadorCurpDocUrlPatch(url)) {
                    filter { eq("id", remote) }
                }
                if (patch.curpDocumentoUrlSupabase != url) {
                    patch = patch.copy(curpDocumentoUrlSupabase = url)
                    dao.update(patch)
                }
            }
        }
    }

    private suspend fun pushHistorial(academiaId: String) {
        val jDao = db.jugadorDao()
        for (h in jDao.getHistorialSinRemoto()) {
            val jug = jDao.getById(h.jugadorId) ?: continue
            val remote = jug.remoteId ?: continue
            val row = client.from("jugador_historial").insert(
                h.toCloudInsert(academiaId, remote),
            ) { select() }.decodeSingle<HistorialRow>()
            jDao.updateHistorial(h.copy(remoteId = row.id))
        }
    }

    private suspend fun pushAsistencias(academiaId: String) {
        val jDao = db.jugadorDao()
        val aDao = db.asistenciaDao()
        for (a in aDao.getSinRemoto()) {
            val jug = jDao.getById(a.jugadorId) ?: continue
            val remote = jug.remoteId ?: continue
            val row = client.from("asistencias").insert(
                a.toCloudInsert(academiaId, remote),
            ) { select() }.decodeSingle<AsistenciaRow>()
            aDao.upsert(a.copy(remoteId = row.id))
        }
    }

    private suspend fun pushStaff(academiaId: String) {
        val dao = db.staffDao()
        for (s in dao.getSinRemoto()) {
            val row = client.from("equipo_staff").insert(s.toCloudInsert(academiaId)) {
                select()
            }.decodeSingle<StaffRow>()
            dao.update(s.copy(remoteId = row.id))
        }
    }

    private suspend fun pushStaffFotosLocales(uid: String, academiaId: String) {
        val dao = db.staffDao()
        for (s in dao.getAll()) {
            val remote = s.remoteId ?: continue
            val p = s.fotoRutaAbsoluta ?: continue
            val f = File(p)
            if (!f.isFile) continue
            val ext = normalizeImageExt(f.extension)
            val objectPath = "$uid/$academiaId/staff/$remote.$ext"
            val url = client.uploadAcademiaPublicImage(objectPath, f)
            client.from("equipo_staff").update(StaffFotoUrlPatch(url)) {
                filter { eq("id", remote) }
            }
            if (s.fotoUrlSupabase != url) dao.update(s.copy(fotoUrlSupabase = url))
        }
    }

    private suspend fun pullCategorias(academiaId: String) {
        val dao = db.categoriaDao()
        val rows = client.from("categorias").select {
            filter { eq("academia_id", academiaId) }
        }.decodeList<CategoriaRow>()
        for (row in rows) {
            val local = dao.getByNombre(row.nombre)
            val url = row.portadaUrl?.takeIf { it.isNotBlank() }
            if (local != null) {
                dao.update(
                    local.copy(
                        remoteId = row.id,
                        portadaUrlSupabase = url ?: local.portadaUrlSupabase,
                    ),
                )
            } else {
                dao.insert(
                    Categoria(
                        nombre = row.nombre,
                        remoteId = row.id,
                        portadaUrlSupabase = url,
                    ),
                )
            }
        }
    }

    private suspend fun pullJugadores(academiaId: String) {
        val dao = db.jugadorDao()
        val rows = client.from("jugadores").select {
            filter { eq("academia_id", academiaId) }
        }.decodeList<JugadorRow>()
        for (row in rows) {
            val existing = dao.getJugadorPorRemoteId(row.id)
            if (existing != null) {
                dao.update(row.toLocalMerged(existing))
            } else {
                val nuevo = row.toLocalMerged(null)
                dao.insert(nuevo.copy(id = 0))
            }
        }
    }

    private suspend fun buildJugadorRemoteMap(): Map<String, Long> =
        db.jugadorDao().getAllIncludingInactive()
            .mapNotNull { j -> j.remoteId?.let { it to j.id } }
            .toMap()

    private suspend fun pullHistorial(academiaId: String, jugadorMap: Map<String, Long>) {
        val jDao = db.jugadorDao()
        val rows = client.from("jugador_historial").select {
            filter { eq("academia_id", academiaId) }
        }.decodeList<HistorialRow>()
        for (row in rows) {
            val localJugId = jugadorMap[row.jugadorId] ?: continue
            val existing = jDao.getHistorialPorRemoteId(row.id)
            val entity = JugadorHistorial(
                id = existing?.id ?: 0L,
                jugadorId = localJugId,
                tipo = row.tipo,
                fechaMillis = row.fechaMs,
                detalle = row.detalle,
                remoteId = row.id,
            )
            if (existing != null) {
                jDao.updateHistorial(entity.copy(id = existing.id))
            } else {
                jDao.insertHistorial(entity.copy(id = 0))
            }
        }
    }

    private suspend fun pullAsistencias(academiaId: String, jugadorMap: Map<String, Long>) {
        val dao = db.asistenciaDao()
        val rows = client.from("asistencias").select {
            filter { eq("academia_id", academiaId) }
        }.decodeList<AsistenciaRow>()
        for (row in rows) {
            val localJugId = jugadorMap[row.jugadorId] ?: continue
            val existing = dao.getPorRemoteId(row.id)
            val entity = Asistencia(
                id = existing?.id ?: 0L,
                jugadorId = localJugId,
                fechaDia = row.fechaDiaMs,
                presente = row.presente,
                remoteId = row.id,
            )
            if (existing != null) {
                dao.upsert(entity.copy(id = existing.id))
            } else {
                dao.upsert(entity.copy(id = 0L))
            }
        }
    }

    private suspend fun pullStaff(academiaId: String) {
        val dao = db.staffDao()
        val rows = client.from("equipo_staff").select {
            filter { eq("academia_id", academiaId) }
        }.decodeList<StaffRow>()
        for (row in rows) {
            val existing = dao.getPorRemoteId(row.id)
            if (existing != null) {
                dao.update(row.toLocalMerged(existing))
            } else {
                val nuevo = row.toLocalMerged(null)
                dao.insert(nuevo.copy(id = 0))
            }
        }
    }

    private suspend fun pushStaffCategorias(academiaId: String) {
        val staffDao = db.staffDao()
        val catDao = db.categoriaDao()
        val scDao = db.staffCategoriaDao()
        for (staff in staffDao.getAll()) {
            val staffR = staff.remoteId ?: continue
            val localNombres = scDao.getNombresForStaff(staff.id)
            val desiredCatIds = localNombres.mapNotNull { n -> catDao.getByNombre(n)?.remoteId }.toSet()
            val existing = client.from("equipo_staff_categorias").select {
                filter { eq("staff_id", staffR) }
            }.decodeList<StaffCategoriaRow>()
            val existingCatIds = existing.map { it.categoriaId }.toSet()
            for (row in existing) {
                if (row.categoriaId !in desiredCatIds) {
                    client.from("equipo_staff_categorias").delete {
                        filter { eq("id", row.id) }
                    }
                }
            }
            for (catId in desiredCatIds - existingCatIds) {
                client.from("equipo_staff_categorias").insert(
                    StaffCategoriaInsert(
                        academiaId = academiaId,
                        staffId = staffR,
                        categoriaId = catId,
                    ),
                )
            }
        }
    }

    private suspend fun pullStaffCategorias(academiaId: String) {
        val rows = client.from("equipo_staff_categorias").select {
            filter { eq("academia_id", academiaId) }
        }.decodeList<StaffCategoriaRow>()
        val cloudByStaff = rows.groupBy { it.staffId }
        val catByRemote = db.categoriaDao().getAll().associateBy { it.remoteId }
        val scDao = db.staffCategoriaDao()
        for (staff in db.staffDao().getAll()) {
            val staffR = staff.remoteId ?: continue
            scDao.deleteForStaff(staff.id)
            for (link in cloudByStaff[staffR].orEmpty()) {
                val nombre = catByRemote[link.categoriaId]?.nombre ?: continue
                scDao.insert(
                    StaffCategoria(
                        staffId = staff.id,
                        categoriaNombre = nombre,
                    ),
                )
            }
        }
    }

    private suspend fun pullAcademiaConfig(academiaId: String) {
        val row = client.from("academias").select {
            filter { eq("id", academiaId) }
        }.decodeSingle<AcademiaRow>()
        val dao = db.academiaConfigDao()
        val cfg = dao.getActual() ?: return
        val uid = client.auth.currentUserOrNull()?.id?.toString()
        val puedeGestionar = uid?.let { computeAcademiaGestionNubePermitida(it, row) }
            ?: cfg.academiaGestionNubePermitida
        dao.upsert(
            cfg.copy(
                nombreAcademia = row.nombre.ifBlank { cfg.nombreAcademia },
                logoUrlSupabase = row.logoUrl?.takeIf { it.isNotBlank() } ?: cfg.logoUrlSupabase,
                portadaUrlSupabase = row.portadaUrl?.takeIf { it.isNotBlank() } ?: cfg.portadaUrlSupabase,
                temaColorPrimarioHex = row.colorPrimarioHex?.takeIf { it.isNotBlank() }
                    ?: cfg.temaColorPrimarioHex,
                temaColorSecundarioHex = row.colorSecundarioHex?.takeIf { it.isNotBlank() }
                    ?: cfg.temaColorSecundarioHex,
                codigoClubRemoto = row.codigoClub?.takeIf { it.isNotBlank() } ?: cfg.codigoClubRemoto,
                academiaGestionNubePermitida = puedeGestionar,
            ),
        )
    }
}
