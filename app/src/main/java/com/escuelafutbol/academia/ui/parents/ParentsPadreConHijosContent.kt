package com.escuelafutbol.academia.ui.parents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.PrimaryButton
import com.escuelafutbol.academia.ui.design.SectionHeader
import com.escuelafutbol.academia.ui.parents.components.EmptyChildrenState
import com.escuelafutbol.academia.ui.parents.components.LinkedChildCard
import com.escuelafutbol.academia.ui.parents.components.ParentsAvisoDetalleDialog
import com.escuelafutbol.academia.ui.parents.components.ParentsClubNoticesSection
import com.escuelafutbol.academia.ui.parents.components.ParentsLinkChildPanel
import com.escuelafutbol.academia.ui.parents.components.ParentsSummaryCard
import com.escuelafutbol.academia.ui.parents.components.linkedChildStableKey
import com.escuelafutbol.academia.notification.ParentRealNotificationCoordinator
import java.text.DateFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ParentsPadreConHijosContent(
    modifier: Modifier,
    viewModel: ParentsViewModel,
    c: ParentsTabContent.PadreConHijos,
    mensajesState: ParentsMensajesUiState,
    moneyFmt: java.text.NumberFormat,
    dateFmt: DateFormat,
    dateTimeFmt: DateFormat,
    onRefrescarMensajes: () -> Unit,
    remoteAcademiaId: String?,
    snackbar: SnackbarHostState,
    scope: CoroutineScope,
    context: android.content.Context,
    onNavigateToRoute: (String) -> Unit,
    padreNavegacionAtajos: List<PadresNavegacionAtajoUi>,
) {
    var filtroTipo by remember { mutableStateOf<String?>(null) }
    var filtroCategoria by remember { mutableStateOf<String?>(null) }
    var avisoDetalle by remember { mutableStateOf<MensajeCategoriaUi?>(null) }
    var hijoParaDesvincular by remember { mutableStateOf<HijoResumenUi?>(null) }
    var expandedChildKey by remember { mutableStateOf<String?>(null) }
    var linkPanelOpenNonce by remember { mutableIntStateOf(0) }
    val rendimientoMap by viewModel.rendimientoCompPadrePorJugador.collectAsState()
    val rendimientoCargando by viewModel.rendimientoCompPadreCargando.collectAsState()
    val leidos by viewModel.mensajesLeidosIds.collectAsState()
    val itemsConLeido = remember(mensajesState.items, leidos) {
        mensajesState.items.map { it.copy(leido = it.id in leidos) }
    }
    val itemsFiltrados = remember(itemsConLeido, filtroTipo, filtroCategoria) {
        itemsConLeido.filter { m ->
            (filtroTipo == null || m.tipo == filtroTipo) &&
                (filtroCategoria == null || m.categoriaNombre.trim().equals(filtroCategoria, ignoreCase = true))
        }.sortedByDescending { it.createdAtMillis }
    }

    LaunchedEffect(
        rendimientoMap,
        rendimientoCargando,
        c.hijos,
        mensajesState.items,
        mensajesState.cargando,
        remoteAcademiaId,
    ) {
        ParentRealNotificationCoordinator.evaluate(
            context = context,
            remoteAcademiaId = remoteAcademiaId,
            rendimientoCargando = rendimientoCargando,
            rendimientoMap = rendimientoMap,
            hijos = c.hijos,
            mensajesState = mensajesState,
        )
    }

    val hijoDlg = hijoParaDesvincular
    val vidDesvincular = hijoDlg?.vinculoId
    if (hijoDlg != null && vidDesvincular != null) {
        AlertDialog(
            onDismissRequest = { hijoParaDesvincular = null },
            title = { Text(stringResource(R.string.parent_unlink_confirm_title)) },
            text = {
                Text(
                    stringResource(R.string.parent_unlink_confirm_message, hijoDlg.nombre),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        hijoParaDesvincular = null
                        viewModel.desvincularMiHijo(vidDesvincular) { r ->
                            scope.launch {
                                if (r.isSuccess) {
                                    snackbar.showSnackbar(context.getString(R.string.parent_unlink_snackbar_ok))
                                } else {
                                    snackbar.showSnackbar(
                                        r.exceptionOrNull()?.message
                                            ?: context.getString(R.string.parent_unlink_error),
                                    )
                                }
                            }
                        }
                    },
                ) { Text(stringResource(R.string.parent_unlink_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { hijoParaDesvincular = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 0.dp,
                end = 0.dp,
                top = 0.dp,
                bottom = AcademiaDimens.spacingDialogBlock,
            ),
            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingListSection),
        ) {
            item {
                Column(Modifier.fillMaxWidth()) {
                    SectionHeader(
                        title = stringResource(R.string.parent_my_children_title),
                        subtitle = stringResource(R.string.parent_my_children_subtitle),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ParentsSummaryCard(
                        hijosCount = c.hijos.size,
                        totalAdeudoVencido = c.totalAdeudoVencido,
                        reglaLimitePagoActiva = c.reglaLimitePagoActiva,
                        moneyFmt = moneyFmt,
                        onAddChild = { linkPanelOpenNonce++ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = AcademiaDimens.gapMd),
                    )
                }
            }
            if (!remoteAcademiaId.isNullOrBlank()) {
                item {
                    ParentsLinkChildPanel(
                        viewModel = viewModel,
                        remoteAcademiaId = remoteAcademiaId,
                        snackbar = snackbar,
                        scope = scope,
                        context = context,
                        modifier = Modifier.fillMaxWidth(),
                        compactLayout = true,
                        hijosVinculadosCount = c.hijos.size,
                        openRequestNonce = linkPanelOpenNonce,
                    )
                }
            }
            if (c.hijos.isEmpty()) {
                item {
                    EmptyChildrenState()
                }
            }
            items(
                c.hijos,
                key = { it.linkedChildStableKey() },
            ) { hijo ->
                val key = hijo.linkedChildStableKey()
                LinkedChildCard(
                    hijo = hijo,
                    rendimiento = rendimientoMap[hijo.jugadorLocalId],
                    rendimientoCargando = rendimientoCargando,
                    expanded = expandedChildKey == key,
                    onExpandToggle = {
                        expandedChildKey = if (expandedChildKey == key) null else key
                    },
                    moneyFmt = moneyFmt,
                    dateFmt = dateFmt,
                    reglaLimitePagoActiva = c.reglaLimitePagoActiva,
                    onRequestUnlink = { hijoParaDesvincular = hijo },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (c.reglaLimitePagoActiva && c.totalAdeudoVencido > 0.009) {
                item {
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevated = false,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.22f),
                    ) {
                        Text(
                            stringResource(R.string.parent_payment_reminder_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            stringResource(R.string.parent_payment_reminder_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(top = AcademiaDimens.gapSm),
                        )
                        Text(
                            stringResource(
                                R.string.parent_payment_total_label,
                                moneyFmt.format(c.totalAdeudoVencido),
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(top = AcademiaDimens.gapMd),
                        )
                    }
                }
            }
            if (!c.reglaLimitePagoActiva) {
                item {
                    Text(
                        stringResource(R.string.parent_payment_no_deadline_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (!remoteAcademiaId.isNullOrBlank()) {
                item {
                    ParentsClubNoticesSection(
                        filtroTipo = filtroTipo,
                        onFiltroTipoChange = { filtroTipo = it },
                        filtroCategoria = filtroCategoria,
                        onFiltroCategoriaChange = { filtroCategoria = it },
                        mensajesState = mensajesState,
                        itemsFiltrados = itemsFiltrados,
                        onRefresh = {
                            onRefrescarMensajes()
                            scope.launch {
                                snackbar.showSnackbar(context.getString(R.string.parent_inbox_updated))
                            }
                        },
                        dateTimeFmt = dateTimeFmt,
                        onAvisoClick = { avisoDetalle = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (padreNavegacionAtajos.isNotEmpty()) {
                item {
                    PadresAccionesNavegacionBlock(
                        atajos = padreNavegacionAtajos,
                        onNavigateToRoute = onNavigateToRoute,
                    )
                }
            }
        }

        avisoDetalle?.let { detalle ->
            ParentsAvisoDetalleDialog(
                mensaje = detalle,
                dateTimeFmt = dateTimeFmt,
                onDismiss = { avisoDetalle = null },
                onEntendido = {
                    viewModel.marcarMensajeLeido(detalle.id)
                    avisoDetalle = null
                },
            )
        }
    }
}

@Composable
private fun PadresAccionesNavegacionBlock(
    atajos: List<PadresNavegacionAtajoUi>,
    onNavigateToRoute: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.parent_padres_nav_actions_title),
            subtitle = stringResource(R.string.parent_padres_nav_actions_subtitle),
            modifier = Modifier.fillMaxWidth(),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = AcademiaDimens.gapMd),
            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
        ) {
            atajos.forEachIndexed { index, item ->
                val label = stringResource(item.titleRes)
                if (index == 0) {
                    PrimaryButton(
                        text = label,
                        onClick = { onNavigateToRoute(item.route) },
                    )
                } else {
                    OutlinedButton(
                        onClick = { onNavigateToRoute(item.route) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = AcademiaDimens.buttonMinHeight),
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }
}
