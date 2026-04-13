package com.escuelafutbol.academia.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademiaMensajeCategoriaRow(
    val id: String,
    @SerialName("academia_id") val academiaId: String,
    @SerialName("categoria_nombre") val categoriaNombre: String,
    val tipo: String,
    val titulo: String,
    val cuerpo: String,
    @SerialName("author_user_id") val authorUserId: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("event_at") val eventAt: String? = null,
    @SerialName("archived_at") val archivedAt: String? = null,
)

@Serializable
data class AcademiaMensajeCategoriaInsert(
    @SerialName("academia_id") val academiaId: String,
    @SerialName("categoria_nombre") val categoriaNombre: String,
    val tipo: String,
    val titulo: String,
    val cuerpo: String,
    @SerialName("author_user_id") val authorUserId: String,
    @SerialName("event_at") val eventAt: String? = null,
)
