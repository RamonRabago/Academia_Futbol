package com.escuelafutbol.academia.ui.navigation

import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.model.puedeVerMensualidadEnEsteDispositivo
import com.escuelafutbol.academia.data.local.model.rolDispositivoEfectivo
import java.util.Locale

/**
 * Rutas del `NavHost` principal (`inicio`, `jugadores`, …).
 * Mismo criterio que las pestañas inferiores y los atajos de Inicio (`AcademiaRoot` + `InicioScreen.accesoRapidoVisible`).
 */
fun rutaPrincipalVisible(route: String, config: AcademiaConfig): Boolean {
    val cloudRol = config.cloudMembresiaRol?.lowercase(Locale.ROOT)
    val rolDispositivo = config.rolDispositivoEfectivo()
    if (route == "finanzas") {
        if (config.remoteAcademiaId != null && cloudRol == "parent") return false
        return config.puedeVerMensualidadEnEsteDispositivo()
    }
    if (config.remoteAcademiaId != null && cloudRol == "parent") {
        return route == "inicio" || route == "padres" || route == "academia"
    }
    if (route == "padres") {
        return rolDispositivo.puedeVerPestañaPadres()
    }
    return true
}
