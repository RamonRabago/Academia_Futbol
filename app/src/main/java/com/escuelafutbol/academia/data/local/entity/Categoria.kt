package com.escuelafutbol.academia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias")
data class Categoria(
    @PrimaryKey val nombre: String,
    val remoteId: String? = null,
    /** Portada propia de la categoría (archivo local). */
    val portadaRutaAbsoluta: String? = null,
    /** URL en Storage tras sincronizar. */
    val portadaUrlSupabase: String? = null,
)
