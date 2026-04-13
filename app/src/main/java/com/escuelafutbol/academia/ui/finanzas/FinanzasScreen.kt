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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
    var tabIndex by remember { mutableIntStateOf(0) }
    var dialogoCategorias by remember { mutableStateOf(false) }

    val mostrarTabNomina = state.alcance is FinanzasAlcance.GeneralAcademia
    val tabCount = if (mostrarTabNomina) 3 else 2

    LaunchedEffect(tabCount, tabIndex) {
        if (tabIndex >= tabCount) tabIndex = tabCount - 1
    }

    LaunchedEffect(state.categoriasDisponibles, state.alcance) {
        val a = state.alcance
        if (a is FinanzasAlcance.SoloCategoria && a.nombre !in state.categoriasDisponibles) {
            viewModel.setAlcanceGeneral()
        }
    }

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

    val tabTitles = buildList {
        add(stringResource(R.string.finance_tab_summary))
        add(stringResource(R.string.finance_tab_students))
        if (mostrarTabNomina) add(stringResource(R.string.finance_tab_payroll))
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_finances)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
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

            FilledTonalButton(
                onClick = { viewModel.prellenarMesConCuotasAlumnos() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.finance_fill_month_button))
            }

            Spacer(Modifier.padding(top = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = state.alcance is FinanzasAlcance.GeneralAcademia,
                    onClick = { viewModel.setAlcanceGeneral() },
                    label = { Text(stringResource(R.string.finance_scope_all_academy)) },
                )
                if (state.categoriasDisponibles.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { dialogoCategorias = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            when (val a = state.alcance) {
                                is FinanzasAlcance.SoloCategoria -> a.nombre
                                else -> stringResource(R.string.finance_scope_category_placeholder)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(Modifier.padding(bottom = 8.dp))

            TabRow(selectedTabIndex = tabIndex) {
                tabTitles.forEachIndexed { i, title ->
                    Tab(
                        selected = tabIndex == i,
                        onClick = { tabIndex = i },
                        text = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    )
                }
            }

            HorizontalDivider()

            when (tabIndex) {
                0 -> Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Spacer(Modifier.padding(top = 4.dp))
                    BalanceMesCompactCard(
                        titulo = when (val a = state.alcance) {
                            is FinanzasAlcance.SoloCategoria ->
                                stringResource(R.string.finance_balance_month_title_category, a.nombre)
                            else -> stringResource(R.string.finance_balance_month_title)
                        },
                        adeudoHistorico = state.adeudoHistorico,
                        esperado = state.totalEsperadoMes,
                        pagado = state.totalPagadoMes,
                        pendiente = state.pendienteMes,
                    )
                    if (state.alcance is FinanzasAlcance.SoloCategoria) {
                        Text(
                            stringResource(R.string.finance_payroll_only_general_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (state.alcance is FinanzasAlcance.GeneralAcademia && state.porCategoria.isNotEmpty()) {
                        Text(
                            stringResource(R.string.finance_by_category),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        state.porCategoria.forEach { cat ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            ) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                    TextButton(
                                        onClick = {
                                            viewModel.setAlcanceCategoria(cat.categoria)
                                            tabIndex = 1
                                        },
                                    ) {
                                        Text(stringResource(R.string.finance_cat_drill_down))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.padding(bottom = 24.dp))
                }
                1 -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { Spacer(Modifier.padding(top = 8.dp)) }
                    if (state.lineas.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.finance_students_empty_scope),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 16.dp),
                            )
                        }
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
                2 -> Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Spacer(Modifier.padding(top = 8.dp))
                    ResumenCard(
                        titulo = stringResource(R.string.finance_staff_total_monthly),
                        valor = formatMoney(state.totalSueldosStaff),
                    )
                    state.staffOrdenado
                        .filter { it.sueldoMensual != null && it.sueldoMensual!! > 0 }
                        .forEach { s ->
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
                                    Text(
                                        s.nombre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        formatMoney(s.sueldoMensual!!),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    Text(
                        stringResource(R.string.finance_staff_edit_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.padding(bottom = 24.dp))
                }
            }
        }
    }

    if (dialogoCategorias && state.categoriasDisponibles.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { dialogoCategorias = false },
            title = { Text(stringResource(R.string.finance_scope_category_menu)) },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    state.categoriasDisponibles.forEach { cat ->
                        TextButton(
                            onClick = {
                                viewModel.setAlcanceCategoria(cat)
                                dialogoCategorias = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(cat, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogoCategorias = false }) {
                    Text(stringResource(R.string.close))
                }
            },
        )
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
private fun BalanceMesCompactCard(
    titulo: String,
    adeudoHistorico: Double,
    esperado: Double,
    pagado: Double,
    pendiente: Double,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(R.string.finance_balance_historic_short) + " · " + formatMoney(adeudoHistorico),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BalanceMiniColumn(
                    stringResource(R.string.finance_balance_row_expected),
                    formatMoney(esperado),
                )
                BalanceMiniColumn(
                    stringResource(R.string.finance_balance_row_paid),
                    formatMoney(pagado),
                )
                BalanceMiniColumn(
                    stringResource(R.string.finance_balance_row_pending),
                    formatMoney(pendiente),
                    emphasize = pendiente > 0,
                )
            }
        }
    }
}

@Composable
private fun BalanceMiniColumn(etiqueta: String, valor: String, emphasize: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            valor,
            style = MaterialTheme.typography.titleMedium,
            color = if (emphasize) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
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
