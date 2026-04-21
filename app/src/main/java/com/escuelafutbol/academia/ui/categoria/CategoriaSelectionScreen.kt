package com.escuelafutbol.academia.ui.categoria

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.escuelafutbol.academia.data.local.model.puedeEditarCategoriasEnSelector
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.Categoria
import com.escuelafutbol.academia.data.local.model.normalizarClaveCategoriaNombre
import com.escuelafutbol.academia.ui.SessionViewModel
import com.escuelafutbol.academia.ui.util.FullscreenImageViewerDialog
import com.escuelafutbol.academia.ui.util.coilLogoModel
import com.escuelafutbol.academia.ui.util.coilPortadaCategoriaModel
import com.escuelafutbol.academia.ui.util.coilPortadaModel
private sealed class CategoriaPickerImageViewer {
    data object Logo : CategoriaPickerImageViewer()
    data object AcademiaPortada : CategoriaPickerImageViewer()
    data class CategoriaPortada(val nombre: String) : CategoriaPickerImageViewer()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriaSelectionScreen(
    sessionVm: SessionViewModel,
    pickerVm: CategoriaPickerViewModel,
    config: AcademiaConfig,
    /** Si no es null, solo estas categorías (coach en nube). */
    categoriasPermitidasCoach: Set<String>? = null,
    /** Mientras la membresía en nube no está en Room (p. ej. tras cambiar de cuenta). */
    esperandoMembresiaNube: Boolean = false,
    modifier: Modifier = Modifier,
    /** Tras elegir categoría (o «todas»): p. ej. volver a Inicio para no quedar en la pestaña que había detrás. */
    onCategoriaConfirmada: () -> Unit = {},
) {
    val nombreAcademia = config.nombreAcademia
    val categoriasUi by pickerVm.categoriasUi.collectAsState()
    val categoriasMostrar = remember(categoriasUi, categoriasPermitidasCoach) {
        if (categoriasPermitidasCoach == null) {
            categoriasUi
        } else {
            val permitidas = categoriasPermitidasCoach
            val permitidasNorm = permitidas.map { normalizarClaveCategoriaNombre(it) }.toSet()
            fun keyCat(cat: Categoria) = normalizarClaveCategoriaNombre(cat.nombre)
            val coincidentes = categoriasUi.filter { cat ->
                keyCat(cat) in permitidasNorm
            }
            val yaCubiertos = coincidentes.map { keyCat(it) }.toSet()
            val sinteticas = permitidas
                .filter { p -> normalizarClaveCategoriaNombre(p) !in yaCubiertos }
                .map { Categoria(nombre = it.trim()) }
            (coincidentes + sinteticas).sortedBy { normalizarClaveCategoriaNombre(it.nombre) }
        }
    }
    /** Si no es null, el selector está restringido (coach con lista, coach sin categorías, o padre con conjunto vacío): no mostrar «Todas las categorías». */
    val ocultarTodasLasCategorias = categoriasPermitidasCoach != null
    val puedeEditarCategoriasUi = config.puedeEditarCategoriasEnSelector()
    val ctx = LocalContext.current
    var dialogoNueva by remember { mutableStateOf(false) }
    var textoNueva by remember { mutableStateOf("") }
    var portadaPickNombre by remember { mutableStateOf<String?>(null) }
    var imageViewer by remember { mutableStateOf<CategoriaPickerImageViewer?>(null) }
    val pickPortada = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        val n = portadaPickNombre
        portadaPickNombre = null
        if (uri != null && n != null) {
            pickerVm.guardarPortadaCategoria(n, uri)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val logoModel = config.coilLogoModel(ctx)
                        if (logoModel != null) {
                            AsyncImage(
                                model = logoModel,
                                contentDescription = stringResource(R.string.academy_logo_profile),
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        imageViewer = CategoriaPickerImageViewer.Logo
                                    },
                                contentScale = ContentScale.Crop,
                            )
                        }
                        Text(
                            nombreAcademia,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (puedeEditarCategoriasUi) {
                FloatingActionButton(onClick = { dialogoNueva = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_category))
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val portadaModel = config.coilPortadaModel(ctx)
            if (portadaModel != null) {
                AsyncImage(
                    model = portadaModel,
                    contentDescription = stringResource(R.string.academy_cover),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .clickable {
                            imageViewer = CategoriaPickerImageViewer.AcademiaPortada
                        },
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
            ) {
                if (esperandoMembresiaNube) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.pick_category_membership_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                Text(
                    stringResource(R.string.pick_category_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    stringResource(R.string.pick_category_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                )
                if (categoriasPermitidasCoach != null && categoriasPermitidasCoach.isNotEmpty()) {
                    Text(
                        stringResource(
                            R.string.pick_category_coach_assigned_list,
                            categoriasPermitidasCoach.sorted().joinToString(", "),
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                if (ocultarTodasLasCategorias) {
                    Text(
                        stringResource(R.string.pick_category_coach_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                if (categoriasPermitidasCoach != null && categoriasMostrar.isEmpty()) {
                    Text(
                        stringResource(R.string.pick_category_coach_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (!ocultarTodasLasCategorias) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        sessionVm.confirmarSeleccion(null)
                                        onCategoriaConfirmada()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            ) {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            stringResource(R.string.category_all),
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            stringResource(R.string.category_all_hint),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    },
                                )
                            }
                        }
                    }
                    items(
                        categoriasMostrar,
                        key = { c ->
                            "${c.nombre}|${c.portadaUrlSupabase}|${c.portadaRutaAbsoluta}"
                        },
                    ) { cat ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val thumb = cat.coilPortadaCategoriaModel(ctx)
                                Box(
                                    modifier = Modifier
                                        .size(width = 64.dp, height = 44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            if (thumb != null) {
                                                imageViewer =
                                                    CategoriaPickerImageViewer.CategoriaPortada(cat.nombre)
                                            } else {
                                                sessionVm.confirmarSeleccion(cat.nombre)
                                                onCategoriaConfirmada()
                                            }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (thumb != null) {
                                        AsyncImage(
                                            model = thumb,
                                            contentDescription = stringResource(
                                                R.string.player_photo_tap_to_expand,
                                            ),
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 10.dp)
                                        .clickable {
                                            sessionVm.confirmarSeleccion(cat.nombre)
                                            onCategoriaConfirmada()
                                        },
                                ) {
                                    Text(
                                        cat.nombre,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text(
                                        stringResource(R.string.category_row_work_hint),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (puedeEditarCategoriasUi) {
                                    IconButton(
                                        onClick = {
                                            portadaPickNombre = cat.nombre
                                            pickPortada.launch("image/*")
                                        },
                                    ) {
                                        Icon(
                                            Icons.Default.Image,
                                            contentDescription = stringResource(R.string.category_pick_cover_cd),
                                        )
                                    }
                                    if (cat.portadaRutaAbsoluta != null ||
                                        !cat.portadaUrlSupabase.isNullOrBlank()
                                    ) {
                                        IconButton(
                                            onClick = { pickerVm.quitarPortadaCategoria(cat.nombre) },
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.category_clear_cover_cd),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }
        }
    }

    if (dialogoNueva) {
        AlertDialog(
            onDismissRequest = {
                dialogoNueva = false
                textoNueva = ""
            },
            title = { Text(stringResource(R.string.add_category_title)) },
            text = {
                OutlinedTextField(
                    value = textoNueva,
                    onValueChange = { textoNueva = it },
                    label = { Text(stringResource(R.string.category)) },
                    placeholder = { Text(stringResource(R.string.add_category_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerVm.agregarCategoria(textoNueva)
                        dialogoNueva = false
                        textoNueva = ""
                    },
                    enabled = textoNueva.trim().isNotEmpty(),
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dialogoNueva = false
                        textoNueva = ""
                    },
                ) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    imageViewer?.let { v ->
        val model = when (v) {
            CategoriaPickerImageViewer.Logo -> config.coilLogoModel(ctx)
            CategoriaPickerImageViewer.AcademiaPortada -> config.coilPortadaModel(ctx)
            is CategoriaPickerImageViewer.CategoriaPortada ->
                categoriasMostrar.find {
                    normalizarClaveCategoriaNombre(it.nombre) ==
                        normalizarClaveCategoriaNombre(v.nombre)
                }?.coilPortadaCategoriaModel(ctx)
        }
        val titulo = when (v) {
            CategoriaPickerImageViewer.Logo -> stringResource(R.string.academy_logo_section)
            CategoriaPickerImageViewer.AcademiaPortada -> stringResource(R.string.academy_cover_section)
            is CategoriaPickerImageViewer.CategoriaPortada -> stringResource(
                R.string.category_cover_viewer_title,
                v.nombre,
            )
        }
        val cd = when (v) {
            CategoriaPickerImageViewer.Logo -> stringResource(R.string.academy_logo_profile)
            CategoriaPickerImageViewer.AcademiaPortada -> stringResource(R.string.academy_cover)
            is CategoriaPickerImageViewer.CategoriaPortada ->
                stringResource(R.string.player_photo_tap_to_expand)
        }
        if (model != null) {
            FullscreenImageViewerDialog(
                titulo = titulo,
                imageModel = model,
                contentDescription = cd,
                onDismiss = { imageViewer = null },
            )
        } else {
            LaunchedEffect(v) { imageViewer = null }
        }
    }
}
