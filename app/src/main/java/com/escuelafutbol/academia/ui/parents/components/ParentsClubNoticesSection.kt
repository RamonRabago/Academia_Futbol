package com.escuelafutbol.academia.ui.parents.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

private data class FiltroChipOpcion(
    val tipo: String?,
    val labelRes: Int,
)

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
    val opcionesFiltro = remember {
        listOf(
            FiltroChipOpcion(null, R.string.parent_inbox_filter_all),
            FiltroChipOpcion(MensajeCategoriaTipo.PARTIDO_EVENTO, R.string.msg_type_partido),
            FiltroChipOpcion(MensajeCategoriaTipo.CONVIVIO_LOGISTICA, R.string.msg_type_convivio),
            FiltroChipOpcion(MensajeCategoriaTipo.ADMINISTRATIVO, R.string.msg_type_admin),
            FiltroChipOpcion(MensajeCategoriaTipo.OTRO, R.string.msg_type_otro),
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.parent_inbox_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    stringResource(R.string.parent_inbox_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
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

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 2.dp),
        ) {
            items(opcionesFiltro, key = { it.tipo ?: "all" }) { op ->
                val selected = when (op.tipo) {
                    null -> filtroTipo == null
                    else -> filtroTipo == op.tipo
                }
                FilterChip(
                    selected = selected,
                    onClick = { onFiltroTipoChange(op.tipo) },
                    label = {
                        Text(
                            stringResource(op.labelRes),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                )
            }
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
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Outlined.Inbox,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.parent_inbox_empty_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(R.string.parent_inbox_empty_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        } else if (!mensajesState.cargando) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsFiltrados.forEach { m ->
                    ParentsInboxMessageCard(m, dateTimeFmt)
                }
            }
        }
    }
}
