package com.escuelafutbol.academia.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Carga útil en [AcademiaCompetenciaPartidoRow.detalleMarcadorJson] (jsonb) para multideporte:
 * goles, puntos, carreras, etc., según la etiqueta del deporte en UI.
 */
@Serializable
data class DetalleMarcadorPayload(
    val v: Int = 1,
    val anotadores: List<AnotadorMarcadorLinea> = emptyList(),
)

@Serializable
data class AnotadorMarcadorLinea(
    @SerialName("jugador_remote_id") val jugadorRemoteId: String? = null,
    @SerialName("nombre_mostrado") val nombreMostrado: String,
    val cantidad: Int = 1,
)

object DetalleMarcadorJsonCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(payload: DetalleMarcadorPayload): String = json.encodeToString(payload)

    fun decodeOrNull(raw: String?): DetalleMarcadorPayload? {
        if (raw.isNullOrBlank()) return null
        return runCatching { json.decodeFromString<DetalleMarcadorPayload>(raw) }.getOrNull()
    }
}
