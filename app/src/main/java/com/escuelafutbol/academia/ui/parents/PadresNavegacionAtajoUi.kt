package com.escuelafutbol.academia.ui.parents

import androidx.annotation.StringRes

/** Ruta principal + etiqueta para atajos de navegación en la pestaña Padres (solo UI). */
data class PadresNavegacionAtajoUi(
    val route: String,
    @StringRes val titleRes: Int,
)
