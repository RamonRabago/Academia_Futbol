@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)
package com.escuelafutbol.academia.ui.contenido

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.AppTintedPanel
import com.escuelafutbol.academia.ui.design.ChipsGroup
import com.escuelafutbol.academia.ui.design.EmptyState
import com.escuelafutbol.academia.ui.design.PrimaryButton
import com.escuelafutbol.academia.ui.design.SectionHeader
import com.escuelafutbol.academia.ui.util.FullscreenImageViewerDialog
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.model.esPadreMembresiaNube
import com.escuelafutbol.academia.data.remote.dto.ContenidoEstadoPublicacion
import com.escuelafutbol.academia.data.remote.dto.ContenidoReaccionTipo
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenidoScreen(
    viewModel: ContenidoViewModel,
    config: AcademiaConfig,
    categoriaFiltro: String?,
) {
    val ui by viewModel.uiState.collectAsState()
    val items by viewModel.itemsPresentados.collectAsState()
    val filtroTema by viewModel.filtroTemaSeleccionado.collectAsState()
    val categoriasPub by viewModel.categoriasParaPublicar.collectAsState()
    val filtroEstadoPub by viewModel.filtroEstadoPublicacionSeleccionado.collectAsState()
    val esPadreNube = remember(config) { config.esPadreMembresiaNube() }
    val dateFmt = remember {
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale("es", "MX"))
    }

    var detalle by remember { mutableStateOf<ContenidoItemUi?>(null) }
    var editorAbierto by remember { mutableStateOf(false) }
    var confirmarArchivar by remember { mutableStateOf<ContenidoItemUi?>(null) }
    /** Primera = URL de la imagen, segunda = título para el visor. */
    var visorImagenContenido by remember { mutableStateOf<Pair<String, String>?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.marcarRecursosVistos()
    }
    LaunchedEffect(esPadreNube) {
        if (esPadreNube) viewModel.setFiltroEstadoPublicacion(null)
    }

    val puedePublicarAlguna = remember(config, categoriasPub) {
        categoriasPub.any { viewModel.puedePublicar(config, it) }
    }
    var filtrosSheetAbierto by remember { mutableStateOf(false) }
    val hayFiltrosActivos = filtroTema != null || (!esPadreNube && filtroEstadoPub != null)
    val filtroSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    val fabVisible = puedePublicarAlguna && config.remoteAcademiaId != null

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        snackbarHost = { SnackbarHost(snack) },
        floatingActionButton = {
            if (fabVisible) {
                SmallFloatingActionButton(
                    onClick = { editorAbierto = true },
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.resources_publish_cd))
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                contentPadding = PaddingValues(
                    bottom = if (fabVisible) 88.dp else AcademiaDimens.gapSm,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
            ) {
                item(key = "resources_screen_header") {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = AcademiaDimens.paddingScreenHorizontal,
                            vertical = AcademiaDimens.gapSm,
                        ),
                    ) {
                        SectionHeader(
                            title = stringResource(R.string.resources_title),
                            subtitle = stringResource(R.string.resources_screen_subtitle),
                            action = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMicro),
                                ) {
                                    IconButton(
                                        onClick = { filtrosSheetAbierto = true },
                                        modifier = Modifier.size(AcademiaDimens.avatarRow),
                                    ) {
                                        Icon(
                                            Icons.Default.FilterList,
                                            contentDescription = stringResource(R.string.resources_filter_open_cd),
                                            tint = if (hayFiltrosActivos) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.refrescar() },
                                        modifier = Modifier.size(AcademiaDimens.avatarRow),
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = stringResource(R.string.resources_refresh_cd),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
                item(key = "quick_filters") {
                    ContenidoFiltrosRapidosChips(
                        esPadreNube = esPadreNube,
                        filtroTema = filtroTema,
                        filtroEstadoPub = filtroEstadoPub,
                        onTodas = {
                            viewModel.setFiltroTema(null)
                            viewModel.setFiltroEstadoPublicacion(null)
                        },
                        onAvisos = {
                            viewModel.setFiltroEstadoPublicacion(null)
                            viewModel.setFiltroTema(ContenidoTema.NOTICIA)
                        },
                        onFotos = {
                            viewModel.setFiltroEstadoPublicacion(null)
                            viewModel.setFiltroTema(ContenidoTema.ENTRENAMIENTO)
                        },
                        onPendientes = {
                            viewModel.setFiltroTema(null)
                            viewModel.setFiltroEstadoPublicacion(ContenidoEstadoPublicacion.PENDING)
                        },
                        modifier = Modifier.padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
                    )
                }
                if (config.remoteAcademiaId == null) {
                    item(key = "no_cloud") {
                        AppTintedPanel(
                            modifier = Modifier.padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                            contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
                        ) {
                            Text(
                                stringResource(R.string.resources_no_cloud_short),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }
                val errorLista = ui.error
                if (errorLista != null) {
                    item(key = "list_error") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
                            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            EmptyState(
                                title = stringResource(R.string.resources_list_error_title),
                                subtitle = errorLista,
                            )
                            OutlinedButton(onClick = { viewModel.refrescar() }) {
                                Text(stringResource(R.string.resources_list_error_retry))
                            }
                        }
                    }
                }
                if (ui.cargando && items.isEmpty()) {
                    item(key = "loading_initial") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AcademiaDimens.paddingScreenHorizontal)
                                .heightIn(min = AcademiaDimens.contentLoadingMinHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    stringResource(R.string.resources_loading_feed),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                } else if (!ui.cargando && items.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            title = stringResource(R.string.resources_empty_title),
                            subtitle = stringResource(R.string.resources_empty),
                            modifier = Modifier.padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
                        )
                    }
                } else {
                    itemsIndexed(
                        items,
                        key = { _, item -> item.id },
                    ) { _, item ->
                        TarjetaContenidoFeed(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = AcademiaDimens.gapMicro,
                                    vertical = AcademiaDimens.gapSm,
                                ),
                            item = item,
                            mostrarCategoria = categoriaFiltro == null,
                            puedeGestionar = puedePublicarAlguna &&
                                viewModel.puedeGestionarRecursosPublicacion(config, item),
                            puedeModerar = viewModel.puedeModerarPublicacion(config, item),
                            onOpen = { detalle = item },
                            onArchivar = { confirmarArchivar = item },
                            onAprobar = {
                                viewModel.aprobarDesdeUi(item.idsFilasParaAccionesRemotas()) { r ->
                                    scope.launch {
                                        if (r.isFailure) {
                                            snack.showSnackbar(
                                                r.exceptionOrNull()?.message
                                                    ?: context.getString(R.string.resources_moderation_error),
                                            )
                                        } else {
                                            snack.showSnackbar(context.getString(R.string.resources_approved_done))
                                        }
                                    }
                                }
                            },
                            onRechazar = {
                                viewModel.rechazarDesdeUi(item.idsFilasParaAccionesRemotas()) { r ->
                                    scope.launch {
                                        if (r.isFailure) {
                                            snack.showSnackbar(
                                                r.exceptionOrNull()?.message
                                                    ?: context.getString(R.string.resources_moderation_error),
                                            )
                                        } else {
                                            snack.showSnackbar(context.getString(R.string.resources_rejected_done))
                                        }
                                    }
                                }
                            },
                            onReaccionar = { tipo ->
                                viewModel.alternarReaccionDesdeUi(item.id, tipo)
                            },
                            onAbrirImagenGrande = { url ->
                                visorImagenContenido = url to item.titulo
                            },
                        )
                    }
                }
                if (ui.cargando && items.isNotEmpty()) {
                    item(key = "loading_more") {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AcademiaDimens.paddingScreenHorizontal)
                                .padding(vertical = AcademiaDimens.gapMd),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.heightIn(max = AcademiaDimens.iconSizeMd),
                                strokeWidth = AcademiaDimens.gapMicro,
                            )
                        }
                    }
                }
            }
            if (filtrosSheetAbierto) {
                ModalBottomSheet(
                    onDismissRequest = { filtrosSheetAbierto = false },
                    sheetState = filtroSheetState,
                ) {
                    ContenidoRecursosFiltrosSheetContent(
                        esPadreNube = esPadreNube,
                        filtroTema = filtroTema,
                        filtroEstadoPub = filtroEstadoPub,
                        onCerrar = { filtrosSheetAbierto = false },
                        onTema = { viewModel.setFiltroTema(it) },
                        onEstado = { viewModel.setFiltroEstadoPublicacion(it) },
                    )
                }
            }
        }
    }

    detalle?.let { d0 ->
        val d = items.firstOrNull { it.id == d0.id } ?: d0
        AlertDialog(
            onDismissRequest = { detalle = null },
            title = { Text(d.titulo) },
            text = {
                SelectionContainer {
                    Column(
                        Modifier
                            .heightIn(max = AcademiaDimens.contentDetailDialogScrollMax)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingListSection),
                    ) {
                        CabeceraFeedContenido(
                            item = d,
                            mostrarCategoria = categoriaFiltro == null,
                            mostrarTema = true,
                        )
                        CarruselMediaContenido(
                            item = d,
                            onAbrirImagenGrande = { url ->
                                visorImagenContenido = url to d.titulo
                            },
                        )
                        TextoPublicacionFeed(item = d, maxLinesCuerpo = 24)
                        if (d.estadoPublicacion == ContenidoEstadoPublicacion.PUBLISHED) {
                            BarraReaccionesContenido(
                                reacciones = d.reacciones,
                                onReaccionar = { tipo -> viewModel.alternarReaccionDesdeUi(d.id, tipo) },
                            )
                        } else if (d.estadoPublicacion == ContenidoEstadoPublicacion.PENDING) {
                            Text(
                                stringResource(R.string.resources_detail_pending_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        } else {
                            Text(
                                stringResource(R.string.resources_detail_rejected_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        Text(
                            dateFmt.format(Date(d.createdAtMillis)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd)) {
                    if (viewModel.puedeModerarPublicacion(config, d)) {
                        TextButton(
                            onClick = {
                                viewModel.rechazarDesdeUi(d.idsFilasParaAccionesRemotas()) { r ->
                                    scope.launch {
                                        if (r.isFailure) {
                                            snack.showSnackbar(
                                                r.exceptionOrNull()?.message
                                                    ?: context.getString(R.string.resources_moderation_error),
                                            )
                                        } else {
                                            detalle = null
                                            snack.showSnackbar(context.getString(R.string.resources_rejected_done))
                                        }
                                    }
                                }
                            },
                        ) {
                            Text(stringResource(R.string.resources_reject_action))
                        }
                        TextButton(
                            onClick = {
                                viewModel.aprobarDesdeUi(d.idsFilasParaAccionesRemotas()) { r ->
                                    scope.launch {
                                        if (r.isFailure) {
                                            snack.showSnackbar(
                                                r.exceptionOrNull()?.message
                                                    ?: context.getString(R.string.resources_moderation_error),
                                            )
                                        } else {
                                            detalle = null
                                            snack.showSnackbar(context.getString(R.string.resources_approved_done))
                                        }
                                    }
                                }
                            },
                        ) {
                            Text(stringResource(R.string.resources_approve_action))
                        }
                    }
                    TextButton(onClick = { detalle = null }) {
                        Text(stringResource(R.string.close))
                    }
                }
            },
        )
    }

    confirmarArchivar?.let { item ->
        AlertDialog(
            onDismissRequest = { confirmarArchivar = null },
            title = { Text(stringResource(R.string.resources_archive_title)) },
            text = { Text(stringResource(R.string.resources_archive_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarArchivar = null
                        viewModel.archivarDesdeUi(item.idsFilasParaAccionesRemotas()) { r ->
                            scope.launch {
                                if (r.isFailure) {
                                    val detalle = r.exceptionOrNull()?.message?.trim()?.take(180)
                                    snack.showSnackbar(
                                        buildString {
                                            append(context.getString(R.string.resources_archive_error))
                                            if (!detalle.isNullOrEmpty()) {
                                                append("\n")
                                                append(detalle)
                                            }
                                        },
                                    )
                                } else {
                                    snack.showSnackbar(context.getString(R.string.resources_archive_done))
                                }
                            }
                        }
                    },
                ) {
                    Text(stringResource(R.string.resources_archive_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarArchivar = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    visorImagenContenido?.let { (url, tituloVisor) ->
        FullscreenImageViewerDialog(
            titulo = tituloVisor,
            imageModel = url,
            contentDescription = stringResource(R.string.resources_article_inline_image_cd),
            onDismiss = { visorImagenContenido = null },
        )
    }

    if (editorAbierto) {
        DialogoPublicarContenido(
            categoriasDisponibles = categoriasPub,
            filtroPreseleccion = categoriaFiltro,
            puedePublicarSeleccion = { sel ->
                viewModel.puedePublicarSeleccion(config, sel, categoriasPub)
            },
            visibleDirectoFamiliasPermitido = viewModel.puedeElegirVisibleYaParaFamilias(config),
            onDismiss = { editorAbierto = false },
            onPublicar = { cats, tema, titulo, cuerpo, imagenLocal, imgsCuerpo, visibleYa, onResult ->
                viewModel.publicarDesdeUi(
                    cats,
                    tema,
                    titulo,
                    cuerpo,
                    imagenLocal,
                    imgsCuerpo,
                    visibleYa,
                    onResult,
                )
            },
            onExito = { editorAbierto = false },
        )
    }
}

private enum class DestinoCamaraPublicarContenido { PORTADA, CUERPO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoPublicarContenido(
    categoriasDisponibles: List<String>,
    filtroPreseleccion: String?,
    puedePublicarSeleccion: (String) -> Boolean,
    visibleDirectoFamiliasPermitido: Boolean,
    onDismiss: () -> Unit,
    onPublicar: (List<String>, String, String, String, File?, List<File>, Boolean, (Result<Unit>) -> Unit) -> Unit,
    onExito: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val maxCuerpo = ContenidoViewModel.MAX_FOTOS_CUERPO
    val categoriasOpciones = remember(categoriasDisponibles) {
        if (categoriasDisponibles.size > 1) {
            listOf(ContenidoViewModel.CATEGORIA_TODAS_MAGIC) + categoriasDisponibles
        } else {
            categoriasDisponibles
        }
    }
    var categoriaSel by remember { mutableStateOf("") }
    var visibleYaParaFamilias by remember { mutableStateOf(false) }
    LaunchedEffect(categoriasOpciones, filtroPreseleccion) {
        categoriaSel = if (categoriasOpciones.isEmpty()) {
            ""
        } else {
            filtroPreseleccion?.takeIf { f ->
                categoriasOpciones.any { it.equals(f, ignoreCase = true) }
            } ?: categoriasOpciones.first()
        }
    }
    var dialogoCat by remember { mutableStateOf(false) }
    var temaSel by remember { mutableStateOf(ContenidoTema.NOTICIA) }
    var cuerpo by remember { mutableStateOf("") }
    var errLocal by remember { mutableStateOf<String?>(null) }
    var enviando by remember { mutableStateOf(false) }
    var imagenPreview by remember { mutableStateOf<File?>(null) }
    val imagenesCuerpo = remember { mutableStateListOf<File>() }

    var archivoCamaraTemp by remember { mutableStateOf<File?>(null) }
    var destinoCamara by remember { mutableStateOf<DestinoCamaraPublicarContenido?>(null) }
    var destinoCamaraTrasPermiso by remember { mutableStateOf<DestinoCamaraPublicarContenido?>(null) }

    val pickImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            imagenPreview?.let { runCatching { it.delete() } }
            imagenPreview = copiarUriImagenContenido(context, uri)
        }
    }

    val pickImagenCuerpo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        if (imagenesCuerpo.size >= maxCuerpo) return@rememberLauncherForActivityResult
        scope.launch {
            val f = copiarUriImagenContenido(context, uri) ?: return@launch
            if (imagenesCuerpo.size < maxCuerpo) imagenesCuerpo.add(f)
        }
    }

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val f = archivoCamaraTemp
        val dest = destinoCamara
        archivoCamaraTemp = null
        destinoCamara = null
        if (f == null || dest == null) return@rememberLauncherForActivityResult
        scope.launch {
            if (!success || !f.exists()) {
                runCatching { f.delete() }
                return@launch
            }
            val copiado = copiarArchivoJpegCapturaContenido(context, f) ?: return@launch
            when (dest) {
                DestinoCamaraPublicarContenido.PORTADA -> {
                    imagenPreview?.let { runCatching { it.delete() } }
                    imagenPreview = copiado
                }
                DestinoCamaraPublicarContenido.CUERPO -> {
                    if (imagenesCuerpo.size < maxCuerpo) {
                        imagenesCuerpo.add(copiado)
                    } else {
                        runCatching { copiado.delete() }
                    }
                }
            }
        }
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val pend = destinoCamaraTrasPermiso
        destinoCamaraTrasPermiso = null
        if (!granted || pend == null) return@rememberLauncherForActivityResult
        val f = File(context.cacheDir, "contenido_cam_${System.currentTimeMillis()}.jpg")
        runCatching { if (!f.exists()) f.createNewFile() }
        archivoCamaraTemp = f
        destinoCamara = pend
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            f,
        )
        takePicture.launch(uri)
    }

    fun lanzarCamara(dest: DestinoCamaraPublicarContenido) {
        if (dest == DestinoCamaraPublicarContenido.CUERPO && imagenesCuerpo.size >= maxCuerpo) return
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> {
                val f = File(context.cacheDir, "contenido_cam_${System.currentTimeMillis()}.jpg")
                runCatching { if (!f.exists()) f.createNewFile() }
                archivoCamaraTemp = f
                destinoCamara = dest
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    f,
                )
                takePicture.launch(uri)
            }
            else -> {
                destinoCamaraTrasPermiso = dest
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    fun limpiarAdjuntosYCerrar() {
        imagenPreview?.let { runCatching { it.delete() } }
        imagenPreview = null
        imagenesCuerpo.forEach { runCatching { it.delete() } }
        imagenesCuerpo.clear()
        onDismiss()
    }

    val maxChars = ContenidoViewModel.MAX_CHARS_CUERPO_SOCIAL
    val formularioListoParaPublicar =
        categoriaSel.isNotBlank() &&
            puedePublicarSeleccion(categoriaSel)

    fun ejecutarPublicar() {
        errLocal = null
        enviando = true
        val destino = if (categoriaSel.trim() == ContenidoViewModel.CATEGORIA_TODAS_MAGIC) {
            categoriasDisponibles.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        } else {
            listOf(categoriaSel.trim())
        }
        onPublicar(
            destino,
            temaSel,
            tituloDesdeCuerpoSocial(cuerpo),
            cuerpo.trim(),
            imagenPreview,
            imagenesCuerpo.toList(),
            visibleYaParaFamilias && visibleDirectoFamiliasPermitido,
        ) { res ->
            enviando = false
            res.onSuccess {
                imagenPreview = null
                imagenesCuerpo.clear()
                onExito()
            }
                .onFailure { e ->
                    errLocal = e.message
                        ?: context.getString(R.string.resources_publish_error)
                }
        }
    }

    Dialog(
        onDismissRequest = { if (!enviando) limpiarAdjuntosYCerrar() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Scaffold(
                contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    stringResource(R.string.resources_publish_title),
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    stringResource(R.string.resources_publish_editor_intro),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { if (!enviando) limpiarAdjuntosYCerrar() },
                                enabled = !enviando,
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(R.string.resources_publish_close_cd),
                                )
                            }
                        },
                    )
                },
                bottomBar = {
                    Column {
                        if (enviando) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                        HorizontalDivider()
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = AcademiaDimens.paddingScreenHorizontal,
                                    vertical = AcademiaDimens.gapSm,
                                ),
                            horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(
                                onClick = { limpiarAdjuntosYCerrar() },
                                enabled = !enviando,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                            PrimaryButton(
                                text = stringResource(R.string.resources_publish_submit),
                                onClick = { ejecutarPublicar() },
                                enabled = formularioListoParaPublicar,
                                loading = enviando,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                },
            ) { padding ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .imePadding(),
                ) {
                    Column(
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = AcademiaDimens.paddingScreenHorizontal)
                            .padding(
                                top = AcademiaDimens.gapMd,
                                bottom = AcademiaDimens.gapSm,
                            ),
                        verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingDialogBlock),
                    ) {
                        SectionHeader(
                            title = stringResource(R.string.resources_publish_category_label),
                            subtitle = stringResource(R.string.resources_publish_category_help),
                        )
                        AppCard(
                            modifier = Modifier.fillMaxWidth(),
                            elevated = false,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            includeContentPadding = false,
                        ) {
                            Column(
                                Modifier.padding(AcademiaDimens.paddingCardCompact),
                                verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                            ) {
                                TextButton(
                                    onClick = { dialogoCat = true },
                                    enabled = categoriasOpciones.isNotEmpty() && !enviando,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(
                                        horizontal = AcademiaDimens.gapMd,
                                        vertical = AcademiaDimens.gapSm,
                                    ),
                                ) {
                                    Text(
                                        etiquetaCategoriaSeleccionada(categoriaSel),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                        SectionHeader(
                            title = stringResource(R.string.resources_publish_section_visibility_title),
                            subtitle = stringResource(R.string.resources_publish_section_visibility_subtitle),
                        )
                        AppCard(
                            modifier = Modifier.fillMaxWidth(),
                            elevated = false,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            includeContentPadding = false,
                        ) {
                            Column(
                                Modifier.padding(AcademiaDimens.paddingCardCompact),
                                verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                            ) {
                                if (visibleDirectoFamiliasPermitido) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            stringResource(R.string.resources_publish_visible_families_now),
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Switch(
                                            checked = visibleYaParaFamilias,
                                            onCheckedChange = { visibleYaParaFamilias = it },
                                            enabled = !enviando,
                                        )
                                    }
                                    Text(
                                        stringResource(R.string.resources_publish_visible_families_help),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else {
                                    Text(
                                        stringResource(R.string.resources_publish_pending_approval_hint),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        SectionHeader(
                            title = stringResource(R.string.resources_publish_theme_label),
                            subtitle = stringResource(R.string.resources_publish_section_theme_subtitle),
                        )
                        ChipsGroup {
                            ContenidoTema.todosWire.forEach { w ->
                                FilterChip(
                                    selected = temaSel == w,
                                    onClick = { if (!enviando) temaSel = w },
                                    enabled = !enviando,
                                    label = { Text(temaLabel(w), style = MaterialTheme.typography.labelLarge) },
                                )
                            }
                        }
                        OutlinedTextField(
                            value = cuerpo,
                            onValueChange = { v ->
                                if (v.length <= maxChars) {
                                    cuerpo = v
                                    errLocal = null
                                }
                            },
                            label = { Text(stringResource(R.string.resources_publish_field_message, maxChars)) },
                            supportingText = {
                                Text(
                                    stringResource(R.string.resources_publish_char_count, cuerpo.length, maxChars),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = AcademiaDimens.contentEditorMessageMinHeight),
                            minLines = 8,
                            maxLines = 18,
                            enabled = !enviando,
                        )
                        Text(
                            stringResource(R.string.resources_cover_label),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedButton(
                                onClick = { pickImagen.launch("image/*") },
                                enabled = !enviando,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = AcademiaDimens.buttonMinHeight),
                                contentPadding = PaddingValues(
                                    horizontal = AcademiaDimens.gapMd,
                                    vertical = AcademiaDimens.spacingListSection,
                                ),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                                ) {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(AcademiaDimens.iconSizeSm),
                                    )
                                    Text(
                                        stringResource(R.string.staff_pick_gallery),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = { lanzarCamara(DestinoCamaraPublicarContenido.PORTADA) },
                                enabled = !enviando,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = AcademiaDimens.buttonMinHeight),
                                contentPadding = PaddingValues(
                                    horizontal = AcademiaDimens.gapMd,
                                    vertical = AcademiaDimens.spacingListSection,
                                ),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                                ) {
                                    Icon(
                                        Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        modifier = Modifier.size(AcademiaDimens.iconSizeSm),
                                    )
                                    Text(
                                        stringResource(R.string.staff_take_photo),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            }
                        }
                        imagenPreview?.let { f ->
                            AsyncImage(
                                model = f,
                                contentDescription = stringResource(R.string.resources_cover_preview_cd),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = AcademiaDimens.contentEditorCoverMaxHeight)
                                    .clip(RoundedCornerShape(AcademiaDimens.radiusMd)),
                                contentScale = ContentScale.Crop,
                            )
                            TextButton(
                                onClick = {
                                    runCatching { f.delete() }
                                    imagenPreview = null
                                },
                                enabled = !enviando,
                            ) {
                                Text(stringResource(R.string.resources_remove_cover))
                            }
                        }
                        Text(
                            stringResource(R.string.resources_body_photos_label),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            stringResource(R.string.resources_body_photos_hint, maxCuerpo),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedButton(
                                onClick = { pickImagenCuerpo.launch("image/*") },
                                enabled = !enviando && imagenesCuerpo.size < maxCuerpo,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = AcademiaDimens.buttonMinHeight),
                                contentPadding = PaddingValues(
                                    horizontal = AcademiaDimens.gapMd,
                                    vertical = AcademiaDimens.spacingListSection,
                                ),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                                ) {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(AcademiaDimens.iconSizeSm),
                                    )
                                    Text(
                                        stringResource(R.string.staff_pick_gallery),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = { lanzarCamara(DestinoCamaraPublicarContenido.CUERPO) },
                                enabled = !enviando && imagenesCuerpo.size < maxCuerpo,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = AcademiaDimens.buttonMinHeight),
                                contentPadding = PaddingValues(
                                    horizontal = AcademiaDimens.gapMd,
                                    vertical = AcademiaDimens.spacingListSection,
                                ),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                                ) {
                                    Icon(
                                        Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        modifier = Modifier.size(AcademiaDimens.iconSizeSm),
                                    )
                                    Text(
                                        stringResource(R.string.staff_take_photo),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            }
                        }
                        if (imagenesCuerpo.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                itemsIndexed(
                                    imagenesCuerpo,
                                    key = { _, f -> f.absolutePath },
                                ) { _, file ->
                                    Box(
                                        modifier = Modifier.size(
                                            width = AcademiaDimens.contentEditorBodyThumb,
                                            height = AcademiaDimens.contentEditorBodyThumb,
                                        ),
                                    ) {
                                        AsyncImage(
                                            model = file,
                                            contentDescription = stringResource(R.string.resources_cover_preview_cd),
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(AcademiaDimens.radiusSm)),
                                            contentScale = ContentScale.Crop,
                                        )
                                        IconButton(
                                            onClick = {
                                                runCatching { file.delete() }
                                                imagenesCuerpo.remove(file)
                                            },
                                            enabled = !enviando,
                                            modifier = Modifier.align(Alignment.TopEnd),
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = stringResource(R.string.resources_body_photo_remove_cd),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        errLocal?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }

    if (dialogoCat && categoriasOpciones.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { dialogoCat = false },
            title = { Text(stringResource(R.string.resources_publish_category_label)) },
            text = {
                Column(
                    Modifier
                        .heightIn(max = AcademiaDimens.contentCategoryDialogScrollMax)
                        .verticalScroll(rememberScrollState()),
                ) {
                    categoriasOpciones.forEach { nombre ->
                        TextButton(
                            onClick = {
                                categoriaSel = nombre
                                dialogoCat = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                if (nombre == ContenidoViewModel.CATEGORIA_TODAS_MAGIC) {
                                    stringResource(R.string.resources_publish_all_categories)
                                } else {
                                    nombre
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoFiltrosRapidosChips(
    esPadreNube: Boolean,
    filtroTema: String?,
    filtroEstadoPub: String?,
    onTodas: () -> Unit,
    onAvisos: () -> Unit,
    onFotos: () -> Unit,
    onPendientes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selTodas = filtroTema == null && filtroEstadoPub == null
    val selAvisos = filtroTema == ContenidoTema.NOTICIA && filtroEstadoPub == null
    val selFotos = filtroTema == ContenidoTema.ENTRENAMIENTO && filtroEstadoPub == null
    val selPendientes = !esPadreNube &&
        filtroEstadoPub == ContenidoEstadoPublicacion.PENDING &&
        filtroTema == null
    ChipsGroup(modifier = modifier) {
        FilterChip(
            selected = selTodas,
            onClick = onTodas,
            label = { Text(stringResource(R.string.resources_chip_quick_all)) },
        )
        FilterChip(
            selected = selAvisos,
            onClick = onAvisos,
            label = { Text(stringResource(R.string.resources_chip_quick_news)) },
        )
        FilterChip(
            selected = selFotos,
            onClick = onFotos,
            label = { Text(stringResource(R.string.resources_chip_quick_photos)) },
        )
        if (!esPadreNube) {
            FilterChip(
                selected = selPendientes,
                onClick = onPendientes,
                label = { Text(stringResource(R.string.resources_chip_quick_pending)) },
            )
        }
    }
}

@Composable
private fun EstadoPublicacionBadgeFeed(estado: String) {
    when (estado) {
        ContenidoEstadoPublicacion.PENDING -> {
            Surface(
                shape = RoundedCornerShape(AcademiaDimens.radiusSm),
                color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
                Text(
                    stringResource(R.string.resources_state_pending),
                    modifier = Modifier.padding(
                        horizontal = AcademiaDimens.paddingCardCompact,
                        vertical = AcademiaDimens.gapMicro,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
        ContenidoEstadoPublicacion.REJECTED -> {
            Surface(
                shape = RoundedCornerShape(AcademiaDimens.radiusSm),
                color = MaterialTheme.colorScheme.errorContainer,
            ) {
                Text(
                    stringResource(R.string.resources_state_rejected),
                    modifier = Modifier.padding(
                        horizontal = AcademiaDimens.paddingCardCompact,
                        vertical = AcademiaDimens.gapMicro,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
        else -> Spacer(Modifier.size(0.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoRecursosFiltrosSheetContent(
    esPadreNube: Boolean,
    filtroTema: String?,
    filtroEstadoPub: String?,
    onCerrar: () -> Unit,
    onTema: (String?) -> Unit,
    onEstado: (String?) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AcademiaDimens.paddingScreenHorizontal)
            .padding(bottom = AcademiaDimens.gapMd),
    ) {
        SectionHeader(
            title = stringResource(R.string.resources_filter_sheet_title),
            subtitle = stringResource(R.string.resources_filter_sheet_intro),
        )
        Spacer(Modifier.height(AcademiaDimens.spacingListSection))
        SectionHeader(
            title = stringResource(R.string.resources_filter_sheet_section_theme),
            subtitle = stringResource(R.string.resources_filter_sheet_section_theme_subtitle),
        )
        ChipsGroup {
            FilterChip(
                selected = filtroTema == null,
                onClick = { onTema(null) },
                label = { Text(stringResource(R.string.resources_filter_all_themes)) },
            )
            ContenidoTema.todosWire.forEach { wire ->
                FilterChip(
                    selected = filtroTema == wire,
                    onClick = { onTema(if (filtroTema == wire) null else wire) },
                    label = { Text(temaLabel(wire)) },
                )
            }
        }
        if (!esPadreNube) {
            Spacer(Modifier.height(AcademiaDimens.spacingRowComfort))
            SectionHeader(
                title = stringResource(R.string.resources_filter_sheet_section_state),
                subtitle = stringResource(R.string.resources_filter_sheet_section_state_subtitle),
            )
            ChipsGroup {
                FilterChip(
                    selected = filtroEstadoPub == null,
                    onClick = { onEstado(null) },
                    label = { Text(stringResource(R.string.resources_filter_all_states)) },
                )
                FilterChip(
                    selected = filtroEstadoPub == ContenidoEstadoPublicacion.PUBLISHED,
                    onClick = {
                        onEstado(
                            if (filtroEstadoPub == ContenidoEstadoPublicacion.PUBLISHED) {
                                null
                            } else {
                                ContenidoEstadoPublicacion.PUBLISHED
                            },
                        )
                    },
                    label = { Text(stringResource(R.string.resources_state_visible_families)) },
                )
                FilterChip(
                    selected = filtroEstadoPub == ContenidoEstadoPublicacion.PENDING,
                    onClick = {
                        onEstado(
                            if (filtroEstadoPub == ContenidoEstadoPublicacion.PENDING) {
                                null
                            } else {
                                ContenidoEstadoPublicacion.PENDING
                            },
                        )
                    },
                    label = { Text(stringResource(R.string.resources_state_pending)) },
                )
                FilterChip(
                    selected = filtroEstadoPub == ContenidoEstadoPublicacion.REJECTED,
                    onClick = {
                        onEstado(
                            if (filtroEstadoPub == ContenidoEstadoPublicacion.REJECTED) {
                                null
                            } else {
                                ContenidoEstadoPublicacion.REJECTED
                            },
                        )
                    },
                    label = { Text(stringResource(R.string.resources_state_rejected)) },
                )
            }
        }
        Spacer(Modifier.height(AcademiaDimens.spacingRowComfort))
        HorizontalDivider()
        Spacer(Modifier.height(AcademiaDimens.paddingCardCompact))
        PrimaryButton(
            text = stringResource(R.string.resources_filter_sheet_done),
            onClick = onCerrar,
        )
    }
}

@Composable
private fun etiquetaCategoriaSeleccionada(sel: String): String =
    if (sel.trim() == ContenidoViewModel.CATEGORIA_TODAS_MAGIC) {
        stringResource(R.string.resources_publish_all_categories)
    } else {
        sel
    }

@Composable
private fun temaLabel(wire: String): String = stringResource(
    when (wire) {
        ContenidoTema.NOTICIA -> R.string.resources_theme_noticia
        ContenidoTema.ENTRENAMIENTO -> R.string.resources_theme_training
        ContenidoTema.NUTRICION -> R.string.resources_theme_nutrition
        ContenidoTema.EJERCICIO -> R.string.resources_theme_exercise
        ContenidoTema.BIENESTAR -> R.string.resources_theme_wellness
        else -> R.string.resources_theme_other
    },
)

@Composable
private fun ChipTemaContenido(temaWire: String) {
    AppTintedPanel(
        shape = RoundedCornerShape(AcademiaDimens.radiusSm),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentPadding = PaddingValues(
            horizontal = AcademiaDimens.paddingChipVerticalDense + AcademiaDimens.gapSm,
            vertical = AcademiaDimens.gapSm,
        ),
    ) {
        Text(
            temaLabel(temaWire),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun TituloYPreviewTarjetaFeed(item: ContenidoItemUi) {
    val tit = item.titulo.trim()
    val cue = item.cuerpo.trim()
    val mismoInicio = tit.isNotEmpty() &&
        (cue.startsWith(tit) || tit == cue.take(tit.length.coerceAtMost(cue.length)).trim())
    if (tit.isNotEmpty() && !mismoInicio) {
        Text(
            item.titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(AcademiaDimens.gapVerticalTight))
    }
    Text(
        item.cuerpo,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun CabeceraFeedContenido(
    item: ContenidoItemUi,
    mostrarCategoria: Boolean,
    mostrarTema: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        if (mostrarCategoria) {
            Text(
                item.categoriaNombre,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (mostrarTema) {
                Text(
                    temaLabel(item.tema),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    " · ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                tiempoRelativoEspañol(item.createdAtMillis),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CarruselMediaContenido(
    item: ContenidoItemUi,
    onAbrirImagenGrande: ((String) -> Unit)? = null,
) {
    val urls = remember(item.id, item.imagenUrl, item.cuerpoImagenesUrls) {
        listaUrlsMedia(item)
    }
    if (urls.isEmpty()) return
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { urls.size })
    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(AcademiaDimens.contentFeedPagerHeight),
        ) { page ->
            val url = urls[page]
            AsyncImage(
                model = url,
                contentDescription = stringResource(R.string.resources_article_inline_image_cd),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (onAbrirImagenGrande != null) {
                            Modifier.clickable { onAbrirImagenGrande(url) }
                        } else {
                            Modifier
                        },
                    ),
                contentScale = ContentScale.Crop,
            )
        }
        if (urls.size > 1) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = AcademiaDimens.gapVerticalTight),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(urls.size) { i ->
                    Box(
                        Modifier
                            .padding(horizontal = AcademiaDimens.contentPagerDotSpacingH)
                            .size(
                                if (pagerState.currentPage == i) {
                                    AcademiaDimens.contentPagerDotActive
                                } else {
                                    AcademiaDimens.contentPagerDotInactive
                                },
                            )
                            .background(
                                color = if (pagerState.currentPage == i) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                                },
                                shape = CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun TextoPublicacionFeed(item: ContenidoItemUi, maxLinesCuerpo: Int) {
    val tit = item.titulo.trim()
    val cue = item.cuerpo.trim()
    val mismoInicio = tit.isNotEmpty() && (cue.startsWith(tit) || tit == cue.take(tit.length.coerceAtMost(cue.length)).trim())
    if (tit.isNotEmpty() && !mismoInicio) {
        Text(
            item.titulo,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
    Text(
        item.cuerpo,
        style = MaterialTheme.typography.bodyLarge,
        maxLines = maxLinesCuerpo,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun BarraReaccionesContenido(
    reacciones: ContenidoReaccionesUi,
    onReaccionar: (String) -> Unit,
) {
    val reaccionesCd = stringResource(R.string.resources_reactions_cd)
    val chips = listOf(
        Triple(ContenidoReaccionTipo.LIKE, "❤️", reacciones.like),
        Triple(ContenidoReaccionTipo.CELEBRATE, "👏", reacciones.celebrate),
        Triple(ContenidoReaccionTipo.THANKS, "🙏", reacciones.thanks),
        Triple(ContenidoReaccionTipo.STRONG, "💪", reacciones.strong),
    )
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .semantics { contentDescription = reaccionesCd },
        horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingRowComfort),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        chips.forEach { (wire, emoji, count) ->
            val selected = reacciones.miTipo == wire
            Row(
                modifier = Modifier
                    .clickable { onReaccionar(wire) }
                    .padding(
                        horizontal = AcademiaDimens.gapMicro,
                        vertical = AcademiaDimens.gapSm,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapSm),
            ) {
                Text(
                    emoji,
                    style = MaterialTheme.typography.bodySmall,
                )
                if (count > 0) {
                    Text(
                        count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        },
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaContenidoFeed(
    modifier: Modifier = Modifier,
    item: ContenidoItemUi,
    mostrarCategoria: Boolean,
    puedeGestionar: Boolean,
    puedeModerar: Boolean,
    onOpen: () -> Unit,
    onArchivar: () -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    onReaccionar: (String) -> Unit,
    onAbrirImagenGrande: (String) -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    AppCard(
        modifier = modifier.fillMaxWidth(),
        elevated = false,
        containerColor = MaterialTheme.colorScheme.surface,
        includeContentPadding = false,
    ) {
        Column(Modifier.fillMaxWidth()) {
            CarruselMediaContenido(
                item = item,
                onAbrirImagenGrande = onAbrirImagenGrande,
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { onOpen() }
                    .padding(
                        horizontal = AcademiaDimens.paddingCard,
                        vertical = AcademiaDimens.paddingCardCompact,
                    ),
                verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ChipTemaContenido(item.tema)
                    if (item.estadoPublicacion != ContenidoEstadoPublicacion.PUBLISHED) {
                        Spacer(Modifier.width(AcademiaDimens.gapSm))
                        EstadoPublicacionBadgeFeed(item.estadoPublicacion)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        tiempoRelativoEspañol(item.createdAtMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.85f),
                        maxLines = 1,
                    )
                    if (puedeGestionar || puedeModerar) {
                        Box {
                            IconButton(
                                onClick = { menu = true },
                                modifier = Modifier.size(AcademiaDimens.avatarRow),
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.resources_item_menu_cd),
                                )
                            }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                if (puedeModerar) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.resources_approve_action)) },
                                        onClick = {
                                            menu = false
                                            onAprobar()
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.resources_reject_action)) },
                                        onClick = {
                                            menu = false
                                            onRechazar()
                                        },
                                    )
                                }
                                if (puedeGestionar) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.resources_archive_action)) },
                                        onClick = {
                                            menu = false
                                            onArchivar()
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                if (mostrarCategoria) {
                    Text(
                        item.categoriaNombre,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TituloYPreviewTarjetaFeed(item = item)
            }
            if (item.estadoPublicacion == ContenidoEstadoPublicacion.PUBLISHED) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = AcademiaDimens.dividerThickness,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                )
                Column(
                    Modifier.padding(
                        start = AcademiaDimens.paddingCard,
                        end = AcademiaDimens.paddingCard,
                        top = AcademiaDimens.gapVerticalTight,
                        bottom = AcademiaDimens.paddingChipVerticalDense + AcademiaDimens.gapMd,
                    ),
                ) {
                    BarraReaccionesContenido(
                        reacciones = item.reacciones,
                        onReaccionar = onReaccionar,
                    )
                }
            }
        }
    }
}

private fun tituloDesdeCuerpoSocial(cuerpo: String): String {
    val t = cuerpo.trim()
    if (t.isEmpty()) return ""
    val line = t.lineSequence().firstOrNull()?.trim().orEmpty()
    val base = line.ifEmpty { t.take(40) }
    return if (base.length <= 80) base else "${base.take(80).trimEnd()}…"
}

private fun listaUrlsMedia(item: ContenidoItemUi): List<String> {
    val first = item.imagenUrl?.trim()?.takeIf { it.isNotEmpty() }
    val rest = item.cuerpoImagenesUrls.map { it.trim() }.filter { it.isNotEmpty() }
    return buildList {
        if (first != null) add(first)
        addAll(rest)
    }
}

private fun tiempoRelativoEspañol(ms: Long): String {
    val ahora = System.currentTimeMillis()
    val s = (ahora - ms) / 1000L
    if (s < 60) return "ahora"
    val m = s / 60
    if (m < 60) return "hace ${m} min"
    val h = m / 60
    if (h < 24) return "hace ${h} h"
    val d = h / 24
    if (d < 7) return "hace ${d} día${if (d == 1L) "" else "s"}"
    val w = d / 7
    if (w < 5) return "hace ${w} sem"
    val mes = d / 30
    return "hace ${mes.coerceAtLeast(1)} mes${if (mes <= 1L) "" else "es"}"
}

private suspend fun copiarArchivoJpegCapturaContenido(context: Context, origen: File): File? = withContext(Dispatchers.IO) {
    if (!origen.exists() || origen.length() <= 0L) {
        runCatching { origen.delete() }
        return@withContext null
    }
    val dest = File(context.cacheDir, "contenido_pub_${System.currentTimeMillis()}.jpg")
    val ok = runCatching {
        origen.copyTo(dest, overwrite = true)
        origen.delete()
        dest.exists() && dest.length() > 0L
    }.getOrDefault(false)
    if (!ok) {
        runCatching { dest.delete() }
        return@withContext null
    }
    dest
}

private suspend fun copiarUriImagenContenido(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
    val mime = context.contentResolver.getType(uri) ?: return@withContext null
    if (!mime.startsWith("image/")) return@withContext null
    val ext = when {
        mime.contains("png") -> "png"
        mime.contains("webp") -> "webp"
        else -> "jpg"
    }
    val dest = File(context.cacheDir, "contenido_pub_${System.currentTimeMillis()}.$ext")
    runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { out -> input.copyTo(out) }
        }
        dest.takeIf { it.exists() && it.length() > 0L }
    }.getOrNull()
}
