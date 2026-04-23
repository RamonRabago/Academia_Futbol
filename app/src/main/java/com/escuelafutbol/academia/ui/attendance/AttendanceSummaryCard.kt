package com.escuelafutbol.academia.ui.attendance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import java.util.Locale

@Composable
fun AttendanceSummaryCard(
    resumen: AsistenciaResumenUi,
    onPeriodoChange: (AsistenciaPeriodoResumen) -> Unit,
    onAnioAnterior: () -> Unit,
    onAnioSiguiente: () -> Unit,
    modifier: Modifier = Modifier,
    /** Si true, al abrir la pantalla solo se muestra una línea; el detalle se despliega al tocar. */
    colapsadoInicial: Boolean = true,
) {
    var ayudaResumen by remember { mutableStateOf(false) }
    var expandido by remember { mutableStateOf(!colapsadoInicial) }

    if (ayudaResumen) {
        AlertDialog(
            onDismissRequest = { ayudaResumen = false },
            title = { Text(stringResource(R.string.attendance_help_dialog_title)) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        stringResource(R.string.attendance_help_summary_section_period),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        stringResource(R.string.attendance_summary_scope_month),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.attendance_summary_scope_year),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider()
                    Text(
                        stringResource(R.string.attendance_help_summary_section_percent),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        stringResource(R.string.attendance_summary_footnote),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { ayudaResumen = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }

    val pctText = resumen.porcentaje?.let { p ->
        String.format(Locale.getDefault(), "%.0f%%", p)
    } ?: "—"
    val lineaCompacta = stringResource(
        R.string.attendance_summary_collapsed_line,
        pctText,
        resumen.presentes,
        resumen.ausentes,
        resumen.totalRegistros,
        resumen.etiqueta,
    )
    val frac = ((resumen.porcentaje ?: 0f).coerceIn(0f, 100f)) / 100f

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { expandido = !expandido },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Column(Modifier.weight(1f, fill = false)) {
                        Text(
                            stringResource(R.string.attendance_summary_title),
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!expandido) {
                            Text(
                                lineaCompacta,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Icon(
                        imageVector = if (expandido) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = stringResource(
                            if (expandido) {
                                R.string.attendance_summary_collapse_cd
                            } else {
                                R.string.attendance_summary_expand_cd
                            },
                        ),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(
                    onClick = { ayudaResumen = true },
                    modifier = Modifier.padding(start = 2.dp),
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.attendance_help_summary_cd),
                        modifier = Modifier.padding(2.dp),
                    )
                }
            }

            if (!expandido && resumen.totalRegistros > 0) {
                LinearProgressIndicator(
                    progress = { frac },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    gapSize = 0.dp,
                )
            }

            AnimatedVisibility(
                visible = expandido,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    resumen.nombreAlumnoFoco?.let { nombre ->
                        Text(
                            stringResource(R.string.attendance_summary_only_player, nombre),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FilterChip(
                            selected = resumen.periodo == AsistenciaPeriodoResumen.MesVista,
                            onClick = { onPeriodoChange(AsistenciaPeriodoResumen.MesVista) },
                            label = { Text(stringResource(R.string.attendance_summary_mode_month)) },
                            modifier = Modifier.weight(1f),
                        )
                        FilterChip(
                            selected = resumen.periodo == AsistenciaPeriodoResumen.AnioCompleto,
                            onClick = { onPeriodoChange(AsistenciaPeriodoResumen.AnioCompleto) },
                            label = { Text(stringResource(R.string.attendance_summary_mode_year)) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    HorizontalDivider()

                    when (resumen.periodo) {
                        AsistenciaPeriodoResumen.MesVista -> Text(
                            resumen.etiqueta,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                        AsistenciaPeriodoResumen.AnioCompleto -> Surface(
                            tonalElevation = 1.dp,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                IconButton(onClick = onAnioAnterior) {
                                    Icon(
                                        Icons.Default.ChevronLeft,
                                        contentDescription = stringResource(R.string.attendance_summary_year_prev_cd),
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.widthIn(min = 120.dp),
                                ) {
                                    Text(
                                        stringResource(R.string.attendance_summary_mode_year),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        resumen.etiqueta,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                }
                                IconButton(onClick = onAnioSiguiente) {
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = stringResource(R.string.attendance_summary_year_next_cd),
                                    )
                                }
                            }
                        }
                    }

                    if (resumen.totalRegistros == 0) {
                        Text(
                            if (resumen.hayMarcasFueraDeDiasEntreno) {
                                stringResource(R.string.attendance_summary_no_data_entreno)
                            } else {
                                stringResource(R.string.attendance_summary_no_data)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            stringResource(
                                R.string.attendance_summary_days_took_list,
                                resumen.diasConRegistro,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.attendance_summary_percent_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    stringResource(
                                        R.string.attendance_summary_counts,
                                        resumen.presentes,
                                        resumen.ausentes,
                                        resumen.totalRegistros,
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                pctText,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        LinearProgressIndicator(
                            progress = { frac },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            gapSize = 0.dp,
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                }
            }
        }
    }
}
