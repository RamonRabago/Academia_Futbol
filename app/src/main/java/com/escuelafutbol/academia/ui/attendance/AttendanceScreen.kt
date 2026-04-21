package com.escuelafutbol.academia.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImage
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.model.esPadreMembresiaNube
import com.escuelafutbol.academia.ui.util.coilFotoModel
import com.escuelafutbol.academia.ui.util.formatearFechaAsistenciaTitulo
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    categoriaFiltro: String?,
    configAcademia: AcademiaConfig,
) {
    if (configAcademia.esPadreMembresiaNube()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(R.string.role_route_blocked_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp),
            )
        }
        return
    }
    val filas by viewModel.filas.collectAsState()
    val fechaDia by viewModel.fechaDia.collectAsState()
    val resumen by viewModel.resumenAsistencia.collectAsState()
    val focoJugador by viewModel.focoResumenJugadorId.collectAsState()
    val esDiaEntreno by viewModel.esDiaEntrenamientoMarcado.collectAsState()
    val zone = ZoneId.systemDefault()

    val etiquetaFecha = remember(fechaDia, zone) {
        formatearFechaAsistenciaTitulo(fechaDia, zone)
    }
    val jugadoresOpcion = remember(filas) {
        filas.map { it.jugador }.distinctBy { it.id }.sortedBy { it.nombre.trim() }
    }
    LaunchedEffect(filas, focoJugador) {
        if (focoJugador != null && filas.none { it.jugador.id == focoJugador }) {
            viewModel.setResumenFocoJugador(null)
        }
    }

    var mostrarCalendario by remember { mutableStateOf(false) }
    var ayudaDiaEntreno by remember { mutableStateOf(false) }

    if (ayudaDiaEntreno) {
        AlertDialog(
            onDismissRequest = { ayudaDiaEntreno = false },
            title = { Text(stringResource(R.string.attendance_help_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item {
                val cdCalendario = stringResource(R.string.attendance_date_open_calendar_cd)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        stringResource(R.string.tab_attendance),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { viewModel.diaAnterior(zone) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.attendance_day_prev_cd),
                        )
                    }
                    Text(
                        etiquetaFecha,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .widthIn(max = 200.dp)
                            .clickable { mostrarCalendario = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .semantics { contentDescription = cdCalendario },
                    )
                    IconButton(onClick = { viewModel.diaSiguiente(zone) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.attendance_day_next_cd),
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        stringResource(R.string.attendance_training_day_title),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { ayudaDiaEntreno = true },
                        modifier = Modifier.padding(0.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.attendance_help_training_cd),
                        )
                    }
                    Switch(
                        checked = esDiaEntreno,
                        onCheckedChange = { viewModel.setDiaEntrenamientoMarcado(it) },
                    )
                }
            }
            item {
                AttendanceAlumnoResumenPicker(
                    jugadores = jugadoresOpcion,
                    focoJugadorId = focoJugador,
                    onFocoChange = { viewModel.setResumenFocoJugador(it) },
                    enabled = filas.isNotEmpty(),
                )
            }
            item {
                AttendanceSummaryCard(
                    resumen = resumen,
                    onPeriodoChange = { viewModel.setPeriodoResumen(it) },
                    onAnioAnterior = { viewModel.resumenAnioAnterior() },
                    onAnioSiguiente = { viewModel.resumenAnioSiguiente() },
                )
            }
            if (filas.isNotEmpty()) {
                item {
                    Button(
                        onClick = { viewModel.marcarTodosPresentes() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.attendance_mark_all_present))
                    }
                }
                item {
                    Column(Modifier.fillMaxWidth()) {
                        HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))
                        Text(
                            stringResource(R.string.attendance_day_list_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(filas, key = { it.jugador.id }) { fila ->
                    AttendanceListaDiaFila(
                        fila = fila,
                        onPresenteChange = { presente ->
                            viewModel.marcarAsistencia(fila.jugador.id, presente)
                        },
                    )
                }
            } else {
                item {
                    Text(
                        if (categoriaFiltro != null) {
                            stringResource(R.string.no_players_in_category)
                        } else {
                            stringResource(R.string.no_players_yet)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 24.dp),
                    )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(fila.jugador.nombre, style = MaterialTheme.typography.titleSmall)
                Text(
                    fila.jugador.categoria,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Switch(
                checked = fila.presente,
                onCheckedChange = onPresenteChange,
            )
        }
    }
}
