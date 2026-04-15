package com.escuelafutbol.academia.ui.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.attendance_summary_title),
                style = MaterialTheme.typography.titleMedium,
            )
            resumen.nombreAlumnoFoco?.let { nombre ->
                Text(
                    stringResource(R.string.attendance_summary_only_player, nombre),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Selector claro: uno relleno y el otro contorno
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (resumen.periodo == AsistenciaPeriodoResumen.MesVista) {
                    Button(
                        onClick = { onPeriodoChange(AsistenciaPeriodoResumen.MesVista) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.attendance_summary_mode_month))
                    }
                    OutlinedButton(
                        onClick = { onPeriodoChange(AsistenciaPeriodoResumen.AnioCompleto) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.attendance_summary_mode_year))
                    }
                } else {
                    OutlinedButton(
                        onClick = { onPeriodoChange(AsistenciaPeriodoResumen.MesVista) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.attendance_summary_mode_month))
                    }
                    Button(
                        onClick = { onPeriodoChange(AsistenciaPeriodoResumen.AnioCompleto) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.attendance_summary_mode_year))
                    }
                }
            }

            Text(
                when (resumen.periodo) {
                    AsistenciaPeriodoResumen.MesVista ->
                        stringResource(R.string.attendance_summary_scope_month)
                    AsistenciaPeriodoResumen.AnioCompleto ->
                        stringResource(R.string.attendance_summary_scope_year)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider()

            when (resumen.periodo) {
                AsistenciaPeriodoResumen.MesVista -> Text(
                    resumen.etiqueta,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                AsistenciaPeriodoResumen.AnioCompleto -> Surface(
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
                                style = MaterialTheme.typography.headlineSmall,
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        stringResource(R.string.attendance_summary_percent_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        resumen.porcentaje?.let { p ->
                            String.format(Locale.getDefault(), "%.1f %%", p)
                        } ?: "—",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                val frac = ((resumen.porcentaje ?: 0f).coerceIn(0f, 100f)) / 100f
                LinearProgressIndicator(
                    progress = { frac },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp),
                    gapSize = 0.dp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(
                        R.string.attendance_summary_counts,
                        resumen.presentes,
                        resumen.ausentes,
                        resumen.totalRegistros,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                stringResource(R.string.attendance_summary_footnote),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
