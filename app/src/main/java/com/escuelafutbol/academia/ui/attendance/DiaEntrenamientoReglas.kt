package com.escuelafutbol.academia.ui.attendance

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
