package com.escuelafutbol.academia.data.local.entity

import androidx.room.Entity

/**
 * Última categoría elegida en el selector de sesión, por usuario de Auth (UUID).
 * `categoriaNombre` null = «Todas las categorías».
 */
@Entity(
    tableName = "session_categoria_reciente",
    primaryKeys = ["userId"],
)
data class SessionCategoriaReciente(
    val userId: String,
    val categoriaNombre: String? = null,
)
