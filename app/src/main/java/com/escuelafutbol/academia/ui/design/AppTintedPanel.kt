package com.escuelafutbol.academia.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/**
 * Panel con fondo del tema y esquina tokenizada (sin contorno de tarjeta).
 * Para avisos, filas tintadas y bloques que no deben usar [Surface] suelto.
 */
@Composable
fun AppTintedPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(AcademiaDimens.radiusMd),
    containerColor: Color,
    contentPadding: PaddingValues = PaddingValues(AcademiaDimens.paddingCardCompact),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .clip(shape)
            .background(containerColor)
            .padding(contentPadding),
        content = content,
    )
}
