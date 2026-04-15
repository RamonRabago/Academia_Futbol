@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.escuelafutbol.academia.ui.contenido

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.escuelafutbol.academia.R
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

    val puedePublicarAlguna = remember(config, categoriasPub) {
        categoriasPub.any { viewModel.puedePublicar(config, it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.resources_title)) },
            )
        },
        floatingActionButton = {
            if (puedePublicarAlguna && config.remoteAcademiaId != null) {
                FloatingActionButton(
                    onClick = { editorAbierto = true },
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.resources_publish_cd))
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                stringResource(R.string.resources_intro_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            if (config.remoteAcademiaId == null) {
                Text(
                    stringResource(R.string.resources_no_cloud),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filtroTema == null,
                    onClick = { viewModel.setFiltroTema(null) },
                    label = { Text(stringResource(R.string.resources_filter_all_themes)) },
                )
                ContenidoTema.todosWire.forEach { wire ->
                    FilterChip(
                        selected = filtroTema == wire,
                        onClick = {
                            viewModel.setFiltroTema(if (filtroTema == wire) null else wire)
                        },
                        label = { Text(temaLabel(wire)) },
                    )
                }
            }
            if (!esPadreNube) {
                Spacer(Modifier.padding(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = filtroEstadoPub == null,
                        onClick = { viewModel.setFiltroEstadoPublicacion(null) },
                        label = { Text(stringResource(R.string.resources_filter_all_states)) },
                    )
                    FilterChip(
                        selected = filtroEstadoPub == ContenidoEstadoPublicacion.PUBLISHED,
                        onClick = {
                            viewModel.setFiltroEstadoPublicacion(
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
                            viewModel.setFiltroEstadoPublicacion(
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
                            viewModel.setFiltroEstadoPublicacion(
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
            Spacer(Modifier.padding(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.resources_list_heading),
                    style = MaterialTheme.typography.titleSmall,
                )
                TextButton(onClick = { viewModel.refrescar() }) {
                    Text(stringResource(R.string.resources_reload))
                }
            }
            ui.error?.let { err ->
                Text(
                    err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                if (ui.cargando && items.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (!ui.cargando && items.isEmpty()) {
                            item {
                                Text(
                                    stringResource(R.string.resources_empty),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 24.dp),
                                )
                            }
                        }
                        items(items, key = { it.id }) { item ->
                            TarjetaContenidoFeed(
                                item = item,
                                mostrarCategoria = categoriaFiltro == null,
                                puedeGestionar = puedePublicarAlguna &&
                                    viewModel.puedePublicar(config, item.categoriaNombre),
                                puedeModerar = viewModel.puedeModerarPublicacion(config, item),
                                onOpen = { detalle = item },
                                onArchivar = { confirmarArchivar = item },
                                onAprobar = {
                                    viewModel.aprobarDesdeUi(item.id) { r ->
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
                                    viewModel.rechazarDesdeUi(item.id) { r ->
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
                        if (ui.cargando && items.isNotEmpty()) {
                            item {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.heightIn(max = 24.dp),
                                        strokeWidth = 2.dp,
                                    )
                                }
                            }
                        }
                    }
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
                            .heightIn(max = 460.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (viewModel.puedeModerarPublicacion(config, d)) {
                        TextButton(
                            onClick = {
                                viewModel.rechazarDesdeUi(d.id) { r ->
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
                                viewModel.aprobarDesdeUi(d.id) { r ->
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
                        viewModel.archivarDesdeUi(item.id) { r ->
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

    fun limpiarAdjuntosYCerrar() {
        imagenPreview?.let { runCatching { it.delete() } }
        imagenPreview = null
        imagenesCuerpo.forEach { runCatching { it.delete() } }
        imagenesCuerpo.clear()
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = { if (!enviando) limpiarAdjuntosYCerrar() },
        title = { Text(stringResource(R.string.resources_publish_title)) },
        text = {
            Column(
                Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(R.string.resources_publish_category_help), style = MaterialTheme.typography.bodySmall)
                TextButton(
                    onClick = { dialogoCat = true },
                    enabled = categoriasOpciones.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        etiquetaCategoriaSeleccionada(categoriaSel),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (visibleDirectoFamiliasPermitido) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.resources_publish_visible_families_now),
                            style = MaterialTheme.typography.bodyMedium,
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
                Text(stringResource(R.string.resources_publish_theme_label), style = MaterialTheme.typography.labelMedium)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ContenidoTema.todosWire.forEach { w ->
                        FilterChip(
                            selected = temaSel == w,
                            onClick = { temaSel = w },
                            label = { Text(temaLabel(w), style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
                Text(
                    stringResource(R.string.resources_cover_label),
                    style = MaterialTheme.typography.labelMedium,
                )
                FilledTonalButton(
                    onClick = { pickImagen.launch("image/*") },
                    enabled = !enviando,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.resources_add_cover))
                }
                imagenPreview?.let { f ->
                    AsyncImage(
                        model = f,
                        contentDescription = stringResource(R.string.resources_cover_preview_cd),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    TextButton(
                        onClick = {
                            runCatching { f.delete() }
                            imagenPreview = null
                        },
                    ) {
                        Text(stringResource(R.string.resources_remove_cover))
                    }
                }
                Text(
                    stringResource(R.string.resources_body_photos_label),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    stringResource(R.string.resources_body_photos_hint, maxCuerpo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FilledTonalButton(
                    onClick = { pickImagenCuerpo.launch("image/*") },
                    enabled = !enviando && imagenesCuerpo.size < maxCuerpo,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.resources_add_body_photo))
                }
                if (imagenesCuerpo.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        itemsIndexed(
                            imagenesCuerpo,
                            key = { _, f -> f.absolutePath },
                        ) { _, file ->
                            Box(
                                modifier = Modifier.size(width = 88.dp, height = 88.dp),
                            ) {
                                AsyncImage(
                                    model = file,
                                    contentDescription = stringResource(R.string.resources_cover_preview_cd),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                                IconButton(
                                    onClick = {
                                        runCatching { file.delete() }
                                        imagenesCuerpo.remove(file)
                                    },
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
                val maxChars = ContenidoViewModel.MAX_CHARS_CUERPO_SOCIAL
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
                        .heightIn(min = 100.dp),
                    minLines = 3,
                    maxLines = 10,
                )
                errLocal?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !enviando &&
                    categoriaSel.isNotBlank() &&
                    puedePublicarSeleccion(categoriaSel),
                onClick = {
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
                },
            ) {
                Text(stringResource(R.string.resources_publish_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = { limpiarAdjuntosYCerrar() }, enabled = !enviando) {
                Text(stringResource(R.string.cancel))
            }
        },
    )

    if (dialogoCat && categoriasOpciones.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { dialogoCat = false },
            title = { Text(stringResource(R.string.resources_publish_category_label)) },
            text = {
                Column(
                    Modifier
                        .heightIn(max = 360.dp)
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
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp)),
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
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(urls.size) { i ->
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (pagerState.currentPage == i) 7.dp else 5.dp)
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
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        chips.forEach { (wire, emoji, count) ->
            val selected = reacciones.miTipo == wire
            FilterChip(
                selected = selected,
                onClick = { onReaccionar(wire) },
                label = {
                    Text(
                        if (count > 0) "$emoji $count" else emoji,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}

@Composable
private fun TarjetaContenidoFeed(
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
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    CabeceraFeedContenido(
                        item = item,
                        mostrarCategoria = mostrarCategoria,
                        mostrarTema = true,
                    )
                    if (item.estadoPublicacion == ContenidoEstadoPublicacion.PENDING) {
                        Text(
                            stringResource(R.string.resources_state_pending),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    } else if (item.estadoPublicacion == ContenidoEstadoPublicacion.REJECTED) {
                        Text(
                            stringResource(R.string.resources_state_rejected),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                if (puedeGestionar || puedeModerar) {
                    Box {
                        IconButton(onClick = { menu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.resources_item_menu_cd))
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
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { onOpen() },
            ) {
                CarruselMediaContenido(
                    item = item,
                    onAbrirImagenGrande = onAbrirImagenGrande,
                )
                Column(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextoPublicacionFeed(item = item, maxLinesCuerpo = 6)
                }
            }
            if (item.estadoPublicacion == ContenidoEstadoPublicacion.PUBLISHED) {
                Column(Modifier.padding(start = 8.dp, end = 12.dp, bottom = 10.dp)) {
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
