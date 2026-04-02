package com.escuelafutbol.academia.data.local.model

import com.escuelafutbol.academia.data.local.entity.AcademiaConfig

/**
 * Quién usa la app **en este dispositivo** (no confundir con el rol de una persona en el staff).
 * Determina qué pestañas y secciones se muestran y si se ven datos sensibles (p. ej. mensualidad).
 */
enum class RolDispositivo {
    PADRE_TUTOR,
    PROFESOR,
    COORDINADOR,
    DUENO_ACADEMIA,
    ;

    companion object {
        fun fromStored(value: String): RolDispositivo =
            entries.find { it.name == value } ?: PADRE_TUTOR
    }

    /** Modo familia: sin acceso a gestión del club ni pestaña «Padres». */
    fun esModoFamilia(): Boolean = this == PADRE_TUTOR

    /** Personal del club (profesor, coordinador o dueño en este dispositivo). */
    fun esPersonalClub(): Boolean = !esModoFamilia()

    /** Pestaña «Padres» (avisos a tutores): solo tiene sentido para quien gestiona el club. */
    fun puedeVerPestañaPadres(): Boolean = esPersonalClub()
}

/** true si el rol actual del dispositivo puede ver mensualidades según permisos de la academia. */
fun AcademiaConfig.puedeVerMensualidadEnEsteDispositivo(): Boolean {
    val rol = RolDispositivo.fromStored(rolDispositivo)
    return when (rol) {
        RolDispositivo.PADRE_TUTOR -> false
        RolDispositivo.PROFESOR -> mensualidadVisibleProfesor
        RolDispositivo.COORDINADOR -> mensualidadVisibleCoordinador
        RolDispositivo.DUENO_ACADEMIA -> mensualidadVisibleDueno
    }
}
