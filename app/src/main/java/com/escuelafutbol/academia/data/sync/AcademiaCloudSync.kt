package com.escuelafutbol.academia.data.sync

import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.Asistencia
import com.escuelafutbol.academia.data.local.entity.Categoria
import com.escuelafutbol.academia.data.local.entity.StaffCategoria
import com.escuelafutbol.academia.data.local.entity.JugadorHistorial
import com.escuelafutbol.academia.data.local.AcademiaDatabase
import com.escuelafutbol.academia.data.local.dao.AcademiaConfigDao
import com.escuelafutbol.academia.data.local.dao.CategoriaDao
import com.escuelafutbol.academia.data.local.model.RolDispositivo
import com.escuelafutbol.academia.data.local.model.rolDispositivoSugeridoDesdeRolNube
import com.escuelafutbol.academia.data.remote.dto.AcademiaColoresPatch
import com.escuelafutbol.academia.data.remote.dto.AcademiaDiaLimitePagoPatch
import com.escuelafutbol.academia.data.remote.dto.AcademiaInsert
import com.escuelafutbol.academia.data.remote.dto.AcademiaLogoUrlPatch
import com.escuelafutbol.academia.data.remote.dto.AcademiaMiembroCategoriaLinkRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaMiembroListRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaPadresAlumnoInsert
import com.escuelafutbol.academia.data.remote.dto.CoachCategoryNombreRow
import com.escuelafutbol.academia.data.remote.dto.CoachCategoriaPortadaRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaMiembroRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaNombrePatch
import com.escuelafutbol.academia.data.remote.dto.AcademiaPortadaUrlPatch
import com.escuelafutbol.academia.data.remote.dto.AcademiaRow
import com.escuelafutbol.academia.data.remote.dto.RegenerateInviteCodesResult
import com.escuelafutbol.academia.data.remote.dto.AsistenciaRow
import com.escuelafutbol.academia.data.remote.dto.AsistenciaUpdatePatch
import com.escuelafutbol.academia.data.remote.dto.CobroMensualPatch
import com.escuelafutbol.academia.data.remote.dto.CobroMensualRow
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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
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

    private val jsonCoachCategorias = Json { ignoreUnknownKeys = true }
    private val coachCatNamesSerializer = ListSerializer(String.serializer())

    private data class MembresiaCloudLocal(
        val rol: String?,
        val coachCategoriasJson: String?,
    )

    /**
     * @param skipPush Si true, solo descarga desde la nube (pull). Sirve para pull-to-refresh: un fallo en
     * subida (push) no debe impedir traer categorías, jugadores y portadas.
     */
    suspend fun syncAll(skipPush: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val uid = client.auth.currentUserOrNull()?.id?.toString()
                ?: error("No hay sesión. Inicia sesión antes de sincronizar.")
            val academiaId = ensureAcademiaIdForSync(uid)
            val cfgPre = db.academiaConfigDao().getActual() ?: AcademiaConfig.DEFAULT
            val esPadreNube = cfgPre.remoteAcademiaId != null &&
                cfgPre.cloudMembresiaRol?.equals("parent", ignoreCase = true) == true

            if (!esPadreNube && !skipPush) {
                pushAcademiaNombre(academiaId)
                pushAcademiaThemeColors(academiaId)
                pushAcademiaDiaLimitePago(academiaId)
                pushAcademiaMedia(uid, academiaId)
                pushCategorias(academiaId)
                pushCategoriaPortadas(uid, academiaId)
                pushJugadores(academiaId)
                pushJugadorFotosLocales(uid, academiaId)
                pushHistorial(academiaId)
                pushAsistencias(academiaId)
                pushCobrosMensuales(academiaId)
                pushStaff(academiaId)
                pushStaffFotosLocales(uid, academiaId)
                pushStaffCategorias(academiaId)
            }

            // Membresía (coach / parent / …) antes de categorías: el pull de categorías y la RPC de portadas coach usan Room actualizado.
            pullAcademiaConfig(academiaId)
            pullCategorias(academiaId)
            val jugadoresPulledIds = pullJugadores(academiaId)
            if (esPadreNube) {
                pruneJugadoresNoAutorizadosParaPadre(jugadoresPulledIds)
            }
            val jugadorMap = buildJugadorRemoteMap()
            pullHistorial(academiaId, jugadorMap)
            pullAsistencias(academiaId, jugadorMap)
            pullCobrosMensuales(academiaId, jugadorMap)
            pullStaff(academiaId)
            pullStaffCategorias(academiaId)
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
                // Misma academia en caché pero otro usuario (p. ej. cerrar sesión admin → profesor):
                // hay que volver a fusionar fila academias + membresía; si no, cloudMembresiaRol queda null hasta sync tardío.
                val row = client.from("academias").select {
                    filter { eq("id", cached) }
                }.decodeSingle<AcademiaRow>()
                val latestCfg = dao.getActual() ?: cfg
                mergeAcademiaRowIntoLocal(dao, latestCfg, row)
                return AcademiaBindingResult.Ok(cached)
            }
            dao.upsert(
                cfg.copy(
                    remoteAcademiaId = null,
                    codigoClubRemoto = null,
                    codigoInviteCoachRemoto = null,
                    codigoInviteCoordinatorRemoto = null,
                    codigoInviteParentRemoto = null,
                    academiaGestionNubePermitida = true,
                    cloudMembresiaRol = null,
                    cloudCoachCategoriasJson = null,
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
            rolDispositivo = RolDispositivo.DUENO_ACADEMIA.name,
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

    /** Une por código de invitación; el rol lo fija el servidor según el código (entrenador / coordinador / padre). */
    suspend fun joinAcademiaByInviteCode(code: String): Result<String> = runCatching {
        val params = buildJsonObject {
            put("p_codigo", code.trim().uppercase())
        }
        val result = client.postgrest.rpc("join_academia_by_invite_code", params)
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

    /** Genera tres códigos (entrenador, coordinador, padre) vía RPC y actualiza Room. */
    suspend fun regenerateAcademiaInviteCodes(academiaId: String): Result<RegenerateInviteCodesResult> =
        runCatching {
            val params = buildJsonObject { put("p_academia_id", academiaId) }
            val result = client.postgrest.rpc("regenerate_academia_invite_codes", params)
            val codes = result.decodeAs<RegenerateInviteCodesResult>()
            val dao = db.academiaConfigDao()
            val cfg = dao.getActual() ?: AcademiaConfig.DEFAULT
            dao.upsert(
                cfg.copy(
                    codigoInviteCoachRemoto = codes.coach,
                    codigoInviteCoordinatorRemoto = codes.coordinator,
                    codigoInviteParentRemoto = codes.parent,
                    codigoClubRemoto = null,
                ),
            )
            codes
        }

    /**
     * Nombre de categoría para armar `cloudCoachCategoriasJson`.
     * Primero exige coincidencia con [academiaId]; si no hay fila (datos viejos / academia_id mal copiado),
     * reintenta solo por `id` por si RLS aún devuelve la fila.
     */
    private suspend fun nombreCategoriaCloudParaCoach(categoriaId: String, academiaId: String): String? {
        val nombreExacto = runCatching {
            client.from("categorias").select {
                filter {
                    eq("id", categoriaId)
                    eq("academia_id", academiaId)
                }
            }.decodeSingle<CategoriaRow>()
        }.getOrNull()?.nombre?.trim()?.takeIf { it.isNotEmpty() }
        if (nombreExacto != null) return nombreExacto
        return runCatching {
            client.from("categorias").select {
                filter { eq("id", categoriaId) }
            }.decodeList<CategoriaRow>().firstOrNull()
        }.getOrNull()?.nombre?.trim()?.takeIf { it.isNotEmpty() }
    }

    /**
     * Nombres de categorías del coach: primero RPC `list_my_coach_category_names` (definer, ignora fallos de RLS del cliente);
     * si vacío o error, lectura directa por tablas (compatibilidad si la RPC no está desplegada).
     */
    private suspend fun coachCategoryNombresParaMiembro(academiaId: String, miembroId: String): List<String> {
        val desdeRpc = runCatching {
            val params = buildJsonObject { put("p_academia_id", academiaId) }
            client.postgrest.rpc("list_my_coach_category_names", params)
                .decodeList<CoachCategoryNombreRow>()
                .mapNotNull { it.nombre.trim().takeIf { n -> n.isNotEmpty() } }
        }.getOrNull().orEmpty()
        if (desdeRpc.isNotEmpty()) {
            return desdeRpc.distinct().sorted()
        }
        val links = runCatching {
            client.from("academia_miembro_categorias").select {
                filter { eq("miembro_id", miembroId) }
            }.decodeList<AcademiaMiembroCategoriaLinkRow>()
        }.getOrElse { emptyList() }
        val ids = links.map { it.categoriaId.trim() }.filter { it.isNotEmpty() }.distinct()
        return ids.mapNotNull { cid -> nombreCategoriaCloudParaCoach(cid, academiaId) }.distinct().sorted()
    }

    /**
     * Rol efectivo para sync y UI.
     *
     * **Dueño de cuenta** (`academias.user_id` = sesión) tiene **prioridad absoluta** sobre cualquier fila en
     * `academia_miembros`. Si no, un dueño que también estuviera dado de alta como **padre** (mismo correo /
     * invitación tutor) quedaba con `cloudMembresiaRol = parent`: sync en modo padre, RLS devolvía pocos
     * jugadores y a menudo vacío `categorias`, sin portadas.
     *
     * Si no es dueño de cuenta, se usa la membresía (coach, coordinator, admin, parent, …).
     */
    private suspend fun resolveMembresiaCloud(uid: String, row: AcademiaRow): MembresiaCloudLocal {
        val uidNorm = uid.trim()
        val ownerNorm = row.userId.trim()
        if (ownerNorm.isNotEmpty() && uidNorm.equals(ownerNorm, ignoreCase = true)) {
            return MembresiaCloudLocal(rol = "owner", coachCategoriasJson = null)
        }

        val member = client.from("academia_miembros").select {
            filter {
                eq("academia_id", row.id)
                eq("user_id", uid)
                eq("activo", true)
            }
        }.decodeList<AcademiaMiembroRow>().firstOrNull()

        if (member != null) {
            val r = member.rol.trim().lowercase(Locale.ROOT)
            val coachJson = if (r == "coach") {
                val nombres = coachCategoryNombresParaMiembro(row.id, member.id)
                jsonCoachCategorias.encodeToString(coachCatNamesSerializer, nombres)
            } else {
                null
            }
            return MembresiaCloudLocal(rol = r, coachCategoriasJson = coachJson)
        }

        return MembresiaCloudLocal(rol = null, coachCategoriasJson = null)
    }

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
        return r == "owner" || r == "admin" || r == "coordinator"
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
        val memb = uid?.let { resolveMembresiaCloud(it, row) }
        val (cloudRol, cloudCats) = cloudMembresiaFieldsForPull(uid, memb, cfg)
        val rolPersistido = rolDispositivoSugeridoDesdeRolNube(cloudRol)?.name
            ?: RolDispositivo.PADRE_TUTOR.name
        dao.upsert(
            cfg.copy(
                remoteAcademiaId = row.id,
                nombreAcademia = row.nombre.ifBlank { cfg.nombreAcademia },
                logoUrlSupabase = row.logoUrl?.takeIf { it.isNotBlank() } ?: cfg.logoUrlSupabase,
                portadaUrlSupabase = row.portadaUrl?.takeIf { it.isNotBlank() } ?: cfg.portadaUrlSupabase,
                mensualidadVisibleProfesor = row.mensualidadVisibleProfesor,
                mensualidadVisibleCoordinador = row.mensualidadVisibleCoordinador,
                mensualidadVisibleDueno = row.mensualidadVisibleDueno,
                rolDispositivo = rolPersistido,
                pinStaffHash = row.pinStaffHash ?: cfg.pinStaffHash,
                temaColorPrimarioHex = row.colorPrimarioHex?.takeIf { it.isNotBlank() }
                    ?: cfg.temaColorPrimarioHex,
                temaColorSecundarioHex = row.colorSecundarioHex?.takeIf { it.isNotBlank() }
                    ?: cfg.temaColorSecundarioHex,
                codigoClubRemoto = row.codigoClub?.takeIf { it.isNotBlank() } ?: cfg.codigoClubRemoto,
                codigoInviteCoachRemoto = row.codigoInviteCoach?.takeIf { it.isNotBlank() }
                    ?: cfg.codigoInviteCoachRemoto,
                codigoInviteCoordinatorRemoto = row.codigoInviteCoordinator?.takeIf { it.isNotBlank() }
                    ?: cfg.codigoInviteCoordinatorRemoto,
                codigoInviteParentRemoto = row.codigoInviteParent?.takeIf { it.isNotBlank() }
                    ?: cfg.codigoInviteParentRemoto,
                academiaGestionNubePermitida = puedeGestionar,
                cloudMembresiaRol = cloudRol,
                cloudCoachCategoriasJson = cloudCats,
                diaLimitePagoMes = row.diaLimitePagoMes,
                remoteAcademiaCuentaUserId = row.userId.trim().takeIf { it.isNotEmpty() },
            ),
        )
    }

    /**
     * Con sesión conocida, siempre tomar rol/categorías coach del usuario actual ([memb]);
     * no reutilizar [cfg] del usuario anterior (mismo dispositivo, otra cuenta).
     */
    private fun cloudMembresiaFieldsForPull(
        uid: String?,
        memb: MembresiaCloudLocal?,
        cfg: AcademiaConfig,
    ): Pair<String?, String?> {
        if (uid == null) {
            return cfg.cloudMembresiaRol to cfg.cloudCoachCategoriasJson
        }
        val m = memb ?: return null to null
        return m.rol to m.coachCategoriasJson
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

    suspend fun pushAcademiaDiaLimitePago(academiaId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cfg = db.academiaConfigDao().getActual() ?: return@runCatching
            if (cfg.remoteAcademiaId != null && !cfg.academiaGestionNubePermitida) return@runCatching
            if (cfg.remoteAcademiaId != null) {
                val uid = client.auth.currentUserOrNull()?.id?.toString()?.trim()?.lowercase(Locale.ROOT)
                val owner = cfg.remoteAcademiaCuentaUserId?.trim()?.lowercase(Locale.ROOT)
                if (owner.isNullOrEmpty() || uid.isNullOrEmpty() || uid != owner) return@runCatching
            }
            client.from("academias").update(
                AcademiaDiaLimitePagoPatch(diaLimitePagoMes = cfg.diaLimitePagoMes),
            ) {
                filter { eq("id", academiaId) }
            }
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
        val pendientes = dao.getJugadoresSinRemoto()
        if (pendientes.isEmpty()) return
        val padresPorEmail = padresMiembrosEmailNormalizado(academiaId)
        for (j in pendientes) {
            val row = client.from("jugadores").insert(j.toCloudInsert(academiaId)) {
                select()
            }.decodeSingle<JugadorRow>()
            dao.update(j.copy(remoteId = row.id))
            intentarAutoVinculoPadrePorEmailTutor(
                academiaId = academiaId,
                jugadorRemoteId = row.id,
                emailTutor = j.emailTutor,
                padresPorEmail = padresPorEmail,
            )
        }
        // Jugadores ya subidos antes (p. ej. primer alumno) sin fila en academia_padres_alumnos: reintenta si el correo tutor coincide.
        if (padresPorEmail.isNotEmpty()) {
            for (j in dao.getAll()) {
                if (!j.activo || j.remoteId.isNullOrBlank()) continue
                val tutor = j.emailTutor?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotEmpty() }
                    ?: continue
                if (tutor !in padresPorEmail) continue
                intentarAutoVinculoPadrePorEmailTutor(
                    academiaId = academiaId,
                    jugadorRemoteId = j.remoteId!!,
                    emailTutor = j.emailTutor,
                    padresPorEmail = padresPorEmail,
                )
            }
        }
    }

    /**
     * Miembros activos con rol `parent` y correo conocido (RPC gestión).
     * Clave = email en minúsculas; valor = user_id (puede haber varios user_id con el mismo email teórico; se enlazan todos).
     */
    private suspend fun padresMiembrosEmailNormalizado(academiaId: String): Map<String, List<String>> {
        val params = buildJsonObject { put("p_academia_id", academiaId) }
        val rows = runCatching {
            client.postgrest.rpc("list_academia_miembros_for_manage", params)
                .decodeList<AcademiaMiembroListRow>()
        }.getOrNull().orEmpty()
        val out = mutableMapOf<String, MutableList<String>>()
        for (m in rows) {
            if (!m.activo || !m.rol.equals("parent", ignoreCase = true)) continue
            val mail = m.memberEmail?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotEmpty() }
                ?: continue
            out.getOrPut(mail) { mutableListOf() }.add(m.userId)
        }
        return out
    }

    /**
     * Tras insertar el jugador en la nube: si el correo del tutor coincide con el de un miembro `parent`,
     * crea fila en [academia_padres_alumnos] (mismo criterio que el vínculo manual en gestión de miembros).
     */
    private suspend fun intentarAutoVinculoPadrePorEmailTutor(
        academiaId: String,
        jugadorRemoteId: String,
        emailTutor: String?,
        padresPorEmail: Map<String, List<String>>,
    ) {
        val tutor = emailTutor?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotEmpty() } ?: return
        val userIds = padresPorEmail[tutor] ?: return
        for (parentUserId in userIds) {
            runCatching {
                client.from("academia_padres_alumnos").insert(
                    AcademiaPadresAlumnoInsert(
                        academiaId = academiaId,
                        parentUserId = parentUserId,
                        jugadorId = jugadorRemoteId,
                    ),
                )
            }
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
            aDao.upsert(a.copy(remoteId = row.id, needsCloudPush = false))
        }
        for (a in aDao.getRemotasPendientesPush()) {
            val rid = a.remoteId ?: continue
            val jug = jDao.getById(a.jugadorId) ?: continue
            if (jug.remoteId == null) continue
            client.from("asistencias").update(
                AsistenciaUpdatePatch(
                    fechaDiaMs = a.fechaDia,
                    presente = a.presente,
                ),
            ) {
                filter { eq("id", rid) }
            }
            aDao.upsert(a.copy(needsCloudPush = false))
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

    private suspend fun mergeCategoriaDesdeNube(
        dao: CategoriaDao,
        nombreRaw: String,
        remoteId: String?,
        portadaUrl: String?,
    ) {
        val nombre = nombreRaw.trim()
        if (nombre.isEmpty()) return
        val local = dao.getByNombre(nombre)
        val url = portadaUrl?.takeIf { it.isNotBlank() }
        val rid = remoteId?.trim()?.takeIf { it.isNotEmpty() }
        if (local != null) {
            dao.update(
                local.copy(
                    remoteId = rid ?: local.remoteId,
                    portadaUrlSupabase = url ?: local.portadaUrlSupabase,
                ),
            )
        } else {
            dao.insert(
                Categoria(
                    nombre = nombre,
                    remoteId = rid,
                    portadaUrlSupabase = url,
                ),
            )
        }
    }

    private suspend fun pullCategorias(academiaId: String) {
        val dao = db.categoriaDao()
        val rows = client.from("categorias").select {
            filter { eq("academia_id", academiaId) }
        }.decodeList<CategoriaRow>()
        for (row in rows) {
            mergeCategoriaDesdeNube(dao, row.nombre, row.id, row.portadaUrl)
        }
        pullCategoriasCoachPortadasRpc(academiaId, dao)
        pullCategoriasParentPortadasRpc(academiaId, dao)
    }

    /**
     * Refuerzo para entrenador: si el SELECT a [categorias] devolvió poco o nada (RLS / desfase),
     * la RPC definer trae las categorías asignadas con [portada_url] para Room (miniaturas en «Cambiar categoría»).
     */
    private suspend fun pullCategoriasCoachPortadasRpc(academiaId: String, dao: CategoriaDao) {
        val rpcRows = runCatching {
            val params = buildJsonObject { put("p_academia_id", academiaId) }
            client.postgrest.rpc("list_my_coach_categorias_portadas", params)
                .decodeList<CoachCategoriaPortadaRow>()
        }.getOrNull().orEmpty()
        if (rpcRows.isEmpty()) return
        for (row in rpcRows) {
            mergeCategoriaDesdeNube(dao, row.nombre, row.categoriaId, row.portadaUrl)
        }
    }

    /**
     * Tutor en nube: RLS de [categorias] no incluye rol parent; esta RPC (definer) trae nombre + portada_url
     * de las categorías de los hijos vinculados en [academia_padres_alumnos] (misma forma que el coach vía RPC).
     */
    private suspend fun pullCategoriasParentPortadasRpc(academiaId: String, dao: CategoriaDao) {
        val rpcRows = runCatching {
            val params = buildJsonObject { put("p_academia_id", academiaId) }
            client.postgrest.rpc("list_my_parent_categorias_portadas", params)
                .decodeList<CoachCategoriaPortadaRow>()
        }.getOrNull().orEmpty()
        if (rpcRows.isEmpty()) return
        for (row in rpcRows) {
            mergeCategoriaDesdeNube(dao, row.nombre, row.categoriaId, row.portadaUrl)
        }
    }

    /** Devuelve los `remoteId` devueltos por PostgREST (RLS: padre solo ve hijos vinculados). */
    private suspend fun pullJugadores(academiaId: String): Set<String> {
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
        return rows.map { it.id }.toSet()
    }

    /** Evita que en dispositivos de padre queden jugadores de un rol staff previo en el mismo SQLite. */
    private suspend fun pruneJugadoresNoAutorizadosParaPadre(allowedRemoteIds: Set<String>) {
        val dao = db.jugadorDao()
        for (j in dao.getAllIncludingInactive()) {
            val rid = j.remoteId ?: continue
            if (rid !in allowedRemoteIds) dao.delete(j)
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
                needsCloudPush = false,
            )
            if (existing != null) {
                dao.upsert(entity.copy(id = existing.id))
            } else {
                dao.upsert(entity.copy(id = 0L))
            }
        }
    }

    private suspend fun pushCobrosMensuales(academiaId: String) {
        val jDao = db.jugadorDao()
        val cDao = db.cobroMensualDao()
        for (c in cDao.getSinRemoto()) {
            val j = jDao.getById(c.jugadorId) ?: continue
            val jr = j.remoteId ?: continue
            val row = client.from("jugador_cobros_mensual").insert(c.toCloudInsert(academiaId, jr)) {
                select()
            }.decodeSingle<CobroMensualRow>()
            cDao.update(c.copy(remoteId = row.id, needsCloudPush = false))
        }
        for (c in cDao.getRemotosPendientesPush()) {
            val rid = c.remoteId ?: continue
            client.from("jugador_cobros_mensual").update(
                CobroMensualPatch(
                    importeEsperado = c.importeEsperado,
                    importePagado = c.importePagado,
                    notas = c.notas,
                ),
            ) {
                filter { eq("id", rid) }
            }
            cDao.update(c.copy(needsCloudPush = false))
        }
    }

    private suspend fun pullCobrosMensuales(academiaId: String, jugadorMap: Map<String, Long>) {
        val cDao = db.cobroMensualDao()
        val rows = runCatching {
            client.from("jugador_cobros_mensual").select {
                filter { eq("academia_id", academiaId) }
            }.decodeList<CobroMensualRow>()
        }.getOrElse { emptyList() }
        for (row in rows) {
            val localJid = jugadorMap[row.jugadorId] ?: continue
            val existingRemote = cDao.getPorRemoteId(row.id)
            val existingLocal = cDao.getByJugadorYPeriodo(localJid, row.periodoYyyyMm)
            val base = existingRemote ?: existingLocal
            val merged = row.toLocalMerged(localJid, base)
            if (base != null) {
                cDao.update(merged.copy(id = base.id))
            } else {
                cDao.insert(merged.copy(id = 0L))
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
        val memb = uid?.let { resolveMembresiaCloud(it, row) }
        val (cloudRol, cloudCats) = cloudMembresiaFieldsForPull(uid, memb, cfg)
        val rolPersistidoPull = rolDispositivoSugeridoDesdeRolNube(cloudRol)?.name
            ?: RolDispositivo.PADRE_TUTOR.name
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
                codigoInviteCoachRemoto = row.codigoInviteCoach?.takeIf { it.isNotBlank() }
                    ?: cfg.codigoInviteCoachRemoto,
                codigoInviteCoordinatorRemoto = row.codigoInviteCoordinator?.takeIf { it.isNotBlank() }
                    ?: cfg.codigoInviteCoordinatorRemoto,
                codigoInviteParentRemoto = row.codigoInviteParent?.takeIf { it.isNotBlank() }
                    ?: cfg.codigoInviteParentRemoto,
                academiaGestionNubePermitida = puedeGestionar,
                rolDispositivo = rolPersistidoPull,
                cloudMembresiaRol = cloudRol,
                cloudCoachCategoriasJson = cloudCats,
                diaLimitePagoMes = row.diaLimitePagoMes,
                remoteAcademiaCuentaUserId = row.userId.trim().takeIf { it.isNotEmpty() },
            ),
        )
    }
}
