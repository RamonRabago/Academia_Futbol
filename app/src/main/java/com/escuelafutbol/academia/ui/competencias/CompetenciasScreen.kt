@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.escuelafutbol.academia.ui.competencias

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Scaffold
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
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaCategoriaRow
import com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaPartidoRow
import com.escuelafutbol.academia.data.remote.dto.CatalogoDeporteRow
import com.escuelafutbol.academia.data.remote.dto.CompetenciaPartidoEstado
import com.escuelafutbol.academia.data.remote.dto.DetalleMarcadorJsonCodec
import com.escuelafutbol.academia.data.remote.dto.DetalleMarcadorPayload
import com.escuelafutbol.academia.domain.competencias.LiderOfensivoResumen
import com.escuelafutbol.academia.domain.competencias.LideresOfensivosTablaResultado
import com.escuelafutbol.academia.domain.competencias.LineaTablaPosicion
import com.escuelafutbol.academia.domain.competencias.construirLideresOfensivosTabla
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch
import com.escuelafutbol.academia.ui.design.AcademiaContextBanner
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.AppTintedPanel
import com.escuelafutbol.academia.ui.design.ChipsGroup
import com.escuelafutbol.academia.ui.design.EmptyState
import com.escuelafutbol.academia.ui.design.PrimaryButton
import com.escuelafutbol.academia.ui.design.SectionHeader

private const val RUTA_LISTA = "lista"
private const val RUTA_DETALLE = "detalle/{competenciaId}"
private const val RUTA_NUEVA = "nueva"

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
    val scheme = MaterialTheme.colorScheme
    val bar = AcademiaDimens.matchResultBarWidth
    return when (rv) {
        PartidoResultadoVisual.Victoria ->
            PartidoTarjetaResultadoEstilo(
                anchoBarra = bar,
                colorBarra = scheme.primary,
                colorBorde = scheme.primary.copy(alpha = 0.42f),
                colorFondo = scheme.primaryContainer.copy(alpha = 0.5f),
                colorMarcador = scheme.onPrimaryContainer,
            )
        PartidoResultadoVisual.Derrota ->
            PartidoTarjetaResultadoEstilo(
                anchoBarra = bar,
                colorBarra = scheme.error,
                colorBorde = scheme.error.copy(alpha = 0.42f),
                colorFondo = scheme.errorContainer.copy(alpha = 0.5f),
                colorMarcador = scheme.onErrorContainer,
            )
        PartidoResultadoVisual.Empate ->
            PartidoTarjetaResultadoEstilo(
                anchoBarra = bar,
                colorBarra = scheme.tertiary,
                colorBorde = scheme.outline.copy(alpha = 0.45f),
                colorFondo = scheme.surfaceVariant.copy(alpha = 0.65f),
                colorMarcador = scheme.onSurfaceVariant,
            )
        PartidoResultadoVisual.Pendiente ->
            PartidoTarjetaResultadoEstilo(
                anchoBarra = bar,
                colorBarra = scheme.outline.copy(alpha = 0.58f),
                colorBorde = scheme.outlineVariant,
                colorFondo = scheme.surface,
                colorMarcador = scheme.onSurfaceVariant,
            )
    }
}

private fun inscripcionCoincideConHijo(
    hijo: Jugador,
    inscripciones: List<AcademiaCompetenciaCategoriaRow>,
): AcademiaCompetenciaCategoriaRow? =
    inscripciones.firstOrNull { insc ->
        normalizarClaveCategoriaNombre(insc.categoriaNombre) ==
            normalizarClaveCategoriaNombre(hijo.categoria)
    }

private fun hijosParticipantesEnCompetencia(
    hijos: List<Jugador>,
    inscripciones: List<AcademiaCompetenciaCategoriaRow>,
): List<Jugador> {
    if (inscripciones.isEmpty()) return emptyList()
    val catKeys = inscripciones.map { normalizarClaveCategoriaNombre(it.categoriaNombre) }.toSet()
    return hijos
        .filter { normalizarClaveCategoriaNombre(it.categoria) in catKeys }
        .distinctBy { it.id }
        .sortedBy { it.nombre.lowercase(Locale.ROOT) }
}

private fun hijosEnCategoriasDeListaPadre(
    hijos: List<Jugador>,
    categoriasRelacionadas: List<String>,
): List<Jugador> {
    if (categoriasRelacionadas.isEmpty() || hijos.isEmpty()) return emptyList()
    val keys = categoriasRelacionadas.map { normalizarClaveCategoriaNombre(it) }.toSet()
    return hijos
        .filter { normalizarClaveCategoriaNombre(it.categoria) in keys }
        .distinctBy { it.id }
        .sortedBy { it.nombre.lowercase(Locale.ROOT) }
}

private fun nombresHijosLineaPartidoMx(hijosCat: List<Jugador>): String {
    val n = hijosCat.map { it.nombre.trim().ifBlank { "—" } }
    return when (n.size) {
        0 -> ""
        1 -> n[0]
        2 -> "${n[0]} y ${n[1]}"
        else -> n.dropLast(1).joinToString(", ") + " y " + n.last()
    }
}

private fun partidoLineaContextoPadre(
    partido: AcademiaCompetenciaPartidoRow,
    inscripciones: List<AcademiaCompetenciaCategoriaRow>,
    hijos: List<Jugador>,
): String? {
    if (hijos.isEmpty() || inscripciones.isEmpty()) return null
    val insc = inscripciones.find { it.id == partido.categoriaEnCompetenciaId }
        ?: inscripciones.firstOrNull {
            normalizarClaveCategoriaNombre(it.categoriaNombre) ==
                normalizarClaveCategoriaNombre(partido.categoriaNombre)
        }
        ?: return null
    val hijosCat = hijos.filter {
        normalizarClaveCategoriaNombre(it.categoria) ==
            normalizarClaveCategoriaNombre(insc.categoriaNombre)
    }
    if (hijosCat.isEmpty()) return null
    val nombres = nombresHijosLineaPartidoMx(hijosCat)
    val cat = insc.categoriaNombre.trim().ifBlank { partido.categoriaNombre }
    return "$nombres · $cat"
}

@Composable
private fun PadreContextoLigaDetalleBanner(
    hijosParticipantes: List<Jugador>,
    inscripciones: List<AcademiaCompetenciaCategoriaRow>,
) {
    if (hijosParticipantes.isEmpty()) return
    val idsKey = hijosParticipantes.joinToString(",") { it.id.toString() }
    var selectedId by remember(idsKey) {
        mutableStateOf(hijosParticipantes.first().id)
    }
    LaunchedEffect(idsKey) {
        if (hijosParticipantes.none { it.id == selectedId }) {
            selectedId = hijosParticipantes.first().id
        }
    }
    val hijoSel = hijosParticipantes.find { it.id == selectedId } ?: hijosParticipantes.first()
    val insc = inscripcionCoincideConHijo(hijoSel, inscripciones)
    val scheme = MaterialTheme.colorScheme
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = false,
        includeContentPadding = false,
        containerColor = scheme.secondaryContainer.copy(alpha = 0.32f),
    ) {
        Column(
            Modifier.padding(
                horizontal = AcademiaDimens.paddingCardCompact,
                vertical = AcademiaDimens.gapMd,
            ),
        ) {
            if (hijosParticipantes.size > 1) {
                ChipsGroup {
                    hijosParticipantes.forEach { h ->
                        val nombre = h.nombre.trim().ifBlank { "—" }
                        FilterChip(
                            selected = h.id == hijoSel.id,
                            onClick = { selectedId = h.id },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 0.dp)
                                .heightIn(max = 34.dp),
                            label = {
                                Text(
                                    nombre,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                        )
                    }
                }
                Spacer(Modifier.height(AcademiaDimens.gapVerticalTight))
            }
            Text(
                stringResource(R.string.competitions_parent_context_emotional),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            Text(
                hijoSel.nombre.trim().ifBlank { "—" },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSecondaryContainer,
                modifier = Modifier.padding(top = AcademiaDimens.gapSm),
            )
            val cat = (insc?.categoriaNombre?.trim()?.ifBlank { null } ?: hijoSel.categoria).trim().ifBlank { "—" }
            val equipo = insc?.nombreEquipoMostrado?.trim()?.takeIf { it.isNotEmpty() }
            val categoriaYEquipo = if (equipo != null) "$cat · $equipo" else cat
            Text(
                categoriaYEquipo,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = AcademiaDimens.gapMicro),
            )
        }
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
                onNuevaCompetencia = { innerNav.navigate(RUTA_NUEVA) },
            )
        }
        composable(RUTA_NUEVA) {
            NuevaCompetenciaScreen(
                viewModel = viewModel,
                onBack = { innerNav.popBackStack() },
                onCreada = { innerNav.popBackStack() },
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

/** Misma pauta que Inicio (portada hijo) y [com.escuelafutbol.academia.ui.parents.components.ChildHeaderRow]. */
@Composable
private fun CompetenciasPadreAvatarHijo(jugador: Jugador) {
    val context = LocalContext.current
    val modeloFoto = remember(jugador.fotoUrlSupabase, jugador.fotoRutaAbsoluta) {
        coilFotoJugadorModel(context, jugador.fotoUrlSupabase, jugador.fotoRutaAbsoluta)
    }
    Box(
        modifier = Modifier
            .size(AcademiaDimens.iconSizeMd)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (modeloFoto != null) {
            AsyncImage(
                model = modeloFoto,
                contentDescription = stringResource(R.string.player_photo_cd),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                modifier = Modifier.size(AcademiaDimens.iconSizeSm - AcademiaDimens.gapMicro),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
    onNuevaCompetencia: () -> Unit,
) {
    val ui by viewModel.listaUi.collectAsState()
    val categoriasHijo by viewModel.categoriasHijoPadre.collectAsState()
    val filtroLocalPadre by viewModel.filtroLocalPadre.collectAsState()
    val vinculosPadreRemotos by viewModel.vinculosPadreJugadorRemoteIds.collectAsState()
    val hijosPadre by viewModel.hijosPadreVinculados.collectAsState()
    val idsHijosKey = remember(hijosPadre) { hijosPadre.joinToString(",") { it.id.toString() } }
    var hijoFiltroListaPadre by remember(idsHijosKey) { mutableStateOf<Long?>(null) }

    LaunchedEffect(padreSoloLectura) {
        if (padreSoloLectura) {
            viewModel.refrescarAmbitoPadreCompetencias()
        }
    }

    val itemsLista = remember(ui.items, hijoFiltroListaPadre, hijosPadre, padreSoloLectura) {
        if (!padreSoloLectura || hijosPadre.size <= 1 || hijoFiltroListaPadre == null) {
            ui.items
        } else {
            val h = hijosPadre.find { it.id == hijoFiltroListaPadre }
            if (h == null) {
                ui.items
            } else {
                ui.items.filter { row ->
                    hijosEnCategoriasDeListaPadre(listOf(h), row.categoriasRelacionadas).isNotEmpty()
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (padreSoloLectura) {
                                R.string.competitions_screen_title_parent
                            } else {
                                R.string.tab_competitions
                            },
                        ),
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refrescarLista() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.competitions_reload_cd))
                    }
                },
            )
        },
        floatingActionButton = {},
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
        ) {
            SectionHeader(
                title = stringResource(R.string.competitions_list_header_title),
                subtitle = stringResource(
                    if (padreSoloLectura) {
                        R.string.competitions_list_intro_parent_human
                    } else {
                        R.string.competitions_list_intro_staff
                    },
                ),
                modifier = Modifier.padding(bottom = 8.dp),
            )
            if (!padreSoloLectura && viewModel.puedeCrearCompetencia(config)) {
                PrimaryButton(
                    text = stringResource(R.string.competitions_fab_new),
                    onClick = onNuevaCompetencia,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
            if (padreSoloLectura && hijosPadre.size > 1) {
                val todosHijos = stringResource(R.string.competitions_parent_filter_children_all)
                val nombreHijoBanner = hijosPadre
                    .find { it.id == hijoFiltroListaPadre }
                    ?.nombre
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: todosHijos
                AcademiaContextBanner(
                    contextText = stringResource(
                        R.string.competitions_parent_context_banner_child,
                        nombreHijoBanner,
                    ),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                ChipsGroup(modifier = Modifier.padding(bottom = 8.dp)) {
                    FilterChip(
                        selected = hijoFiltroListaPadre == null,
                        onClick = { hijoFiltroListaPadre = null },
                        label = { Text(stringResource(R.string.competitions_parent_filter_children_all)) },
                    )
                    hijosPadre.forEach { h ->
                        val nombre = h.nombre.trim().ifBlank { "—" }
                        FilterChip(
                            selected = hijoFiltroListaPadre == h.id,
                            onClick = { hijoFiltroListaPadre = h.id },
                            leadingIcon = { CompetenciasPadreAvatarHijo(jugador = h) },
                            label = {
                                Text(
                                    nombre,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }
            }
            if (padreSoloLectura && categoriasHijo.size > 1) {
                val todasCats = stringResource(R.string.competitions_parent_filter_all)
                val catBanner = filtroLocalPadre?.takeIf { it.isNotBlank() } ?: todasCats
                AcademiaContextBanner(
                    contextText = stringResource(R.string.working_in, catBanner),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                ChipsGroup(modifier = Modifier.padding(bottom = 8.dp)) {
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
            } else if (itemsLista.isEmpty()) {
                val vacioPadre = padreSoloLectura && categoriasHijo.isEmpty()
                val vacioFiltroHijo = padreSoloLectura &&
                    hijoFiltroListaPadre != null &&
                    ui.items.isNotEmpty()
                val msg = when {
                    vacioFiltroHijo ->
                        stringResource(R.string.competitions_parent_empty_child_filter)
                    vacioPadre && vinculosPadreRemotos.isEmpty() ->
                        stringResource(R.string.competitions_parent_empty_no_cloud_links)
                    vacioPadre ->
                        stringResource(R.string.competitions_parent_empty_sync_local)
                    padreSoloLectura -> stringResource(R.string.competitions_parent_empty_no_matches)
                    else -> stringResource(R.string.competitions_staff_empty_onboarding)
                }
                EmptyState(
                    title = msg,
                    icon = {
                        Icon(
                            Icons.Outlined.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        )
                    },
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                if (!padreSoloLectura) {
                    CompetenciasListaResumenStaff(
                        totalCompetencias = ui.items.size,
                        totalPartidosJugados = ui.items.sumOf { it.partidosJugados },
                        totalCategoriasInscritas = ui.items.sumOf { it.numCategoriasInscritas },
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                    contentPadding = PaddingValues(bottom = AcademiaDimens.gapSm),
                ) {
                    items(itemsLista, key = { it.competencia.id }) { row ->
                        if (padreSoloLectura) {
                            CompetenciaCardPadre(
                                item = row,
                                hijosEnCompetencia = hijosEnCategoriasDeListaPadre(
                                    hijosPadre,
                                    row.categoriasRelacionadas,
                                ),
                                onClick = { onAbrirCompetencia(row.competencia.id) },
                            )
                        } else {
                            CompetenciaCardStaff(
                                item = row,
                                onClick = { onAbrirCompetencia(row.competencia.id) },
                            )
                        }
                    }
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
    var filtroInscripcionDetalle by remember(competenciaId) { mutableStateOf<String?>(null) }
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
    val inscripcionesIdsKeyDetalle = remember(ui.inscripciones) {
        ui.inscripciones.joinToString(",") { it.id }
    }
    LaunchedEffect(inscripcionesIdsKeyDetalle, filtroInscripcionDetalle) {
        val f = filtroInscripcionDetalle
        if (f != null && ui.inscripciones.none { it.id == f }) {
            filtroInscripcionDetalle = null
        }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
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
                val participantesPadre = remember(hijosPadre, ui.inscripciones) {
                    hijosParticipantesEnCompetencia(hijosPadre, ui.inscripciones)
                }
                val detallePadreSinContenidoFiltrado = padreSoloLectura &&
                    ui.competencia != null &&
                    !ui.cargando &&
                    ui.error == null &&
                    ui.inscripciones.isEmpty()
                if (padreSoloLectura && participantesPadre.isNotEmpty() && !detallePadreSinContenidoFiltrado) {
                    PadreContextoLigaDetalleBanner(
                        hijosParticipantes = participantesPadre,
                        inscripciones = ui.inscripciones,
                    )
                    HorizontalDivider()
                }
                if (detallePadreSinContenidoFiltrado) {
                    AppTintedPanel(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        contentPadding = PaddingValues(
                            horizontal = AcademiaDimens.paddingCardCompact,
                            vertical = AcademiaDimens.paddingCardCompact + AcademiaDimens.gapMicro,
                        ),
                    ) {
                        Text(
                            stringResource(R.string.competitions_parent_detail_empty_filtered),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
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
                        padreSoloLectura = padreSoloLectura,
                        hijosPadreVinculados = hijosPadre,
                        filtroInscripcionDetalle = filtroInscripcionDetalle,
                        onFiltroInscripcionDetalleChange = { filtroInscripcionDetalle = it },
                        onAbrirResultado = { partidoResultado = it },
                    )
                    1 -> TabTabla(
                        ui = ui,
                        filtroInscripcionDetalle = filtroInscripcionDetalle,
                        onFiltroInscripcionDetalleChange = { filtroInscripcionDetalle = it },
                    )
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
    val scheme = MaterialTheme.colorScheme
    val (bg, fg) = when (norm) {
        CompetenciaPartidoEstado.JUGADO ->
            scheme.primaryContainer.copy(alpha = 0.55f) to scheme.onPrimaryContainer
        CompetenciaPartidoEstado.PROGRAMADO ->
            scheme.tertiaryContainer.copy(alpha = 0.55f) to scheme.onTertiaryContainer
        CompetenciaPartidoEstado.CANCELADO ->
            scheme.errorContainer.copy(alpha = 0.55f) to scheme.onErrorContainer
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
    AppTintedPanel(
        modifier = Modifier.wrapContentWidth(),
        shape = RoundedCornerShape(AcademiaDimens.radiusDense),
        containerColor = bg,
        contentPadding = PaddingValues(
            horizontal = AcademiaDimens.paddingCardCompact,
            vertical = AcademiaDimens.gapVerticalTight,
        ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

@Composable
private fun AvatarAnotadorSinFotoPlaceholder() {
    val cd = stringResource(R.string.competitions_scorer_list_avatar_none_cd)
    Box(
        Modifier
            .size(AcademiaDimens.avatarRow)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = cd,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.size(AcademiaDimens.iconSizeSm),
        )
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
        .size(AcademiaDimens.avatarRow)
        .clip(CircleShape)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = AcademiaDimens.gapSm),
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
        Spacer(Modifier.width(AcademiaDimens.paddingCardCompact))
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
                .widthIn(min = AcademiaDimens.columnMinScoreboard)
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
private fun EncabezadoCategoriaYResumenPartidos(
    inscripciones: List<AcademiaCompetenciaCategoriaRow>,
    filtroInscripcionId: String?,
    onCambiarFiltro: (String?) -> Unit,
    partidosVisibles: List<AcademiaCompetenciaPartidoRow>,
    mostrarResumenPartidos: Boolean = true,
    /** Texto cuando el filtro es «todas» (p. ej. tabla vs partidos). */
    allCategoriesContextRes: Int = R.string.competitions_detail_matches_all_categories,
) {
    if (inscripciones.isEmpty()) return
    val ordenadas = remember(inscripciones) {
        inscripciones.distinctBy { it.id }.sortedBy { it.categoriaNombre.lowercase(Locale.ROOT) }
    }
    var victorias = 0
    var empates = 0
    var derrotas = 0
    var pendientes = 0
    partidosVisibles.forEach { p ->
        when (partidoResultadoVisual(p)) {
            PartidoResultadoVisual.Victoria -> victorias++
            PartidoResultadoVisual.Empate -> empates++
            PartidoResultadoVisual.Derrota -> derrotas++
            PartidoResultadoVisual.Pendiente -> pendientes++
        }
    }
    val jugados = victorias + empates + derrotas
    Column(
        Modifier
            .fillMaxWidth()
            .padding(
                horizontal = AcademiaDimens.paddingScreenHorizontal,
                vertical = AcademiaDimens.paddingCardCompact + AcademiaDimens.gapMicro,
            ),
        verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingListSection),
    ) {
        SectionHeader(
            title = stringResource(R.string.competitions_detail_matches_category_heading),
            subtitle = null,
        )
        if (ordenadas.size == 1) {
            val solo = ordenadas.first().categoriaNombre.trim().ifBlank { "—" }
            AppTintedPanel(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AcademiaDimens.radiusMd),
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                contentPadding = PaddingValues(
                    horizontal = AcademiaDimens.paddingCardCompact,
                    vertical = AcademiaDimens.paddingCardCompact + AcademiaDimens.gapMicro,
                ),
            ) {
                Text(
                    stringResource(R.string.competitions_detail_matches_category_active, solo),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        } else {
            ChipsGroup {
                FilterChip(
                    selected = filtroInscripcionId == null,
                    onClick = { onCambiarFiltro(null) },
                    label = { Text(stringResource(R.string.competitions_detail_matches_filter_all)) },
                )
                ordenadas.forEach { insc ->
                    val nombre = insc.categoriaNombre.trim().ifBlank { "—" }
                    FilterChip(
                        selected = filtroInscripcionId == insc.id,
                        onClick = { onCambiarFiltro(insc.id) },
                        label = {
                            Text(
                                nombre,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }
            val textoContexto = when (filtroInscripcionId) {
                null -> stringResource(allCategoriesContextRes)
                else -> {
                    val nom = ordenadas.find { it.id == filtroInscripcionId }?.categoriaNombre?.trim()?.ifBlank { null }
                        ?: "—"
                    stringResource(R.string.competitions_detail_matches_category_active, nom)
                }
            }
            AcademiaContextBanner(
                contextText = textoContexto,
                modifier = Modifier.padding(top = AcademiaDimens.gapMd),
            )
        }
        if (mostrarResumenPartidos) {
            Text(
                stringResource(
                    R.string.competitions_detail_matches_summary,
                    jugados,
                    victorias,
                    empates,
                    derrotas,
                    pendientes,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    padreSoloLectura: Boolean,
    hijosPadreVinculados: List<Jugador>,
    filtroInscripcionDetalle: String?,
    onFiltroInscripcionDetalleChange: (String?) -> Unit,
    onAbrirResultado: (com.escuelafutbol.academia.data.remote.dto.AcademiaCompetenciaPartidoRow) -> Unit,
) {
    var dialogoPartido by remember { mutableStateOf(false) }
    var expandedPartidoId by remember { mutableStateOf<String?>(null) }
    val fallbackSingular = stringResource(R.string.competitions_scorers_fallback_singular)
    val fallbackPlural = stringResource(R.string.competitions_scorers_fallback_label)

    val partidosMostrados = remember(ui.partidos, filtroInscripcionDetalle) {
        when (val f = filtroInscripcionDetalle) {
            null -> ui.partidos
            else -> ui.partidos.filter { it.categoriaEnCompetenciaId == f }
        }
    }

    Column(Modifier.fillMaxSize()) {
        if (ui.inscripciones.isNotEmpty()) {
            AppCard(
                elevated = false,
                includeContentPadding = false,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                EncabezadoCategoriaYResumenPartidos(
                    inscripciones = ui.inscripciones,
                    filtroInscripcionId = filtroInscripcionDetalle,
                    onCambiarFiltro = onFiltroInscripcionDetalleChange,
                    partidosVisibles = partidosMostrados,
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            )
        }
        if (viewModel.puedeAgregarInscripcionOPartido(config) && ui.inscripciones.isNotEmpty()) {
                Button(
                onClick = { dialogoPartido = true },
                modifier = Modifier
                    .padding(
                        horizontal = AcademiaDimens.paddingScreenHorizontal,
                        vertical = AcademiaDimens.paddingCardCompact,
                    )
                    .fillMaxWidth()
                    .heightIn(min = AcademiaDimens.buttonMinHeight),
                colors = ButtonDefaults.buttonColors(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(AcademiaDimens.iconSizeSm))
                Spacer(Modifier.width(AcademiaDimens.chipSpacing))
                Text(stringResource(R.string.competitions_add_match))
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            )
        }
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (ui.cargando && ui.partidos.isEmpty()) {
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                )
            } else if (!ui.cargando && ui.partidos.isEmpty()) {
                Text(
                    stringResource(R.string.competitions_no_matches),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (partidosMostrados.isEmpty()) {
                Text(
                    stringResource(R.string.competitions_detail_matches_filtered_empty),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = AcademiaDimens.paddingScreenHorizontal,
                        vertical = AcademiaDimens.paddingCardCompact,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AcademiaDimens.chipSpacing),
                ) {
                    items(partidosMostrados, key = { it.id }) { p ->
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
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AcademiaDimens.radiusXl))
                            .clickable(
                                interactionSource = expandInteraction,
                                indication = ripple(bounded = true),
                                onClickLabel = stringResource(R.string.competitions_match_toggle_scorers_cd),
                                role = Role.Button,
                                onClick = toggleExpand,
                            ),
                        elevated = false,
                        containerColor = estiloTarjeta.colorFondo,
                        borderColor = estiloTarjeta.colorBorde,
                        includeContentPadding = false,
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
                                        shape = RoundedCornerShape(
                                            topStart = AcademiaDimens.radiusLg,
                                            bottomStart = AcademiaDimens.radiusLg,
                                        ),
                                    ),
                            )
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(
                                        horizontal = AcademiaDimens.paddingCardCompact,
                                        vertical = AcademiaDimens.chipSpacing,
                                    )
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
                                if (padreSoloLectura) {
                                    val lineaCtx = partidoLineaContextoPadre(p, ui.inscripciones, hijosPadreVinculados)
                                    if (lineaCtx != null) {
                                        Text(
                                            text = lineaCtx,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .padding(top = AcademiaDimens.radiusSm / 2)
                                                .fillMaxWidth(),
                                        )
                                    }
                                }

                                Spacer(Modifier.height(AcademiaDimens.chipSpacing))

                                val marcadorTexto = if (p.jugado && p.scorePropio != null && p.scoreRival != null) {
                                    "${p.scorePropio} – ${p.scoreRival}"
                                } else {
                                    stringResource(R.string.competitions_match_pending)
                                }
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = AcademiaDimens.gapVerticalTight),
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
                                            Spacer(Modifier.width(AcademiaDimens.chipSpacing))
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

                                Spacer(Modifier.height(AcademiaDimens.gapVerticalTight))

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
                                            .padding(top = AcademiaDimens.paddingCardCompact),
                                    ) {
                                        AppTintedPanel(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(AcademiaDimens.radiusMd),
                                            containerColor = scheme.surfaceVariant.copy(alpha = 0.45f),
                                            contentPadding = PaddingValues(
                                                horizontal = AcademiaDimens.contextBannerHorizontalPadding,
                                                vertical = AcademiaDimens.chipSpacing,
                                            ),
                                        ) {
                                            Text(
                                                text = stringResource(R.string.competitions_scorers_section),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = scheme.primary,
                                            )
                                            if (anotadores.isNotEmpty()) {
                                                Spacer(Modifier.height(AcademiaDimens.radiusSm / 2))
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
                                            Spacer(Modifier.height(AcademiaDimens.radiusSm))
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
                                                            Modifier.padding(vertical = AcademiaDimens.radiusSm / 2),
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
private fun TabTabla(
    ui: CompetenciasDetalleUi,
    filtroInscripcionDetalle: String?,
    onFiltroInscripcionDetalleChange: (String?) -> Unit,
) {
    val deporte = ui.deporte
    val partidosParaTabla = remember(ui.partidos, filtroInscripcionDetalle) {
        when (val f = filtroInscripcionDetalle) {
            null -> ui.partidos
            else -> ui.partidos.filter { it.categoriaEnCompetenciaId == f }
        }
    }
    val tablaMostrada = remember(ui.tabla, filtroInscripcionDetalle) {
        when (val f = filtroInscripcionDetalle) {
            null -> ui.tabla
            else -> ui.tabla.filter { it.categoriaEnCompetenciaId == f }
        }
    }
    val lideresMostrados = remember(partidosParaTabla) {
        construirLideresOfensivosTabla(partidosParaTabla, limite = 3)
    }
    Column(Modifier.fillMaxSize()) {
        if (ui.inscripciones.isNotEmpty()) {
            AppCard(
                elevated = false,
                includeContentPadding = false,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                EncabezadoCategoriaYResumenPartidos(
                    inscripciones = ui.inscripciones,
                    filtroInscripcionId = filtroInscripcionDetalle,
                    onCambiarFiltro = onFiltroInscripcionDetalleChange,
                    partidosVisibles = partidosParaTabla,
                    mostrarResumenPartidos = false,
                    allCategoriesContextRes = R.string.competitions_detail_table_all_categories,
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            )
        }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(
                    horizontal = AcademiaDimens.paddingScreenHorizontal,
                    vertical = AcademiaDimens.radiusSm,
                )
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.chipSpacing),
        ) {
            when {
                ui.tabla.isEmpty() -> {
                    EmptyState(
                        title = stringResource(R.string.competitions_table_empty),
                        icon = {
                            Icon(
                                Icons.Outlined.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                            )
                        },
                    )
                }
                tablaMostrada.isEmpty() -> {
                    EmptyState(
                        title = stringResource(R.string.competitions_table_filtered_empty),
                        icon = {
                            Icon(
                                Icons.Outlined.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                            )
                        },
                    )
                }
                else -> {
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevated = true,
                    ) {
                        Column {
                            CabeceraTablaPosiciones(
                                labelGf = stringResource(R.string.competitions_col_sf),
                                labelGc = stringResource(R.string.competitions_col_sc),
                            )
                            HorizontalDivider(
                                Modifier.padding(vertical = AcademiaDimens.radiusSm / 2),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                            )
                            tablaMostrada.forEachIndexed { index, linea ->
                                FilaTabla(linea)
                                if (index < tablaMostrada.lastIndex) {
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
                        resultado = lideresMostrados,
                    )
                }
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
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = false,
    ) {
        SectionHeader(
            title = tituloSeccionLideresOfensivos(deporte),
            subtitle = mensaje,
            action = {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            },
        )
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
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = false,
    ) {
        SectionHeader(
            title = tituloSeccionLideresOfensivos(deporte),
            subtitle = stringResource(R.string.competitions_offense_leaders_subtitle),
        )
        Spacer(Modifier.height(AcademiaDimens.chipSpacing))
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
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = true,
    ) {
        Column {
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
                modifier = Modifier.padding(top = AcademiaDimens.chipSpacing),
            )
            filas.forEachIndexed { idx, ins ->
                if (idx > 0) {
                    HorizontalDivider(
                        Modifier.padding(vertical = AcademiaDimens.paddingCardCompact),
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
                        modifier = Modifier.padding(top = AcademiaDimens.gapVerticalTight),
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
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = false,
    ) {
        Column {
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
                modifier = Modifier.padding(top = AcademiaDimens.gapVerticalTight),
            )
            inscripciones.forEachIndexed { idx, ins ->
                if (idx > 0) {
                    HorizontalDivider(
                        Modifier.padding(vertical = AcademiaDimens.chipSpacing),
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
            PrimaryButton(
                text = stringResource(R.string.competitions_add_team),
                onClick = { dialogo = true },
                modifier = Modifier
                    .padding(AcademiaDimens.paddingScreenHorizontal)
                    .fillMaxWidth(),
            )
        }
        if (padreSoloLectura) {
            TabInscripcionesPadre(ui = ui, hijosVinculados = hijosPadreVinculados)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(AcademiaDimens.paddingScreenHorizontal),
                verticalArrangement = Arrangement.spacedBy(AcademiaDimens.radiusSm),
            ) {
                items(ui.inscripciones, key = { it.id }) { ins ->
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevated = false,
                    ) {
                        Column {
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
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
        ) {
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
                    Column(Modifier.fillMaxWidth()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(
                                    horizontal = AcademiaDimens.paddingScreenHorizontal,
                                    vertical = AcademiaDimens.gapSm,
                                ),
                            horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingDialogBlock),
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
                        .padding(horizontal = AcademiaDimens.paddingCard + AcademiaDimens.gapSm),
                    verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingDialogBlock),
                    contentPadding = PaddingValues(top = AcademiaDimens.gapSm, bottom = AcademiaDimens.gapSm),
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
                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { insSel = row },
                            elevated = false,
                            includeContentPadding = false,
                            containerColor = if (sel) {
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
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = AcademiaDimens.buttonMinHeight)
                                    .padding(
                                        horizontal = AcademiaDimens.paddingScreenHorizontal,
                                        vertical = AcademiaDimens.paddingCardCompact + AcademiaDimens.gapMicro,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingRowComfort),
                            ) {
                                Icon(
                                    imageVector = if (sel) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = null,
                                    modifier = Modifier.size(AcademiaDimens.iconDialogList),
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
                        HorizontalDivider(Modifier.padding(vertical = AcademiaDimens.gapMd))
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
                            modifier = Modifier.padding(
                                start = AcademiaDimens.gapSm,
                                top = 0.dp,
                                bottom = AcademiaDimens.gapSm,
                            ),
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
                            AppTintedPanel(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(AcademiaDimens.radiusMd),
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
                            ) {
                                Text(
                                    msg,
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
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd)) {
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = jugadores.isNotEmpty()) {
                    if (jugadores.isNotEmpty()) onAbrirSelector()
                },
            elevated = false,
            includeContentPadding = false,
            containerColor = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AcademiaDimens.paddingCardCompact,
                        vertical = AcademiaDimens.paddingCardCompact + AcademiaDimens.gapMicro,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingListSection),
            ) {
                Box(
                    modifier = Modifier
                        .size(AcademiaDimens.avatarResultRow)
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
                            modifier = Modifier.size(AcademiaDimens.iconInset),
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
                    modifier = Modifier.size(AcademiaDimens.iconSizeSm),
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

        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
        ) {
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
                    Column(Modifier.fillMaxWidth()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(
                                    horizontal = AcademiaDimens.paddingScreenHorizontal,
                                    vertical = AcademiaDimens.gapSm,
                                ),
                            horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingDialogBlock),
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
                        .padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingDialogBlock),
                    contentPadding = PaddingValues(bottom = AcademiaDimens.gapSm),
                ) {
                    item {
                        AppTintedPanel(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AcademiaDimens.radiusXl),
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            contentPadding = PaddingValues(AcademiaDimens.paddingCard),
                        ) {
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
                    item { HorizontalDivider(Modifier.padding(vertical = AcademiaDimens.gapSm)) }
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
                            modifier = Modifier.padding(top = AcademiaDimens.gapSm),
                        )
                    }
                    items(filas, key = { it.key }) { fila ->
                        AppTintedPanel(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AcademiaDimens.radiusXl),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
                        ) {
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
                    item {
                        PrimaryButton(
                            text = stringResource(R.string.competitions_scorer_add),
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
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(AcademiaDimens.iconSizeSm),
                                )
                            },
                        )
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
                            AppTintedPanel(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(AcademiaDimens.radiusMd),
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
                            ) {
                                Text(
                                    msg,
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
                        Modifier.fillMaxWidth(),
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

