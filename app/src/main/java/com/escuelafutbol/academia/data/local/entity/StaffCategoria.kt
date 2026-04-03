package com.escuelafutbol.academia.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "staff_categorias",
    primaryKeys = ["staffId", "categoriaNombre"],
    foreignKeys = [
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["id"],
            childColumns = ["staffId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["nombre"],
            childColumns = ["categoriaNombre"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("staffId"),
        Index("categoriaNombre"),
    ],
)
data class StaffCategoria(
    val staffId: Long,
    val categoriaNombre: String,
)
