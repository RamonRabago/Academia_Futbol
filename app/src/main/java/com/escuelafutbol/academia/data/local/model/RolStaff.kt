package com.escuelafutbol.academia.data.local.model

enum class RolStaff {
    PROFESOR,
    COORDINADOR,
    ;

    companion object {
        fun fromStored(value: String): RolStaff =
            entries.find { it.name == value } ?: PROFESOR
    }
}
