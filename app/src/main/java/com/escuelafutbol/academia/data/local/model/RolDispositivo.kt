package com.escuelafutbol.academia.data.local.model

import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import java.util.Locale

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

/**
 * Mapea el rol de `academia_miembros` / dueño de cuenta (`owner`) al modo de dispositivo.
 * Roles desconocidos devuelven null.
 */
fun rolDispositivoSugeridoDesdeRolNube(cloudRol: String?): RolDispositivo? {
    val r = cloudRol?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotEmpty() } ?: return null
    return when (r) {
        "parent" -> RolDispositivo.PADRE_TUTOR
        "coach" -> RolDispositivo.PROFESOR
        "coordinator" -> RolDispositivo.COORDINADOR
        "admin", "owner" -> RolDispositivo.DUENO_ACADEMIA
        else -> null
    }
}

/**
 * Modo de la app según la membresía en nube (sin selector manual).
 * Sin academia enlazada en nube: se asume gestión local como dueño.
 * Con academia en nube pero rol aún no resuelto: modo restrictivo hasta el sync.
 */
fun AcademiaConfig.rolDispositivoEfectivo(): RolDispositivo {
    rolDispositivoSugeridoDesdeRolNube(cloudMembresiaRol)?.let { return it }
    return if (remoteAcademiaId == null) RolDispositivo.DUENO_ACADEMIA else RolDispositivo.PADRE_TUTOR
}

/** true si el rol actual del dispositivo puede ver mensualidades según permisos de la academia. */
fun AcademiaConfig.puedeVerMensualidadEnEsteDispositivo(): Boolean {
    val rol = rolDispositivoEfectivo()
    return when (rol) {
        RolDispositivo.PADRE_TUTOR -> false
        RolDispositivo.PROFESOR -> mensualidadVisibleProfesor
        RolDispositivo.COORDINADOR -> mensualidadVisibleCoordinador
        RolDispositivo.DUENO_ACADEMIA -> mensualidadVisibleDueno
    }
}
