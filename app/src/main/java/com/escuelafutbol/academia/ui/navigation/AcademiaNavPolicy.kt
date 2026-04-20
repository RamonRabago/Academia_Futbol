package com.escuelafutbol.academia.ui.navigation

import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.model.puedeVerMensualidadEnEsteDispositivo
import com.escuelafutbol.academia.data.local.model.rolDispositivoEfectivo
import java.util.Locale

/**
 * Rutas del `NavHost` principal (`inicio`, `jugadores`, …).
 * Mismo criterio que la barra inferior (solo Inicio / Padres / Academia) y el menú superior (`tabsMenuDesplegable`), más atajos de Inicio (`AcademiaRoot` + `InicioScreen.accesoRapidoVisible`). La ruta `competencias` exige academia en nube y oculta a padres en nube.
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
    /** Competencias y partidos: solo academia vinculada a la nube; padres en nube quedan fuera hasta fase de solo lectura. */
    if (route == "competencias") {
        if (config.remoteAcademiaId.isNullOrBlank()) return false
        if (cloudRol == "parent") return false
        return true
    }
    if (config.remoteAcademiaId != null && cloudRol == "parent") {
        return route == "inicio" || route == "contenido" || route == "padres" || route == "academia"
    }
    if (route == "padres") {
        return rolDispositivo.puedeVerPestañaPadres()
    }
    return true
}
