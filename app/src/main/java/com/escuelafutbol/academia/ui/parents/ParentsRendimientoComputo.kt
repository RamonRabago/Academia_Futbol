package com.escuelafutbol.academia.ui.parents

import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.data.local.model.normalizarClaveCategoriaNombre
import com.escuelafutbol.academia.data.remote.AcademiaCompetenciasRepository
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaPartidoRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaRow
import com.escuelafutbol.academia.data.remote.dto.CompetenciaPartidoEstado
import com.escuelafutbol.academia.data.remote.dto.DetalleMarcadorJsonCodec
import java.time.LocalDate
import java.util.Locale

data class ProximoPartidoPadreUi(
    val competenciaNombre: String,
    val rival: String,
    val fechaIso: String,
    val hora: String?,
    val sede: String?,
)

data class HijoRendimientoCompPadreUi(
    /** Suma de goles del alumno en partidos jugados (detalle de marcador en nube). */
    val totalGolesLigas: Int,
    val proximoPartido: ProximoPartidoPadreUi?,
    /** Letras G / E / P del equipo en los últimos partidos con marcador válido (máx. 5). */
    val ultimosResultadosEquipo: List<String>,
    /** Cuántos de [ultimosResultadosEquipo] el alumno anotó al menos un gol. */
    val partidosConGolEnVentana: Int,
)

private fun remoteIdsCoinciden(a: String?, b: String?): Boolean {
    val x = a?.trim()?.takeIf { it.isNotEmpty() } ?: return false
    val y = b?.trim()?.takeIf { it.isNotEmpty() } ?: return false
    return x.equals(y, ignoreCase = true)
}

private fun golesJugadorEnPartido(partido: AcademiaCompetenciaPartidoRow, jugadorRemoteId: String): Int {
    val payload = DetalleMarcadorJsonCodec.decodeOrNull(partido.detalleMarcadorJson) ?: return 0
    return payload.anotadores.sumOf { linea ->
        if (remoteIdsCoinciden(linea.jugadorRemoteId, jugadorRemoteId)) linea.cantidad.coerceAtLeast(0) else 0
    }
}

private fun letraResultadoEquipo(partido: AcademiaCompetenciaPartidoRow): String? {
    val a = partido.scorePropio
    val b = partido.scoreRival
    if (a == null || b == null) return null
    return when {
        a > b -> "G"
        a < b -> "P"
        else -> "E"
    }
}

private fun parseFechaPartido(partido: AcademiaCompetenciaPartidoRow): LocalDate? =
    runCatching { LocalDate.parse(partido.fecha.trim()) }.getOrNull()

private fun candidatoProximoPartido(partido: AcademiaCompetenciaPartidoRow, hoy: LocalDate): Boolean {
    if (partido.jugado) return false
    val est = partido.estado.trim().lowercase(Locale.ROOT)
    if (est == CompetenciaPartidoEstado.CANCELADO) return false
    val f = parseFechaPartido(partido) ?: return false
    return !f.isBefore(hoy)
}

/**
 * Agrega datos de competencias ya expuestos por [AcademiaCompetenciasRepository] (solo lectura).
 * No persiste ni altera reglas de negocio existentes.
 */
suspend fun computarRendimientoCompPadrePorJugadores(
    repo: AcademiaCompetenciasRepository,
    academiaId: String,
    jugadores: List<Jugador>,
): Map<Long, HijoRendimientoCompPadreUi> {
    if (jugadores.isEmpty()) return emptyMap()
    val comps = repo.listarCompetencias(academiaId).getOrElse { return emptyMap() }
    val hoy = LocalDate.now()
    val out = LinkedHashMap<Long, HijoRendimientoCompPadreUi>()
    for (j in jugadores) {
        val rid = j.remoteId?.trim()?.takeIf { it.isNotEmpty() } ?: continue
        val catNorm = normalizarClaveCategoriaNombre(j.categoria)
        var totalGoles = 0
        val jugadosConFecha = mutableListOf<Pair<LocalDate, AcademiaCompetenciaPartidoRow>>()
        val proximosCandidatos = mutableListOf<Pair<LocalDate, Pair<AcademiaCompetenciaPartidoRow, String>>>()
        for (comp in comps) {
            val insc = repo.listarInscripciones(comp.id)
            val idsCat = insc
                .asSequence()
                .filter { normalizarClaveCategoriaNombre(it.categoriaNombre) == catNorm }
                .map { it.id }
                .toSet()
            if (idsCat.isEmpty()) continue
            val partidos = repo.listarPartidos(comp.id).filter { it.categoriaEnCompetenciaId in idsCat }
            for (p in partidos) {
                if (p.jugado) {
                    totalGoles += golesJugadorEnPartido(p, rid)
                    parseFechaPartido(p)?.let { fd -> jugadosConFecha.add(fd to p) }
                } else if (candidatoProximoPartido(p, hoy)) {
                    val fd = parseFechaPartido(p) ?: continue
                    proximosCandidatos.add(fd to (p to comp.nombre))
                }
            }
        }
        val proximo = proximosCandidatos.minByOrNull { it.first }?.second?.let { (p, nombreComp) ->
            ProximoPartidoPadreUi(
                competenciaNombre = nombreComp.trim().ifEmpty { "—" },
                rival = p.rival.trim().ifEmpty { "—" },
                fechaIso = p.fecha.trim(),
                hora = p.hora?.trim()?.takeIf { it.isNotEmpty() },
                sede = p.sede?.trim()?.takeIf { it.isNotEmpty() },
            )
        }
        val ultimosOrdenados = jugadosConFecha
            .sortedByDescending { it.first }
            .map { it.second }
        val letrasVentana = mutableListOf<String>()
        val ventanaPartidos = mutableListOf<AcademiaCompetenciaPartidoRow>()
        for (p in ultimosOrdenados) {
            if (letrasVentana.size >= 5) break
            val letra = letraResultadoEquipo(p) ?: continue
            letrasVentana.add(letra)
            ventanaPartidos.add(p)
        }
        var marcoEnVentana = 0
        for (p in ventanaPartidos) {
            if (golesJugadorEnPartido(p, rid) > 0) marcoEnVentana++
        }
        out[j.id] = HijoRendimientoCompPadreUi(
            totalGolesLigas = totalGoles,
            proximoPartido = proximo,
            ultimosResultadosEquipo = letrasVentana,
            partidosConGolEnVentana = marcoEnVentana,
        )
    }
    return out
}
