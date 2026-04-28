package com.escuelafutbol.academia.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    /** Si no es null, sustituye el color de contenedor por defecto del tema (p. ej. tarjetas por estado). */
    containerColor: Color? = null,
    /** Si no es null, sustituye el color del trazo del contorno en variante no elevada. */
    borderColor: Color? = null,
    /** Si no es null, sustituye el [BorderStroke] completo (grosor, selección, etc.). */
    border: BorderStroke? = null,
    /** Si es false, no aplica [AcademiaDimens.paddingCard] alrededor del contenido (p. ej. fila a ancho completo con barra lateral). */
    includeContentPadding: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(AcademiaDimens.radiusXl)
    val innerColumnModifier = if (includeContentPadding) {
        Modifier.padding(AcademiaDimens.paddingCard)
    } else {
        Modifier
    }
    if (elevated) {
        val colors = CardDefaults.cardColors(
            containerColor = containerColor ?: MaterialTheme.colorScheme.surface,
        )
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = innerColumnModifier, content = content)
        }
    } else {
        val colors = CardDefaults.outlinedCardColors(
            containerColor = containerColor ?: MaterialTheme.colorScheme.surface,
        )
        val borderResolved = border ?: BorderStroke(
            1.dp,
            borderColor ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        )
        OutlinedCard(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            border = borderResolved,
            elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = innerColumnModifier, content = content)
        }
    }
}
