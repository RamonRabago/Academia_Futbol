package com.escuelafutbol.academia.ui.parents.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.parents.MensajeCategoriaTipo
import com.escuelafutbol.academia.ui.parents.MensajeCategoriaUi
import com.escuelafutbol.academia.ui.parents.ParentsMensajesUiState
import java.text.DateFormat

/**
 * Módulo visual de **Avisos del club**: cabecera, filtros, actualizar, estado vacío y lista de mensajes.
 * La lista se renderiza aquí de forma explícita para mantener el módulo autocontenido (feed dentro de la misma Card).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParentsClubNoticesSection(
    filtroTipo: String?,
    onFiltroTipoChange: (String?) -> Unit,
    mensajesState: ParentsMensajesUiState,
    itemsFiltrados: List<MensajeCategoriaUi>,
    onRefresh: () -> Unit,
    dateTimeFmt: DateFormat,
    modifier: Modifier = Modifier,
) {
    val refreshCd = stringResource(R.string.parent_inbox_refresh_cd)
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.parent_inbox_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        stringResource(R.string.parent_inbox_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.semantics {
                        contentDescription = refreshCd
                    },
                ) {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filtroTipo == null,
                    onClick = { onFiltroTipoChange(null) },
                    label = { Text(stringResource(R.string.parent_inbox_filter_all)) },
                )
                FilterChip(
                    selected = filtroTipo == MensajeCategoriaTipo.PARTIDO_EVENTO,
                    onClick = { onFiltroTipoChange(MensajeCategoriaTipo.PARTIDO_EVENTO) },
                    label = { Text(stringResource(R.string.msg_type_partido)) },
                )
                FilterChip(
                    selected = filtroTipo == MensajeCategoriaTipo.CONVIVIO_LOGISTICA,
                    onClick = { onFiltroTipoChange(MensajeCategoriaTipo.CONVIVIO_LOGISTICA) },
                    label = { Text(stringResource(R.string.msg_type_convivio)) },
                )
                FilterChip(
                    selected = filtroTipo == MensajeCategoriaTipo.ADMINISTRATIVO,
                    onClick = { onFiltroTipoChange(MensajeCategoriaTipo.ADMINISTRATIVO) },
                    label = { Text(stringResource(R.string.msg_type_admin)) },
                )
                FilterChip(
                    selected = filtroTipo == MensajeCategoriaTipo.OTRO,
                    onClick = { onFiltroTipoChange(MensajeCategoriaTipo.OTRO) },
                    label = { Text(stringResource(R.string.msg_type_otro)) },
                )
            }

            if (mensajesState.cargando) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .heightIn(max = 28.dp)
                            .size(28.dp),
                        strokeWidth = 2.dp,
                    )
                    Text(
                        stringResource(R.string.parent_inbox_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            mensajesState.error?.let { err ->
                Text(
                    err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (!mensajesState.cargando && itemsFiltrados.isEmpty()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Outlined.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.parent_inbox_empty_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        stringResource(R.string.parent_inbox_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            } else if (!mensajesState.cargando) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    itemsFiltrados.forEach { m ->
                        ParentsInboxMessageCard(m, dateTimeFmt)
                    }
                }
            }
        }
    }
}
