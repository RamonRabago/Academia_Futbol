package com.escuelafutbol.academia.ui.parents

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentsScreen(
    viewModel: ParentsViewModel,
    config: AcademiaConfig,
) {
    val mensaje by viewModel.mensaje.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dateFmt = remember {
        DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
    }

    val contenidoFlow = remember(config.remoteAcademiaId, config.cloudMembresiaRol) {
        viewModel.contenidoSegunMembresia(config)
    }
    val contenido by contenidoFlow.collectAsState(initial = ParentsTabContent.StaffComunicaciones)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.tab_parents)) })
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        when (val c = contenido) {
            is ParentsTabContent.StaffComunicaciones -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                ) {
                    Text(
                        stringResource(R.string.parent_staff_comm_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    OutlinedTextField(
                        value = mensaje,
                        onValueChange = viewModel::actualizarMensaje,
                        label = { Text(stringResource(R.string.draft_message)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                    Button(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("aviso_padres", mensaje))
                            scope.launch {
                                snackbar.showSnackbar(context.getString(R.string.message_copied))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    ) {
                        Text(stringResource(R.string.copy_message))
                    }
                }
            }
            ParentsTabContent.PadreSinHijos -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                ) {
                    Text(
                        stringResource(R.string.parent_my_children_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        stringResource(R.string.parent_my_children_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        stringResource(R.string.parent_my_children_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }
            is ParentsTabContent.PadreConHijos -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    item {
                        Text(
                            stringResource(R.string.parent_my_children_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            stringResource(R.string.parent_my_children_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                        )
                    }
                    items(c.hijos, key = { it.nombre + it.categoria }) { hijo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            ),
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(hijo.nombre, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    hijo.categoria,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                if (hijo.ultimasAsistencias.isNotEmpty()) {
                                    HorizontalDivider(Modifier.padding(vertical = 10.dp))
                                    Text(
                                        stringResource(R.string.parent_attendance_recent),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                    hijo.ultimasAsistencias.forEach { linea ->
                                        val fecha = dateFmt.format(Date(linea.fechaDia))
                                        val estado = stringResource(
                                            if (linea.presente) {
                                                R.string.parent_attendance_present
                                            } else {
                                                R.string.parent_attendance_absent
                                            },
                                        )
                                        Text(
                                            stringResource(
                                                R.string.parent_attendance_line,
                                                fecha,
                                                estado,
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp),
                                        )
                                    }
                                } else {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        stringResource(R.string.parent_no_attendance_yet),
                                        style = MaterialTheme.typography.bodySmall,
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
}
