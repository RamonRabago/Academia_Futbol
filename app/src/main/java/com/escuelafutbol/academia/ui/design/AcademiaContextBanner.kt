package com.escuelafutbol.academia.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

/**
 * Línea de contexto reutilizable (categoría activa, vista familia, etc.) para cabeceras.
 */
@Composable
fun AcademiaContextBanner(
    contextText: String,
    modifier: Modifier = Modifier,
    emphasize: Boolean = false,
) {
    val bg = if (emphasize) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    Text(
        text = contextText,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AcademiaDimens.radiusMd))
            .background(bg)
            .padding(
                horizontal = AcademiaDimens.contextBannerHorizontalPadding,
                vertical = AcademiaDimens.contextBannerVerticalPadding,
            ),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
