package com.escuelafutbol.academia.ui.parents.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.ChipsGroup
import com.escuelafutbol.academia.ui.design.EmptyState
import com.escuelafutbol.academia.ui.design.SectionHeader
import com.escuelafutbol.academia.ui.parents.MensajeCategoriaTipo
import com.escuelafutbol.academia.ui.parents.MensajeCategoriaUi
import com.escuelafutbol.academia.ui.parents.ParentsMensajesUiState
import java.text.DateFormat
import java.util.Date

private data class FiltroTipoOpcion(
    val tipo: String?,
    val labelRes: Int,
)

/** Prioridad visual derivada del tipo (sin columna en backend). */
private enum class AvisoPrioridadVisual {
    Importante,
    Normal,
    Informativo,
}

private fun prioridadPorTipo(tipo: String): AvisoPrioridadVisual = when (tipo) {
    MensajeCategoriaTipo.PARTIDO_EVENTO,
    MensajeCategoriaTipo.ADMINISTRATIVO -> AvisoPrioridadVisual.Importante
    MensajeCategoriaTipo.CONVIVIO_LOGISTICA -> AvisoPrioridadVisual.Normal
    else -> AvisoPrioridadVisual.Informativo
}

@Composable
private fun prioridadLabel(p: AvisoPrioridadVisual): String = when (p) {
    AvisoPrioridadVisual.Importante -> stringResource(R.string.parent_aviso_priority_important)
    AvisoPrioridadVisual.Normal -> stringResource(R.string.parent_aviso_priority_normal)
    AvisoPrioridadVisual.Informativo -> stringResource(R.string.parent_aviso_priority_info)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParentsClubNoticesSection(
    filtroTipo: String?,
    onFiltroTipoChange: (String?) -> Unit,
    filtroCategoria: String?,
    onFiltroCategoriaChange: (String?) -> Unit,
    mensajesState: ParentsMensajesUiState,
    itemsFiltrados: List<MensajeCategoriaUi>,
    onRefresh: () -> Unit,
    dateTimeFmt: DateFormat,
    onAvisoClick: (MensajeCategoriaUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshCd = stringResource(R.string.parent_inbox_refresh_cd)
    val opcionesTipo = remember {
        listOf(
            FiltroTipoOpcion(null, R.string.parent_inbox_filter_all),
            FiltroTipoOpcion(MensajeCategoriaTipo.PARTIDO_EVENTO, R.string.msg_type_partido),
            FiltroTipoOpcion(MensajeCategoriaTipo.CONVIVIO_LOGISTICA, R.string.msg_type_convivio),
            FiltroTipoOpcion(MensajeCategoriaTipo.ADMINISTRATIVO, R.string.msg_type_admin),
            FiltroTipoOpcion(MensajeCategoriaTipo.OTRO, R.string.msg_type_otro),
        )
    }
    val categoriasDisponibles = remember(mensajesState.items) {
        mensajesState.items
            .map { it.categoriaNombre.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
    }

    AppCard(
        modifier = modifier.fillMaxWidth(),
        elevated = false,
    ) {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingListSection),
        ) {
            SectionHeader(
                title = stringResource(R.string.parent_inbox_title),
                subtitle = stringResource(R.string.parent_inbox_subtitle),
                modifier = Modifier.fillMaxWidth(),
                action = {
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
                },
            )

            SectionHeader(title = stringResource(R.string.parent_avisos_filter_category))
            ChipsGroup {
                FilterChip(
                    selected = filtroCategoria == null,
                    onClick = { onFiltroCategoriaChange(null) },
                    label = { Text(stringResource(R.string.parent_inbox_filter_all)) },
                )
                categoriasDisponibles.forEach { cat ->
                    FilterChip(
                        selected = filtroCategoria == cat,
                        onClick = {
                            onFiltroCategoriaChange(
                                if (filtroCategoria == cat) null else cat,
                            )
                        },
                        label = { Text(cat) },
                    )
                }
            }

            SectionHeader(title = stringResource(R.string.parent_avisos_filter_type))
            ChipsGroup {
                opcionesTipo.forEach { op ->
                    val selected = when (op.tipo) {
                        null -> filtroTipo == null
                        else -> filtroTipo == op.tipo
                    }
                    FilterChip(
                        selected = selected,
                        onClick = { onFiltroTipoChange(op.tipo) },
                        label = { Text(stringResource(op.labelRes)) },
                    )
                }
            }

            if (mensajesState.cargando) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingRowComfort),
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
                EmptyState(
                    title = stringResource(R.string.parent_inbox_empty_title),
                    subtitle = stringResource(R.string.parent_inbox_empty_subtitle),
                    modifier = Modifier.padding(vertical = AcademiaDimens.paddingCardCompact),
                    icon = {
                        Icon(
                            Icons.Outlined.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(AcademiaDimens.iconSizeMd + AcademiaDimens.iconSizeSm),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        )
                    },
                )
            } else if (!mensajesState.cargando) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    itemsFiltrados.forEach { m ->
                        ParentsAvisoHistorialCard(
                            m = m,
                            dateTimeFmt = dateTimeFmt,
                            onClick = { onAvisoClick(m) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParentsAvisoDetalleDialog(
    mensaje: MensajeCategoriaUi,
    dateTimeFmt: DateFormat,
    onDismiss: () -> Unit,
    onEntendido: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                mensaje.titulo,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    dateTimeFmt.format(Date(mensaje.createdAtMillis)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    parentsInboxTipoEtiqueta(mensaje.tipo),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = AcademiaDimens.gapSm),
                )
                Text(
                    mensaje.categoriaNombre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
                HorizontalDivider(Modifier.padding(vertical = AcademiaDimens.gapMd))
                Text(
                    mensaje.cuerpo,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        confirmButton = {
            Button(onClick = onEntendido) {
                Text(stringResource(R.string.parent_aviso_entendido))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}

@Composable
private fun ParentsAvisoHistorialCard(
    m: MensajeCategoriaUi,
    dateTimeFmt: DateFormat,
    onClick: () -> Unit,
) {
    val unreadCd = stringResource(R.string.parent_aviso_unread_cd)
    val prioridad = prioridadPorTipo(m.tipo)
    val prioridadColor = when (prioridad) {
        AvisoPrioridadVisual.Importante -> MaterialTheme.colorScheme.error
        AvisoPrioridadVisual.Normal -> MaterialTheme.colorScheme.primary
        AvisoPrioridadVisual.Informativo -> MaterialTheme.colorScheme.tertiary
    }
    val preview = remember(m.cuerpo) {
        m.cuerpo.trim().replace("\n", " ").let { t ->
            if (t.length <= 140) t else t.take(137).trimEnd() + "…"
        }
    }
    val cardAlpha = if (m.leido) 0.78f else 1f
    AppCard(
        elevated = !m.leido,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .clickable(onClick = onClick),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 88.dp),
            horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
        ) {
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(prioridadColor, shape = RoundedCornerShape(3.dp)),
            )
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapSm),
                ) {
                    if (!m.leido) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .semantics { contentDescription = unreadCd },
                        )
                    }
                    Text(
                        m.titulo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (m.leido) FontWeight.Medium else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = AcademiaDimens.gapSm),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AcademiaDimens.gapMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        dateTimeFmt.format(Date(m.createdAtMillis)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        prioridadLabel(prioridad),
                        style = MaterialTheme.typography.labelSmall,
                        color = prioridadColor,
                    )
                }
                Row(
                    modifier = Modifier.padding(top = AcademiaDimens.gapSm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapSm),
                ) {
                    Text(
                        parentsInboxTipoEtiqueta(m.tipo),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text("·", color = MaterialTheme.colorScheme.outline)
                    Text(
                        m.categoriaNombre,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
