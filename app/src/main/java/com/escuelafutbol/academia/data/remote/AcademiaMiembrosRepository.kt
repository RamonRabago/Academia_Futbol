package com.escuelafutbol.academia.data.remote

import com.escuelafutbol.academia.data.local.AcademiaDatabase
import com.escuelafutbol.academia.data.remote.dto.AcademiaMiembroActivoPatch
import com.escuelafutbol.academia.data.remote.dto.AcademiaMiembroCategoriaInsert
import com.escuelafutbol.academia.data.remote.dto.AcademiaMiembroCategoriaLinkRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaMiembroRolPatch
import com.escuelafutbol.academia.data.remote.dto.AltaPorUserLabelRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaMiembroListRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaMiembroRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaRow
import com.escuelafutbol.academia.data.remote.dto.CategoriaRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AcademiaMiembrosRepository(
    private val client: SupabaseClient,
    private val db: AcademiaDatabase,
) {

    /**
     * Etiquetas legibles (metadata Auth / correo) para cada [Jugador.altaPorUserId] usado en la academia.
     * Útil cuando [Jugador.altaPorNombre] quedó vacío en datos antiguos.
     */
    suspend fun etiquetasAltaPorUsuario(academiaId: String): Map<String, String> = withContext(Dispatchers.IO) {
        val params = buildJsonObject { put("p_academia_id", academiaId) }
        runCatching {
            client.postgrest.rpc("alta_por_user_labels_for_academia", params)
                .decodeList<AltaPorUserLabelRow>()
                .associate { row -> row.userId.lowercase() to row.displayLabel }
        }.getOrElse { emptyMap() }
    }

    suspend fun getAcademiaOwnerUserId(academiaId: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            client.from("academias").select {
                filter { eq("id", academiaId) }
            }.decodeSingle<AcademiaRow>().userId
        }.getOrNull()
    }

    suspend fun listMiembros(academiaId: String): List<AcademiaMiembroListRow> = withContext(Dispatchers.IO) {
        val params = buildJsonObject { put("p_academia_id", academiaId) }
        runCatching {
            client.postgrest.rpc("list_academia_miembros_for_manage", params)
                .decodeList<AcademiaMiembroListRow>()
        }.getOrElse {
            client.from("academia_miembros").select {
                filter { eq("academia_id", academiaId) }
            }.decodeList<AcademiaMiembroRow>().map { r ->
                AcademiaMiembroListRow(
                    id = r.id,
                    academiaId = r.academiaId,
                    userId = r.userId,
                    rol = r.rol,
                    activo = r.activo,
                    displayLabel = null,
                    memberEmail = null,
                )
            }.sortedWith(compareBy({ it.rol }, { it.userId }))
        }
    }

    suspend fun setMiembroActivo(miembroId: String, activo: Boolean) = withContext(Dispatchers.IO) {
        client.from("academia_miembros").update(AcademiaMiembroActivoPatch(activo = activo)) {
            filter { eq("id", miembroId) }
        }
    }

    suspend fun setMiembroRol(miembroId: String, rol: String) = withContext(Dispatchers.IO) {
        client.from("academia_miembros").update(AcademiaMiembroRolPatch(rol = rol)) {
            filter { eq("id", miembroId) }
        }
    }

    /** Quita la fila de membresía (revocar). Las filas en `academia_miembro_categorias` se eliminan en cascada. */
    suspend fun deleteMiembro(miembroId: String) = withContext(Dispatchers.IO) {
        client.from("academia_miembros").delete {
            filter { eq("id", miembroId) }
        }
    }

    suspend fun getCategoriaIdsForMiembro(miembroId: String): List<String> = withContext(Dispatchers.IO) {
        client.from("academia_miembro_categorias").select {
            filter { eq("miembro_id", miembroId) }
        }.decodeList<AcademiaMiembroCategoriaLinkRow>().map { it.categoriaId }
    }

    suspend fun replaceMiembroCategorias(miembroId: String, categoriaIds: List<String>) =
        withContext(Dispatchers.IO) {
            client.from("academia_miembro_categorias").delete {
                filter { eq("miembro_id", miembroId) }
            }
            val distinct = categoriaIds.distinct()
            if (distinct.isEmpty()) return@withContext
            for (cid in distinct) {
                client.from("academia_miembro_categorias").insert(
                    AcademiaMiembroCategoriaInsert(
                        miembroId = miembroId,
                        categoriaId = cid,
                    ),
                )
            }
        }

    /** Categorías remotas con nombre (solo las que tienen `remoteId` en Room). */
    suspend fun categoriasConRemotoParaUi(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        db.categoriaDao().getAll()
            .mapNotNull { c -> c.remoteId?.let { it to c.nombre } }
            .sortedBy { it.second }
    }

    suspend fun nombresCategoriasPorIds(academiaId: String, ids: Collection<String>): Map<String, String> =
        withContext(Dispatchers.IO) {
            if (ids.isEmpty()) return@withContext emptyMap()
            val out = mutableMapOf<String, String>()
            for (cid in ids.toSet()) {
                runCatching {
                    client.from("categorias").select {
                        filter {
                            eq("id", cid)
                            eq("academia_id", academiaId)
                        }
                    }.decodeSingle<CategoriaRow>()
                }.getOrNull()?.let { row -> out[cid] = row.nombre }
            }
            out
        }
}
