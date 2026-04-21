package com.escuelafutbol.academia.ui.parents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.escuelafutbol.academia.ui.parents.components.EmptyChildrenState
import com.escuelafutbol.academia.ui.parents.components.LinkedChildCard
import com.escuelafutbol.academia.ui.parents.components.ParentsClubNoticesSection
import com.escuelafutbol.academia.ui.parents.components.ParentsLinkChildPanel
import com.escuelafutbol.academia.ui.parents.components.ParentsSummaryCard
import com.escuelafutbol.academia.ui.parents.components.linkedChildStableKey
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
) {
    var filtroTipo by remember { mutableStateOf<String?>(null) }
    var hijoParaDesvincular by remember { mutableStateOf<HijoResumenUi?>(null) }
    var expandedChildKey by remember { mutableStateOf<String?>(null) }
    var linkPanelOpenNonce by remember { mutableIntStateOf(0) }
    val rendimientoMap by viewModel.rendimientoCompPadrePorJugador.collectAsState()
    val rendimientoCargando by viewModel.rendimientoCompPadreCargando.collectAsState()
    val itemsFiltrados = remember(mensajesState.items, filtroTipo) {
        if (filtroTipo == null) mensajesState.items
        else mensajesState.items.filter { it.tipo == filtroTipo }
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
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 4.dp),
        ) {
            if (!remoteAcademiaId.isNullOrBlank()) {
                item {
                    ParentsClubNoticesSection(
                        filtroTipo = filtroTipo,
                        onFiltroTipoChange = { filtroTipo = it },
                        mensajesState = mensajesState,
                        itemsFiltrados = itemsFiltrados,
                        onRefresh = {
                            onRefrescarMensajes()
                            scope.launch {
                                snackbar.showSnackbar(context.getString(R.string.parent_inbox_updated))
                            }
                        },
                        dateTimeFmt = dateTimeFmt,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                item {
                    HorizontalDivider(Modifier.padding(vertical = 5.dp))
                }
            }

            item {
                Text(
                    stringResource(R.string.parent_my_children_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    stringResource(R.string.parent_my_children_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                )
                ParentsSummaryCard(
                    hijosCount = c.hijos.size,
                    totalAdeudoVencido = c.totalAdeudoVencido,
                    reglaLimitePagoActiva = c.reglaLimitePagoActiva,
                    moneyFmt = moneyFmt,
                    onAddChild = { linkPanelOpenNonce++ },
                    modifier = Modifier.padding(bottom = 8.dp),
                )
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
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            if (c.reglaLimitePagoActiva && c.totalAdeudoVencido > 0.009) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    ) {
                        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                stringResource(R.string.parent_payment_reminder_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Text(
                                stringResource(R.string.parent_payment_reminder_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            Text(
                                stringResource(
                                    R.string.parent_payment_total_label,
                                    moneyFmt.format(c.totalAdeudoVencido),
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }
            if (!c.reglaLimitePagoActiva) {
                item {
                    Text(
                        stringResource(R.string.parent_payment_no_deadline_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
