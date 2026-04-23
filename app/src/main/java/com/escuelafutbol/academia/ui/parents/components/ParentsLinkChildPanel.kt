package com.escuelafutbol.academia.ui.parents.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.parents.ParentVinculoCandidatoUi
import com.escuelafutbol.academia.ui.parents.ParentsViewModel
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParentsLinkChildPanel(
    viewModel: ParentsViewModel,
    remoteAcademiaId: String?,
    snackbar: SnackbarHostState,
    scope: CoroutineScope,
    context: Context,
    modifier: Modifier = Modifier,
    compactLayout: Boolean = false,
    hijosVinculadosCount: Int = 0,
    /** Incrementar desde fuera (p. ej. botón «Agregar hijo») para abrir el panel y recargar candidatos. */
    openRequestNonce: Int = 0,
) {
    val candidatos by viewModel.candidatosVinculo.collectAsState()
    val cargandoCand by viewModel.candidatosVinculoCargando.collectAsState()
    val errorCand by viewModel.candidatosVinculoError.collectAsState()
    var categoriaFiltro by remember { mutableStateOf<String?>(null) }
    val categoriasCand = remember(candidatos) {
        candidatos.map { it.categoria }.distinct().sorted()
    }
    val candidatosFiltrados = remember(candidatos, categoriaFiltro) {
        when (val cat = categoriaFiltro) {
            null -> candidatos
            else -> candidatos.filter { it.categoria == cat }
        }
    }
    var expanded by remember(hijosVinculadosCount) {
        mutableStateOf(hijosVinculadosCount == 0)
    }
    var usuarioOcultoPanel by remember { mutableStateOf(false) }
    var candidatoParaVincular by remember { mutableStateOf<ParentVinculoCandidatoUi?>(null) }
    var vinculoEnProgreso by remember { mutableStateOf(false) }
    val candidatosFirma = remember(candidatos) {
        candidatos.joinToString("\u0001") { it.remoteId }
    }
    LaunchedEffect(remoteAcademiaId) {
        if (!remoteAcademiaId.isNullOrBlank()) {
            viewModel.refrescarVinculosPadre()
            viewModel.cargarCandidatosVinculo()
        }
    }
    LaunchedEffect(openRequestNonce) {
        if (openRequestNonce > 0) {
            usuarioOcultoPanel = false
            expanded = true
            viewModel.cargarCandidatosVinculo()
        }
    }
    LaunchedEffect(candidatosFirma, cargandoCand) {
        if (cargandoCand || candidatos.isEmpty()) return@LaunchedEffect
        if (!usuarioOcultoPanel) expanded = true
    }
    val candDlg = candidatoParaVincular
    if (candDlg != null) {
        AlertDialog(
            onDismissRequest = { if (!vinculoEnProgreso) candidatoParaVincular = null },
            title = { Text(stringResource(R.string.parent_self_link_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.parent_self_link_confirm_message,
                        candDlg.nombre,
                        candDlg.categoria,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (vinculoEnProgreso) return@TextButton
                        vinculoEnProgreso = true
                        viewModel.vincularMiHijo(candDlg.remoteId) { r ->
                            vinculoEnProgreso = false
                            candidatoParaVincular = null
                            scope.launch {
                                if (r.isSuccess) {
                                    snackbar.showSnackbar(
                                        context.getString(R.string.parent_self_link_ok),
                                    )
                                } else {
                                    snackbar.showSnackbar(
                                        r.exceptionOrNull()?.message
                                            ?: context.getString(R.string.parent_self_link_error),
                                    )
                                }
                            }
                        }
                    },
                    enabled = !vinculoEnProgreso,
                ) {
                    if (vinculoEnProgreso) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(R.string.parent_self_link_confirm_action))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { candidatoParaVincular = null },
                    enabled = !vinculoEnProgreso,
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Column(modifier = modifier) {
        if (!remoteAcademiaId.isNullOrBlank()) {
            Spacer(Modifier.height(if (compactLayout) 4.dp else 8.dp))
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = {
                                usuarioOcultoPanel = true
                                expanded = false
                            },
                        ) {
                            Text(stringResource(R.string.parent_link_hide_banner))
                        }
                    }
                    if (cargandoCand) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp,
                            )
                            Text(
                                stringResource(R.string.parent_link_loading_cloud),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else {
                        errorCand?.let { err ->
                            Text(
                                err,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            FilledTonalButton(
                                onClick = { viewModel.cargarCandidatosVinculo() },
                                modifier = Modifier.padding(top = 8.dp),
                            ) {
                                Text(stringResource(R.string.resources_reload))
                            }
                        }
                        if (errorCand == null) {
                            when {
                                candidatos.isNotEmpty() -> {
                                    Text(
                                        stringResource(R.string.parent_link_candidates_intro),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                    Text(
                                        stringResource(R.string.parent_self_link_category_hint),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                    FlowRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        FilterChip(
                                            selected = categoriaFiltro == null,
                                            onClick = { categoriaFiltro = null },
                                            label = { Text(stringResource(R.string.category_all)) },
                                        )
                                        categoriasCand.forEach { nombreCat ->
                                            FilterChip(
                                                selected = categoriaFiltro == nombreCat,
                                                onClick = {
                                                    categoriaFiltro =
                                                        if (categoriaFiltro == nombreCat) null else nombreCat
                                                },
                                                label = { Text(nombreCat) },
                                            )
                                        }
                                    }
                                    if (candidatosFiltrados.isEmpty()) {
                                        Text(
                                            stringResource(R.string.members_parent_links_no_players_in_category),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 8.dp),
                                        )
                                    } else {
                                        candidatosFiltrados.forEach { c ->
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Column(Modifier.weight(1f)) {
                                                    Text(c.nombre, style = MaterialTheme.typography.bodyLarge)
                                                    Text(
                                                        c.categoria,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                                TextButton(
                                                    onClick = { candidatoParaVincular = c },
                                                ) {
                                                    Text(stringResource(R.string.parent_self_link_vincular))
                                                }
                                            }
                                        }
                                    }
                                }
                                hijosVinculadosCount == 0 -> {
                                    Text(
                                        stringResource(R.string.parent_children_empty_state),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                }
                                else -> {
                                    Text(
                                        stringResource(R.string.parent_link_no_extra_candidates),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
