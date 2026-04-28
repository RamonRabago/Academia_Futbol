package com.escuelafutbol.academia.ui.finanzas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.entity.CobroMensualAlumno
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.AppTintedPanel
import com.escuelafutbol.academia.ui.design.ChipsGroup
import com.escuelafutbol.academia.ui.design.EmptyState
import com.escuelafutbol.academia.ui.design.SectionHeader
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FinanzasScreen(
    viewModel: FinanzasViewModel,
    puedeVerFinanzas: Boolean,
    /** Padre/tutor en nube: no cargar ni mostrar finanzas de staff aunque la ruta se componga por error. */
    bloqueoPadreEnNube: Boolean = false,
) {
    if (!puedeVerFinanzas || bloqueoPadreEnNube) {
        val titulo = stringResource(R.string.tab_finances)
        val cuerpo = if (bloqueoPadreEnNube) {
            stringResource(R.string.role_route_blocked_body)
        } else {
            stringResource(R.string.finance_no_permission_hint)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
            contentAlignment = Alignment.Center,
        ) {
            EmptyState(title = titulo, subtitle = cuerpo)
        }
        return
    }

    val state by viewModel.uiState.collectAsState()
    var lineaEditar by remember { mutableStateOf<FinanzaLineaAlumno?>(null) }
    var lineaNuevo by remember { mutableStateOf<Jugador?>(null) }
    var tabIndex by remember { mutableIntStateOf(0) }

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

    val tabTitles = buildList {
        add(stringResource(R.string.finance_tab_summary))
        add(stringResource(R.string.finance_tab_students))
        if (mostrarTabNomina) add(stringResource(R.string.finance_tab_payroll))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AcademiaDimens.paddingScreenHorizontal)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingListSection),
    ) {
        SectionHeader(
            title = stringResource(R.string.tab_finances),
            subtitle = state.periodoTitulo.ifEmpty { state.periodoYyyyMm },
            action = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMicro),
                ) {
                    IconButton(
                        onClick = { viewModel.periodoAnterior() },
                        modifier = Modifier.size(AcademiaDimens.avatarRow),
                    ) {
                        Icon(
                            Icons.Filled.ChevronLeft,
                            contentDescription = stringResource(R.string.finance_month_prev_cd),
                        )
                    }
                    IconButton(
                        onClick = { viewModel.periodoSiguiente() },
                        modifier = Modifier.size(AcademiaDimens.avatarRow),
                    ) {
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = stringResource(R.string.finance_month_next_cd),
                        )
                    }
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(2f),
            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingListSection),
        ) {
            FinanzasCompactTabRow(
                titles = tabTitles,
                selectedIndex = tabIndex,
                onSelect = { tabIndex = it },
            )

            SectionHeader(
                title = stringResource(R.string.finance_header_scope_title),
                subtitle = stringResource(R.string.finance_header_scope_subtitle),
            )
            ChipsGroup {
                FilterChip(
                    selected = state.alcance is FinanzasAlcance.GeneralAcademia,
                    onClick = { viewModel.setAlcanceGeneral() },
                    label = {
                        Text(
                            stringResource(R.string.finance_scope_all_academy),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
                state.categoriasDisponibles.forEach { cat ->
                    val sel = (state.alcance as? FinanzasAlcance.SoloCategoria)?.nombre == cat
                    FilterChip(
                        selected = sel,
                        onClick = { viewModel.setAlcanceCategoria(cat) },
                        label = {
                            Text(
                                cat,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }

            SectionHeader(
                title = stringResource(R.string.finance_header_actions_title),
                subtitle = stringResource(R.string.finance_header_actions_subtitle),
            )
            OutlinedButton(
                onClick = { viewModel.prellenarMesConCuotasAlumnos() },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp, max = 48.dp),
            ) {
                Text(
                    stringResource(R.string.finance_fill_month_button),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        when (tabIndex) {
            0 -> Column(
                Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth()
                    .zIndex(0f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingListSection),
            ) {
                SectionHeader(
                    title = stringResource(R.string.finance_summary_section),
                    subtitle = stringResource(R.string.finance_header_balance_subtitle),
                )
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
                    AppTintedPanel(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
                    ) {
                        Text(
                            stringResource(R.string.finance_payroll_only_general_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                if (state.alcance is FinanzasAlcance.GeneralAcademia && state.porCategoria.isNotEmpty()) {
                    SectionHeader(
                        title = stringResource(R.string.finance_by_category),
                        subtitle = stringResource(R.string.finance_header_categories_subtitle),
                    )
                    state.porCategoria.forEach { cat ->
                        AppCard(
                            elevated = false,
                        ) {
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                Spacer(Modifier.padding(bottom = AcademiaDimens.paddingCard))
            }
            1 -> LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth()
                    .zIndex(0f),
                contentPadding = PaddingValues(bottom = AcademiaDimens.paddingCard),
                verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
            ) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.finance_students_section),
                        subtitle = stringResource(R.string.finance_header_students_subtitle),
                    )
                }
                if (state.lineas.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(R.string.finance_students_empty_title),
                            subtitle = stringResource(R.string.finance_students_empty_scope),
                        )
                    }
                } else {
                    items(state.lineas, key = { it.jugador.id }) { linea ->
                        AlumnoCobroRow(
                            linea = linea,
                            onEditar = { lineaEditar = linea },
                            onRegistrar = { lineaNuevo = linea.jugador },
                        )
                    }
                }
            }
            2 -> Column(
                Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth()
                    .zIndex(0f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingListSection),
            ) {
                SectionHeader(
                    title = stringResource(R.string.finance_tab_payroll),
                    subtitle = stringResource(R.string.finance_header_payroll_subtitle),
                )
                ResumenCard(
                    titulo = stringResource(R.string.finance_staff_total_monthly),
                    valor = formatMoney(state.totalSueldosStaff),
                )
                state.staffOrdenado
                    .filter { it.sueldoMensual != null && it.sueldoMensual!! > 0 }
                    .forEach { s ->
                        AppCard(elevated = false) {
                            Row(
                                Modifier.fillMaxWidth(),
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
                AppTintedPanel(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
                ) {
                    Text(
                        stringResource(R.string.finance_staff_edit_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.padding(bottom = AcademiaDimens.paddingCard))
            }
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
private fun FinanzasCompactTabRow(
    titles: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    val shape = RoundedCornerShape(AcademiaDimens.radiusDense)
    AppTintedPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentPadding = PaddingValues(
            horizontal = AcademiaDimens.gapSm,
            vertical = 2.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape),
        ) {
            titles.forEachIndexed { i, title ->
                val selected = selectedIndex == i
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clickable { onSelect(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = AcademiaDimens.gapMicro,
                                vertical = 4.dp,
                            ),
                    ) {
                        Text(
                            title,
                            style = if (selected) {
                                MaterialTheme.typography.labelLarge
                            } else {
                                MaterialTheme.typography.labelMedium
                            },
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AcademiaDimens.paddingCardCompact)
                                .height(2.dp)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(1.dp),
                                ),
                        )
                    }
                }
            }
        }
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
    AppCard(
        elevated = true,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd)) {
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
private fun AlumnoTarjetaMontoColumn(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            valor,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
    val dash = stringResource(R.string.finance_student_amount_dash)
    val esperadoNum: Double? = when {
        c != null -> c.importeEsperado
        !j.becado && j.mensualidad != null && j.mensualidad > 0 -> j.mensualidad
        else -> null
    }
    val pagadoNum: Double? = c?.importePagado
    val pendienteLinea: Double? =
        if (c != null) (c.importeEsperado - c.importePagado).coerceAtLeast(0.0) else null

    val estadoLabel: String
    val estadoFondo: Color
    val estadoTexto: Color
    when {
        j.becado -> {
            estadoLabel = stringResource(R.string.finance_line_scholarship)
            estadoFondo = MaterialTheme.colorScheme.tertiaryContainer
            estadoTexto = MaterialTheme.colorScheme.onTertiaryContainer
        }
        c == null && !j.becado -> {
            estadoLabel = stringResource(R.string.finance_student_status_no_record)
            estadoFondo = MaterialTheme.colorScheme.surfaceContainerHigh
            estadoTexto = MaterialTheme.colorScheme.onSurfaceVariant
        }
        pendienteLinea != null && pendienteLinea > 0.005 -> {
            estadoLabel = stringResource(R.string.finance_student_status_pending)
            estadoFondo = MaterialTheme.colorScheme.errorContainer
            estadoTexto = MaterialTheme.colorScheme.onErrorContainer
        }
        else -> {
            estadoLabel = stringResource(R.string.finance_student_status_paid)
            estadoFondo = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            estadoTexto = MaterialTheme.colorScheme.onPrimaryContainer
        }
    }

    AppCard(elevated = false) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        j.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        j.categoria,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(AcademiaDimens.radiusSm),
                    color = estadoFondo,
                ) {
                    Text(
                        estadoLabel,
                        modifier = Modifier.padding(
                            horizontal = AcademiaDimens.paddingCardCompact,
                            vertical = AcademiaDimens.gapMicro,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = estadoTexto,
                        maxLines = 1,
                    )
                }
            }
            HorizontalDivider(
                thickness = AcademiaDimens.dividerThickness,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                AlumnoTarjetaMontoColumn(
                    stringResource(R.string.finance_balance_row_expected),
                    esperadoNum?.let { formatMoney(it) } ?: dash,
                    Modifier.weight(1f),
                )
                AlumnoTarjetaMontoColumn(
                    stringResource(R.string.finance_balance_row_paid),
                    pagadoNum?.let { formatMoney(it) } ?: dash,
                    Modifier.weight(1f),
                )
                AlumnoTarjetaMontoColumn(
                    stringResource(R.string.finance_balance_row_pending),
                    pendienteLinea?.let { formatMoney(it) } ?: dash,
                    Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    c != null -> {
                        TextButton(onClick = onEditar) {
                            Text(
                                stringResource(R.string.player_edit),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                    !j.becado -> {
                        OutlinedButton(onClick = onRegistrar) {
                            Text(stringResource(R.string.finance_register_month))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenCard(titulo: String, valor: String) {
    AppCard(elevated = true) {
        Column(verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapSm)) {
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
                verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingDialogBlock),
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
                verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingDialogBlock),
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
