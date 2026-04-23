package com.escuelafutbol.academia.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.model.esPadreMembresiaNube
import com.escuelafutbol.academia.data.local.model.puedeVerMensualidadEnEsteDispositivo
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    configAcademia: AcademiaConfig,
    sessionAuthUserId: String = "",
) {
    if (configAcademia.esPadreMembresiaNube()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(R.string.role_route_blocked_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp),
            )
        }
        return
    }
    val stats by viewModel.stats.collectAsState()
    val mesEconomia by viewModel.mesEconomia.collectAsState()
    val uidSesion = sessionAuthUserId.takeIf { it.isNotBlank() }
    val puedeCuotas = configAcademia.puedeVerMensualidadEnEsteDispositivo(uidSesion)
    var mostrarDetalleCuotas by remember { mutableStateOf(false) }
    var mostrarElegirMesEconomia by remember { mutableStateOf(false) }
    var ordenEconomia by remember { mutableStateOf(EconomiaOrden.Estimado) }
    var filtroEconomia by remember { mutableStateOf(EconomiaFiltro.Todas) }
    var categoriasExpandidas by remember { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(mesEconomia) {
        categoriasExpandidas = emptySet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.tab_stats),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        StatsCardHero(
            value = stats.porcentajeAsistenciaGlobal?.let { pct ->
                String.format(Locale.getDefault(), "%.1f%%", pct)
            } ?: "—",
            label = stringResource(R.string.avg_attendance),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatsCardMedium(
                value = stats.totalJugadores.toString(),
                label = stringResource(R.string.total_players),
                modifier = Modifier.weight(1f),
            )
            StatsCardMedium(
                value = stats.diasConRegistro.toString(),
                label = stringResource(R.string.days_with_records),
                modifier = Modifier.weight(1f),
            )
        }
        if (stats.hayMarcasSinDiaEntreno) {
            Text(
                stringResource(R.string.stats_training_day_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }

        if (puedeCuotas) {
            val c = stats.cuotasResumen
            val eco = stats.economiaPorCategoria

            Text(
                stringResource(R.string.stats_economy_section_title),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 4.dp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            val enMesCorriente = mesEconomia == YearMonth.now()
            val puedeIrMesSiguiente = mesEconomia.isBefore(YearMonth.now())

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Text(
                stringResource(R.string.stats_economy_heading_cobros),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
            )
            StatsEconomiaSelectorMesFila(
                puedeIrMesSiguiente = puedeIrMesSiguiente,
                habilitarBotonMesActual = !enMesCorriente,
                etiquetaMes = etiquetaMesAnio(mesEconomia),
                onAnterior = { viewModel.irMesEconomiaAnterior() },
                onSiguiente = { viewModel.irMesEconomiaSiguiente() },
                onMesActual = { viewModel.irMesEconomiaActual() },
                onElegirMes = { mostrarElegirMesEconomia = true },
            )
            Text(
                eco.etiquetaPeriodoHumano,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!eco.hayCobrosRegistradosEnSistema) {
                Text(
                    stringResource(R.string.stats_economy_no_cobros_short),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            StatsEconomiaCategoriasDashboard(
                eco = eco,
                orden = ordenEconomia,
                onOrdenChange = { ordenEconomia = it },
                filtro = filtroEconomia,
                onFiltroChange = { filtroEconomia = it },
                categoriasExpandidas = categoriasExpandidas,
                onToggleCategoria = { cat ->
                    categoriasExpandidas =
                        if (cat in categoriasExpandidas) categoriasExpandidas - cat
                        else categoriasExpandidas + cat
                },
                formatImporte = ::formatImporte,
            )

            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Text(
                stringResource(R.string.stats_fees_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatsCardMedium(
                        value = formatImporte(c.totalMensual),
                        label = stringResource(R.string.stats_fees_total_monthly),
                        modifier = Modifier.weight(1f),
                    )
                    StatsCardMedium(
                        value = c.nBecados.toString(),
                        label = stringResource(R.string.stats_fees_scholarship_count),
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatsCardMedium(
                        value = c.nConCuota.toString(),
                        label = stringResource(R.string.stats_fees_paying_count),
                        modifier = Modifier.weight(1f),
                    )
                    StatsCardMedium(
                        value = c.nSinCuota.toString(),
                        label = stringResource(R.string.stats_fees_undefined_count),
                        modifier = Modifier.weight(1f),
                    )
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

    if (mostrarElegirMesEconomia) {
        StatsEconomiaMonthPickerDialog(
            mesSeleccionado = mesEconomia,
            onElegir = { ym ->
                viewModel.setMesEconomia(ym)
                mostrarElegirMesEconomia = false
            },
            onDismiss = { mostrarElegirMesEconomia = false },
        )
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
private fun StatsEconomiaSelectorMesFila(
    puedeIrMesSiguiente: Boolean,
    habilitarBotonMesActual: Boolean,
    etiquetaMes: String,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    onMesActual: () -> Unit,
    onElegirMes: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(onClick = onAnterior) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.stats_economy_month_previous_cd),
            )
        }
        TextButton(
            onClick = onElegirMes,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                etiquetaMes,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
        IconButton(
            onClick = onSiguiente,
            enabled = puedeIrMesSiguiente,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.stats_economy_month_next_cd),
            )
        }
        TextButton(
            onClick = onMesActual,
            enabled = habilitarBotonMesActual,
        ) {
            Text(
                stringResource(R.string.stats_economy_month_this_month),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun StatsEconomiaMonthPickerDialog(
    mesSeleccionado: YearMonth,
    onElegir: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
) {
    val hoy = YearMonth.now()
    val opciones = remember(hoy) {
        buildList {
            var m = hoy
            repeat(48) {
                add(m)
                m = m.minusMonths(1)
            }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.stats_economy_month_pick_title)) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                items(opciones, key = { it.toString() }) { ym ->
                    val sel = ym == mesSeleccionado
                    TextButton(
                        onClick = { onElegir(ym) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            etiquetaMesAnio(ym),
                            style = if (sel) {
                                MaterialTheme.typography.titleSmall
                            } else {
                                MaterialTheme.typography.bodyLarge
                            },
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            color = if (sel) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}

