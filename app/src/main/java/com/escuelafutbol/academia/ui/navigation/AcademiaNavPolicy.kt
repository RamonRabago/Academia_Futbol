package com.escuelafutbol.academia.ui.navigation

import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.model.puedeVerMensualidadEnEsteDispositivo
import com.escuelafutbol.academia.data.local.model.rolDispositivoEfectivo
import java.util.Locale

/**
 * Rutas del `NavHost` principal (`inicio`, `jugadores`, …).
 * Mismo criterio que la barra inferior (solo Inicio / Padres / Academia) y el menú superior (`tabsMenuDesplegable`), más atajos de Inicio (`AcademiaRoot` + `InicioScreen.accesoRapidoVisible`). La ruta `competencias` exige academia en nube; **padre en nube** la ve en **solo lectura** (Fase 3).
 */
fun rutaPrincipalVisible(
    route: String,
    config: AcademiaConfig,
    uidSesionAuth: String? = null,
): Boolean {
    val cloudRol = config.cloudMembresiaRol?.lowercase(Locale.ROOT)
    val rolDispositivo = config.rolDispositivoEfectivo(uidSesionAuth)
    if (route == "finanzas") {
        if (config.remoteAcademiaId != null && cloudRol == "parent") return false
        return config.puedeVerMensualidadEnEsteDispositivo(uidSesionAuth)
    }
    /** Hub «Equipo»: agrupa jugadores / asistencia / estadísticas / recursos / competencias (solo staff en nube). */
    if (route == "equipo_hub") {
        if (config.remoteAcademiaId != null && cloudRol == "parent") return false
        return listOf("jugadores", "asistencia", "estadisticas", "contenido", "competencias").any { sub ->
            rutaPrincipalVisible(sub, config, uidSesionAuth)
        }
    }
    /** Competencias: academia en nube; padre en nube accede en solo lectura (sin alta ni edición en UI). */
    if (route == "competencias") {
        return !config.remoteAcademiaId.isNullOrBlank()
    }
    if (config.remoteAcademiaId != null && cloudRol == "parent") {
        return route == "inicio" || route == "contenido" || route == "padres" || route == "academia"
    }
    if (route == "padres") {
        return rolDispositivo.puedeVerPestañaPadres()
    }
    return true
}
