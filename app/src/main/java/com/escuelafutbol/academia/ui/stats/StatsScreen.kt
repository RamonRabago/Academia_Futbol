package com.escuelafutbol.academia.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.model.puedeVerMensualidadEnEsteDispositivo
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    configAcademia: AcademiaConfig,
    sessionAuthUserId: String = "",
) {
    val stats by viewModel.stats.collectAsState()
    val uidSesion = sessionAuthUserId.takeIf { it.isNotBlank() }
    val puedeCuotas = configAcademia.puedeVerMensualidadEnEsteDispositivo(uidSesion)
    var mostrarDetalleCuotas by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            stringResource(R.string.tab_stats),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(stringResource(R.string.stats_summary), style = MaterialTheme.typography.titleSmall)
            StatCard(
                title = stringResource(R.string.total_players),
                value = stats.totalJugadores.toString(),
            )
            StatCard(
                title = stringResource(R.string.avg_attendance),
                value = stats.porcentajeAsistenciaGlobal?.let { pct ->
                    String.format(Locale.getDefault(), "%.1f%%", pct)
                } ?: "—",
            )
            StatCard(
                title = stringResource(R.string.days_with_records),
                value = stats.diasConRegistro.toString(),
            )
            if (stats.hayMarcasSinDiaEntreno) {
                Text(
                    stringResource(R.string.stats_training_day_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            if (puedeCuotas) {
                val c = stats.cuotasResumen
                Text(
                    stringResource(R.string.stats_fees_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    stringResource(R.string.stats_fees_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatCard(
                    title = stringResource(R.string.stats_fees_total_monthly),
                    value = formatImporte(c.totalMensual),
                )
                StatCard(
                    title = stringResource(R.string.stats_fees_scholarship_count),
                    value = c.nBecados.toString(),
                )
                StatCard(
                    title = stringResource(R.string.stats_fees_paying_count),
                    value = c.nConCuota.toString(),
                )
                StatCard(
                    title = stringResource(R.string.stats_fees_undefined_count),
                    value = c.nSinCuota.toString(),
                )
                Text(
                    stringResource(R.string.stats_fees_by_category),
                    style = MaterialTheme.typography.titleSmall,
                )
                c.porCategoria.forEach { cat ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(cat.categoria, style = MaterialTheme.typography.titleSmall)
                            Text(
                                stringResource(
                                    R.string.stats_fees_cat_line,
                                    cat.alumnosConCuota,
                                    cat.becados,
                                    cat.sinCuotaDefinida,
                                    formatImporte(cat.totalMensual),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                OutlinedButton(
                    onClick = { mostrarDetalleCuotas = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.stats_fees_detail_button))
                }
            }
    }

    if (mostrarDetalleCuotas && puedeCuotas) {
        val lineas = stats.cuotasResumen.lineas
        AlertDialog(
            onDismissRequest = { mostrarDetalleCuotas = false },
            title = { Text(stringResource(R.string.stats_fees_detail_title)) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    items(lineas, key = { it.jugadorId }) { linea ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        ) {
                            Text(linea.nombre, style = MaterialTheme.typography.titleSmall)
                            Text(
                                linea.categoria,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                when {
                                    linea.becado -> stringResource(R.string.stats_fees_line_becado)
                                    linea.mensualidad != null && linea.mensualidad > 0 ->
                                        stringResource(
                                            R.string.stats_fees_line_pays,
                                            formatImporte(linea.mensualidad),
                                        )
                                    else -> stringResource(R.string.stats_fees_line_undefined)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDetalleCuotas = false }) {
                    Text(stringResource(R.string.close))
                }
            },
        )
    }
}

private fun formatImporte(valor: Double): String =
    NumberFormat.getCurrencyInstance(Locale.getDefault()).format(valor)

@Composable
private fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
