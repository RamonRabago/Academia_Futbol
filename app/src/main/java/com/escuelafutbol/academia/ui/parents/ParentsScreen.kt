@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.escuelafutbol.academia.ui.parents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
                )
            }
            is ParentsTabContent.PadreConHijos -> {
                PadreConHijosContent(
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
private fun PadreSelfLinkCandidatesBody(
    viewModel: ParentsViewModel,
    remoteAcademiaId: String?,
    snackbar: SnackbarHostState,
    scope: CoroutineScope,
    context: android.content.Context,
    modifier: Modifier = Modifier,
    /** Menos margen superior cuando la sección va debajo de «Mis hijos» (padre con hijos ya vinculados). */
    compactLayout: Boolean = false,
    sectionTitleRes: Int = R.string.parent_self_link_section_title,
    sectionHintRes: Int = R.string.parent_self_link_hint,
) {
    val candidatos by viewModel.candidatosVinculo.collectAsState()
    val cargandoCand by viewModel.candidatosVinculoCargando.collectAsState()
    val errorCand by viewModel.candidatosVinculoError.collectAsState()
    var categoriaFiltro by remember { mutableStateOf<String?>(null) }
    val categoriasCand = remember(candidatos) {
        candidatos.map { it.categoria }.distinct().sorted()
    }
    val candidatosFiltrados = remember(candidatos, categoriaFiltro) {
        when (val c = categoriaFiltro) {
            null -> candidatos
            else -> candidatos.filter { it.categoria == c }
        }
    }
    LaunchedEffect(remoteAcademiaId) {
        if (!remoteAcademiaId.isNullOrBlank()) {
            viewModel.refrescarVinculosPadre()
            viewModel.cargarCandidatosVinculo()
        }
    }
    Column(modifier = modifier) {
        if (!remoteAcademiaId.isNullOrBlank()) {
            Spacer(Modifier.height(if (compactLayout) 8.dp else 24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(sectionTitleRes),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(sectionHintRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
            if (cargandoCand) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(32.dp),
                )
            } else {
                errorCand?.let { err ->
                    Text(
                        err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    FilledTonalButton(
                        onClick = { viewModel.cargarCandidatosVinculo() },
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text(stringResource(R.string.resources_reload))
                    }
                }
                if (errorCand == null) {
                    if (candidatos.isEmpty()) {
                        Text(
                            stringResource(R.string.parent_self_link_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    } else {
                        Text(
                            stringResource(R.string.parent_self_link_category_hint),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            FilterChip(
                                selected = categoriaFiltro == null,
                                onClick = { categoriaFiltro = null },
                                label = { Text(stringResource(R.string.category_all)) },
                            )
                            categoriasCand.forEach { nombreCat ->
                                FilterChip(
                                    selected = categoriaFiltro == nombreCat,
                                    onClick = {
                                        categoriaFiltro =
                                            if (categoriaFiltro == nombreCat) null else nombreCat
                                    },
                                    label = { Text(nombreCat) },
                                )
                            }
                        }
                        if (candidatosFiltrados.isEmpty()) {
                            Text(
                                stringResource(R.string.members_parent_links_no_players_in_category),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        } else {
                            candidatosFiltrados.forEach { c ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(c.nombre, style = MaterialTheme.typography.bodyLarge)
                                        Text(
                                            c.categoria,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    TextButton(
                                        onClick = {
                                            viewModel.vincularMiHijo(c.remoteId) { r ->
                                                scope.launch {
                                                    if (r.isSuccess) {
                                                        snackbar.showSnackbar(
                                                            context.getString(R.string.parent_self_link_ok),
                                                        )
                                                    } else {
                                                        snackbar.showSnackbar(
                                                            r.exceptionOrNull()?.message
                                                                ?: context.getString(R.string.parent_self_link_error),
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                    ) {
                                        Text(stringResource(R.string.parent_self_link_vincular))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
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
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        Text(
            stringResource(R.string.parent_my_children_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            stringResource(R.string.parent_my_children_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            stringResource(R.string.parent_my_children_empty),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
        PadreSelfLinkCandidatesBody(
            viewModel = viewModel,
            remoteAcademiaId = remoteAcademiaId,
            snackbar = snackbar,
            scope = scope,
            context = context,
            modifier = Modifier.fillMaxWidth(),
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
            TarjetaMensaje(m, dateTimeFmt)
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

@Composable
private fun TarjetaMensaje(m: MensajeCategoriaUi, dateTimeFmt: DateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    m.categoriaNombre,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    etiquetaTipo(m.tipo),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Text(m.titulo, style = MaterialTheme.typography.titleSmall)
            Text(m.cuerpo, style = MaterialTheme.typography.bodyMedium)
            Text(
                dateTimeFmt.format(Date(m.createdAtMillis)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun etiquetaTipo(tipo: String): String = when (tipo) {
    MensajeCategoriaTipo.PARTIDO_EVENTO -> stringResource(R.string.msg_type_partido)
    MensajeCategoriaTipo.CONVIVIO_LOGISTICA -> stringResource(R.string.msg_type_convivio)
    MensajeCategoriaTipo.ADMINISTRATIVO -> stringResource(R.string.msg_type_admin)
    else -> stringResource(R.string.msg_type_otro)
}

@Composable
private fun PadreConHijosContent(
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
    scope: kotlinx.coroutines.CoroutineScope,
    context: android.content.Context,
) {
    var filtroTipo by remember { mutableStateOf<String?>(null) }
    var hijoParaDesvincular by remember { mutableStateOf<HijoResumenUi?>(null) }
    val itemsFiltrados = remember(mensajesState.items, filtroTipo) {
        if (filtroTipo == null) mensajesState.items
        else mensajesState.items.filter { it.tipo == filtroTipo }
    }

    LaunchedEffect(remoteAcademiaId) {
        if (!remoteAcademiaId.isNullOrBlank()) {
            viewModel.refrescarVinculosPadre()
            viewModel.cargarCandidatosVinculo()
        }
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
        contentPadding = PaddingValues(16.dp),
    ) {
        if (!remoteAcademiaId.isNullOrBlank()) {
            item {
                Text(
                    stringResource(R.string.parent_inbox_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    stringResource(R.string.parent_inbox_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    FilterChip(
                        selected = filtroTipo == null,
                        onClick = { filtroTipo = null },
                        label = { Text(stringResource(R.string.parent_inbox_filter_all)) },
                    )
                    FilterChip(
                        selected = filtroTipo == MensajeCategoriaTipo.PARTIDO_EVENTO,
                        onClick = { filtroTipo = MensajeCategoriaTipo.PARTIDO_EVENTO },
                        label = { Text(stringResource(R.string.msg_type_partido)) },
                    )
                    FilterChip(
                        selected = filtroTipo == MensajeCategoriaTipo.CONVIVIO_LOGISTICA,
                        onClick = { filtroTipo = MensajeCategoriaTipo.CONVIVIO_LOGISTICA },
                        label = { Text(stringResource(R.string.msg_type_convivio)) },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = filtroTipo == MensajeCategoriaTipo.ADMINISTRATIVO,
                        onClick = { filtroTipo = MensajeCategoriaTipo.ADMINISTRATIVO },
                        label = { Text(stringResource(R.string.msg_type_admin)) },
                    )
                    FilterChip(
                        selected = filtroTipo == MensajeCategoriaTipo.OTRO,
                        onClick = { filtroTipo = MensajeCategoriaTipo.OTRO },
                        label = { Text(stringResource(R.string.msg_type_otro)) },
                    )
                }
                TextButton(onClick = {
                    onRefrescarMensajes()
                    scope.launch {
                        snackbar.showSnackbar(context.getString(R.string.parent_inbox_updated))
                    }
                }) {
                    Text(stringResource(R.string.parent_msg_reload))
                }
                if (mensajesState.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .heightIn(max = 28.dp),
                        strokeWidth = 2.dp,
                    )
                }
                mensajesState.error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
            }
            if (itemsFiltrados.isEmpty() && !mensajesState.cargando) {
                item {
                    Text(
                        stringResource(R.string.parent_inbox_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }
            } else {
                items(itemsFiltrados, key = { it.id }) { m ->
                    TarjetaMensaje(m, dateTimeFmt)
                    Spacer(Modifier.height(8.dp))
                }
            }
            item {
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
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
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
        }
        if (!remoteAcademiaId.isNullOrBlank()) {
            item {
                PadreSelfLinkCandidatesBody(
                    viewModel = viewModel,
                    remoteAcademiaId = remoteAcademiaId,
                    snackbar = snackbar,
                    scope = scope,
                    context = context,
                    modifier = Modifier.fillMaxWidth(),
                    compactLayout = true,
                    sectionTitleRes = R.string.parent_add_child_section_title,
                    sectionHintRes = R.string.parent_add_child_section_hint,
                )
            }
        }
        if (c.reglaLimitePagoActiva && c.totalAdeudoVencido > 0.009) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.parent_payment_reminder_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            stringResource(R.string.parent_payment_reminder_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        Text(
                            stringResource(
                                R.string.parent_payment_total_label,
                                moneyFmt.format(c.totalAdeudoVencido),
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
            }
        }
        items(
            c.hijos,
            key = { h -> "${h.nombre}|${h.categoria}|${h.vinculoId.orEmpty()}" },
        ) { hijo ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(hijo.nombre, style = MaterialTheme.typography.titleSmall)
                            Text(
                                hijo.categoria,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        if (hijo.vinculoId != null) {
                            TextButton(
                                onClick = { hijoParaDesvincular = hijo },
                                modifier = Modifier.semantics {
                                    contentDescription = context.getString(R.string.parent_unlink_cd)
                                },
                            ) {
                                Text(stringResource(R.string.parent_unlink))
                            }
                        }
                    }
                    if (hijo.mesesVencidos.isNotEmpty()) {
                        HorizontalDivider(Modifier.padding(vertical = 10.dp))
                        Text(
                            stringResource(R.string.parent_payment_section_child),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            stringResource(
                                R.string.parent_payment_child_owed,
                                moneyFmt.format(hijo.totalAdeudoHijo),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        hijo.mesesVencidos.forEach { mes ->
                            Text(
                                stringResource(
                                    R.string.parent_payment_month_line,
                                    mes.etiquetaMes,
                                    moneyFmt.format(mes.saldoPendiente),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                    if (hijo.ultimasAsistencias.isNotEmpty()) {
                        HorizontalDivider(Modifier.padding(vertical = 10.dp))
                        Text(
                            stringResource(R.string.parent_attendance_recent),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        hijo.ultimasAsistencias.forEach { linea ->
                            val fecha = dateFmt.format(Date(linea.fechaDia))
                            val estado = stringResource(
                                if (linea.presente) {
                                    R.string.parent_attendance_present
                                } else {
                                    R.string.parent_attendance_absent
                                },
                            )
                            Text(
                                stringResource(
                                    R.string.parent_attendance_line,
                                    fecha,
                                    estado,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    } else {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.parent_no_attendance_yet),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
    }
}
