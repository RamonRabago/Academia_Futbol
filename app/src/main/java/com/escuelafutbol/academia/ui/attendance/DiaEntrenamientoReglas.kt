package com.escuelafutbol.academia.ui.attendance

import com.escuelafutbol.academia.data.local.entity.Asistencia
import com.escuelafutbol.academia.data.local.entity.DiaEntrenamiento

fun scopeKeyAsistencia(categoriaFiltro: String?): String =
    categoriaFiltro?.trim().orEmpty()

/**
 * Indica si [fechaDia] está marcado como día de entrenamiento para la vista actual ([scopeKeyVista]).
 * Una marca con [DiaEntrenamiento.scopeKey] vacío aplica a cualquier categoría concreta, no a la vista «todas».
 */
fun diaMarcadoComoEntrenamiento(
    fechaDia: Long,
    scopeKeyVista: String,
    marcas: Collection<DiaEntrenamiento>,
): Boolean = marcas.any { d ->
    d.fechaDia == fechaDia && (
        d.scopeKey == scopeKeyVista ||
            (scopeKeyVista.isNotEmpty() && d.scopeKey.isEmpty())
        )
}

/**
 * Cuenta presentes y ausentes en la rejilla (jugador × día de entrenamiento marcado).
 * Si no hay fila de [Asistencia] para un cupo, cuenta como **ausente** (igual que el interruptor por defecto en la UI).
 */
fun contarPresentesAusentesEntrenamientoImplicitos(
    jugadorIds: Collection<Long>,
    diasEntreno: Collection<DiaEntrenamiento>,
    asistencias: Collection<Asistencia>,
    scopeKeyVista: String,
): PresentesAusentesEntrenoConteo {
    val ids = jugadorIds.toSet()
    if (ids.isEmpty()) {
        return PresentesAusentesEntrenoConteo(presentes = 0, ausentes = 0, totalCupos = 0)
    }
    val fechasEntreno = diasEntreno
        .map { it.fechaDia }
        .distinct()
        .filter { diaMarcadoComoEntrenamiento(it, scopeKeyVista, diasEntreno) }
        .toSet()
    if (fechasEntreno.isEmpty()) {
        return PresentesAusentesEntrenoConteo(presentes = 0, ausentes = 0, totalCupos = 0)
    }
    val porJugadorDia = asistencias
        .filter { it.jugadorId in ids && it.fechaDia in fechasEntreno }
        .associateBy { it.jugadorId to it.fechaDia }
    var pres = 0
    var aus = 0
    for (dia in fechasEntreno) {
        for (jid in ids) {
            if (porJugadorDia[jid to dia]?.presente == true) {
                pres++
            } else {
                aus++
            }
        }
    }
    return PresentesAusentesEntrenoConteo(
        presentes = pres,
        ausentes = aus,
        totalCupos = pres + aus,
    )
}

data class PresentesAusentesEntrenoConteo(
    val presentes: Int,
    val ausentes: Int,
    val totalCupos: Int,
)
