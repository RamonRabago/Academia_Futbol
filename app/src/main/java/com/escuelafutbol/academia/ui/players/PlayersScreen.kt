package com.escuelafutbol.academia.ui.players

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.yalantis.ucrop.UCrop
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.escuelafutbol.academia.ui.util.FullscreenImageViewerDialog
import com.escuelafutbol.academia.ui.util.coilFotoModel
import com.escuelafutbol.academia.ui.util.formatearFechaCalendarioUtc
import com.escuelafutbol.academia.ui.util.formatearFechaDiaLocal
import com.escuelafutbol.academia.ui.util.formatearFechaHoraLocal
import com.escuelafutbol.academia.R
import androidx.compose.ui.text.style.TextAlign
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.EmptyState
import com.escuelafutbol.academia.ui.design.PrimaryButton
import com.escuelafutbol.academia.ui.design.SectionHeader
import com.escuelafutbol.academia.ui.SessionViewModel
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.data.local.model.RolDispositivo
import com.escuelafutbol.academia.data.local.model.rolDispositivoEfectivo
import com.escuelafutbol.academia.data.local.model.puedeVerMensualidadEnEsteDispositivo
import com.escuelafutbol.academia.data.local.model.esPadreMembresiaNube
import com.escuelafutbol.academia.data.local.model.JugadorHistorialTipo
import java.io.File
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import java.util.UUID
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Vacío permitido; si hay texto, formato de correo razonable (local@dominio con punto). */
private fun emailTutorFormatoSuaveValido(texto: String): Boolean {
    val t = texto.trim()
    if (t.isEmpty()) return true
    val at = t.indexOf('@')
    if (at <= 0 || at != t.lastIndexOf('@')) return false
    val local = t.substring(0, at)
    val domain = t.substring(at + 1)
    if (local.isBlank() || domain.isBlank() || !domain.contains('.')) return false
    val tld = domain.substringAfterLast('.')
    return tld.length >= 2 && domain.none { it.isWhitespace() }
}

/** Lado máximo (px) al decodificar en uCrop; si es bajo, el zoom mínimo queda muy alto y no se puede alejar. */
private const val UCROP_DECODE_MAX_SIDE_PX = 4096

private fun formatImporte(valor: Double): String =
    NumberFormat.getCurrencyInstance(Locale.getDefault()).format(valor)

private const val ADEUDO_LISTA_EPS = 0.009

private enum class FiltroEstadoJugadores {
    TODOS,
    ADEUDO,
    PAGADO,
    BECADOS,
}

private fun extensionParaActa(context: Context, uri: Uri): String {
    val mime = context.contentResolver.getType(uri)
    return when {
        mime == "application/pdf" -> "pdf"
        mime?.startsWith("image/png") == true -> "png"
        mime?.startsWith("image/webp") == true -> "webp"
        mime?.startsWith("image/") == true -> "jpg"
        else -> {
            val name = context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { c ->
                if (c.moveToFirst()) {
                    val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (i >= 0) c.getString(i) else null
                } else {
                    null
                }
            }
            val ext = name?.substringAfterLast('.', "")?.lowercase()?.takeIf { it.length <= 5 }
            when (ext) {
                "pdf", "png", "webp" -> ext
                "jpeg", "jpg" -> "jpg"
                else -> null
            } ?: "pdf"
        }
    }
}

private suspend fun copiarUriAdjuntoAJugador(
    context: Context,
    uri: Uri,
    subcarpeta: String,
): String? {
    val ext = withContext(Dispatchers.IO) { extensionParaActa(context, uri) }
    val dir = File(context.filesDir, subcarpeta).apply { mkdirs() }
    val dest = File(dir, "${UUID.randomUUID()}.$ext")
    val ok = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { out -> input.copyTo(out) }
            }
            dest.exists() && dest.length() > 0L
        }.getOrDefault(false)
    }
    return dest.absolutePath.takeIf { ok }
}

@Composable
private fun MensualidadVisibilidadAviso(
    config: AcademiaConfig,
    uidSesionAuth: String?,
) {
    val texto = when (config.rolDispositivoEfectivo(uidSesionAuth)) {
        RolDispositivo.PADRE_TUTOR -> stringResource(R.string.fee_visibility_as_padre)
        else -> stringResource(R.string.fee_visibility_as_staff_off)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersScreen(
    viewModel: PlayersViewModel,
    sessionVm: SessionViewModel,
    categoriaFiltro: String?,
    configAcademia: AcademiaConfig,
    sessionAuthUserId: String = "",
    /** Navegación a otra pestaña principal del `NavHost` (misma ruta que `AcademiaRoot` Tab.route). */
    onNavigateToMainTab: (String) -> Unit = {},
) {
    if (configAcademia.esPadreMembresiaNube()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
            contentAlignment = Alignment.Center,
        ) {
            EmptyState(
                title = stringResource(R.string.role_route_blocked_title),
                subtitle = stringResource(R.string.role_route_blocked_body),
            )
        }
        return
    }
    val uidSesion = sessionAuthUserId.takeIf { it.isNotBlank() }
    val puedeVerMensualidad = configAcademia.puedeVerMensualidadEnEsteDispositivo(uidSesion)
    val jugadoresUi by viewModel.jugadoresUi.collectAsState()
    val etiquetasAltaPorUid by viewModel.etiquetasAltaPorUid.collectAsState()
    var busquedaNombre by remember { mutableStateOf("") }
    var categoriaChip by remember { mutableStateOf<String?>(null) }
    var estadoFiltro by remember { mutableStateOf(FiltroEstadoJugadores.TODOS) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val formularioJugador by viewModel.formularioJugador.collectAsState()
    val formularioAbierto = formularioJugador !is FormularioJugadorUi.Oculto
    val formularioSession by viewModel.formularioSession.collectAsState()
    val formCurpDocPath by viewModel.formCurpDocPath.collectAsState()
    val formActaPath by viewModel.formActaPath.collectAsState()

    var jugadorHistorial by remember { mutableStateOf<Jugador?>(null) }
    var jugadorBaja by remember { mutableStateOf<Jugador?>(null) }
    var expandedJugadorId by remember { mutableStateOf<Long?>(null) }
    var jugadorFotoAmpliada by remember { mutableStateOf<Jugador?>(null) }
    var jugadorDetalle by remember { mutableStateOf<Jugador?>(null) }

    /** Evita que un "atrás" del sistema al cerrar el selector cierre el alta. */
    var awaitingExternalActivityResult by remember { mutableStateOf(false) }

    LaunchedEffect(formularioAbierto) {
        sessionVm.setImpideVolverASeleccionCategoria(formularioAbierto)
    }

    LaunchedEffect(puedeVerMensualidad) {
        if (!puedeVerMensualidad) {
            if (estadoFiltro == FiltroEstadoJugadores.ADEUDO ||
                estadoFiltro == FiltroEstadoJugadores.PAGADO
            ) {
                estadoFiltro = FiltroEstadoJugadores.TODOS
            }
        }
    }

    LaunchedEffect(jugadoresUi) {
        val id = expandedJugadorId ?: return@LaunchedEffect
        if (jugadoresUi.none { it.jugador.id == id }) expandedJugadorId = null
    }

    LaunchedEffect(jugadoresUi) {
        val id = jugadorDetalle?.id ?: return@LaunchedEffect
        if (jugadoresUi.none { it.jugador.id == id }) jugadorDetalle = null
    }

    val categoriasOrdenadas = remember(jugadoresUi) {
        jugadoresUi.map { it.jugador.categoria.trim() }.distinct().sorted()
    }

    val listaFiltrada = remember(
        jugadoresUi,
        busquedaNombre,
        categoriaChip,
        estadoFiltro,
        puedeVerMensualidad,
    ) {
        val q = busquedaNombre.trim().lowercase(Locale.getDefault())
        jugadoresUi.filter { ui ->
            val j = ui.jugador
            if (q.isNotEmpty() && !j.nombre.lowercase(Locale.getDefault()).contains(q)) {
                return@filter false
            }
            val catSel = categoriaChip?.trim()?.takeIf { it.isNotEmpty() }
            if (catSel != null && j.categoria.trim() != catSel) {
                return@filter false
            }
            when (estadoFiltro) {
                FiltroEstadoJugadores.TODOS -> true
                FiltroEstadoJugadores.BECADOS -> j.becado
                FiltroEstadoJugadores.ADEUDO ->
                    !j.becado && puedeVerMensualidad && ui.adeudoTotal > ADEUDO_LISTA_EPS
                FiltroEstadoJugadores.PAGADO ->
                    !j.becado && puedeVerMensualidad &&
                        ui.adeudoTotal <= ADEUDO_LISTA_EPS &&
                        j.mensualidad != null && j.mensualidad > 0
            }
        }
    }

    val agrupadosPorCategoria = remember(listaFiltrada) {
        listaFiltrada
            .groupBy { it.jugador.categoria.trim() }
            .toSortedMap(naturalOrder())
    }
    DisposableEffect(Unit) {
        onDispose {
            sessionVm.setImpideVolverASeleccionCategoria(false)
        }
    }

    val pickCurpDocLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        awaitingExternalActivityResult = false
        if (uri == null) return@rememberLauncherForActivityResult
        lifecycleOwner.lifecycleScope.launch {
            val path = copiarUriAdjuntoAJugador(context, uri, "jugador_curp_docs") ?: return@launch
            viewModel.aplicarRutaCurpCopiada(path)
        }
    }

    val pickActaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        awaitingExternalActivityResult = false
        if (uri == null) return@rememberLauncherForActivityResult
        lifecycleOwner.lifecycleScope.launch {
            val path = copiarUriAdjuntoAJugador(context, uri, "jugador_actas") ?: return@launch
            viewModel.aplicarRutaActaCopiada(path)
        }
    }

    Box(Modifier.fillMaxSize()) {
        BackHandler(enabled = formularioAbierto && !awaitingExternalActivityResult) {
            viewModel.cerrarFormularioJugador()
        }
        BackHandler(enabled = jugadorDetalle != null && !formularioAbierto) {
            jugadorDetalle = null
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            Text(
                stringResource(R.string.tab_players),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            if (jugadoresUi.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 72.dp),
                ) {
                    if (!puedeVerMensualidad) {
                        MensualidadVisibilidadAviso(configAcademia, uidSesion)
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(
                        if (categoriaFiltro != null) {
                            stringResource(R.string.no_players_in_category)
                        } else {
                            stringResource(R.string.no_players_yet)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 72.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (!puedeVerMensualidad) {
                        item(key = "aviso_mensualidad") {
                            MensualidadVisibilidadAviso(configAcademia, uidSesion)
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                    item(key = "buscador") {
                        OutlinedTextField(
                            value = busquedaNombre,
                            onValueChange = { busquedaNombre = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = {
                                Text(stringResource(R.string.players_search_placeholder))
                            },
                        )
                    }
                    item(key = "filtros") {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            item {
                                FilterChip(
                                    selected = categoriaChip == null &&
                                        estadoFiltro == FiltroEstadoJugadores.TODOS,
                                    onClick = {
                                        categoriaChip = null
                                        estadoFiltro = FiltroEstadoJugadores.TODOS
                                    },
                                    label = { Text(stringResource(R.string.players_filter_all)) },
                                )
                            }
                            items(categoriasOrdenadas, key = { it }) { cat ->
                                FilterChip(
                                    selected = categoriaChip == cat,
                                    onClick = {
                                        categoriaChip = if (categoriaChip == cat) null else cat
                                        estadoFiltro = FiltroEstadoJugadores.TODOS
                                    },
                                    label = { Text(cat) },
                                )
                            }
                            if (puedeVerMensualidad) {
                                item {
                                    FilterChip(
                                        selected = estadoFiltro == FiltroEstadoJugadores.ADEUDO,
                                        onClick = {
                                            estadoFiltro = if (estadoFiltro == FiltroEstadoJugadores.ADEUDO) {
                                                FiltroEstadoJugadores.TODOS
                                            } else {
                                                FiltroEstadoJugadores.ADEUDO
                                            }
                                        },
                                        label = { Text(stringResource(R.string.players_filter_debt)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                                        ),
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = estadoFiltro == FiltroEstadoJugadores.PAGADO,
                                        onClick = {
                                            estadoFiltro = if (estadoFiltro == FiltroEstadoJugadores.PAGADO) {
                                                FiltroEstadoJugadores.TODOS
                                            } else {
                                                FiltroEstadoJugadores.PAGADO
                                            }
                                        },
                                        label = { Text(stringResource(R.string.players_filter_paid)) },
                                    )
                                }
                            }
                            item {
                                FilterChip(
                                    selected = estadoFiltro == FiltroEstadoJugadores.BECADOS,
                                    onClick = {
                                        estadoFiltro = if (estadoFiltro == FiltroEstadoJugadores.BECADOS) {
                                            FiltroEstadoJugadores.TODOS
                                        } else {
                                            FiltroEstadoJugadores.BECADOS
                                        }
                                    },
                                    label = { Text(stringResource(R.string.players_filter_scholarship)) },
                                )
                            }
                        }
                    }
                    if (listaFiltrada.isEmpty()) {
                        item(key = "sin_coincidencias") {
                            Text(
                                stringResource(R.string.players_empty_filter),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    } else {
                        agrupadosPorCategoria.forEach { (categoriaNombre, listaCat) ->
                            item(key = "grp_$categoriaNombre") {
                                val cdGrupo = stringResource(
                                    R.string.players_group_category_cd,
                                    categoriaNombre,
                                )
                                Text(
                                    categoriaNombre,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 2.dp)
                                        .semantics { contentDescription = cdGrupo },
                                )
                            }
                            items(
                                items = listaCat,
                                key = { it.jugador.id },
                            ) { ui ->
                                JugadorCard(
                                    jugador = ui.jugador,
                                    adeudoTotal = ui.adeudoTotal,
                                    expanded = expandedJugadorId == ui.jugador.id,
                                    onExpandToggle = {
                                        expandedJugadorId =
                                            if (expandedJugadorId == ui.jugador.id) null else ui.jugador.id
                                    },
                                    puedeVerMensualidad = puedeVerMensualidad,
                                    etiquetasAltaPorUid = etiquetasAltaPorUid,
                                    onAmpliarFoto = { jugadorFotoAmpliada = ui.jugador },
                                    onAbrirDetalle = { jugadorDetalle = ui.jugador },
                                    onEditar = { viewModel.abrirEdicionJugador(ui.jugador) },
                                    onHistorial = { jugadorHistorial = ui.jugador },
                                    onDarBaja = { jugadorBaja = ui.jugador },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!formularioAbierto && jugadorDetalle == null) {
            FloatingActionButton(
                onClick = { viewModel.abrirAltaJugador() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_player))
            }
        }

        jugadorDetalle?.let { detalle ->
            JugadorDetalleScreen(
                jugador = detalle,
                onBack = { jugadorDetalle = null },
                onEditar = {
                    viewModel.abrirEdicionJugador(detalle)
                    jugadorDetalle = null
                },
                onNavigateToTab = onNavigateToMainTab,
            )
        }

    if (formularioAbierto) {
        val jugadorExistente = (formularioJugador as? FormularioJugadorUi.Edicion)?.jugador
        key(formularioSession) {
            JugadorFormDialog(
                categoriaInicialAlta = categoriaFiltro.orEmpty(),
                jugadorExistente = jugadorExistente,
                puedeVerMensualidad = puedeVerMensualidad,
                curpDocPath = formCurpDocPath,
                onCurpDocPathChange = { viewModel.setFormCurpDocPath(it) },
                actaPath = formActaPath,
                onActaPathChange = { viewModel.setFormActaPath(it) },
                onRequestPickCurpDocumento = {
                    awaitingExternalActivityResult = true
                    pickCurpDocLauncher.launch("*/*")
                },
                onRequestPickActa = {
                    awaitingExternalActivityResult = true
                    pickActaLauncher.launch("*/*")
                },
                setAwaitingExternalResult = { awaitingExternalActivityResult = it },
                onDismiss = { viewModel.cerrarFormularioJugador() },
                onGuardar = { nombre, categoria, fechaNacMs, tel, email, notas, foto, curpVal, curpDocRuta, actaRuta, becadoVal, mensualidad ->
                    val base = jugadorExistente
                    if (base != null) {
                        viewModel.actualizarJugador(
                            base,
                            puedeVerMensualidad,
                            nombre,
                            categoria,
                            fechaNacMs,
                            tel,
                            email,
                            notas,
                            foto,
                            curpVal,
                            curpDocRuta,
                            actaRuta,
                            becadoVal,
                            mensualidad,
                        )
                    } else {
                        viewModel.guardarJugador(
                            nombre,
                            categoria,
                            fechaNacMs,
                            tel,
                            email,
                            notas,
                            foto,
                            curpVal,
                            curpDocRuta,
                            actaRuta,
                            becadoVal,
                            mensualidad,
                        )
                    }
                },
            )
        }
    }

    jugadorHistorial?.let { j ->
        val historial by viewModel.historialFlow(j.id).collectAsState(initial = emptyList())
        AlertDialog(
            onDismissRequest = { jugadorHistorial = null },
            title = { Text(stringResource(R.string.player_history_title, j.nombre)) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        stringResource(R.string.player_joined_date, formatearFechaDiaLocal(j.fechaAltaMillis)),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    JugadorAltaPorTexto(
                        j,
                        MaterialTheme.typography.bodyMedium,
                        Modifier.padding(bottom = 8.dp),
                        etiquetasAltaPorUid = etiquetasAltaPorUid,
                    )
                    if (puedeVerMensualidad) {
                        when {
                            j.becado -> Text(
                                stringResource(R.string.player_scholarship),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            j.mensualidad != null && j.mensualidad > 0 -> Text(
                                stringResource(R.string.player_monthly_fee, formatImporte(j.mensualidad)),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            else -> Text(
                                stringResource(R.string.player_fee_undefined),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                    }
                    historial.forEach { ev ->
                        Text(
                            stringResource(
                                R.string.historial_event_at,
                                when (ev.tipo) {
                                    JugadorHistorialTipo.ALTA.name ->
                                        stringResource(R.string.historial_event_alta)
                                    JugadorHistorialTipo.BAJA.name ->
                                        stringResource(R.string.historial_event_baja)
                                    else -> ev.tipo
                                },
                                formatearFechaHoraLocal(ev.fechaMillis),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                        ev.detalle?.takeIf { it.isNotBlank() }?.let { d ->
                            Text(
                                d,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { jugadorHistorial = null }) {
                    Text(stringResource(R.string.close))
                }
            },
        )
    }

    jugadorBaja?.let { j ->
        AlertDialog(
            onDismissRequest = { jugadorBaja = null },
            title = { Text(stringResource(R.string.discharge_confirm_title)) },
            text = { Text(stringResource(R.string.discharge_confirm_message, j.nombre)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.darBaja(j)
                        jugadorBaja = null
                    },
                ) { Text(stringResource(R.string.player_discharge)) }
            },
            dismissButton = {
                TextButton(onClick = { jugadorBaja = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    jugadorFotoAmpliada?.let { amp ->
        val fotoModel = amp.coilFotoModel(context)
        if (fotoModel != null) {
            FullscreenImageViewerDialog(
                titulo = amp.nombre,
                imageModel = fotoModel,
                contentDescription = stringResource(R.string.player_photo_cd),
                onDismiss = { jugadorFotoAmpliada = null },
            )
        } else {
            LaunchedEffect(amp.id) { jugadorFotoAmpliada = null }
        }
    }
    }
}

@Composable
private fun JugadorEstadoResumenBadges(
    jugador: Jugador,
    adeudoTotal: Double,
    puedeVerMensualidad: Boolean,
    modifier: Modifier = Modifier,
) {
    val deuda = adeudoTotal > ADEUDO_LISTA_EPS
    val chipText: String
    val container: Color
    val onLabel: Color
    when {
        jugador.becado -> {
            chipText = stringResource(R.string.players_badge_scholarship_short)
            container = MaterialTheme.colorScheme.tertiaryContainer
            onLabel = MaterialTheme.colorScheme.onTertiaryContainer
        }
        !puedeVerMensualidad -> return
        deuda -> {
            chipText = stringResource(R.string.players_badge_debt) + " · " + formatImporte(adeudoTotal)
            container = MaterialTheme.colorScheme.errorContainer
            onLabel = MaterialTheme.colorScheme.onErrorContainer
        }
        jugador.mensualidad != null && jugador.mensualidad > 0 -> {
            chipText = stringResource(R.string.players_badge_paid)
            container = MaterialTheme.colorScheme.primaryContainer
            onLabel = MaterialTheme.colorScheme.onPrimaryContainer
        }
        else -> {
            chipText = stringResource(R.string.players_badge_fee_undefined)
            container = MaterialTheme.colorScheme.surfaceVariant
            onLabel = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = container,
    ) {
        Text(
            chipText,
            style = MaterialTheme.typography.labelSmall,
            color = onLabel,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun JugadorAltaPorTexto(
    jugador: Jugador,
    style: TextStyle,
    modifier: Modifier = Modifier,
    etiquetasAltaPorUid: Map<String, String> = emptyMap(),
) {
    val nombreAlta = jugador.altaPorNombre?.trim()?.takeIf { it.isNotEmpty() }
    val uidKey = jugador.altaPorUserId?.lowercase()
    val etiquetaNube = uidKey?.let { etiquetasAltaPorUid[it] }?.trim()?.takeIf { it.isNotEmpty() }
    val linea = when {
        nombreAlta != null ->
            stringResource(R.string.player_registered_by_name, nombreAlta)
        etiquetaNube != null ->
            stringResource(R.string.player_registered_by_name, etiquetaNube)
        jugador.altaPorUserId != null ->
            stringResource(R.string.player_registered_by_unknown)
        else -> null
    } ?: return
    Text(
        linea,
        style = style,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun JugadorCard(
    jugador: Jugador,
    adeudoTotal: Double = 0.0,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    puedeVerMensualidad: Boolean,
    etiquetasAltaPorUid: Map<String, String>,
    onAmpliarFoto: () -> Unit,
    onAbrirDetalle: () -> Unit,
    onEditar: () -> Unit,
    onHistorial: () -> Unit,
    onDarBaja: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(220),
        label = "jugador_chevron",
    )
    val headerCd = stringResource(R.string.player_card_header_cd, jugador.nombre)
    val stateDesc = stringResource(
        if (expanded) R.string.player_card_state_expanded else R.string.player_card_state_collapsed,
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (expanded) 2.dp else 0.dp,
            pressedElevation = 0.dp,
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val fotoModel = jugador.coilFotoModel(context)
                val avatarSize = 36.dp
                if (fotoModel != null) {
                    AsyncImage(
                        model = fotoModel,
                        contentDescription = stringResource(R.string.player_photo_tap_to_expand),
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .clickable(onClick = onAmpliarFoto),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = onAbrirDetalle),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(onClick = onAbrirDetalle)
                        .semantics(mergeDescendants = true) {
                            contentDescription = headerCd
                            stateDescription = stateDesc
                        },
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                jugador.nombre,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                jugador.categoria,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (!expanded &&
                            (jugador.becado || puedeVerMensualidad)
                        ) {
                            JugadorEstadoResumenBadges(
                                jugador = jugador,
                                adeudoTotal = adeudoTotal,
                                puedeVerMensualidad = puedeVerMensualidad,
                                modifier = Modifier.widthIn(max = 120.dp),
                            )
                        }
                    }
                }
                IconButton(
                        onClick = onExpandToggle,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = stringResource(R.string.player_card_expand_icon_cd),
                            modifier = Modifier.rotate(chevronRotation),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = tween(240),
                    expandFrom = Alignment.Top,
                ) + fadeIn(animationSpec = tween(200)),
                exit = shrinkVertically(
                    animationSpec = tween(200),
                    shrinkTowards = Alignment.Top,
                ) + fadeOut(animationSpec = tween(160)),
            ) {
                Column(Modifier.fillMaxWidth()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp,
                    )
                    Column(
                        Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Text(
                            stringResource(
                                R.string.player_joined_date,
                                formatearFechaDiaLocal(jugador.fechaAltaMillis),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        JugadorAltaPorTexto(
                            jugador,
                            MaterialTheme.typography.bodySmall,
                            Modifier.padding(top = 4.dp),
                            etiquetasAltaPorUid = etiquetasAltaPorUid,
                        )
                        if (puedeVerMensualidad) {
                            when {
                                jugador.becado -> Text(
                                    stringResource(R.string.player_scholarship),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                                jugador.mensualidad != null && jugador.mensualidad > 0 -> Text(
                                    stringResource(
                                        R.string.player_monthly_fee,
                                        formatImporte(jugador.mensualidad),
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                                else -> Text(
                                    stringResource(R.string.player_fee_undefined),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                            if (!jugador.becado && adeudoTotal > ADEUDO_LISTA_EPS) {
                                Text(
                                    stringResource(R.string.players_badge_debt) + ": " +
                                        formatImporte(adeudoTotal),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                        jugador.fechaNacimientoMillis?.let { ms ->
                            Text(
                                stringResource(R.string.birth_date) + ": " + formatearFechaCalendarioUtc(ms),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        } ?: jugador.anioNacimiento?.let { anio ->
                            Text(
                                stringResource(R.string.birth_year_only, anio),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        jugador.curp?.takeIf { it.isNotBlank() }?.let { c ->
                            Text(
                                stringResource(R.string.player_curp) + ": $c",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        val curpDocLocal = jugador.curpDocumentoRutaAbsoluta?.let { File(it).exists() } == true
                        val curpDocUrl = jugador.curpDocumentoUrlSupabase?.takeIf { it.isNotBlank() }
                        if (curpDocLocal || curpDocUrl != null) {
                            Text(
                                stringResource(R.string.player_curp_doc_attached),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            curpDocUrl?.let { url ->
                                TextButton(
                                    onClick = { uriHandler.openUri(url) },
                                    modifier = Modifier.padding(top = 0.dp),
                                ) {
                                    Text(stringResource(R.string.player_curp_doc_open))
                                }
                            }
                        }
                        val actaLocal = jugador.actaNacimientoRutaAbsoluta?.let { File(it).exists() } == true
                        val actaUrl = jugador.actaNacimientoUrlSupabase?.takeIf { it.isNotBlank() }
                        if (actaLocal || actaUrl != null) {
                            Text(
                                stringResource(R.string.player_birth_cert_attached),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            actaUrl?.let { url ->
                                TextButton(
                                    onClick = { uriHandler.openUri(url) },
                                    modifier = Modifier.padding(top = 0.dp),
                                ) {
                                    Text(stringResource(R.string.player_birth_cert_open))
                                }
                            }
                        }
                        val contacto = listOfNotNull(jugador.telefonoTutor, jugador.emailTutor)
                            .joinToString(" · ")
                        if (contacto.isNotEmpty()) {
                            Text(
                                contacto,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        jugador.notas?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            TextButton(onClick = onHistorial, modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.player_history),
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            TextButton(onClick = onEditar, modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.player_edit),
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            TextButton(onClick = onDarBaja, modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.player_discharge),
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JugadorFormDialog(
    categoriaInicialAlta: String,
    jugadorExistente: Jugador?,
    puedeVerMensualidad: Boolean,
    curpDocPath: String?,
    onCurpDocPathChange: (String?) -> Unit,
    actaPath: String?,
    onActaPathChange: (String?) -> Unit,
    onRequestPickCurpDocumento: () -> Unit,
    onRequestPickActa: () -> Unit,
    /** Marcar true antes de abrir cámara/galería/recorte o permisos; false al volver (callback del launcher). */
    setAwaitingExternalResult: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onGuardar: (
        nombre: String,
        categoria: String,
        fechaNacimientoMillis: Long?,
        tel: String?,
        email: String?,
        notas: String?,
        fotoRuta: String?,
        curp: String?,
        curpDocumentoRuta: String?,
        actaRuta: String?,
        becado: Boolean,
        mensualidad: Double?,
    ) -> Unit,
) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(categoriaInicialAlta) }
    var fechaNacMs by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val maxYear = remember { LocalDate.now(ZoneOffset.UTC).year }
    val minYear = remember { maxYear - 100 }
    val selectableDates = remember {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val d = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                val today = LocalDate.now(ZoneOffset.UTC)
                val oldest = today.minusYears(100)
                return !d.isAfter(today) && !d.isBefore(oldest)
            }
        }
    }
    var tel by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var mensualidadTxt by remember { mutableStateOf("") }
    var becado by remember { mutableStateOf(false) }
    var fotoPath by remember { mutableStateOf<String?>(null) }
    var cameraFile by remember { mutableStateOf<File?>(null) }
    var curpTxt by remember { mutableStateOf("") }

    LaunchedEffect(jugadorExistente?.id, categoriaInicialAlta) {
        val j = jugadorExistente
        if (j == null) {
            nombre = ""
            categoria = categoriaInicialAlta
            fechaNacMs = null
            tel = ""
            email = ""
            notas = ""
            mensualidadTxt = ""
            becado = false
            fotoPath = null
            curpTxt = ""
        } else {
            nombre = j.nombre
            categoria = j.categoria
            fechaNacMs = j.fechaNacimientoMillis
            tel = j.telefonoTutor.orEmpty()
            email = j.emailTutor.orEmpty()
            notas = j.notas.orEmpty()
            becado = j.becado
            mensualidadTxt = when {
                j.becado -> ""
                j.mensualidad != null -> {
                    val v = j.mensualidad
                    if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
                }
                else -> ""
            }
            fotoPath = j.fotoRutaAbsoluta?.takeIf { File(it).exists() }
            curpTxt = j.curp.orEmpty()
        }
    }

    LaunchedEffect(becado) {
        if (becado) mensualidadTxt = ""
    }

    fun jugadorPhotoDest(): File {
        val dir = File(context.filesDir, "jugador_photos").apply { mkdirs() }
        return File(dir, "${UUID.randomUUID()}.jpg")
    }

    var cropDestFile by remember { mutableStateOf<File?>(null) }
    var deleteAfterCrop by remember { mutableStateOf<File?>(null) }

    val cropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        setAwaitingExternalResult(false)
        val cam = deleteAfterCrop
        deleteAfterCrop = null
        runCatching { cam?.takeIf { it.exists() }?.delete() }

        val dest = cropDestFile
        cropDestFile = null
        if (result.resultCode == Activity.RESULT_OK && dest != null && dest.exists() && dest.length() > 0L) {
            fotoPath?.let { runCatching { File(it).delete() } }
            fotoPath = dest.absolutePath
        } else {
            runCatching { dest?.delete() }
        }
    }

    fun iniciarRecorteFoto(origen: Uri, archivoCamaraABorrar: File?) {
        val destinoArchivo = jugadorPhotoDest()
        cropDestFile = destinoArchivo
        deleteAfterCrop = archivoCamaraABorrar
        val destinoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            destinoArchivo,
        )
        val opciones = UCrop.Options().apply {
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
            setCompressionQuality(90)
            // Por defecto uCrop limita el decode ~al tamaño de pantalla; fotos grandes quedan muy
            // muestreadas y el zoom mínimo en % sube mucho (p. ej. 240%) sin poder alejar más.
            setMaxBitmapSize(UCROP_DECODE_MAX_SIDE_PX)
            setMaxScaleMultiplier(16f)
            // Redimensionar y mover el recuadro (esquinas / bordes / arrastre central); alternativa a la rueda Escala.
            setFreeStyleCropEnabled(true)
            setCircleDimmedLayer(true)
            setShowCropGrid(true)
            setShowCropFrame(true)
            setToolbarTitle(context.getString(R.string.player_photo_crop_title))
        }
        val intent = UCrop.of(origen, destinoUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1024, 1024)
            .withOptions(opciones)
            .getIntent(context)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        setAwaitingExternalResult(true)
        cropLauncher.launch(intent)
    }

    val pickGallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        setAwaitingExternalResult(false)
        if (uri == null) return@rememberLauncherForActivityResult
        iniciarRecorteFoto(uri, null)
    }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        setAwaitingExternalResult(false)
        val f = cameraFile
        if (f == null) return@rememberLauncherForActivityResult
        if (!success || !f.exists()) {
            runCatching { f.delete() }
            return@rememberLauncherForActivityResult
        }
        val srcUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            f,
        )
        iniciarRecorteFoto(srcUri, f)
    }

    fun launchCameraCapture() {
        val f = File(context.cacheDir, "jug_cam_${System.currentTimeMillis()}.jpg")
        runCatching { if (!f.exists()) f.createNewFile() }
        cameraFile = f
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            f,
        )
        setAwaitingExternalResult(true)
        takePicture.launch(uri)
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        setAwaitingExternalResult(false)
        if (granted) launchCameraCapture()
    }

    fun openCamera() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> launchCameraCapture()
            else -> {
                setAwaitingExternalResult(true)
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    val configuration = LocalConfiguration.current
    val scrollMaxHeight = configuration.screenHeightDp.dp * 0.5f
    val formScroll = rememberScrollState()
    var fotoSheetVisible by remember { mutableStateOf(false) }
    val fotoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
        ) {
            Column(Modifier.padding(AcademiaDimens.paddingCard)) {
                Text(
                    stringResource(
                        if (jugadorExistente == null) R.string.add_player else R.string.edit_player,
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(AcademiaDimens.spacingListSection))
                val path = fotoPath
                val hayFotoLocal = path != null && File(path).exists()
                val modeloRemoto = remember(jugadorExistente?.id, path) {
                    if (jugadorExistente != null && !hayFotoLocal) {
                        jugadorExistente.coilFotoModel(context)
                    } else {
                        null
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(AcademiaDimens.avatarFormHero)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { fotoSheetVisible = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                hayFotoLocal -> AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(File(path!!))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = stringResource(R.string.player_photo_cd),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                                modeloRemoto != null -> AsyncImage(
                                    model = modeloRemoto,
                                    contentDescription = stringResource(R.string.player_photo_cd),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                                else -> Icon(
                                    Icons.Default.Add,
                                    contentDescription = stringResource(R.string.player_photo_sheet_title),
                                    modifier = Modifier.size(AcademiaDimens.iconSizeMd * 2),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        SmallFloatingActionButton(
                            onClick = { fotoSheetVisible = true },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-AcademiaDimens.gapSm), y = (-AcademiaDimens.gapSm)),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = stringResource(R.string.staff_take_photo),
                                modifier = Modifier.size(AcademiaDimens.iconSizeSm),
                            )
                        }
                    }
                }
                Text(
                    stringResource(R.string.player_photo_tap_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AcademiaDimens.gapSm),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(AcademiaDimens.spacingListSection))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = scrollMaxHeight)
                        .verticalScroll(formScroll),
                    verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingListSection),
                ) {
                    AppCard {
                        SectionHeader(
                            title = stringResource(R.string.player_form_basic_section),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd)) {
                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = { Text(stringResource(R.string.name)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = categoria,
                                onValueChange = { categoria = it },
                                label = { Text(stringResource(R.string.category)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = fechaNacMs?.let { formatearFechaCalendarioUtc(it) } ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.birth_date)) },
                                placeholder = { Text(stringResource(R.string.birth_date_hint)) },
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = stringResource(R.string.calendar_pick_cd),
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker = true },
                            )
                            if (fechaNacMs != null) {
                                TextButton(
                                    onClick = { fechaNacMs = null },
                                    modifier = Modifier.align(Alignment.End),
                                ) {
                                    Text(stringResource(R.string.birth_date_clear))
                                }
                            }
                        }
                    }
                    AppCard {
                        OutlinedTextField(
                            value = curpTxt,
                            onValueChange = { s ->
                                curpTxt = s.uppercase().filter { it.isLetterOrDigit() }.take(18)
                            },
                            label = { Text(stringResource(R.string.player_curp)) },
                            placeholder = { Text(stringResource(R.string.player_curp_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    AppCard {
                        SectionHeader(
                            title = stringResource(R.string.player_curp_doc_section),
                            subtitle = stringResource(R.string.player_curp_doc_hint),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd)) {
                            OutlinedButton(
                                onClick = { onRequestPickCurpDocumento() },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Icon(
                                        Icons.Default.AttachFile,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(AcademiaDimens.gapMd))
                                    Text(stringResource(R.string.player_curp_doc_pick))
                                }
                            }
                            curpDocPath?.let { p ->
                                val f = File(p)
                                if (f.exists()) {
                                    Text(
                                        f.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    TextButton(
                                        onClick = {
                                            runCatching { f.delete() }
                                            onCurpDocPathChange(null)
                                        },
                                        modifier = Modifier.align(Alignment.End),
                                    ) {
                                        Text(stringResource(R.string.player_curp_doc_remove))
                                    }
                                }
                            }
                        }
                    }
                    AppCard {
                        SectionHeader(
                            title = stringResource(R.string.player_birth_cert_section),
                            subtitle = stringResource(R.string.player_birth_cert_hint),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd)) {
                            OutlinedButton(
                                onClick = { onRequestPickActa() },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Icon(
                                        Icons.Default.AttachFile,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(AcademiaDimens.gapMd))
                                    Text(stringResource(R.string.player_birth_cert_pick))
                                }
                            }
                            actaPath?.let { p ->
                                val f = File(p)
                                if (f.exists()) {
                                    Text(
                                        f.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    TextButton(
                                        onClick = {
                                            runCatching { f.delete() }
                                            onActaPathChange(null)
                                        },
                                        modifier = Modifier.align(Alignment.End),
                                    ) {
                                        Text(stringResource(R.string.player_birth_cert_remove))
                                    }
                                }
                            }
                        }
                    }
                    AppCard {
                        SectionHeader(title = stringResource(R.string.player_form_contact_section))
                        val emailTutorOk = emailTutorFormatoSuaveValido(email)
                        Column(verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd)) {
                            OutlinedTextField(
                                value = tel,
                                onValueChange = { tel = it },
                                label = { Text(stringResource(R.string.parent_phone)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text(stringResource(R.string.parent_email)) },
                                placeholder = { Text(stringResource(R.string.parent_email_placeholder)) },
                                supportingText = {
                                    Text(
                                        if (emailTutorOk) {
                                            stringResource(R.string.parent_email_hint)
                                        } else {
                                            stringResource(R.string.parent_email_error_format)
                                        },
                                        color = if (emailTutorOk) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        },
                                    )
                                },
                                isError = !emailTutorOk,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = notas,
                                onValueChange = { notas = it },
                                label = { Text(stringResource(R.string.notes)) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    if (puedeVerMensualidad) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.fee_scholarship_switch),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    stringResource(R.string.fee_scholarship_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = becado,
                                onCheckedChange = { becado = it },
                            )
                        }
                        OutlinedTextField(
                            value = mensualidadTxt,
                            onValueChange = { mensualidadTxt = it },
                            label = { Text(stringResource(R.string.fee_monthly_label)) },
                            placeholder = { Text(stringResource(R.string.fee_monthly_hint)) },
                            singleLine = true,
                            enabled = !becado,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Spacer(Modifier.height(AcademiaDimens.spacingListSection))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapSm),
                ) {
                    PrimaryButton(
                        text = stringResource(R.string.save),
                        onClick = {
                            val men = mensualidadTxt
                                .trim()
                                .replace(',', '.')
                                .takeIf { it.isNotEmpty() }
                                ?.toDoubleOrNull()
                            val menFinal = if (puedeVerMensualidad && !becado) men else null
                            onGuardar(
                                nombre,
                                categoria,
                                fechaNacMs,
                                tel,
                                email.trim(),
                                notas,
                                fotoPath,
                                curpTxt.trim().takeIf { it.isNotEmpty() },
                                curpDocPath,
                                actaPath,
                                becado && puedeVerMensualidad,
                                menFinal,
                            )
                        },
                        enabled = nombre.isNotBlank() &&
                            categoria.isNotBlank() &&
                            emailTutorFormatoSuaveValido(email),
                    )
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(R.string.cancel),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }

    if (fotoSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { fotoSheetVisible = false },
            sheetState = fotoSheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AcademiaDimens.paddingScreenHorizontal)
                    .padding(bottom = AcademiaDimens.gapSm),
            ) {
                Text(
                    text = stringResource(R.string.player_photo_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(R.string.player_photo_sheet_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = AcademiaDimens.gapSm, bottom = AcademiaDimens.gapMd),
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.staff_pick_gallery)) },
                    leadingContent = {
                        Icon(Icons.Default.Image, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        fotoSheetVisible = false
                        setAwaitingExternalResult(true)
                        pickGallery.launch("image/*")
                    },
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(stringResource(R.string.staff_take_photo)) },
                    leadingContent = {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        fotoSheetVisible = false
                        openCamera()
                    },
                )
                if (fotoPath != null) {
                    HorizontalDivider()
                    ListItem(
                        headlineContent = {
                            Text(
                                stringResource(R.string.staff_remove_photo),
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        modifier = Modifier.clickable {
                            fotoSheetVisible = false
                            fotoPath?.let { runCatching { File(it).delete() } }
                            fotoPath = null
                        },
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaNacMs,
            yearRange = minYear..maxYear,
            selectableDates = selectableDates,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { fechaNacMs = it }
                        showDatePicker = false
                    },
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
