package com.escuelafutbol.academia.data.sync

/**
 * Resultado de enlazar la sesión de Supabase con una fila `academias` (dueño o miembro).
 */
sealed class AcademiaBindingResult {

    data class Ok(val academiaId: String) : AcademiaBindingResult()

    /** No es dueño ni tiene membresías: crear academia nueva o unirse por código. */
    data object NeedsOnboarding : AcademiaBindingResult()

    /** Varias academias como miembro: elegir una. */
    data class PickAcademy(val options: List<AcademiaBindingOption>) : AcademiaBindingResult()
}

data class AcademiaBindingOption(
    val academiaId: String,
    val nombre: String,
    val rol: String,
)
