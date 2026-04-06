package com.escuelafutbol.academia.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "asistencias",
    foreignKeys = [
        ForeignKey(
            entity = Jugador::class,
            parentColumns = ["id"],
            childColumns = ["jugadorId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("jugadorId"),
        Index(value = ["jugadorId", "fechaDia"], unique = true),
    ],
)
data class Asistencia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jugadorId: Long,
    /** Inicio del día en millis (UTC) para agrupar por sesión. */
    val fechaDia: Long,
    val presente: Boolean,
    val remoteId: String? = null,
    /** True si hay cambios locales aún no reflejados en Supabase (insert o update). */
    val needsCloudPush: Boolean = false,
)
