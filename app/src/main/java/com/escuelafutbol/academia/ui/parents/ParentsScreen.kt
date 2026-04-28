package com.escuelafutbol.academia.ui.parents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.parents.components.PadresEmptyChildrenOnboardingCard
import com.escuelafutbol.academia.ui.design.PrimaryButton
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
    onNavigateToRoute: (String) -> Unit = {},
    padreNavegacionAtajos: List<PadresNavegacionAtajoUi> = emptyList(),
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

    val contenido by viewModel.parentsTabContent.collectAsState()
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
        /** El `Scaffold` principal (`AcademiaRoot`) ya aplica insets; sin esto se duplica el hueco inferior (y aire extra arriba). */
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AcademiaDimens.paddingScreenHorizontal, vertical = 0.dp),
        ) {
            Text(
                stringResource(R.string.tab_parents).uppercase(Locale.ROOT),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 1.dp),
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
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    remoteAcademiaId = remoteAcademiaId,
                    snackbar = snackbar,
                    scope = scope,
                    context = context,
                    moneyFmt = moneyFmt,
                )
            }
            is ParentsTabContent.PadreConHijos -> {
                LaunchedEffect(remoteAcademiaId, c.hijos.map { it.jugadorLocalId }) {
                    if (!remoteAcademiaId.isNullOrBlank() && c.hijos.isNotEmpty()) {
                        viewModel.refrescarRendimientoCompetenciasPadre()
                    }
                }
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
                    onNavigateToRoute = onNavigateToRoute,
                    padreNavegacionAtajos = padreNavegacionAtajos,
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
        PadresEmptyChildrenOnboardingCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AcademiaDimens.gapMd),
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
        modifier = modifier,
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(Modifier.weight(1f).padding(end = 4.dp)) {
                            Text(
                                stringResource(R.string.parent_staff_send_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.parent_staff_send_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (mensajesState.cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(28.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            IconButton(onClick = { onRefrescar(); onRefrescarExito() }) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.parent_inbox_refresh_cd),
                                )
                            }
                        }
                    }

                    if (remoteAcademiaId.isNullOrBlank()) {
                        Text(
                            stringResource(R.string.parent_staff_cloud_required),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            StaffAvisoSeccionTitulo(
                                icono = "📍",
                                texto = stringResource(R.string.parent_msg_category_label),
                            )
                            OutlinedButton(
                                onClick = { dialogoCat = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp),
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
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            StaffAvisoSeccionTitulo(
                                icono = "📢",
                                texto = stringResource(R.string.parent_staff_section_message_type),
                            )
                            val gapTipo = 10.dp
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(gapTipo),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(gapTipo),
                                ) {
                                    TipoAvisoGridCelda(
                                        modifier = Modifier.weight(1f),
                                        selected = tipoSel == MensajeCategoriaTipo.PARTIDO_EVENTO,
                                        label = stringResource(R.string.parent_msg_type_grid_partido),
                                        icon = Icons.Filled.SportsSoccer,
                                        onClick = { tipoSel = MensajeCategoriaTipo.PARTIDO_EVENTO },
                                    )
                                    TipoAvisoGridCelda(
                                        modifier = Modifier.weight(1f),
                                        selected = tipoSel == MensajeCategoriaTipo.CONVIVIO_LOGISTICA,
                                        label = stringResource(R.string.parent_msg_type_grid_logistica),
                                        icon = Icons.Filled.Place,
                                        onClick = { tipoSel = MensajeCategoriaTipo.CONVIVIO_LOGISTICA },
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(gapTipo),
                                ) {
                                    TipoAvisoGridCelda(
                                        modifier = Modifier.weight(1f),
                                        selected = tipoSel == MensajeCategoriaTipo.ADMINISTRATIVO,
                                        label = stringResource(R.string.parent_msg_type_grid_admin),
                                        icon = Icons.Filled.ManageAccounts,
                                        onClick = { tipoSel = MensajeCategoriaTipo.ADMINISTRATIVO },
                                    )
                                    TipoAvisoGridCelda(
                                        modifier = Modifier.weight(1f),
                                        selected = tipoSel == MensajeCategoriaTipo.OTRO,
                                        label = stringResource(R.string.parent_msg_type_grid_otro),
                                        icon = Icons.Filled.MoreHoriz,
                                        onClick = { tipoSel = MensajeCategoriaTipo.OTRO },
                                    )
                                }
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                stringResource(R.string.parent_staff_section_content),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            OutlinedTextField(
                                value = titulo,
                                onValueChange = { titulo = it },
                                label = { Text(stringResource(R.string.parent_msg_title_label)) },
                                singleLine = true,
                                leadingIcon = {
                                    StaffCampoEmojiIcono(emoji = "📝")
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = cuerpo,
                                onValueChange = { cuerpo = it },
                                label = { Text(stringResource(R.string.parent_msg_body_label)) },
                                leadingIcon = {
                                    StaffCampoEmojiIcono(emoji = "💬")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 148.dp),
                            )
                        }
                        PrimaryButton(
                            text = if (enviandoMensaje) {
                                stringResource(R.string.parent_msg_sending)
                            } else {
                                stringResource(R.string.parent_msg_send)
                            },
                            onClick = {
                                categoriaSel?.let { cat ->
                                    onEnviar(cat, tipoSel, titulo, cuerpo)
                                }
                            },
                            enabled = categoriaSel != null &&
                                titulo.isNotBlank() &&
                                cuerpo.isNotBlank(),
                            loading = enviandoMensaje,
                            modifier = Modifier.heightIn(min = 56.dp),
                        )
                    }
                }
            }
        }

        item {
            Column(Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    stringResource(R.string.parent_msg_sent_recent),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                )
                mensajesState.error?.let { err ->
                    Text(
                        err,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
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
private fun StaffAvisoSeccionTitulo(icono: String, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = icono,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun StaffCampoEmojiIcono(emoji: String) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun TipoAvisoGridCelda(
    modifier: Modifier,
    selected: Boolean,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .height(76.dp)
            .clip(shape)
            .then(
                if (selected) {
                    Modifier.background(scheme.primary, shape)
                } else {
                    Modifier
                        .background(Color.Transparent, shape)
                        .border(
                            BorderStroke(1.dp, scheme.outline.copy(alpha = 0.38f)),
                            shape,
                        )
                },
            )
            .clickable(onClick = onClick, onClickLabel = label)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (selected) scheme.onPrimary else scheme.primary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) scheme.onPrimary else scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
