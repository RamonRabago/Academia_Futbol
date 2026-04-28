package com.escuelafutbol.academia.ui.attendance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.AppTintedPanel
import com.escuelafutbol.academia.ui.design.ChipsGroup
import com.escuelafutbol.academia.ui.design.EmptyState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                    verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingDialogBlock),
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

    AppCard(
        modifier = modifier.fillMaxWidth(),
        elevated = true,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapSm),
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
                    horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapSm),
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
                    modifier = Modifier.padding(start = AcademiaDimens.gapMicro),
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.attendance_help_summary_cd),
                        modifier = Modifier.padding(AcademiaDimens.gapMicro),
                    )
                }
            }

            if (!expandido && resumen.totalRegistros > 0) {
                LinearProgressIndicator(
                    progress = { frac },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AcademiaDimens.gapMicro * 2),
                    gapSize = 0.dp,
                )
            }

            AnimatedVisibility(
                visible = expandido,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
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

                    ChipsGroup {
                        FilterChip(
                            selected = resumen.periodo == AsistenciaPeriodoResumen.MesVista,
                            onClick = { onPeriodoChange(AsistenciaPeriodoResumen.MesVista) },
                            label = { Text(stringResource(R.string.attendance_summary_mode_month)) },
                        )
                        FilterChip(
                            selected = resumen.periodo == AsistenciaPeriodoResumen.AnioCompleto,
                            onClick = { onPeriodoChange(AsistenciaPeriodoResumen.AnioCompleto) },
                            label = { Text(stringResource(R.string.attendance_summary_mode_year)) },
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
                        AsistenciaPeriodoResumen.AnioCompleto -> AppTintedPanel(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentPadding = PaddingValues(
                                vertical = AcademiaDimens.gapSm,
                                horizontal = AcademiaDimens.gapMicro,
                            ),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                IconButton(onClick = onAnioAnterior) {
                                    Icon(
                                        Icons.Filled.ChevronLeft,
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
                                        Icons.Filled.ChevronRight,
                                        contentDescription = stringResource(R.string.attendance_summary_year_next_cd),
                                    )
                                }
                            }
                        }
                    }

                    if (resumen.totalRegistros == 0) {
                        EmptyState(
                            title = if (resumen.hayMarcasFueraDeDiasEntreno) {
                                stringResource(R.string.attendance_summary_no_data_without_training_day)
                            } else {
                                stringResource(R.string.attendance_summary_no_data)
                            },
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
                            horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingRowComfort),
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
                                .height(AcademiaDimens.gapMd - AcademiaDimens.gapMicro),
                            gapSize = 0.dp,
                        )
                    }
                    Spacer(Modifier.height(AcademiaDimens.gapMicro))
                }
            }
        }
    }
}
