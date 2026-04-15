package com.escuelafutbol.academia.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object ContenidoReaccionTipo {
    const val LIKE = "like"
    const val CELEBRATE = "celebrate"
    const val THANKS = "thanks"
    const val STRONG = "strong"

    val todosWire: List<String> = listOf(LIKE, CELEBRATE, THANKS, STRONG)
}

@Serializable
data class AcademiaContenidoReaccionRow(
    val id: String,
    @SerialName("academia_id") val academiaId: String,
    @SerialName("contenido_id") val contenidoId: String,
    @SerialName("user_id") val userId: String,
    val tipo: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class AcademiaContenidoReaccionInsert(
    @SerialName("academia_id") val academiaId: String,
    @SerialName("contenido_id") val contenidoId: String,
    @SerialName("user_id") val userId: String,
    val tipo: String,
)
