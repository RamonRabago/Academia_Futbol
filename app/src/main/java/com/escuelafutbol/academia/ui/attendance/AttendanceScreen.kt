package com.escuelafutbol.academia.ui.attendance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.model.esPadreMembresiaNube
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.AppTintedPanel
import com.escuelafutbol.academia.ui.design.ChipsGroup
import com.escuelafutbol.academia.ui.design.EmptyState
import com.escuelafutbol.academia.ui.design.SectionHeader
import com.escuelafutbol.academia.ui.util.coilFotoModel
import com.escuelafutbol.academia.ui.util.formatearFechaAsistenciaTitulo
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
private fun AttendanceDiaEntrenamientoBarra(
    esDiaEntreno: Boolean,
    textoDeteccion: String?,
    onCambiar: (Boolean) -> Unit,
    onAyuda: () -> Unit,
) {
    AppTintedPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AcademiaDimens.radiusDense),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentPadding = PaddingValues(
            horizontal = AcademiaDimens.paddingCardCompact,
            vertical = AcademiaDimens.gapSm,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapSm),
        ) {
            Text(
                stringResource(R.string.attendance_training_day_title),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onAyuda,
                modifier = Modifier.size(AcademiaDimens.avatarRow),
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.attendance_help_training_cd),
                    modifier = Modifier.size(AcademiaDimens.iconSizeSm),
                )
            }
            Switch(
                checked = esDiaEntreno,
                onCheckedChange = onCambiar,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
        if (textoDeteccion != null) {
            Text(
                textoDeteccion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = AcademiaDimens.gapMicro,
                        end = AcademiaDimens.avatarRow,
                    ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    categoriaFiltro: String?,
    configAcademia: AcademiaConfig,
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
    val filas by viewModel.filas.collectAsState()
    val fechaDia by viewModel.fechaDia.collectAsState()
    val resumen by viewModel.resumenAsistencia.collectAsState()
    val focoJugador by viewModel.focoResumenJugadorId.collectAsState()
    val esDiaEntreno by viewModel.esDiaEntrenamientoMarcado.collectAsState()
    val diaEntrenoBarra by viewModel.diaEntrenoBarraUi.collectAsState()
    val zone = ZoneId.systemDefault()

    var listaVaciaConfirmada by remember { mutableStateOf(false) }
    LaunchedEffect(filas) {
        listaVaciaConfirmada = false
        if (filas.isNotEmpty()) return@LaunchedEffect
        delay(400)
        if (filas.isEmpty()) listaVaciaConfirmada = true
    }

    val textoDeteccionDiaEntreno = when {
        diaEntrenoBarra.hayOverrideManual -> null
        diaEntrenoBarra.esDiaSemanaHabitual ->
            stringResource(R.string.attendance_training_day_hint_habitual_weekday)
        else -> stringResource(R.string.attendance_training_day_hint_auto_detected)
    }

    val etiquetaFecha = remember(fechaDia, zone) {
        formatearFechaAsistenciaTitulo(fechaDia, zone)
    }
    val jugadoresOpcion = remember(filas) {
        filas.map { it.jugador }.distinctBy { it.id }.sortedBy { it.nombre.trim() }
    }

    var busqueda by remember { mutableStateOf("") }
    var chipCategoria by remember { mutableStateOf<String?>(null) }
    val filasMostradas = remember(filas, busqueda, chipCategoria) {
        val q = busqueda.trim().lowercase(Locale.getDefault())
        val base = if (chipCategoria == null) {
            filas
        } else {
            filas.filter { it.jugador.categoria.trim() == chipCategoria }
        }
        if (q.isEmpty()) base else base.filter {
            it.jugador.nombre.trim().lowercase(Locale.getDefault()).contains(q)
        }
    }
    val categoriasEnLista = remember(filas) {
        filas.map { it.jugador.categoria.trim() }.filter { it.isNotEmpty() }.distinct().sorted()
    }
    val mostrarChipsCategoria =
        categoriaFiltro == null && categoriasEnLista.size > 1

    LaunchedEffect(filas, focoJugador) {
        if (focoJugador != null && filas.none { it.jugador.id == focoJugador }) {
            viewModel.setResumenFocoJugador(null)
        }
    }
    LaunchedEffect(filas, chipCategoria) {
        val c = chipCategoria ?: return@LaunchedEffect
        if (filas.none { it.jugador.categoria.trim() == c }) {
            chipCategoria = null
        }
    }

    var mostrarCalendario by remember { mutableStateOf(false) }
    var ayudaDiaEntreno by remember { mutableStateOf(false) }

    if (ayudaDiaEntreno) {
        AlertDialog(
            onDismissRequest = { ayudaDiaEntreno = false },
            title = { Text(stringResource(R.string.attendance_help_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingDialogBlock)) {
                    Text(
                        stringResource(R.string.attendance_training_day_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        stringResource(R.string.attendance_training_day_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        stringResource(R.string.attendance_training_day_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { ayudaDiaEntreno = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }

    val idsVisibles = remember(filasMostradas) { filasMostradas.map { it.jugador.id } }
    val hayLista = filas.isNotEmpty()
    val puedeMasivo = idsVisibles.isNotEmpty()
    val hayPresentesEnListado = remember(filasMostradas) { filasMostradas.any { it.presente } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = AcademiaDimens.paddingScreenHorizontal,
                vertical = AcademiaDimens.gapSm,
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (!hayLista) {
                when {
                    !listaVaciaConfirmada -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    stringResource(R.string.attendance_loading_list),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    else -> {
                        val tituloVacio = if (categoriaFiltro != null) {
                            stringResource(R.string.no_players_in_category)
                        } else {
                            stringResource(R.string.no_players_yet)
                        }
                        val subtituloVacio = if (categoriaFiltro == null) {
                            stringResource(R.string.attendance_empty_subtitle_staff)
                        } else {
                            null
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyState(
                                title = tituloVacio,
                                subtitle = subtituloVacio,
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        top = AcademiaDimens.gapSm,
                        bottom = AcademiaDimens.gapSm,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                ) {
                    item {
                        SectionHeader(
                            title = stringResource(R.string.attendance_section_calendar),
                            subtitle = stringResource(R.string.attendance_section_calendar_subtitle),
                        )
                        AppCard {
                            val cdCalendario = stringResource(R.string.attendance_date_open_calendar_cd)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                IconButton(
                                    onClick = { viewModel.diaAnterior(zone) },
                                    modifier = Modifier.size(AcademiaDimens.avatarRow),
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = stringResource(R.string.attendance_day_prev_cd),
                                        modifier = Modifier.size(AcademiaDimens.iconSizeSm),
                                    )
                                }
                                Text(
                                    etiquetaFecha,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .widthIn(max = 220.dp)
                                        .clickable { mostrarCalendario = true }
                                        .padding(
                                            horizontal = AcademiaDimens.gapSm,
                                            vertical = AcademiaDimens.gapMd,
                                        )
                                        .semantics { contentDescription = cdCalendario },
                                )
                                IconButton(
                                    onClick = { viewModel.diaSiguiente(zone) },
                                    modifier = Modifier.size(AcademiaDimens.avatarRow),
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = stringResource(R.string.attendance_day_next_cd),
                                        modifier = Modifier.size(AcademiaDimens.iconSizeSm),
                                    )
                                }
                            }
                        }
                    }

                    item {
                        SectionHeader(
                            title = stringResource(R.string.attendance_section_actions),
                            subtitle = stringResource(R.string.attendance_section_actions_subtitle),
                        )
                        AppCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                            ) {
                                Button(
                                    onClick = { viewModel.marcarPresentesVisibles(idsVisibles) },
                                    enabled = puedeMasivo,
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = AcademiaDimens.buttonMinHeight),
                                    contentPadding = PaddingValues(
                                        horizontal = AcademiaDimens.paddingCardCompact,
                                        vertical = AcademiaDimens.gapMd,
                                    ),
                                ) {
                                    Text(
                                        stringResource(R.string.attendance_mark_all_present),
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                OutlinedButton(
                                    onClick = { viewModel.limpiarAsistenciasVisibles(idsVisibles) },
                                    enabled = puedeMasivo,
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = AcademiaDimens.buttonMinHeight),
                                    contentPadding = PaddingValues(
                                        horizontal = AcademiaDimens.paddingCardCompact,
                                        vertical = AcademiaDimens.gapMd,
                                    ),
                                ) {
                                    Text(
                                        stringResource(R.string.attendance_clear_visible),
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }

                    item {
                        SectionHeader(
                            title = stringResource(R.string.attendance_section_players),
                            subtitle = stringResource(R.string.attendance_section_players_subtitle),
                        )
                        AppCard {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                            ) {
                                OutlinedTextField(
                                    value = busqueda,
                                    onValueChange = { nuevo -> busqueda = nuevo },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    placeholder = { Text(stringResource(R.string.attendance_search_hint)) },
                                )
                                if (mostrarChipsCategoria) {
                                    ChipsGroup {
                                        FilterChip(
                                            selected = chipCategoria == null,
                                            onClick = { chipCategoria = null },
                                            label = { Text(stringResource(R.string.attendance_filter_list_all)) },
                                        )
                                        categoriasEnLista.forEach { nombre ->
                                            FilterChip(
                                                selected = chipCategoria == nombre,
                                                onClick = {
                                                    chipCategoria =
                                                        if (chipCategoria == nombre) null else nombre
                                                },
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
                                AttendanceDiaEntrenamientoBarra(
                                    esDiaEntreno = esDiaEntreno,
                                    textoDeteccion = textoDeteccionDiaEntreno,
                                    onCambiar = { viewModel.setDiaEntrenamientoMarcado(it) },
                                    onAyuda = { ayudaDiaEntreno = true },
                                )
                            }
                        }
                    }

                    if (hayPresentesEnListado && !esDiaEntreno) {
                        item {
                            AppTintedPanel(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                                contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
                            ) {
                                Text(
                                    stringResource(R.string.attendance_training_day_off_but_present_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            }
                        }
                    }

                    if (filasMostradas.isEmpty()) {
                        item {
                            EmptyState(
                                title = stringResource(R.string.attendance_search_no_matches_title),
                                subtitle = stringResource(R.string.attendance_search_no_matches_subtitle),
                            )
                        }
                    } else {
                        items(filasMostradas, key = { it.jugador.id }) { fila ->
                            AttendanceListaDiaFila(
                                fila = fila,
                                onPresenteChange = { presente ->
                                    viewModel.marcarAsistencia(fila.jugador.id, presente)
                                },
                            )
                        }
                    }

                    item {
                        SectionHeader(
                            title = stringResource(R.string.attendance_section_summary),
                            subtitle = stringResource(R.string.attendance_section_summary_subtitle),
                        )
                        AttendanceAlumnoResumenPicker(
                            jugadores = jugadoresOpcion,
                            focoJugadorId = focoJugador,
                            onFocoChange = { viewModel.setResumenFocoJugador(it) },
                            enabled = filas.isNotEmpty(),
                            modifier = Modifier.padding(bottom = AcademiaDimens.gapMd),
                        )
                        AttendanceSummaryCard(
                            resumen = resumen,
                            onPeriodoChange = { viewModel.setPeriodoResumen(it) },
                            onAnioAnterior = { viewModel.resumenAnioAnterior() },
                            onAnioSiguiente = { viewModel.resumenAnioSiguiente() },
                            colapsadoInicial = true,
                        )
                    }
                }
            }
        }

        if (mostrarCalendario) {
            val pickerState = rememberDatePickerState(
                initialSelectedDateMillis = fechaDia,
            )
            DatePickerDialog(
                onDismissRequest = { mostrarCalendario = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pickerState.selectedDateMillis?.let {
                                viewModel.seleccionarFechaCalendario(it, zone)
                            }
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
}

@Composable
private fun AttendanceListaDiaFila(
    fila: JugadorAsistenciaUi,
    onPresenteChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val modeloFoto = remember(fila.jugador.id, fila.jugador.fotoRutaAbsoluta, fila.jugador.fotoUrlSupabase) {
        fila.jugador.coilFotoModel(context)
    }
    val esClaro = !isSystemInDarkTheme()
    val fondoPresente = if (esClaro) Color(0xFFE8F5E9) else Color(0xFF1B5E20).copy(alpha = 0.35f)
    val bordePresente = if (esClaro) Color(0xFF66BB6A) else Color(0xFF81C784)
    val cdTarjeta = stringResource(
        if (fila.presente) {
            R.string.attendance_card_cd_present
        } else {
            R.string.attendance_card_cd_absent
        },
        fila.jugador.nombre,
    )
    val alternarCd = stringResource(R.string.attendance_toggle_action)
    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = bordePresente,
        checkedTrackColor = bordePresente.copy(alpha = 0.45f),
        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = false,
        containerColor = if (fila.presente) {
            fondoPresente
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = if (fila.presente) BorderStroke(1.dp, bordePresente) else null,
        includeContentPadding = false,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AcademiaDimens.paddingCardCompact,
                    vertical = AcademiaDimens.gapMd,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingRowComfort),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = cdTarjeta }
                    .clickable(
                        onClick = { onPresenteChange(!fila.presente) },
                        role = Role.Button,
                        onClickLabel = alternarCd,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingRowComfort),
            ) {
                Box(
                    modifier = Modifier
                        .size(AcademiaDimens.avatarResultRow)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
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
                            modifier = Modifier.size(AcademiaDimens.iconSizeResumen),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Column(Modifier.weight(1f, fill = false)) {
                    Text(
                        fila.jugador.nombre,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(AcademiaDimens.gapMicro))
                    Text(
                        fila.jugador.anioNacimiento?.toString() ?: fila.jugador.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (fila.presente) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = bordePresente,
                        modifier = Modifier.size(AcademiaDimens.iconSizeResumen),
                    )
                } else {
                    Spacer(Modifier.size(AcademiaDimens.iconSizeResumen))
                }
            }
            Switch(
                checked = fila.presente,
                onCheckedChange = onPresenteChange,
                colors = switchColors,
                modifier = Modifier.defaultMinSize(minWidth = 52.dp, minHeight = AcademiaDimens.avatarRow),
            )
        }
    }
}
