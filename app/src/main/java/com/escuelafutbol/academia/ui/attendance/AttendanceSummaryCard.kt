package com.escuelafutbol.academia.ui.attendance

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
) {
    var ayudaResumen by remember { mutableStateOf(false) }

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

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(R.string.attendance_summary_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { ayudaResumen = true },
                    modifier = Modifier.padding(start = 4.dp),
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.attendance_help_summary_cd),
                    )
                }
            }
            resumen.nombreAlumnoFoco?.let { nombre ->
                Text(
                    stringResource(R.string.attendance_summary_only_player, nombre),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                    style = MaterialTheme.typography.titleLarge,
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
                                style = MaterialTheme.typography.titleLarge,
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    stringResource(
                        R.string.attendance_summary_days_took_list,
                        resumen.diasConRegistro,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
                val pctText = resumen.porcentaje?.let { p ->
                    String.format(Locale.getDefault(), "%.1f%%", p)
                } ?: "—"
                val frac = ((resumen.porcentaje ?: 0f).coerceIn(0f, 100f)) / 100f
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                LinearProgressIndicator(
                    progress = { frac },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    gapSize = 0.dp,
                )
            }
        }
    }
}
