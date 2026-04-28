package com.escuelafutbol.academia.ui.categoria

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.AppTintedPanel
import com.escuelafutbol.academia.ui.design.EmptyState
import com.escuelafutbol.academia.ui.design.PrimaryButton
import com.escuelafutbol.academia.ui.design.SectionHeader
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
    val categoriasUi by pickerVm.categoriasUi.collectAsState()
    val jugadoresCountByClave by pickerVm.jugadoresCountByCategoriaClave.collectAsState()
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
    val ocultarTodasLasCategorias = categoriasPermitidasCoach != null
    val puedeEditarCategoriasUi = config.puedeEditarCategoriasEnSelector()
    val ctx = LocalContext.current
    var dialogoNueva by remember { mutableStateOf(false) }
    var textoNueva by remember { mutableStateOf("") }
    var portadaPickNombre by remember { mutableStateOf<String?>(null) }
    var imageViewer by remember { mutableStateOf<CategoriaPickerImageViewer?>(null) }
    var menuCategoriaAbierto by remember { mutableStateOf<String?>(null) }
    val pickPortada = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        val n = portadaPickNombre
        portadaPickNombre = null
        if (uri != null && n != null) {
            pickerVm.guardarPortadaCategoria(n, uri)
        }
    }

    val portadaModel = config.coilPortadaModel(ctx)
    val logoModel = config.coilLogoModel(ctx)
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (puedeEditarCategoriasUi) {
                ExtendedFloatingActionButton(
                    onClick = { dialogoNueva = true },
                    icon = {
                        Icon(Icons.Default.Add, contentDescription = null)
                    },
                    text = { Text(stringResource(R.string.pick_category_new_fab)) },
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = AcademiaDimens.paddingScreenHorizontal,
                end = AcademiaDimens.paddingScreenHorizontal,
                top = 0.dp,
                bottom = AcademiaDimens.homeHeroLogoSize + AcademiaDimens.paddingCard,
            ),
            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingRowComfort),
        ) {
            item {
                CategoriaSelectorHero(
                    portadaModel = portadaModel,
                    logoModel = logoModel,
                    onPortadaClick = {
                        if (portadaModel != null) {
                            imageViewer = CategoriaPickerImageViewer.AcademiaPortada
                        }
                    },
                    onLogoClick = {
                        if (logoModel != null) {
                            imageViewer = CategoriaPickerImageViewer.Logo
                        }
                    },
                )
            }

            if (esperandoMembresiaNube) {
                item {
                    AppTintedPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AcademiaDimens.paddingCard + AcademiaDimens.avatarRow),
                        containerColor = scheme.surfaceContainerLow,
                        contentPadding = PaddingValues(AcademiaDimens.paddingCard),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.paddingCardCompact),
                        ) {
                            CircularProgressIndicator()
                            Text(
                                stringResource(R.string.pick_category_membership_loading),
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                item {
                    SectionHeader(
                        title = stringResource(R.string.pick_category_title),
                        subtitle = stringResource(R.string.pick_category_subtitle_short),
                    )
                }

                if (categoriasPermitidasCoach != null && categoriasPermitidasCoach.isNotEmpty()) {
                    item {
                        AppTintedPanel(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = scheme.surfaceContainerLow.copy(alpha = 0.92f),
                            contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
                        ) {
                            Text(
                                stringResource(
                                    R.string.pick_category_coach_assigned_list,
                                    categoriasPermitidasCoach.sorted().joinToString(", "),
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onSurface,
                            )
                        }
                    }
                }
                if (ocultarTodasLasCategorias) {
                    item {
                        AppTintedPanel(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = scheme.primaryContainer.copy(alpha = 0.35f),
                            contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
                        ) {
                            Text(
                                stringResource(R.string.pick_category_coach_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.primary,
                            )
                        }
                    }
                }
                if (categoriasPermitidasCoach != null && categoriasMostrar.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(R.string.pick_category_coach_empty_title),
                            subtitle = stringResource(R.string.pick_category_coach_empty),
                        )
                    }
                }
                if (categoriasPermitidasCoach == null && categoriasMostrar.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(R.string.pick_category_list_empty_title),
                            subtitle = stringResource(R.string.pick_category_list_empty_subtitle),
                        )
                    }
                }

                if (!ocultarTodasLasCategorias) {
                    item {
                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(),
                            elevated = false,
                            containerColor = scheme.primaryContainer.copy(alpha = 0.55f),
                            border = BorderStroke(
                                AcademiaDimens.gapMicro,
                                scheme.primary.copy(alpha = 0.5f),
                            ),
                            includeContentPadding = false,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(AcademiaDimens.paddingCard + AcademiaDimens.gapSm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.paddingCard),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(AcademiaDimens.buttonMinHeight)
                                        .clip(CircleShape)
                                        .background(scheme.primary.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Filled.Groups,
                                        contentDescription = null,
                                        modifier = Modifier.size(AcademiaDimens.iconSizeMd),
                                        tint = scheme.primary,
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        stringResource(R.string.category_all),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = scheme.onPrimaryContainer,
                                    )
                                    Spacer(Modifier.height(AcademiaDimens.gapSm))
                                    Text(
                                        stringResource(R.string.pick_category_all_card_subtitle),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = scheme.onSurfaceVariant,
                                    )
                                }
                                Box(Modifier.width(AcademiaDimens.contentEditorBodyThumb)) {
                                    PrimaryButton(
                                        text = stringResource(R.string.pick_category_enter),
                                        onClick = {
                                            sessionVm.confirmarSeleccion(null)
                                            onCategoriaConfirmada()
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                items(
                    categoriasMostrar,
                    key = { c ->
                        "${c.nombre}|${c.portadaUrlSupabase}|${c.portadaRutaAbsoluta}"
                    },
                ) { cat ->
                    val clave = normalizarClaveCategoriaNombre(cat.nombre)
                    val numJugadores = jugadoresCountByClave[clave] ?: 0
                    val thumb = cat.coilPortadaCategoriaModel(ctx)
                    val hasPortada =
                        cat.portadaRutaAbsoluta != null || !cat.portadaUrlSupabase.isNullOrBlank()
                    val entrar: () -> Unit = {
                        sessionVm.confirmarSeleccion(cat.nombre)
                        onCategoriaConfirmada()
                    }
                    val abrirPortadaOEntrar: () -> Unit = {
                        if (thumb != null) {
                            imageViewer = CategoriaPickerImageViewer.CategoriaPortada(cat.nombre)
                        } else {
                            entrar()
                        }
                    }
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        elevated = false,
                        containerColor = scheme.surface,
                        includeContentPadding = false,
                    ) {
                        Column {
                            val interactionImg = remember(cat.nombre) { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(AcademiaDimens.categoryPickerCardImageHeight)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = AcademiaDimens.radiusXl,
                                            topEnd = AcademiaDimens.radiusXl,
                                        ),
                                    )
                                    .background(scheme.surfaceVariant)
                                    .clickable(
                                        interactionSource = interactionImg,
                                        indication = ripple(bounded = true),
                                        onClick = abrirPortadaOEntrar,
                                    ),
                            ) {
                                if (thumb != null) {
                                    AsyncImage(
                                        model = thumb,
                                        contentDescription = stringResource(R.string.player_photo_tap_to_expand),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = 0.05f),
                                                        Color.Black.copy(alpha = 0.38f),
                                                    ),
                                                ),
                                            ),
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = AcademiaDimens.paddingScreenHorizontal,
                                        vertical = AcademiaDimens.paddingCardCompact + AcademiaDimens.gapSm,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingDialogBlock),
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        cat.nombre,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = scheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Spacer(Modifier.height(AcademiaDimens.gapSm))
                                    Text(
                                        if (numJugadores > 0) {
                                            stringResource(R.string.pick_category_players_line, numJugadores)
                                        } else {
                                            stringResource(R.string.pick_category_players_none)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = scheme.onSurfaceVariant,
                                    )
                                }
                                Box(Modifier.width(AcademiaDimens.contentEditorBodyThumb)) {
                                    PrimaryButton(
                                        text = stringResource(R.string.pick_category_enter),
                                        onClick = entrar,
                                    )
                                }
                                if (puedeEditarCategoriasUi) {
                                    Box {
                                        IconButton(
                                            onClick = {
                                                menuCategoriaAbierto =
                                                    if (menuCategoriaAbierto == cat.nombre) null else cat.nombre
                                            },
                                        ) {
                                            Icon(
                                                Icons.Default.MoreVert,
                                                contentDescription = stringResource(R.string.pick_category_menu_cd),
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = menuCategoriaAbierto == cat.nombre,
                                            onDismissRequest = { menuCategoriaAbierto = null },
                                        ) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(stringResource(R.string.pick_category_menu_change_cover))
                                                },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Image, contentDescription = null)
                                                },
                                                onClick = {
                                                    menuCategoriaAbierto = null
                                                    portadaPickNombre = cat.nombre
                                                    pickPortada.launch("image/*")
                                                },
                                            )
                                            if (hasPortada) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(stringResource(R.string.pick_category_menu_remove_cover))
                                                    },
                                                    onClick = {
                                                        menuCategoriaAbierto = null
                                                        pickerVm.quitarPortadaCategoria(cat.nombre)
                                                    },
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
                PrimaryButton(
                    text = stringResource(R.string.save),
                    onClick = {
                        pickerVm.agregarCategoria(textoNueva)
                        dialogoNueva = false
                        textoNueva = ""
                    },
                    enabled = textoNueva.trim().isNotEmpty(),
                )
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

@Composable
private fun CategoriaSelectorHero(
    portadaModel: Any?,
    logoModel: Any?,
    onPortadaClick: () -> Unit,
    onLogoClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val shapeBottom = RoundedCornerShape(
        bottomStart = AcademiaDimens.radiusLg + AcademiaDimens.gapMd,
        bottomEnd = AcademiaDimens.radiusLg + AcademiaDimens.gapMd,
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AcademiaDimens.categoryPickerHeroHeight)
            .clip(shapeBottom),
    ) {
        val interactionHero = remember { MutableInteractionSource() }
        if (portadaModel != null) {
            AsyncImage(
                model = portadaModel,
                contentDescription = stringResource(R.string.academy_cover),
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = interactionHero,
                        indication = ripple(bounded = true),
                        onClick = onPortadaClick,
                    ),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                scheme.primary.copy(alpha = 0.85f),
                                scheme.primary.copy(alpha = 0.55f),
                            ),
                        ),
                    ),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.12f),
                            Color.Black.copy(alpha = 0.48f),
                        ),
                    ),
                ),
        )
        if (logoModel != null) {
            val interactionLogo = remember { MutableInteractionSource() }
            AsyncImage(
                model = logoModel,
                contentDescription = stringResource(R.string.academy_logo_profile),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(AcademiaDimens.contentEditorBodyThumb)
                    .clip(CircleShape)
                    .border(
                        AcademiaDimens.gapMd,
                        scheme.surface.copy(alpha = 0.9f),
                        CircleShape,
                    )
                    .clickable(
                        interactionSource = interactionLogo,
                        indication = ripple(bounded = true),
                        onClick = onLogoClick,
                    ),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
