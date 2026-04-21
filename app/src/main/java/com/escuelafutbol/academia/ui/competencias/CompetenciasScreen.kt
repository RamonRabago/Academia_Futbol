package com.escuelafutbol.academia.ui.competencias

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ripple
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.escuelafutbol.academia.R
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.data.local.model.normalizarClaveCategoriaNombre
import com.escuelafutbol.academia.ui.util.coilFotoJugadorModel
import com.escuelafutbol.academia.ui.util.coilFotoModel
import com.escuelafutbol.academia.data.remote.dto.AnotadorMarcadorLinea
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaPartidoRow
import com.escuelafutbol.academia.data.remote.dto.CatalogoDeporteRow
import com.escuelafutbol.academia.data.remote.dto.CompetenciaPartidoEstado
import com.escuelafutbol.academia.data.remote.dto.DetalleMarcadorJsonCodec
import com.escuelafutbol.academia.data.remote.dto.DetalleMarcadorPayload
import com.escuelafutbol.academia.domain.competencias.LiderOfensivoResumen
import com.escuelafutbol.academia.domain.competencias.LideresOfensivosTablaResultado
import com.escuelafutbol.academia.domain.competencias.LineaTablaPosicion
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private const val RUTA_LISTA = "lista"
private const val RUTA_DETALLE = "detalle/{competenciaId}"

/** Debe coincidir en cabecera y filas de [TabTabla] / [FilaTabla] para evitar columnas desfasadas. */
private const val TablaPesoColEquipo = 2.68f
private const val TablaPesoColStat = 0.8f
private const val TablaPesoColVariacion = 0.5f

private enum class PartidoResultadoVisual { Pendiente, Victoria, Empate, Derrota }

private fun partidoResultadoVisual(p: AcademiaCompetenciaPartidoRow): PartidoResultadoVisual {
    if (!p.jugado || p.scorePropio == null || p.scoreRival == null) return PartidoResultadoVisual.Pendiente
    val a = p.scorePropio
    val b = p.scoreRival
    return when {
        a > b -> PartidoResultadoVisual.Victoria
        a < b -> PartidoResultadoVisual.Derrota
        else -> PartidoResultadoVisual.Empate
    }
}

private data class PartidoTarjetaResultadoEstilo(
    val anchoBarra: Dp,
    val colorBarra: Color,
    val colorBorde: Color,
    val colorFondo: Color,
    val colorMarcador: Color,
)

@Composable
private fun partidoTarjetaResultadoEstilo(rv: PartidoResultadoVisual): PartidoTarjetaResultadoEstilo {
    val dark = isSystemInDarkTheme()
    val scheme = MaterialTheme.colorScheme
    return when (rv) {
        PartidoResultadoVisual.Victoria ->
            if (!dark) {
                PartidoTarjetaResultadoEstilo(
                    anchoBarra = 5.dp,
                    colorBarra = Color(0xFF2E7D32),
                    colorBorde = Color(0xFF2E7D32).copy(alpha = 0.42f),
                    colorFondo = Color(0xFFE8F5E9).copy(alpha = 0.55f),
                    colorMarcador = Color(0xFF1B5E20),
                )
            } else {
                PartidoTarjetaResultadoEstilo(
                    anchoBarra = 5.dp,
                    colorBarra = Color(0xFF66BB6A),
                    colorBorde = Color(0xFF66BB6A).copy(alpha = 0.45f),
                    colorFondo = Color(0xFF1B3D2F).copy(alpha = 0.42f),
                    colorMarcador = Color(0xFFC8E6C9),
                )
            }
        PartidoResultadoVisual.Derrota ->
            if (!dark) {
                PartidoTarjetaResultadoEstilo(
                    anchoBarra = 5.dp,
                    colorBarra = Color(0xFFC62828),
                    colorBorde = Color(0xFFC62828).copy(alpha = 0.42f),
                    colorFondo = Color(0xFFFFEBEE).copy(alpha = 0.5f),
                    colorMarcador = Color(0xFFB71C1C),
                )
            } else {
                PartidoTarjetaResultadoEstilo(
                    anchoBarra = 5.dp,
                    colorBarra = Color(0xFFEF5350),
                    colorBorde = Color(0xFFEF5350).copy(alpha = 0.45f),
                    colorFondo = Color(0xFF4A1C1C).copy(alpha = 0.38f),
                    colorMarcador = Color(0xFFFFCDD2),
                )
            }
        PartidoResultadoVisual.Empate ->
            if (!dark) {
                PartidoTarjetaResultadoEstilo(
                    anchoBarra = 5.dp,
                    colorBarra = Color(0xFF546E7A),
                    colorBorde = Color(0xFF78909C).copy(alpha = 0.5f),
                    colorFondo = Color(0xFFECEFF1).copy(alpha = 0.65f),
                    colorMarcador = Color(0xFF455A64),
                )
            } else {
                PartidoTarjetaResultadoEstilo(
                    anchoBarra = 5.dp,
                    colorBarra = Color(0xFF90A4AE),
                    colorBorde = Color(0xFF90A4AE).copy(alpha = 0.45f),
                    colorFondo = Color(0xFF37474F).copy(alpha = 0.35f),
                    colorMarcador = Color(0xFFCFD8DC),
                )
            }
        PartidoResultadoVisual.Pendiente ->
            PartidoTarjetaResultadoEstilo(
                anchoBarra = 4.dp,
                colorBarra = scheme.outline.copy(alpha = if (dark) 0.65f else 0.55f),
                colorBorde = scheme.outlineVariant,
                colorFondo = scheme.surface,
                colorMarcador = scheme.onSurfaceVariant,
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetenciasScreen(
    viewModel: CompetenciasViewModel,
    config: AcademiaConfig,
) {
    val padreSoloLectura = !config.remoteAcademiaId.isNullOrBlank() &&
        config.cloudMembresiaRol?.equals("parent", ignoreCase = true) == true
    val innerNav = rememberNavController()
    NavHost(
        navController = innerNav,
        startDestination = RUTA_LISTA,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(RUTA_LISTA) {
            CompetenciasListaScaffold(
                viewModel = viewModel,
                config = config,
                padreSoloLectura = padreSoloLectura,
                onAbrirCompetencia = { id ->
                    innerNav.navigate("detalle/$id")
                },
            )
        }
        composable(
            route = RUTA_DETALLE,
            arguments = listOf(navArgument("competenciaId") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("competenciaId") ?: return@composable
            CompetenciaDetalleScaffold(
                competenciaId = id,
                viewModel = viewModel,
                config = config,
                padreSoloLectura = padreSoloLectura,
                onBack = {
                    viewModel.limpiarDetalle()
                    innerNav.popBackStack()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompetenciasListaScaffold(
    viewModel: CompetenciasViewModel,
    config: AcademiaConfig,
    padreSoloLectura: Boolean,
    onAbrirCompetencia: (String) -> Unit,
) {
    val ui by viewModel.listaUi.collectAsState()
    val categoriasHijo by viewModel.categoriasHijoPadre.collectAsState()
    val filtroLocalPadre by viewModel.filtroLocalPadre.collectAsState()
    val vinculosPadreRemotos by viewModel.vinculosPadreJugadorRemoteIds.collectAsState()
    var dialogoNueva by remember { mutableStateOf(false) }

    LaunchedEffect(padreSoloLectura) {
        if (padreSoloLectura) {
            viewModel.refrescarAmbitoPadreCompetencias()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.tab_competitions)) },
                actions = {
                    IconButton(onClick = { viewModel.refrescarLista() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.competitions_reload_cd))
                    }
                },
            )
        },
        floatingActionButton = {
            if (viewModel.puedeCrearCompetencia(config)) {
                ExtendedFloatingActionButton(
                    onClick = { dialogoNueva = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.competitions_fab_new)) },
                )
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                stringResource(
                    if (padreSoloLectura) {
                        R.string.competitions_list_intro_parent
                    } else {
                        R.string.competitions_list_intro
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            if (padreSoloLectura && categoriasHijo.size > 1) {
                val chipScroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(chipScroll)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = filtroLocalPadre == null,
                        onClick = { viewModel.setFiltroLocalPadreCategoria(null) },
                        label = { Text(stringResource(R.string.competitions_parent_filter_all)) },
                    )
                    categoriasHijo.forEach { nombreCat ->
                        FilterChip(
                            selected = filtroLocalPadre == nombreCat,
                            onClick = { viewModel.setFiltroLocalPadreCategoria(nombreCat) },
                            label = { Text(nombreCat) },
                        )
                    }
                }
            }
            if (ui.error != null) {
                Text(ui.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            if (ui.cargando && ui.items.isEmpty()) {
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(24.dp),
                )
            } else if (ui.items.isEmpty()) {
                val vacioPadre = padreSoloLectura && categoriasHijo.isEmpty()
                val msg = when {
                    vacioPadre && vinculosPadreRemotos.isEmpty() ->
                        stringResource(R.string.competitions_parent_empty_no_cloud_links)
                    vacioPadre ->
                        stringResource(R.string.competitions_parent_empty_sync_local)
                    padreSoloLectura -> stringResource(R.string.competitions_parent_empty_no_matches)
                    else -> stringResource(R.string.competitions_empty)
                }
                Text(
                    msg,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp),
                ) {
                    items(ui.items, key = { it.competencia.id }) { row ->
                        OutlinedCard(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onAbrirCompetencia(row.competencia.id) },
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(row.competencia.nombre, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(R.string.competitions_row_sport, row.deporteNombre),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                if (padreSoloLectura && row.categoriasRelacionadas.size > 1) {
                                    Text(
                                        stringResource(
                                            R.string.competitions_parent_categories_in_row,
                                            row.categoriasRelacionadas.joinToString(" · "),
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                                row.competencia.temporada?.takeIf { it.isNotBlank() }?.let { t ->
                                    Text(
                                        stringResource(R.string.competitions_row_season, t),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (dialogoNueva) {
        DialogoNuevaCompetencia(
            viewModel = viewModel,
            onDismiss = { dialogoNueva = false },
            onCreada = { dialogoNueva = false },
        )
    }
}

@Composable
private fun DialogoNuevaCompetencia(
    viewModel: CompetenciasViewModel,
    onDismiss: () -> Unit,
    onCreada: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxSheetHeight = (LocalConfiguration.current.screenHeightDp * 0.88f).dp
    val catalogo by viewModel.catalogoDeportes.collectAsState()
    var nombre by remember { mutableStateOf("") }
    var temporada by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("liga") }
    var deporteSel by remember { mutableStateOf<CatalogoDeporteRow?>(null) }
    var err by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(catalogo) {
        if (deporteSel == null && catalogo.isNotEmpty()) {
            deporteSel = catalogo.first()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp,
            shadowElevation = 6.dp,
        ) {
            Column(
                Modifier
                    .heightIn(max = maxSheetHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    stringResource(R.string.competitions_dialog_new_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text(stringResource(R.string.competitions_field_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = temporada,
                    onValueChange = { temporada = it },
                    label = { Text(stringResource(R.string.competitions_field_season_optional)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = tipo,
                    onValueChange = { tipo = it },
                    label = { Text(stringResource(R.string.competitions_field_type_hint)) },
                    placeholder = { Text(stringResource(R.string.competitions_field_type_placeholder)) },
                    supportingText = { Text(stringResource(R.string.competitions_field_type_allowed_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Text(
                    stringResource(R.string.competitions_field_sport),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    stringResource(R.string.competitions_field_sport_help),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val chipShape = RoundedCornerShape(16.dp)
                catalogo.forEach { d ->
                    val sel = deporteSel?.id == d.id
                    Surface(
                        onClick = { deporteSel = d },
                        modifier = Modifier.fillMaxWidth(),
                        shape = chipShape,
                        color = if (sel) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        border = BorderStroke(
                            width = if (sel) 2.dp else 1.dp,
                            color = if (sel) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                        ),
                        shadowElevation = if (sel) 2.dp else 0.dp,
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Icon(
                                imageVector = if (sel) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = if (sel) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                },
                            )
                            Text(
                                d.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                err?.let { msg ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Text(
                            msg,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(
                        onClick = {
                            val d = deporteSel ?: run {
                                err = context.getString(R.string.competitions_error_pick_sport)
                                return@TextButton
                            }
                            if (nombre.isBlank()) {
                                err = context.getString(R.string.competitions_error_name_required)
                                return@TextButton
                            }
                            scope.launch {
                                viewModel.crearCompetencia(
                                    nombre = nombre,
                                    deporteId = d.id,
                                    tipoCompetencia = tipo,
                                    temporada = temporada.takeIf { it.isNotBlank() },
                                ) { r ->
                                    if (r.isSuccess) onCreada()
                                    else {
                                        err = r.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                                            ?: context.getString(R.string.competitions_error_save_generic)
                                    }
                                }
                            }
                        },
                    ) { Text(stringResource(R.string.competitions_save)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompetenciaDetalleScaffold(
    competenciaId: String,
    viewModel: CompetenciasViewModel,
    config: AcademiaConfig,
    padreSoloLectura: Boolean,
    onBack: () -> Unit,
) {
    val ui by viewModel.detalleUi.collectAsState()
    val hijosPadre by viewModel.hijosPadreVinculados.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var partidoResultado by remember { mutableStateOf<com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaPartidoRow?>(null) }
    val tabs = listOf(
        stringResource(R.string.competitions_tab_matches),
        stringResource(R.string.competitions_tab_table),
        stringResource(R.string.competitions_tab_teams),
    )
    LaunchedEffect(competenciaId) {
        viewModel.cargarDetalle(competenciaId)
        partidoResultado = null
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(ui.competencia?.nombre ?: stringResource(R.string.competitions_detail_loading)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back_cd))
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.refrescarDetalle() }) {
                            Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.competitions_reload_cd))
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                PrimaryTabRow(selectedTabIndex = tab) {
                    tabs.forEachIndexed { i, t ->
                        Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) })
                    }
                }
                HorizontalDivider()
                if (padreSoloLectura) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    ) {
                        Text(
                            stringResource(R.string.competitions_detail_read_only_banner),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                    HorizontalDivider()
                }
                val detallePadreSinContenidoFiltrado = padreSoloLectura &&
                    ui.competencia != null &&
                    !ui.cargando &&
                    ui.error == null &&
                    ui.inscripciones.isEmpty()
                if (detallePadreSinContenidoFiltrado) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    ) {
                        Text(
                            stringResource(R.string.competitions_parent_detail_empty_filtered),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        )
                    }
                    HorizontalDivider()
                }
                when (tab) {
                    0 -> TabPartidos(
                        competenciaId = competenciaId,
                        ui = ui,
                        viewModel = viewModel,
                        config = config,
                        onAbrirResultado = { partidoResultado = it },
                    )
                    1 -> TabTabla(ui = ui)
                    2 -> TabInscripciones(
                        competenciaId = competenciaId,
                        ui = ui,
                        viewModel = viewModel,
                        config = config,
                        padreSoloLectura = padreSoloLectura,
                        hijosPadreVinculados = hijosPadre,
                    )
                }
            }
        }

        partidoResultado?.let { partido ->
            Dialog(
                onDismissRequest = { partidoResultado = null },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false,
                ),
            ) {
                PantallaResultadoPartido(
                    competenciaId = competenciaId,
                    partido = partido,
                    deporte = ui.deporte,
                    onDismiss = { partidoResultado = null },
                    onGuardado = { partidoResultado = null },
                    viewModel = viewModel,
                )
            }
        }
    }
}

private fun jugadorRemoteIdCoincide(a: String?, b: String?): Boolean {
    val x = a?.trim()?.takeIf { it.isNotEmpty() } ?: return false
    val y = b?.trim()?.takeIf { it.isNotEmpty() } ?: return false
    return x.equals(y, ignoreCase = true)
}

private fun jugadorParaLineaMarcador(jugadores: List<Jugador>, a: AnotadorMarcadorLinea): Jugador? {
    val rid = a.jugadorRemoteId?.trim()?.takeIf { it.isNotEmpty() }
    if (rid != null) {
        jugadores.find { j -> jugadorRemoteIdCoincide(j.remoteId, rid) }?.let { return it }
    }
    val nom = a.nombreMostrado.trim()
    if (nom.isEmpty()) return null
    return jugadores.find { it.nombre.trim().equals(nom, ignoreCase = true) }
}

@Composable
private fun PartidoEstadoChip(estadoResuelto: String) {
    val norm = estadoResuelto.trim().lowercase(Locale.ROOT)
    val dark = isSystemInDarkTheme()
    val scheme = MaterialTheme.colorScheme
    val (bg, fg) = when (norm) {
        CompetenciaPartidoEstado.JUGADO ->
            if (!dark) Color(0xFFE8F5E9) to Color(0xFF1B5E20)
            else Color(0xFF1B3D2F) to Color(0xFF81C784)
        CompetenciaPartidoEstado.PROGRAMADO ->
            if (!dark) Color(0xFFFFF8E1) to Color(0xFFE65100)
            else Color(0xFF4A3F00) to Color(0xFFFFE082)
        CompetenciaPartidoEstado.CANCELADO ->
            if (!dark) Color(0xFFFFEBEE) to Color(0xFFC62828)
            else Color(0xFF4A1C1C) to Color(0xFFFFCDD2)
        CompetenciaPartidoEstado.POSPUESTO ->
            scheme.surfaceVariant to scheme.onSurfaceVariant
        else ->
            scheme.secondaryContainer to scheme.onSecondaryContainer
    }
    val label = when (norm) {
        CompetenciaPartidoEstado.JUGADO -> stringResource(R.string.competitions_match_status_chip_played)
        CompetenciaPartidoEstado.PROGRAMADO -> stringResource(R.string.competitions_match_status_chip_scheduled)
        CompetenciaPartidoEstado.CANCELADO -> stringResource(R.string.competitions_match_status_chip_cancelled)
        CompetenciaPartidoEstado.POSPUESTO -> stringResource(R.string.competitions_match_status_chip_postponed)
        else ->
            estadoResuelto.replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString()
            }
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun AvatarAnotadorSinFotoPlaceholder() {
    val cd = stringResource(R.string.competitions_scorer_list_avatar_none_cd)
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
    ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = cd,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun FilaAnotadorPartidoTarjeta(
    jugadorResuelto: Jugador?,
    anotador: AnotadorMarcadorLinea,
    unidadSingular: String,
    unidadPlural: String,
) {
    val context = LocalContext.current
    val fallbackNombre = stringResource(R.string.competitions_scorer_name_unlisted)
    val nombreLista = anotador.nombreMostrado.trim()
        .ifBlank { jugadorResuelto?.nombre?.trim().orEmpty() }
        .ifBlank { fallbackNombre }
    val unidad = if (anotador.cantidad == 1) unidadSingular else unidadPlural
    val fotoModel = remember(
        anotador.fotoUrl,
        anotador.jugadorRemoteId,
        jugadorResuelto?.id,
        jugadorResuelto?.fotoUrlSupabase,
        jugadorResuelto?.fotoRutaAbsoluta,
    ) {
        coilFotoJugadorModel(
            context,
            anotador.fotoUrl?.trim()?.takeIf { it.isNotEmpty() } ?: jugadorResuelto?.fotoUrlSupabase,
            jugadorResuelto?.fotoRutaAbsoluta,
        )
    }
    val avatarModifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (fotoModel != null) {
            SubcomposeAsyncImage(
                model = fotoModel,
                contentDescription = nombreLista,
                modifier = avatarModifier,
                contentScale = ContentScale.Crop,
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                    else -> AvatarAnotadorSinFotoPlaceholder()
                }
            }
        } else {
            AvatarAnotadorSinFotoPlaceholder()
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = nombreLista,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Column(
            modifier = Modifier
                .widthIn(min = 56.dp)
                .wrapContentWidth(Alignment.End),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = anotador.cantidad.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
            )
            Text(
                text = unidad,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun TabPartidos(
    competenciaId: String,
    ui: CompetenciasDetalleUi,
    viewModel: CompetenciasViewModel,
    config: AcademiaConfig,
    onAbrirResultado: (com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaPartidoRow) -> Unit,
) {
    var dialogoPartido by remember { mutableStateOf(false) }
    var expandedPartidoId by remember { mutableStateOf<String?>(null) }
    val fallbackSingular = stringResource(R.string.competitions_scorers_fallback_singular)
    val fallbackPlural = stringResource(R.string.competitions_scorers_fallback_label)

    Column(Modifier.fillMaxSize()) {
        if (viewModel.puedeAgregarInscripcionOPartido(config) && ui.inscripciones.isNotEmpty()) {
            FilledTonalButton(
                onClick = { dialogoPartido = true },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Text(stringResource(R.string.competitions_add_match))
            }
        }
        if (ui.cargando && ui.partidos.isEmpty()) {
            CircularProgressIndicator(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(24.dp),
            )
        } else if (ui.partidos.isEmpty()) {
            Text(
                stringResource(R.string.competitions_no_matches),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(ui.partidos, key = { it.id }) { p ->
                    val detalleMarcador = DetalleMarcadorJsonCodec.decodeOrNull(p.detalleMarcadorJson)
                    val anotadores = detalleMarcador?.anotadores.orEmpty().filter { a ->
                        if (a.cantidad <= 0) return@filter false
                        val nombreOk = a.nombreMostrado.trim().isNotEmpty()
                        val ridOk = !a.jugadorRemoteId.isNullOrBlank()
                        nombreOk || ridOk
                    }
                    val puedeEditar = viewModel.puedeAgregarInscripcionOPartido(config)
                    val exp = expandedPartidoId == p.id
                    var jugadoresCat by remember(p.id) { mutableStateOf<List<Jugador>>(emptyList()) }
                    LaunchedEffect(exp, p.categoriaNombre, p.id, p.detalleMarcadorJson) {
                        if (exp) {
                            jugadoresCat = viewModel.jugadoresParaAnotadoresPartido(
                                p.categoriaNombre,
                                anotadores,
                            )
                        }
                    }
                    val toggleExpand = {
                        expandedPartidoId = if (expandedPartidoId == p.id) null else p.id
                    }
                    val rv = partidoResultadoVisual(p)
                    val scheme = MaterialTheme.colorScheme
                    val estiloTarjeta = partidoTarjetaResultadoEstilo(rv)
                    val iconoResultado = when (rv) {
                        PartidoResultadoVisual.Victoria -> Icons.Default.CheckCircle
                        PartidoResultadoVisual.Derrota -> Icons.Default.Close
                        PartidoResultadoVisual.Empate -> Icons.Default.Remove
                        PartidoResultadoVisual.Pendiente -> null
                    }
                    val descripcionResultado = when (rv) {
                        PartidoResultadoVisual.Victoria -> stringResource(R.string.competitions_match_result_win_cd)
                        PartidoResultadoVisual.Derrota -> stringResource(R.string.competitions_match_result_loss_cd)
                        PartidoResultadoVisual.Empate -> stringResource(R.string.competitions_match_result_draw_cd)
                        PartidoResultadoVisual.Pendiente -> ""
                    }
                    val sumaGolesAnotadores = anotadores.sumOf { it.cantidad }
                    val expandInteraction = remember(p.id) { MutableInteractionSource() }
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = expandInteraction,
                                indication = ripple(bounded = true),
                                onClickLabel = stringResource(R.string.competitions_match_toggle_scorers_cd),
                                role = Role.Button,
                                onClick = toggleExpand,
                            ),
                        border = BorderStroke(1.dp, estiloTarjeta.colorBorde),
                        colors = CardDefaults.outlinedCardColors(containerColor = estiloTarjeta.colorFondo),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                Modifier
                                    .width(estiloTarjeta.anchoBarra)
                                    .fillMaxHeight()
                                    .background(
                                        color = estiloTarjeta.colorBarra,
                                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                                    ),
                            )
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .animateContentSize(animationSpec = tween(280)),
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(
                                                R.string.competitions_match_header_one_line,
                                                p.jornada,
                                                p.rival.ifBlank { "—" },
                                                p.fecha,
                                            ),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = scheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    IconButton(onClick = toggleExpand) {
                                        Icon(
                                            imageVector = if (exp) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                            contentDescription = stringResource(R.string.competitions_match_toggle_scorers_cd),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    if (puedeEditar) {
                                        IconButton(onClick = { onAbrirResultado(p) }) {
                                            Icon(
                                                Icons.Filled.Edit,
                                                contentDescription = stringResource(R.string.competitions_match_edit_result_cd),
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                val marcadorTexto = if (p.jugado && p.scorePropio != null && p.scoreRival != null) {
                                    "${p.scorePropio} – ${p.scoreRival}"
                                } else {
                                    stringResource(R.string.competitions_match_pending)
                                }
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (p.jugado && p.scorePropio != null && p.scoreRival != null && iconoResultado != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                        ) {
                                            Icon(
                                                imageVector = iconoResultado,
                                                contentDescription = descripcionResultado,
                                                tint = estiloTarjeta.colorMarcador,
                                                modifier = Modifier.size(32.dp),
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                text = marcadorTexto,
                                                style = MaterialTheme.typography.headlineLarge,
                                                fontWeight = FontWeight.Black,
                                                color = estiloTarjeta.colorMarcador,
                                                maxLines = 1,
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = marcadorTexto,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = estiloTarjeta.colorMarcador,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                Box(
                                    Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    PartidoEstadoChip(estadoPartidoMostrado(p))
                                }

                                AnimatedVisibility(
                                    visible = exp,
                                    enter = expandVertically(
                                        expandFrom = Alignment.Top,
                                        animationSpec = tween(320),
                                    ) + fadeIn(tween(240)),
                                    exit = shrinkVertically(
                                        shrinkTowards = Alignment.Top,
                                        animationSpec = tween(220),
                                    ) + fadeOut(tween(180)),
                                ) {
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp),
                                    ) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            color = scheme.surfaceVariant.copy(alpha = 0.45f),
                                        ) {
                                            Column(Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
                                                Text(
                                                    text = stringResource(R.string.competitions_scorers_section),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = scheme.primary,
                                                )
                                                if (anotadores.isNotEmpty()) {
                                                    Spacer(Modifier.height(4.dp))
                                                    Text(
                                                        text = pluralStringResource(
                                                            R.plurals.competitions_match_goals_total,
                                                            sumaGolesAnotadores,
                                                            sumaGolesAnotadores,
                                                        ) + " · " + pluralStringResource(
                                                            R.plurals.competitions_match_scoring_players,
                                                            anotadores.size,
                                                            anotadores.size,
                                                        ),
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = scheme.onSurfaceVariant,
                                                    )
                                                }
                                                Spacer(Modifier.height(8.dp))
                                                if (anotadores.isEmpty()) {
                                                    Text(
                                                        text = stringResource(R.string.competitions_match_scorers_empty_detail),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = scheme.onSurfaceVariant,
                                                    )
                                                } else {
                                                    anotadores.forEachIndexed { i, a ->
                                                        if (i > 0) {
                                                            HorizontalDivider(
                                                                Modifier.padding(vertical = 4.dp),
                                                                color = scheme.outlineVariant,
                                                            )
                                                        }
                                                        val jRes = jugadorParaLineaMarcador(jugadoresCat, a)
                                                        FilaAnotadorPartidoTarjeta(
                                                            jugadorResuelto = jRes,
                                                            anotador = a,
                                                            unidadSingular = ui.deporte?.etiquetaScoreSingular ?: fallbackSingular,
                                                            unidadPlural = ui.deporte?.etiquetaScorePlural ?: fallbackPlural,
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
        }
    }

    if (dialogoPartido) {
        DialogoNuevoPartido(
            competenciaId = competenciaId,
            inscripciones = ui.inscripciones,
            partidosExistentes = ui.partidos,
            onDismiss = { dialogoPartido = false },
            onGuardado = { dialogoPartido = false },
            viewModel = viewModel,
        )
    }
}

@Composable
private fun TabTabla(ui: CompetenciasDetalleUi) {
    val deporte = ui.deporte
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        if (ui.tabla.isEmpty()) {
            Text(
                stringResource(R.string.competitions_table_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(Modifier.padding(12.dp)) {
                        CabeceraTablaPosiciones(
                            labelGf = stringResource(R.string.competitions_col_sf),
                            labelGc = stringResource(R.string.competitions_col_sc),
                        )
                        HorizontalDivider(
                            Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                        )
                        ui.tabla.forEachIndexed { index, linea ->
                            FilaTabla(linea)
                            if (index < ui.tabla.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    thickness = 0.5.dp,
                                )
                            }
                        }
                    }
                }
                BloqueLideresOfensivosTabla(
                    deporte = deporte,
                    resultado = ui.lideresOfensivosTabla,
                )
            }
        }
    }
}

@Composable
private fun BloqueLideresOfensivosTabla(
    deporte: CatalogoDeporteRow?,
    resultado: LideresOfensivosTablaResultado,
) {
    when (resultado) {
        is LideresOfensivosTablaResultado.Ranking ->
            TarjetaLideresOfensivosRanking(deporte = deporte, lideres = resultado.items)
        LideresOfensivosTablaResultado.SinDesgloseCoherente ->
            MensajeLideresTablaSecundario(
                deporte = deporte,
                mensaje = stringResource(R.string.competitions_offense_leaders_empty_coherent_unavailable),
            )
        LideresOfensivosTablaResultado.InconsistenciaMarcadorIndividual ->
            MensajeLideresTablaSecundario(
                deporte = deporte,
                mensaje = stringResource(R.string.competitions_offense_leaders_empty_inconsistent_scorers),
            )
    }
}

@Composable
private fun MensajeLideresTablaSecundario(
    deporte: CatalogoDeporteRow?,
    mensaje: String,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    tituloSeccionLideresOfensivos(deporte),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    mensaje,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun tituloSeccionLideresOfensivos(deporte: CatalogoDeporteRow?): String {
    val codigo = deporte?.codigo?.trim()?.lowercase(Locale.ROOT).orEmpty()
    val resId = when {
        codigo.contains("fut") || codigo == "soccer" || codigo.contains("football") ->
            R.string.competitions_offense_leaders_section_football
        codigo.contains("basket") || codigo.contains("baloncesto") || codigo.contains("basquet") ||
            codigo.contains("básquet") ->
            R.string.competitions_offense_leaders_section_basketball
        else -> R.string.competitions_offense_leaders_section_generic
    }
    return stringResource(resId)
}

private fun iconoLiderOfensivoPorDeporte(deporte: CatalogoDeporteRow?): ImageVector {
    val codigo = deporte?.codigo?.trim()?.lowercase(Locale.ROOT).orEmpty()
    return when {
        codigo.contains("basket") || codigo.contains("baloncesto") || codigo.contains("basquet") ||
            codigo.contains("básquet") ->
            Icons.Filled.SportsBasketball
        codigo.contains("fut") || codigo == "soccer" || codigo.contains("football") ->
            Icons.Filled.SportsSoccer
        else -> Icons.Filled.SportsSoccer
    }
}

/** Icono deportivo en círculo tonal junto al nombre del anotador (tabla / goleadores). */
@Composable
private fun IconoBalonLiderOfensivo(deporte: CatalogoDeporteRow?) {
    val tint = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = iconoLiderOfensivoPorDeporte(deporte),
            contentDescription = stringResource(R.string.competitions_scorer_ball_cd),
            tint = tint,
            modifier = Modifier.size(17.dp),
        )
    }
}

@Composable
private fun TarjetaLideresOfensivosRanking(
    deporte: CatalogoDeporteRow?,
    lideres: List<LiderOfensivoResumen>,
) {
    val unidadPlural = deporte?.etiquetaScorePlural ?: stringResource(R.string.competitions_scorers_fallback_label)
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                tituloSeccionLideresOfensivos(deporte),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                stringResource(R.string.competitions_offense_leaders_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
            )
            lideres.forEachIndexed { index, l ->
                if (index > 0) {
                    HorizontalDivider(
                        Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${index + 1}.",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFeatureSettings = "tnum",
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.widthIn(min = 22.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    IconoBalonLiderOfensivo(deporte)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        l.nombreMostrado,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${l.total} $unidadPlural",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFeatureSettings = "tnum",
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.CeldaStatNumericaCabecera(texto: String) {
    Box(
        Modifier
            .weight(TablaPesoColStat)
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(end = 2.dp),
        )
    }
}

@Composable
private fun RowScope.CeldaStatNumericaValor(valor: Int) {
    Box(
        Modifier
            .weight(TablaPesoColStat)
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            "$valor",
            style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
            maxLines = 1,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(end = 2.dp),
        )
    }
}

@Composable
private fun RowScope.CeldaVariacionTabla(variacion: Int?) {
    val desc = when {
        variacion == null -> stringResource(R.string.competitions_table_variation_na_cd)
        variacion == 0 -> stringResource(R.string.competitions_table_variation_same_cd)
        variacion > 0 -> stringResource(R.string.competitions_table_variation_up_cd, variacion)
        else -> stringResource(R.string.competitions_table_variation_down_cd, -variacion)
    }
    Box(
        modifier = Modifier
            .weight(TablaPesoColVariacion)
            .semantics { contentDescription = desc },
        contentAlignment = Alignment.Center,
    ) {
        when {
            variacion == null || variacion == 0 -> {
                Text(
                    "—",
                    style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            variacion > 0 -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        "$variacion",
                        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            else -> {
                val n = -variacion
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        "$n",
                        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun CabeceraTablaPosiciones(labelGf: String, labelGc: String) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.competitions_col_team),
            modifier = Modifier.weight(TablaPesoColEquipo),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        val titulos = listOf("PJ", "G", "E", "P", labelGf, labelGc, "±", "Pts")
        titulos.forEach { titulo -> CeldaStatNumericaCabecera(titulo) }
        Box(
            Modifier.weight(TablaPesoColVariacion),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(R.string.competitions_table_variation_abbr),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FilaTabla(linea: LineaTablaPosicion) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            linea.nombreEquipoEnTabla,
            modifier = Modifier.weight(TablaPesoColEquipo),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
        )
        val valores = listOf(
            linea.partidosJugados,
            linea.ganados,
            linea.empatados,
            linea.perdidos,
            linea.scoreFavor,
            linea.scoreContra,
            linea.diferenciaScore,
            linea.puntosTabla,
        )
        valores.forEach { v -> CeldaStatNumericaValor(v) }
        CeldaVariacionTabla(linea.variacionPosicion)
    }
}

private data class HijoGrupoInscripcionesPadre(
    val hijo: Jugador,
    val filas: List<com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaCategoriaRow>,
)

private fun agruparInscripcionesPadrePorHijo(
    hijos: List<Jugador>,
    inscripciones: List<com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaCategoriaRow>,
): List<HijoGrupoInscripcionesPadre> =
    hijos.mapNotNull { h ->
        val clave = normalizarClaveCategoriaNombre(h.categoria)
        val filas = inscripciones.filter { normalizarClaveCategoriaNombre(it.categoriaNombre) == clave }
        if (filas.isEmpty()) null else HijoGrupoInscripcionesPadre(h, filas)
    }

@Composable
private fun FilaDatoInscripcionPadre(
    etiqueta: String,
    valor: String,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant,
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodyLarge,
            color = scheme.onSurface,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TarjetaInscripcionesPorHijoPadre(
    nombreHijo: String,
    filas: List<com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaCategoriaRow>,
    nombreLiga: String?,
    valorSinEquipo: String,
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                nombreHijo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.primary,
            )
            Text(
                stringResource(R.string.competitions_parent_inscriptions_intro),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
            filas.forEachIndexed { idx, ins ->
                if (idx > 0) {
                    HorizontalDivider(
                        Modifier.padding(vertical = 12.dp),
                        color = scheme.outlineVariant,
                    )
                }
                val equipo = ins.nombreEquipoMostrado?.trim()?.takeIf { it.isNotEmpty() } ?: valorSinEquipo
                FilaDatoInscripcionPadre(
                    stringResource(R.string.competitions_parent_inscriptions_team),
                    equipo,
                )
                FilaDatoInscripcionPadre(
                    stringResource(R.string.competitions_parent_inscriptions_category),
                    ins.categoriaNombre.trim().ifEmpty { valorSinEquipo },
                )
                nombreLiga?.let { liga ->
                    FilaDatoInscripcionPadre(
                        stringResource(R.string.competitions_parent_inscriptions_league),
                        liga,
                    )
                }
                ins.grupo?.trim()?.takeIf { it.isNotEmpty() }?.let { g ->
                    Text(
                        stringResource(R.string.competitions_group_label, g),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TabInscripcionesPadre(
    ui: CompetenciasDetalleUi,
    hijosVinculados: List<Jugador>,
) {
    val nombreLiga = ui.competencia?.nombre?.trim()?.takeIf { it.isNotEmpty() }
    val valorSinEquipo = stringResource(R.string.competitions_parent_inscriptions_value_unknown)
    val grupos = remember(hijosVinculados, ui.inscripciones) {
        agruparInscripcionesPadrePorHijo(hijosVinculados, ui.inscripciones)
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (grupos.isEmpty() && ui.inscripciones.isNotEmpty()) {
            item(key = "inscripciones_padre_sin_match") {
                TarjetaInscripcionesPadreSinAsociacion(
                    inscripciones = ui.inscripciones,
                    nombreLiga = nombreLiga,
                    valorSinEquipo = valorSinEquipo,
                )
            }
        } else {
            items(grupos, key = { it.hijo.id }) { g ->
                TarjetaInscripcionesPorHijoPadre(
                    nombreHijo = g.hijo.nombre.trim().ifEmpty { valorSinEquipo },
                    filas = g.filas,
                    nombreLiga = nombreLiga,
                    valorSinEquipo = valorSinEquipo,
                )
            }
        }
    }
}

@Composable
private fun TarjetaInscripcionesPadreSinAsociacion(
    inscripciones: List<com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaCategoriaRow>,
    nombreLiga: String?,
    valorSinEquipo: String,
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = scheme.secondaryContainer.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                stringResource(R.string.competitions_parent_inscriptions_fallback_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSecondaryContainer,
            )
            Text(
                stringResource(R.string.competitions_parent_inscriptions_fallback_hint),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSecondaryContainer.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 6.dp),
            )
            inscripciones.forEachIndexed { idx, ins ->
                if (idx > 0) {
                    HorizontalDivider(
                        Modifier.padding(vertical = 10.dp),
                        color = scheme.outlineVariant,
                    )
                }
                val equipo = ins.nombreEquipoMostrado?.trim()?.takeIf { it.isNotEmpty() } ?: valorSinEquipo
                FilaDatoInscripcionPadre(
                    stringResource(R.string.competitions_parent_inscriptions_team),
                    equipo,
                )
                FilaDatoInscripcionPadre(
                    stringResource(R.string.competitions_parent_inscriptions_category),
                    ins.categoriaNombre.trim().ifEmpty { valorSinEquipo },
                )
                nombreLiga?.let { liga ->
                    FilaDatoInscripcionPadre(
                        stringResource(R.string.competitions_parent_inscriptions_league),
                        liga,
                    )
                }
            }
        }
    }
}

@Composable
private fun TabInscripciones(
    competenciaId: String,
    ui: CompetenciasDetalleUi,
    viewModel: CompetenciasViewModel,
    config: AcademiaConfig,
    padreSoloLectura: Boolean,
    hijosPadreVinculados: List<Jugador>,
) {
    var dialogo by remember { mutableStateOf(false) }
    val categorias by viewModel.nombresCategoriasLocales.collectAsState()
    Column(Modifier.fillMaxSize()) {
        if (viewModel.puedeAgregarInscripcionOPartido(config)) {
            FilledTonalButton(
                onClick = { dialogo = true },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Text(stringResource(R.string.competitions_add_team))
            }
        }
        if (padreSoloLectura) {
            TabInscripcionesPadre(ui = ui, hijosVinculados = hijosPadreVinculados)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ui.inscripciones, key = { it.id }) { ins ->
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(ins.categoriaNombre, style = MaterialTheme.typography.titleSmall)
                            ins.nombreEquipoMostrado?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                            ins.grupo?.takeIf { it.isNotBlank() }?.let { g ->
                                Text(stringResource(R.string.competitions_group_label, g), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
    if (dialogo) {
        DialogoNuevaInscripcion(
            competenciaId = competenciaId,
            categoriasDisponibles = categorias.filter { cat ->
                ui.inscripciones.none { it.categoriaNombre.equals(cat, ignoreCase = true) }
            },
            onDismiss = { dialogo = false },
            onGuardado = { dialogo = false },
            viewModel = viewModel,
        )
    }
}

@Composable
private fun DialogoNuevaInscripcion(
    competenciaId: String,
    categoriasDisponibles: List<String>,
    onDismiss: () -> Unit,
    onGuardado: () -> Unit,
    viewModel: CompetenciasViewModel,
) {
    var catSel by remember { mutableStateOf(categoriasDisponibles.firstOrNull().orEmpty()) }
    var nombreEq by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.competitions_dialog_inscription_title)) },
        text = {
            Column {
                if (categoriasDisponibles.isEmpty()) {
                    Text(stringResource(R.string.competitions_no_categories_left))
                } else {
                    Text(stringResource(R.string.competitions_pick_category))
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(Modifier.height(180.dp)) {
                        items(categoriasDisponibles) { c ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { catSel = c }
                                    .padding(vertical = 6.dp),
                            ) {
                                Text(if (catSel == c) "● " else "○ ", color = MaterialTheme.colorScheme.primary)
                                Text(c)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = nombreEq,
                        onValueChange = { nombreEq = it },
                        label = { Text(stringResource(R.string.competitions_team_display_optional)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (catSel.isBlank()) return@TextButton
                    scope.launch {
                        viewModel.agregarInscripcion(
                            competenciaId = competenciaId,
                            categoriaNombre = catSel,
                            nombreEquipo = nombreEq.takeIf { it.isNotBlank() },
                        ) { r ->
                            if (r.isSuccess) onGuardado()
                            else err = r.exceptionOrNull()?.message
                        }
                    }
                },
                enabled = categoriasDisponibles.isNotEmpty(),
            ) { Text(stringResource(R.string.competitions_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}


private fun sugerirSiguienteJornadaPartido(
    partidos: List<AcademiaCompetenciaPartidoRow>,
    categoriaEnCompetenciaId: String?,
): String {
    if (categoriaEnCompetenciaId == null) return "1"
    val maxJ = partidos
        .filter { it.categoriaEnCompetenciaId == categoriaEnCompetenciaId }
        .maxOfOrNull { it.jornada } ?: 0
    return (maxJ + 1).coerceAtLeast(1).toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoNuevoPartido(
    competenciaId: String,
    inscripciones: List<com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaCategoriaRow>,
    partidosExistentes: List<AcademiaCompetenciaPartidoRow>,
    onDismiss: () -> Unit,
    onGuardado: () -> Unit,
    viewModel: CompetenciasViewModel,
) {
    val context = LocalContext.current
    val calendarioCd = stringResource(R.string.competitions_match_date_picker_cd)
    val cerrarCd = stringResource(R.string.nav_back_cd)
    var insSel by remember { mutableStateOf(inscripciones.firstOrNull()) }
    var jornada by remember { mutableStateOf("1") }
    var fecha by remember { mutableStateOf(LocalDate.now().toString()) }
    var rival by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }
    var mostrarCalendario by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(inscripciones) {
        if (insSel == null || inscripciones.none { it.id == insSel!!.id }) {
            insSel = inscripciones.firstOrNull()
        }
    }
    LaunchedEffect(insSel?.id, partidosExistentes) {
        val id = insSel?.id ?: return@LaunchedEffect
        jornada = sugerirSiguienteJornadaPartido(partidosExistentes, id)
    }
    val chipShape = RoundedCornerShape(18.dp)
    BackHandler {
        if (mostrarCalendario) mostrarCalendario = false
        else onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.surface,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                stringResource(R.string.competitions_dialog_match_title),
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = cerrarCd)
                            }
                        },
                    )
                },
                bottomBar = {
                    Surface(tonalElevation = 1.dp, shadowElevation = 3.dp) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.cancel))
                            }
                            Button(
                                onClick = {
                                    val ins = insSel ?: return@Button
                                    val j = jornada.toIntOrNull() ?: 1
                                    scope.launch {
                                        viewModel.agregarPartido(
                                            competenciaId = competenciaId,
                                            categoriaEnCompetenciaId = ins.id,
                                            categoriaNombre = ins.categoriaNombre,
                                            jornada = j,
                                            fechaIso = fecha.trim(),
                                            rival = rival,
                                        ) { r ->
                                            if (r.isSuccess) onGuardado()
                                            else {
                                                err = r.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                                                    ?: context.getString(R.string.competitions_error_save_generic)
                                            }
                                        }
                                    }
                                },
                                enabled = inscripciones.isNotEmpty(),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(R.string.competitions_save))
                            }
                        }
                    }
                },
            ) { padding ->
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                ) {
                    item {
                        Text(
                            stringResource(R.string.competitions_match_section_inscriptions),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    item {
                        Text(
                            stringResource(R.string.competitions_pick_inscription),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(inscripciones, key = { it.id }) { row ->
                        val sel = insSel?.id == row.id
                        Surface(
                            onClick = { insSel = row },
                            modifier = Modifier.fillMaxWidth(),
                            shape = chipShape,
                            color = if (sel) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            border = BorderStroke(
                                width = if (sel) 2.dp else 1.dp,
                                color = if (sel) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                            ),
                            shadowElevation = if (sel) 2.dp else 0.dp,
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Icon(
                                    imageVector = if (sel) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp),
                                    tint = if (sel) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    },
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        row.categoriaNombre,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    row.nombreEquipoMostrado?.takeIf { it.isNotBlank() }?.let { eq ->
                                        Text(
                                            eq,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text(
                            stringResource(R.string.competitions_match_section_details),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = jornada,
                            onValueChange = { jornada = it.filter { ch -> ch.isDigit() }.take(3) },
                            label = { Text(stringResource(R.string.competitions_field_round)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = fecha,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.competitions_field_date_iso)) },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { mostrarCalendario = true }) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = calendarioCd,
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mostrarCalendario = true },
                        )
                    }
                    item {
                        Text(
                            stringResource(R.string.competitions_match_date_field_help),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 0.dp, bottom = 4.dp),
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = rival,
                            onValueChange = { rival = it },
                            label = { Text(stringResource(R.string.competitions_field_rival)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    item {
                        err?.let { msg ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                            ) {
                                Text(
                                    msg,
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarCalendario) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = isoDiaAPickerMillisUtc(fecha),
        )
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { fecha = pickerMillisUtcAIsoFecha(it) }
                        mostrarCalendario = false
                    },
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

/** Alineado con la convención del DatePicker de Material 3 (medianoche UTC del día). */
private fun isoDiaAPickerMillisUtc(iso: String): Long =
    runCatching {
        LocalDate.parse(iso.trim())
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }.getOrElse {
        LocalDate.now(ZoneOffset.UTC)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }

private fun pickerMillisUtcAIsoFecha(ms: Long): String =
    Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()
        .format(DateTimeFormatter.ISO_LOCAL_DATE)

private fun localDateHoyIso(): String =
    LocalDate.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE)

private fun fechaProgramadaPartido(p: AcademiaCompetenciaPartidoRow): LocalDate? =
    runCatching { LocalDate.parse(p.fecha.trim()) }.getOrNull()

/** Corrige datos incoherentes (marcador con estado «programado»). */
private fun estadoPartidoMostrado(p: AcademiaCompetenciaPartidoRow): String {
    val e = p.estado.trim()
    if (p.jugado &&
        p.scorePropio != null &&
        p.scoreRival != null &&
        e.equals(CompetenciaPartidoEstado.PROGRAMADO, ignoreCase = true)
    ) {
        return CompetenciaPartidoEstado.JUGADO
    }
    return p.estado
}

private fun estadoInicialResultadoPartido(p: AcademiaCompetenciaPartidoRow): String =
    if (p.jugado && p.estado.trim().equals(CompetenciaPartidoEstado.PROGRAMADO, ignoreCase = true)) {
        CompetenciaPartidoEstado.JUGADO
    } else {
        p.estado
    }

private data class AnotadorEditorFila(
    val key: Long,
    val jugadorLocalId: Long?,
    val nombreLibre: String,
    val cantidadTexto: String,
    val remoteIdPendiente: String? = null,
    /** Copia de [AnotadorMarcadorLinea.fotoUrl] al cargar el partido; se reutiliza al guardar si Room no trae foto. */
    val fotoUrlPersistida: String? = null,
)

@Composable
private fun FilaAnotadorResultadoUi(
    fila: AnotadorEditorFila,
    jugadores: List<Jugador>,
    onAbrirSelector: () -> Unit,
    onChange: (AnotadorEditorFila) -> Unit,
    onRemove: () -> Unit,
) {
    val context = LocalContext.current
    val jSel = fila.jugadorLocalId?.let { id -> jugadores.find { it.id == id } }
    val hintElegir = stringResource(R.string.competitions_scorer_pick_player)
    val nombreTitulo = when {
        jSel != null -> jSel.nombre.trim()
        fila.nombreLibre.isNotBlank() -> fila.nombreLibre.trim()
        else -> hintElegir
    }
    val subtitulo = if (jugadores.isEmpty()) {
        stringResource(R.string.competitions_result_no_roster_hint)
    } else {
        stringResource(R.string.competitions_result_tap_to_change_player)
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            onClick = { if (jugadores.isNotEmpty()) onAbrirSelector() },
            enabled = jugadores.isNotEmpty(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    val modelo = jSel?.coilFotoModel(context)
                    if (modelo != null) {
                        AsyncImage(
                            model = modelo,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        nombreTitulo,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        subtitulo,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (jugadores.isEmpty()) 3 else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = fila.cantidadTexto,
                onValueChange = { nv ->
                    onChange(fila.copy(cantidadTexto = nv.filter { it.isDigit() }.take(2)))
                },
                label = { Text(stringResource(R.string.competitions_scorer_qty_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(76.dp),
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.competitions_scorer_remove_cd))
            }
        }
        if (jSel == null) {
            OutlinedTextField(
                value = fila.nombreLibre,
                onValueChange = { onChange(fila.copy(nombreLibre = it)) },
                label = { Text(stringResource(R.string.competitions_scorer_name_manual)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaResultadoPartido(
    competenciaId: String,
    partido: AcademiaCompetenciaPartidoRow,
    deporte: CatalogoDeporteRow?,
    onDismiss: () -> Unit,
    onGuardado: () -> Unit,
    viewModel: CompetenciasViewModel,
) {
    val context = LocalContext.current
    val calendarioCd = stringResource(R.string.competitions_match_date_picker_cd)
    val cerrarCd = stringResource(R.string.competitions_result_close_cd)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectorFilaKey by remember { mutableStateOf<Long?>(null) }
    var busquedaJugador by remember { mutableStateOf("") }
    LaunchedEffect(selectorFilaKey) {
        if (selectorFilaKey != null) busquedaJugador = ""
    }

    var fecha by remember(partido.id) { mutableStateOf(partido.fecha) }
    var sp by remember { mutableStateOf(partido.scorePropio?.toString().orEmpty()) }
    var sr by remember { mutableStateOf(partido.scoreRival?.toString().orEmpty()) }
    var jugado by remember { mutableStateOf(partido.jugado) }
    var estado by remember(partido.id) { mutableStateOf(estadoInicialResultadoPartido(partido)) }
    var err by remember { mutableStateOf<String?>(null) }
    var mostrarCalendarioFecha by remember { mutableStateOf(false) }
    var mostrarCalendarioAnticipoJugado by remember { mutableStateOf(false) }
    BackHandler {
        when {
            mostrarCalendarioAnticipoJugado -> {
                jugado = false
                mostrarCalendarioAnticipoJugado = false
            }
            mostrarCalendarioFecha -> mostrarCalendarioFecha = false
            selectorFilaKey != null -> selectorFilaKey = null
            else -> onDismiss()
        }
    }
    var jugadores by remember { mutableStateOf<List<Jugador>>(emptyList()) }
    val filas = remember(partido.id) { mutableStateListOf<AnotadorEditorFila>() }
    val scope = rememberCoroutineScope()
    val maxSheetList = (LocalConfiguration.current.screenHeightDp * 0.55f).dp

    LaunchedEffect(partido.categoriaNombre) {
        jugadores = viewModel.jugadoresParaCategoria(partido.categoriaNombre)
    }
    LaunchedEffect(partido.id, partido.detalleMarcadorJson) {
        filas.clear()
        val d = DetalleMarcadorJsonCodec.decodeOrNull(partido.detalleMarcadorJson)
        var k = 0L
        d?.anotadores?.forEach { a ->
            k += 1
            filas.add(
                AnotadorEditorFila(
                    key = k,
                    jugadorLocalId = null,
                    nombreLibre = a.nombreMostrado.trim(),
                    cantidadTexto = a.cantidad.coerceIn(1, 99).toString(),
                    remoteIdPendiente = a.jugadorRemoteId?.trim()?.takeIf { it.isNotEmpty() },
                    fotoUrlPersistida = a.fotoUrl?.trim()?.takeIf { it.isNotEmpty() },
                ),
            )
        }
    }
    LaunchedEffect(jugadores) {
        if (jugadores.isEmpty()) return@LaunchedEffect
        for (i in filas.indices) {
            val f = filas[i]
            val rid = f.remoteIdPendiente ?: continue
            if (f.jugadorLocalId != null) continue
            val j = jugadores.find { jr ->
                jr.remoteId?.trim()?.equals(rid, ignoreCase = true) == true
            } ?: continue
            filas[i] = f.copy(
                jugadorLocalId = j.id,
                nombreLibre = j.nombre.trim(),
                remoteIdPendiente = null,
                fotoUrlPersistida = f.fotoUrlPersistida,
            )
        }
    }

    val filtradosJugadores = remember(jugadores, busquedaJugador) {
        val q = busquedaJugador.trim()
        if (q.isEmpty()) jugadores else jugadores.filter { it.nombre.contains(q, ignoreCase = true) }
    }

    val sumaAnotaciones = filas.sumOf { it.cantidadTexto.toIntOrNull() ?: 0 }
    val scorePropioInt = sp.toIntOrNull()
    val avisoSuma = jugado && scorePropioInt != null && filas.isNotEmpty() &&
        sumaAnotaciones != scorePropioInt

    val etiquetaPlural = deporte?.etiquetaScorePlural ?: stringResource(R.string.competitions_scorers_fallback_label)

    fun ejecutarGuardado() {
        val a = sp.toIntOrNull() ?: run {
            err = context.getString(R.string.competitions_error_invalid_score)
            return
        }
        val b = sr.toIntOrNull() ?: run {
            err = context.getString(R.string.competitions_error_invalid_score)
            return
        }
        val lineas = mutableListOf<AnotadorMarcadorLinea>()
        for (f in filas) {
            val nombre = when {
                f.jugadorLocalId != null ->
                    jugadores.find { it.id == f.jugadorLocalId }?.nombre?.trim().orEmpty()
                else -> f.nombreLibre.trim()
            }
            if (nombre.isEmpty()) continue
            val qty = f.cantidadTexto.toIntOrNull()?.coerceIn(1, 99) ?: 1
            val jSel = f.jugadorLocalId?.let { lid -> jugadores.find { it.id == lid } }
            val remote = jSel?.remoteId?.trim()?.takeIf { it.isNotEmpty() }
            val fotoUrlOut = jSel?.fotoUrlSupabase?.trim()?.takeIf { it.isNotEmpty() }
                ?: f.fotoUrlPersistida?.trim()?.takeIf { it.isNotEmpty() }
            lineas.add(
                AnotadorMarcadorLinea(
                    jugadorRemoteId = remote,
                    nombreMostrado = nombre,
                    cantidad = qty,
                    fotoUrl = fotoUrlOut,
                ),
            )
        }
        val jsonDetalle = DetalleMarcadorJsonCodec.encode(
            DetalleMarcadorPayload(v = 1, anotadores = lineas),
        )
        val estadoTrim = estado.trim()
        val estadoFinal = if (jugado) {
            when {
                estadoTrim.isEmpty() -> CompetenciaPartidoEstado.JUGADO
                estadoTrim.equals(CompetenciaPartidoEstado.PROGRAMADO, ignoreCase = true) ->
                    CompetenciaPartidoEstado.JUGADO
                else -> estadoTrim
            }
        } else {
            estadoTrim.ifEmpty { CompetenciaPartidoEstado.PROGRAMADO }
        }
        scope.launch {
            viewModel.guardarResultadoPartido(
                competenciaId = competenciaId,
                partidoId = partido.id,
                fechaIso = fecha.trim(),
                scorePropio = a,
                scoreRival = b,
                jugado = jugado,
                estado = estadoFinal,
                detalleMarcadorJson = jsonDetalle,
            ) { r ->
                if (r.isSuccess) onGuardado()
                else {
                    err = r.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                        ?: context.getString(R.string.competitions_error_save_generic)
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Box(Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.surface,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                stringResource(R.string.competitions_dialog_result_title),
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = cerrarCd)
                            }
                        },
                    )
                },
                bottomBar = {
                    Surface(tonalElevation = 1.dp, shadowElevation = 3.dp) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.cancel))
                            }
                            Button(
                                onClick = { ejecutarGuardado() },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(R.string.competitions_save))
                            }
                        }
                    }
                },
            ) { padding ->
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 0.dp, bottom = 12.dp),
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    partido.rival.ifBlank { "—" },
                                    style = MaterialTheme.typography.headlineSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    stringResource(
                                        R.string.competitions_dialog_result_subtitle,
                                        partido.jornada,
                                        fecha,
                                        partido.rival.ifBlank { "—" },
                                        partido.categoriaNombre,
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    item {
                        Text(
                            stringResource(R.string.competitions_result_date_section),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        OutlinedTextField(
                            value = fecha,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.competitions_field_date_iso)) },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { mostrarCalendarioFecha = true }) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = calendarioCd)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mostrarCalendarioFecha = true },
                        )
                    }
                    item {
                        Text(
                            stringResource(R.string.competitions_result_section_score),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = sp,
                                onValueChange = { sp = it.filter { ch -> ch.isDigit() || ch == '-' } },
                                label = { Text(stringResource(R.string.competitions_score_own)) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.headlineLarge.copy(textAlign = TextAlign.Center),
                                modifier = Modifier.widthIn(min = 104.dp, max = 132.dp),
                            )
                            Text(
                                stringResource(R.string.competitions_scoreboard_vs),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                            OutlinedTextField(
                                value = sr,
                                onValueChange = { sr = it.filter { ch -> ch.isDigit() || ch == '-' } },
                                label = { Text(stringResource(R.string.competitions_score_rival)) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.headlineLarge.copy(textAlign = TextAlign.Center),
                                modifier = Modifier.widthIn(min = 104.dp, max = 132.dp),
                            )
                        }
                    }
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(R.string.competitions_played),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Switch(
                                checked = jugado,
                                onCheckedChange = { checked ->
                                    if (!checked) {
                                        jugado = false
                                        return@Switch
                                    }
                                    val programada = fechaProgramadaPartido(partido)
                                    val hoy = LocalDate.now(ZoneId.systemDefault())
                                    jugado = true
                                    if (programada != null && hoy.isBefore(programada)) {
                                        mostrarCalendarioAnticipoJugado = true
                                    }
                                },
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            estado,
                            { estado = it },
                            label = { Text(stringResource(R.string.competitions_field_state_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    item { HorizontalDivider(Modifier.padding(vertical = 4.dp)) }
                    item {
                        Text(
                            stringResource(R.string.competitions_scorers_section),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            stringResource(R.string.competitions_scorers_section_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            etiquetaPlural,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    items(filas, key = { it.key }) { fila ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                FilaAnotadorResultadoUi(
                                    fila = fila,
                                    jugadores = jugadores,
                                    onAbrirSelector = { selectorFilaKey = fila.key },
                                    onChange = { nuevo ->
                                        val ix = filas.indexOfFirst { it.key == fila.key }
                                        if (ix >= 0) filas[ix] = nuevo
                                    },
                                    onRemove = { filas.removeAll { it.key == fila.key } },
                                )
                            }
                        }
                    }
                    item {
                        FilledTonalButton(
                            onClick = {
                                val nk = (filas.maxOfOrNull { it.key } ?: 0L) + 1L
                                filas.add(
                                    AnotadorEditorFila(
                                        key = nk,
                                        jugadorLocalId = null,
                                        nombreLibre = "",
                                        cantidadTexto = "1",
                                    ),
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(stringResource(R.string.competitions_scorer_add))
                            }
                        }
                    }
                    item {
                        if (avisoSuma) {
                            Text(
                                stringResource(R.string.competitions_scorer_sum_hint, sumaAnotaciones, scorePropioInt!!),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                    item {
                        err?.let { msg ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                            ) {
                                Text(
                                    msg,
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }

            if (selectorFilaKey != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectorFilaKey = null },
                    sheetState = sheetState,
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                    ) {
                        Text(
                            stringResource(R.string.competitions_result_picker_title),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        )
                        OutlinedTextField(
                            value = busquedaJugador,
                            onValueChange = { busquedaJugador = it },
                            label = { Text(stringResource(R.string.competitions_result_search_player)) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                        LazyColumn(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxSheetList),
                        ) {
                            items(filtradosJugadores, key = { it.id }) { j ->
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            j.nombre.trim(),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            j.categoria.trim(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    },
                                    leadingContent = {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            val m = j.coilFotoModel(context)
                                            if (m != null) {
                                                AsyncImage(
                                                    model = m,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop,
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Outlined.Person,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(26.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier.clickable {
                                        val k = selectorFilaKey ?: return@clickable
                                        val ix = filas.indexOfFirst { it.key == k }
                                        if (ix >= 0) {
                                            filas[ix] = filas[ix].copy(
                                                jugadorLocalId = j.id,
                                                nombreLibre = j.nombre.trim(),
                                                remoteIdPendiente = null,
                                                fotoUrlPersistida = null,
                                            )
                                        }
                                        selectorFilaKey = null
                                    },
                                )
                            }
                            item {
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                ListItem(
                                    headlineContent = { Text(stringResource(R.string.competitions_scorer_other)) },
                                    leadingContent = {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                    },
                                    modifier = Modifier.clickable {
                                        val k = selectorFilaKey ?: return@clickable
                                        val ix = filas.indexOfFirst { it.key == k }
                                        if (ix >= 0) {
                                            filas[ix] = filas[ix].copy(
                                                jugadorLocalId = null,
                                                nombreLibre = "",
                                                remoteIdPendiente = null,
                                                fotoUrlPersistida = null,
                                            )
                                        }
                                        selectorFilaKey = null
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarCalendarioFecha || mostrarCalendarioAnticipoJugado) {
        val anticipo = mostrarCalendarioAnticipoJugado
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (anticipo) {
                isoDiaAPickerMillisUtc(localDateHoyIso())
            } else {
                isoDiaAPickerMillisUtc(fecha)
            },
        )
        fun cerrarCalendario(revertirJugadoSiAnticipo: Boolean) {
            if (anticipo && revertirJugadoSiAnticipo) jugado = false
            mostrarCalendarioFecha = false
            mostrarCalendarioAnticipoJugado = false
        }
        DatePickerDialog(
            onDismissRequest = { cerrarCalendario(revertirJugadoSiAnticipo = true) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ms = pickerState.selectedDateMillis
                        fecha = if (ms != null) {
                            pickerMillisUtcAIsoFecha(ms)
                        } else if (anticipo) {
                            localDateHoyIso()
                        } else {
                            fecha
                        }
                        cerrarCalendario(revertirJugadoSiAnticipo = false)
                    },
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { cerrarCalendario(revertirJugadoSiAnticipo = true) }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            Column {
                if (anticipo) {
                    Text(
                        stringResource(R.string.competitions_result_early_play_date_intro),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    )
                }
                DatePicker(state = pickerState)
            }
        }
    }
}

