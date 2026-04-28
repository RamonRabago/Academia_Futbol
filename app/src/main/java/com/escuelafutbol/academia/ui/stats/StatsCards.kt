package com.escuelafutbol.academia.ui.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard

/**
 * Tarjeta grande (hero): número principal arriba, etiqueta debajo.
 */
@Composable
fun StatsCardHero(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        elevated = true,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        includeContentPadding = false,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AcademiaDimens.paddingCardCompact,
                    vertical = AcademiaDimens.paddingCardCompact + AcademiaDimens.gapSm,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapVerticalTight),
        ) {
            Text(
                value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Tarjeta mediana (cuadrícula): altura fija, número arriba, etiqueta abajo.
 */
@Composable
fun StatsCardMedium(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .height(AcademiaDimens.statsMetricTileHeight),
        elevated = false,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        includeContentPadding = false,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(
                    horizontal = AcademiaDimens.paddingCardCompact,
                    vertical = AcademiaDimens.gapMd,
                ),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Marco a ancho completo (resúmenes con varias columnas).
 */
@Composable
fun StatsCardFrame(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        elevated = false,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        includeContentPadding = false,
    ) {
        Column(
            Modifier.padding(
                horizontal = AcademiaDimens.paddingCardCompact,
                vertical = AcademiaDimens.gapMd,
            ),
            content = content,
        )
    }
}
