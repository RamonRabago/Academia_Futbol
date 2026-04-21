package com.escuelafutbol.academia.ui.parents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.ui.parents.components.ParentsInboxMessageCard
import com.escuelafutbol.academia.ui.parents.components.ParentsLinkChildPanel
import com.escuelafutbol.academia.ui.parents.components.ParentsSummaryCard
import com.escuelafutbol.academia.R
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentsScreen(
    viewModel: ParentsViewModel,
    remoteAcademiaId: String?,
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dateFmt = remember {
        DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
    }
    val dateTimeFmt = remember {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
    }
    val moneyFmt = remember { java.text.NumberFormat.getCurrencyInstance(Locale.getDefault()) }

    val contenidoFlow = remember { viewModel.contenidoSegunMembresia() }
    val contenido by contenidoFlow.collectAsState(initial = ParentsTabContent.StaffComunicaciones)
    val mensajesState by viewModel.mensajesNube.collectAsState()
    val enviandoMensaje by viewModel.enviandoMensaje.collectAsState()

    LaunchedEffect(remoteAcademiaId) {
        if (!remoteAcademiaId.isNullOrBlank()) {
            viewModel.refrescarMensajes()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {},
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            Text(
                stringResource(R.string.tab_parents),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (val c = contenido) {
            is ParentsTabContent.StaffComunicaciones -> {
                StaffPadresMensajesContent(
                    modifier = Modifier.fillMaxSize(),
                    remoteAcademiaId = remoteAcademiaId,
                    categorias = viewModel.categoriasParaMensajesStaff.collectAsState().value,
                    mensajesState = mensajesState,
                    enviandoMensaje = enviandoMensaje,
                    onRefrescar = { viewModel.refrescarMensajes() },
                    onEnviar = { cat, tipo, titulo, cuerpo ->
                        viewModel.enviarMensajeCategoria(cat, tipo, titulo, cuerpo) { result ->
                            scope.launch {
                                if (result.isSuccess) {
                                    snackbar.showSnackbar(context.getString(R.string.parent_msg_sent_ok))
                                } else {
                                    snackbar.showSnackbar(
                                        result.exceptionOrNull()?.message
                                            ?: context.getString(R.string.parent_msg_load_error),
                                    )
                                }
                            }
                        }
                    },
                    onRefrescarExito = {
                        scope.launch {
                            snackbar.showSnackbar(context.getString(R.string.parent_inbox_updated))
                        }
                    },
                    dateTimeFmt = dateTimeFmt,
                )
            }
            ParentsTabContent.PadreSinHijos -> {
                PadreSinHijosContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    viewModel = viewModel,
                    remoteAcademiaId = remoteAcademiaId,
                    snackbar = snackbar,
                    scope = scope,
                    context = context,
                    moneyFmt = moneyFmt,
                )
            }
            is ParentsTabContent.PadreConHijos -> {
                ParentsPadreConHijosContent(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    c = c,
                    mensajesState = mensajesState,
                    moneyFmt = moneyFmt,
                    dateFmt = dateFmt,
                    dateTimeFmt = dateTimeFmt,
                    onRefrescarMensajes = { viewModel.refrescarMensajes() },
                    remoteAcademiaId = remoteAcademiaId,
                    snackbar = snackbar,
                    scope = scope,
                    context = context,
                )
            }
                }
            }
        }
    }
}

@Composable
private fun PadreSinHijosContent(
    modifier: Modifier,
    viewModel: ParentsViewModel,
    remoteAcademiaId: String?,
    snackbar: SnackbarHostState,
    scope: CoroutineScope,
    context: android.content.Context,
    moneyFmt: java.text.NumberFormat,
) {
    var linkPanelOpenNonce by remember { mutableIntStateOf(0) }
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        Text(
            stringResource(R.string.parent_my_children_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            stringResource(R.string.parent_my_children_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        ParentsSummaryCard(
            hijosCount = 0,
            totalAdeudoVencido = 0.0,
            reglaLimitePagoActiva = false,
            moneyFmt = moneyFmt,
            onAddChild = { linkPanelOpenNonce++ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        )
        ParentsLinkChildPanel(
            viewModel = viewModel,
            remoteAcademiaId = remoteAcademiaId,
            snackbar = snackbar,
            scope = scope,
            context = context,
            modifier = Modifier.fillMaxWidth(),
            compactLayout = false,
            hijosVinculadosCount = 0,
            openRequestNonce = linkPanelOpenNonce,
        )
    }
}

@Composable
private fun StaffPadresMensajesContent(
    modifier: Modifier,
    remoteAcademiaId: String?,
    categorias: List<String>,
    mensajesState: ParentsMensajesUiState,
    enviandoMensaje: Boolean,
    onRefrescar: () -> Unit,
    onEnviar: (String, String, String, String) -> Unit,
    onRefrescarExito: () -> Unit,
    dateTimeFmt: DateFormat,
) {
    var categoriaSel by remember { mutableStateOf<String?>(null) }
    var tipoSel by remember { mutableStateOf(MensajeCategoriaTipo.PARTIDO_EVENTO) }
    var titulo by remember { mutableStateOf("") }
    var cuerpo by remember { mutableStateOf("") }
    var dialogoCat by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                stringResource(R.string.parent_staff_comm_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (remoteAcademiaId.isNullOrBlank()) {
            item {
                Text(
                    stringResource(R.string.parent_staff_cloud_required),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            item {
                Text(stringResource(R.string.parent_msg_category_label), style = MaterialTheme.typography.labelLarge)
                OutlinedButton(
                    onClick = { dialogoCat = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = categorias.isNotEmpty(),
                ) {
                    Text(
                        categoriaSel ?: stringResource(R.string.parent_msg_category_pick),
                    )
                }
                if (categorias.isEmpty()) {
                    Text(
                        stringResource(R.string.parent_msg_no_categories),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                Text(stringResource(R.string.draft_message), style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TipoChip(MensajeCategoriaTipo.PARTIDO_EVENTO, tipoSel, stringResource(R.string.msg_type_partido)) {
                        tipoSel = MensajeCategoriaTipo.PARTIDO_EVENTO
                    }
                    TipoChip(MensajeCategoriaTipo.CONVIVIO_LOGISTICA, tipoSel, stringResource(R.string.msg_type_convivio)) {
                        tipoSel = MensajeCategoriaTipo.CONVIVIO_LOGISTICA
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TipoChip(MensajeCategoriaTipo.ADMINISTRATIVO, tipoSel, stringResource(R.string.msg_type_admin)) {
                        tipoSel = MensajeCategoriaTipo.ADMINISTRATIVO
                    }
                    TipoChip(MensajeCategoriaTipo.OTRO, tipoSel, stringResource(R.string.msg_type_otro)) {
                        tipoSel = MensajeCategoriaTipo.OTRO
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text(stringResource(R.string.parent_msg_title_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedTextField(
                    value = cuerpo,
                    onValueChange = { cuerpo = it },
                    label = { Text(stringResource(R.string.parent_msg_body_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp),
                )
            }
            item {
                Button(
                    onClick = {
                        val cat = categoriaSel ?: return@Button
                        onEnviar(cat, tipoSel, titulo, cuerpo)
                    },
                    enabled = !enviandoMensaje &&
                        categoriaSel != null &&
                        titulo.isNotBlank() &&
                        cuerpo.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (enviandoMensaje) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                    Text(
                        if (enviandoMensaje) stringResource(R.string.parent_msg_sending)
                        else stringResource(R.string.parent_msg_send),
                    )
                }
            }
            item {
                TextButton(
                    onClick = {
                        onRefrescar()
                        onRefrescarExito()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.parent_msg_reload))
                }
            }
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.parent_msg_sent_recent),
                    style = MaterialTheme.typography.titleSmall,
                )
                if (mensajesState.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.heightIn(max = 22.dp),
                        strokeWidth = 2.dp,
                    )
                }
            }
            mensajesState.error?.let { err ->
                Text(
                    err,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        items(mensajesState.items, key = { it.id }) { m ->
            ParentsInboxMessageCard(m, dateTimeFmt)
        }
    }

    if (dialogoCat && categorias.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { dialogoCat = false },
            title = { Text(stringResource(R.string.parent_msg_category_label)) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    categorias.forEach { nombre ->
                        TextButton(
                            onClick = {
                                categoriaSel = nombre
                                dialogoCat = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(nombre, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogoCat = false }) {
                    Text(stringResource(R.string.close))
                }
            },
        )
    }
}

@Composable
private fun TipoChip(
    wire: String,
    seleccionado: String,
    etiqueta: String,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = seleccionado == wire,
        onClick = onClick,
        label = { Text(etiqueta, style = MaterialTheme.typography.labelMedium) },
    )
}
