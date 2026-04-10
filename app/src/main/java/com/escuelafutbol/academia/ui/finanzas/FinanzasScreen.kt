package com.escuelafutbol.academia.ui.finanzas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.entity.CobroMensualAlumno
import com.escuelafutbol.academia.data.local.entity.Jugador
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanzasScreen(
    viewModel: FinanzasViewModel,
    puedeVerFinanzas: Boolean,
) {
    val state by viewModel.uiState.collectAsState()
    var lineaEditar by remember { mutableStateOf<FinanzaLineaAlumno?>(null) }
    var lineaNuevo by remember { mutableStateOf<Jugador?>(null) }

    if (!puedeVerFinanzas) {
        Scaffold(
            topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_finances)) }) },
        ) { padding ->
            Text(
                stringResource(R.string.finance_no_permission_hint),
                modifier = Modifier.padding(padding).padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        return
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_finances)) }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = { viewModel.periodoAnterior() }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.finance_month_prev_cd))
                    }
                    Text(
                        state.periodoTitulo.ifEmpty { state.periodoYyyyMm },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    IconButton(onClick = { viewModel.periodoSiguiente() }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.finance_month_next_cd))
                    }
                }
            }
            item {
                FilledTonalButton(
                    onClick = { viewModel.prellenarMesConCuotasAlumnos() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.finance_fill_month_button))
                }
            }
            item {
                Text(stringResource(R.string.finance_summary_section), style = MaterialTheme.typography.titleSmall)
            }
            item {
                ResumenCard(
                    titulo = stringResource(R.string.finance_total_historic_debt),
                    valor = formatMoney(state.adeudoHistorico),
                )
            }
            item {
                ResumenCard(
                    titulo = stringResource(R.string.finance_month_expected),
                    valor = formatMoney(state.totalEsperadoMes),
                )
            }
            item {
                ResumenCard(
                    titulo = stringResource(R.string.finance_month_paid),
                    valor = formatMoney(state.totalPagadoMes),
                )
            }
            item {
                ResumenCard(
                    titulo = stringResource(R.string.finance_month_pending),
                    valor = formatMoney(state.pendienteMes),
                )
            }
            item {
                Text(stringResource(R.string.finance_by_category), style = MaterialTheme.typography.titleSmall)
            }
            items(state.porCategoria, key = { it.categoria }) { cat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(cat.categoria, style = MaterialTheme.typography.titleSmall)
                        Text(
                            stringResource(
                                R.string.finance_cat_line,
                                cat.conRegistro,
                                formatMoney(cat.esperado),
                                formatMoney(cat.pagado),
                                formatMoney(cat.pendiente),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            item {
                Text(stringResource(R.string.finance_staff_payroll), style = MaterialTheme.typography.titleSmall)
            }
            item {
                ResumenCard(
                    titulo = stringResource(R.string.finance_staff_total_monthly),
                    valor = formatMoney(state.totalSueldosStaff),
                )
            }
            items(
                state.staffOrdenado.filter { it.sueldoMensual != null && it.sueldoMensual!! > 0 },
                key = { it.id },
            ) { s ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(s.nombre, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(
                            formatMoney(s.sueldoMensual!!),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            item {
                Text(
                    stringResource(R.string.finance_staff_edit_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(stringResource(R.string.finance_students_section), style = MaterialTheme.typography.titleSmall)
            }
            items(state.lineas, key = { it.jugador.id }) { linea ->
                AlumnoCobroRow(
                    linea = linea,
                    onEditar = { lineaEditar = linea },
                    onRegistrar = { lineaNuevo = linea.jugador },
                )
            }
            item { Spacer(Modifier.padding(bottom = 24.dp)) }
        }
    }

    lineaEditar?.let { ln ->
        val c = ln.cobro
        if (c != null) {
            DialogoEditarCobro(
                nombreAlumno = ln.jugador.nombre,
                cobro = c,
                onDismiss = { lineaEditar = null },
                onGuardar = { esp, pag, notas ->
                    viewModel.actualizarCobro(c, esp, pag, notas)
                    lineaEditar = null
                },
            )
        }
    }

    lineaNuevo?.let { j ->
        DialogoNuevoCobro(
            jugador = j,
            defaultEsperado = j.mensualidad?.takeIf { it > 0 && !j.becado },
            onDismiss = { lineaNuevo = null },
            onGuardar = { esp, pag, notas ->
                viewModel.registrarCobroManual(j.id, esp, pag, notas)
                lineaNuevo = null
            },
        )
    }
}

@Composable
private fun AlumnoCobroRow(
    linea: FinanzaLineaAlumno,
    onEditar: () -> Unit,
    onRegistrar: () -> Unit,
) {
    val j = linea.jugador
    val c = linea.cobro
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(j.nombre, style = MaterialTheme.typography.titleSmall)
                    Text(
                        j.categoria,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                when {
                    j.becado -> Text(
                        stringResource(R.string.finance_line_scholarship),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    c != null -> Column(horizontalAlignment = Alignment.End) {
                        Text(
                            stringResource(
                                R.string.finance_line_amounts,
                                formatMoney(c.importePagado),
                                formatMoney(c.importeEsperado),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        val pend = (c.importeEsperado - c.importePagado).coerceAtLeast(0.0)
                        if (pend > 0) {
                            Text(
                                stringResource(R.string.finance_line_pending, formatMoney(pend)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        TextButton(onClick = onEditar) {
                            Text(stringResource(R.string.player_edit))
                        }
                    }
                    else -> OutlinedButton(onClick = onRegistrar) {
                        Text(stringResource(R.string.finance_register_month))
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenCard(titulo: String, valor: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titulo, style = MaterialTheme.typography.bodyMedium)
            Text(valor, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun DialogoEditarCobro(
    nombreAlumno: String,
    cobro: CobroMensualAlumno,
    onDismiss: () -> Unit,
    onGuardar: (Double, Double, String?) -> Unit,
) {
    var espTxt by remember(cobro) { mutableStateOf(cobro.importeEsperado.toString()) }
    var pagTxt by remember(cobro) { mutableStateOf(cobro.importePagado.toString()) }
    var notasTxt by remember(cobro) { mutableStateOf(cobro.notas.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(nombreAlumno) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = espTxt,
                    onValueChange = { espTxt = it },
                    label = { Text(stringResource(R.string.finance_expected)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = pagTxt,
                    onValueChange = { pagTxt = it },
                    label = { Text(stringResource(R.string.finance_paid)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notasTxt,
                    onValueChange = { notasTxt = it },
                    label = { Text(stringResource(R.string.finance_notes_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val esp = espTxt.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val pag = pagTxt.replace(',', '.').toDoubleOrNull() ?: 0.0
                    onGuardar(esp, pag, notasTxt.trim().takeIf { it.isNotEmpty() })
                },
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun DialogoNuevoCobro(
    jugador: Jugador,
    defaultEsperado: Double?,
    onDismiss: () -> Unit,
    onGuardar: (Double, Double, String?) -> Unit,
) {
    var espTxt by remember(jugador) {
        mutableStateOf(defaultEsperado?.toString().orEmpty().ifEmpty { "0" })
    }
    var pagTxt by remember { mutableStateOf("0") }
    var notasTxt by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.finance_register_title, jugador.nombre)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = espTxt,
                    onValueChange = { espTxt = it },
                    label = { Text(stringResource(R.string.finance_expected)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = pagTxt,
                    onValueChange = { pagTxt = it },
                    label = { Text(stringResource(R.string.finance_paid)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notasTxt,
                    onValueChange = { notasTxt = it },
                    label = { Text(stringResource(R.string.finance_notes_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val esp = espTxt.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val pag = pagTxt.replace(',', '.').toDoubleOrNull() ?: 0.0
                    onGuardar(esp, pag, notasTxt.trim().takeIf { it.isNotEmpty() })
                },
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

private fun formatMoney(v: Double): String =
    NumberFormat.getCurrencyInstance(Locale.getDefault()).format(v)
