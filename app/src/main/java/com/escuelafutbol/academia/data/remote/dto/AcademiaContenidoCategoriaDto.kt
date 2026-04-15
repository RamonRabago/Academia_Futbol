package com.escuelafutbol.academia.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val jsonContenidoImgs = Json { ignoreUnknownKeys = true }
private val contenidoImgsListSer = ListSerializer(String.serializer())

fun encodeContenidoCuerpoImagenesUrls(urls: List<String>): String? {
    val clean = urls.map { it.trim() }.filter { it.isNotEmpty() }
    if (clean.isEmpty()) return null
    return jsonContenidoImgs.encodeToString(contenidoImgsListSer, clean)
}

object ContenidoEstadoPublicacion {
    const val PUBLISHED = "published"
    const val PENDING = "pending_approval"
    const val REJECTED = "rejected"
}

fun decodeContenidoCuerpoImagenesUrls(raw: String?): List<String> {
    val s = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return emptyList()
    return runCatching {
        jsonContenidoImgs.decodeFromString(contenidoImgsListSer, s)
    }.getOrElse { emptyList() }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

@Serializable
data class AcademiaContenidoCategoriaRow(
    val id: String,
    @SerialName("academia_id") val academiaId: String,
    @SerialName("categoria_nombre") val categoriaNombre: String,
    val tema: String,
    val titulo: String,
    val cuerpo: String,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    @SerialName("cuerpo_imagenes_urls") val cuerpoImagenesUrlsJson: String? = null,
    @SerialName("author_user_id") val authorUserId: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("archived_at") val archivedAt: String? = null,
    @SerialName("estado_publicacion") val estadoPublicacion: String = ContenidoEstadoPublicacion.PUBLISHED,
    @SerialName("approved_at") val approvedAt: String? = null,
    @SerialName("approved_by_user_id") val approvedByUserId: String? = null,
)

@Serializable
data class AcademiaContenidoCategoriaInsert(
    @SerialName("academia_id") val academiaId: String,
    @SerialName("categoria_nombre") val categoriaNombre: String,
    val tema: String,
    val titulo: String,
    val cuerpo: String,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    @SerialName("cuerpo_imagenes_urls") val cuerpoImagenesUrlsJson: String? = null,
    @SerialName("author_user_id") val authorUserId: String,
    @SerialName("estado_publicacion") val estadoPublicacion: String = ContenidoEstadoPublicacion.PUBLISHED,
)

@Serializable
data class AcademiaContenidoCategoriaArchivarPatch(
    @SerialName("archived_at") val archivedAt: String,
)

@Serializable
data class AcademiaContenidoEstadoPublicacionPatch(
    @SerialName("estado_publicacion") val estadoPublicacion: String,
    @SerialName("approved_at") val approvedAt: String? = null,
    @SerialName("approved_by_user_id") val approvedByUserId: String? = null,
)
