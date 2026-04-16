package com.escuelafutbol.academia.ui.academia

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AcademiaMiembrosAdminScreen(
    academiaId: String,
    vm: AcademiaMiembrosViewModel,
    onCerrar: () -> Unit,
) {
    val context = LocalContext.current
    val ui by vm.uiState.collectAsState()
    val items by vm.items.collectAsState()

    LaunchedEffect(academiaId) {
        vm.cargar(academiaId)
    }

    var dialogoRol by remember { mutableStateOf<MiembroAdminUi?>(null) }
    var dialogoQuitar by remember { mutableStateOf<MiembroAdminUi?>(null) }
    var dialogoVinculosPadre by remember { mutableStateOf<MiembroAdminUi?>(null) }

    Box(Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.members_admin_title)) },
                navigationIcon = {
                    IconButton(onClick = onCerrar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        when (val s = ui) {
            is MiembrosAdminState.Cargando, MiembrosAdminState.Idle -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
            is MiembrosAdminState.Error -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                ) {
                    Text(s.mensaje, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { vm.cargar(academiaId) }) {
                        Text(stringResource(R.string.members_retry))
                    }
                }
            }
            MiembrosAdminState.Listo -> {
                var categoriaFiltroPadres by remember { mutableStateOf<String?>(null) }
                val categoriasEnUso = remember(items) {
                    items
                        .flatMap { it.categoriasDesdeHijos }
                        .distinctBy { it.lowercase(Locale.getDefault()) }
                        .sortedBy { it.lowercase(Locale.getDefault()) }
                }
                val padresFiltrados = remember(items, categoriaFiltroPadres) {
                    when (val cat = categoriaFiltroPadres) {
                        null -> items
                        else -> items.filter { m ->
                            m.categoriasDesdeHijos.any {
                                it.trim().equals(cat, ignoreCase = true)
                            }
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            Text(
                                stringResource(R.string.members_admin_hint),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        stringResource(R.string.members_parents_category_filter),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    FilterChip(
                                        selected = categoriaFiltroPadres == null,
                                        onClick = { categoriaFiltroPadres = null },
                                        label = { Text(stringResource(R.string.category_all)) },
                                    )
                                    categoriasEnUso.forEach { nombreCat ->
                                        FilterChip(
                                            selected = categoriaFiltroPadres == nombreCat,
                                            onClick = {
                                                categoriaFiltroPadres =
                                                    if (categoriaFiltroPadres == nombreCat) null else nombreCat
                                            },
                                            label = { Text(nombreCat) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (items.isEmpty()) {
                        item {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(
                                    Icons.Default.Group,
                                    contentDescription = null,
                                    modifier = Modifier.size(52.dp),
                                    tint = MaterialTheme.colorScheme.outline,
                                )
                                Text(
                                    stringResource(R.string.members_parents_empty),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else if (padresFiltrados.isEmpty()) {
                        item {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = null,
                                    modifier = Modifier.size(52.dp),
                                    tint = MaterialTheme.colorScheme.outline,
                                )
                                Text(
                                    stringResource(R.string.members_parents_filter_empty),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        items(
                            padresFiltrados,
                            key = { "${it.id}_${categoriaFiltroPadres ?: "all"}" },
                        ) { m ->
                            MiembroAdminCard(
                                m = m,
                                onToggleActivo = { nuevo ->
                                    if (m.esDueñoCuentaAcademia && !nuevo) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.members_owner_cannot_deactivate),
                                            Toast.LENGTH_LONG,
                                        ).show()
                                        return@MiembroAdminCard
                                    }
                                    vm.setActivo(m.id, nuevo, academiaId) { r ->
                                        r.onFailure { e ->
                                            Toast.makeText(
                                                context,
                                                e.message,
                                                Toast.LENGTH_LONG,
                                            ).show()
                                        }
                                    }
                                },
                                onCambiarRol = {
                                    if (m.esDueñoCuentaAcademia) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.members_owner_role_locked),
                                            Toast.LENGTH_LONG,
                                        ).show()
                                    } else {
                                        dialogoRol = m
                                    }
                                },
                                onQuitarDelClub = if (m.esDueñoCuentaAcademia) null else {
                                    { dialogoQuitar = m }
                                },
                                onVinculosPadre = { dialogoVinculosPadre = m },
                            )
                        }
                    }
                }
            }
        }
    }

    }

    dialogoVinculosPadre?.let { m ->
        var tickVinculos by remember(m.id) { mutableStateOf(0) }
        var vinculos by remember(m.id) { mutableStateOf<List<PadresVinculoUi>>(emptyList()) }
        var opcionesVinc by remember(m.id) { mutableStateOf<List<JugadorOpcionVinculoUi>>(emptyList()) }
        var categoriaFiltroVinc by remember(m.id) { mutableStateOf<String?>(null) }
        LaunchedEffect(academiaId, m.userId, tickVinculos) {
            vinculos = vm.listarVinculosPadre(academiaId, m.userId)
            opcionesVinc = vm.jugadoresDisponiblesParaVincular(academiaId, m.userId)
        }
        val opcionesFiltradas = remember(opcionesVinc, categoriaFiltroVinc) {
            when (val cat = categoriaFiltroVinc) {
                null -> opcionesVinc
                else -> opcionesVinc.filter {
                    it.categoria.trim().equals(cat, ignoreCase = true)
                }
            }
        }
        val categoriasVinc = remember(opcionesVinc) {
            opcionesVinc
                .map { it.categoria.trim() }
                .filter { it.isNotEmpty() }
                .distinctBy { it.lowercase(Locale.getDefault()) }
                .sortedWith(String.CASE_INSENSITIVE_ORDER)
        }
        val configuration = LocalConfiguration.current
        val altoMaxCuerpo = (configuration.screenHeightDp * 0.55f).dp.coerceIn(200.dp, 480.dp)
        val scrollCuerpoVinculos = rememberScrollState()
        BasicAlertDialog(
            onDismissRequest = { dialogoVinculosPadre = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        stringResource(R.string.members_parent_links_title),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = altoMaxCuerpo)
                            .verticalScroll(scrollCuerpoVinculos),
                    ) {
                        Text(
                            stringResource(R.string.members_parent_links_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        if (vinculos.isEmpty()) {
                            Text(
                                stringResource(R.string.members_parent_links_empty),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        } else {
                            vinculos.forEach { v ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        v.jugadorNombre,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    TextButton(
                                        onClick = {
                                            vm.quitarVinculoPadre(v.linkId) { r ->
                                                r.onFailure { e ->
                                                    Toast.makeText(
                                                        context,
                                                        e.message,
                                                        Toast.LENGTH_LONG,
                                                    ).show()
                                                }
                                                r.onSuccess {
                                                    tickVinculos++
                                                    vm.cargar(academiaId)
                                                }
                                            }
                                        },
                                    ) {
                                        Text(stringResource(R.string.members_parent_links_remove))
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.members_parent_links_add),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        if (opcionesVinc.isEmpty()) {
                            Text(
                                stringResource(R.string.members_parent_links_no_players_local),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        } else {
                            Text(
                                stringResource(R.string.members_parent_links_category_filter_hint),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                FilterChip(
                                    selected = categoriaFiltroVinc == null,
                                    onClick = { categoriaFiltroVinc = null },
                                    label = { Text(stringResource(R.string.category_all)) },
                                )
                                categoriasVinc.forEach { nombreCat ->
                                    FilterChip(
                                        selected = categoriaFiltroVinc == nombreCat,
                                        onClick = {
                                            categoriaFiltroVinc =
                                                if (categoriaFiltroVinc == nombreCat) null else nombreCat
                                        },
                                        label = { Text(nombreCat) },
                                    )
                                }
                            }
                            if (opcionesFiltradas.isEmpty()) {
                                Text(
                                    stringResource(R.string.members_parent_links_no_players_in_category),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            } else {
                                opcionesFiltradas.forEach { op ->
                                    TextButton(
                                        onClick = {
                                            vm.agregarVinculoPadre(academiaId, m.userId, op.remoteId) { r ->
                                                r.onFailure { e ->
                                                    Toast.makeText(
                                                        context,
                                                        e.message,
                                                        Toast.LENGTH_LONG,
                                                    ).show()
                                                }
                                                r.onSuccess {
                                                    tickVinculos++
                                                    vm.cargar(academiaId)
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text("${op.nombre} — ${op.categoria.trim()}")
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { dialogoVinculosPadre = null }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                }
            }
        }
    }

    dialogoQuitar?.let { m ->
        AlertDialog(
            onDismissRequest = { dialogoQuitar = null },
            title = { Text(stringResource(R.string.members_remove_title)) },
            text = { Text(stringResource(R.string.members_remove_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.eliminarMiembro(m.id, academiaId) { r ->
                            r.onFailure { e ->
                                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                            }
                        }
                        dialogoQuitar = null
                    },
                ) {
                    Text(
                        stringResource(R.string.members_remove_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogoQuitar = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    dialogoRol?.let { m ->
        AlertDialog(
            onDismissRequest = { dialogoRol = null },
            title = { Text(stringResource(R.string.members_change_role_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AcademiaMiembrosViewModel.ROLES_ASIGNABLES.forEach { rol ->
                        TextButton(
                            onClick = {
                                vm.setRol(m.id, rol, academiaId) { r ->
                                    r.onFailure { e ->
                                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                                    }
                                }
                                dialogoRol = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(rolLabel(rol))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogoRol = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun rolLabel(rol: String): String = when (rol.lowercase()) {
    "coach" -> stringResource(R.string.members_rol_coach)
    "coordinator" -> stringResource(R.string.members_rol_coordinator)
    "parent" -> stringResource(R.string.members_rol_parent)
    "admin" -> stringResource(R.string.members_rol_admin)
    else -> rol
}

private fun inicialesMiembro(m: MiembroAdminUi): String {
    val label = m.displayLabel?.trim()?.takeIf { it.isNotEmpty() }
    if (label != null) {
        val parts = label.split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 ->
                "${parts[0].first()}${parts[1].first()}".uppercase(Locale.getDefault())
            parts.isNotEmpty() && parts[0].length >= 2 ->
                parts[0].take(2).uppercase(Locale.getDefault())
            parts.isNotEmpty() ->
                "${parts[0].first()}".uppercase(Locale.getDefault())
            else -> "?"
        }
    }
    val mail = m.memberEmail?.trim()?.takeIf { it.isNotEmpty() }
    if (mail != null) {
        return mail.take(2).uppercase(Locale.getDefault())
    }
    return m.userIdCorto.take(2).uppercase(Locale.getDefault())
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MiembroAdminCard(
    m: MiembroAdminUi,
    onToggleActivo: (Boolean) -> Unit,
    onCambiarRol: () -> Unit,
    onQuitarDelClub: (() -> Unit)?,
    onVinculosPadre: () -> Unit,
) {
    val titulo = m.displayLabel?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.members_user_id_short, m.userIdCorto)
    val mail = m.memberEmail?.trim()?.takeIf { it.isNotBlank() }
    val avatarDesc = stringResource(R.string.members_parent_cd_avatar)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (m.activo) 1f else 0.88f),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .semantics { contentDescription = avatarDesc },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        inicialesMiembro(m),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(titulo, style = MaterialTheme.typography.titleSmall)
                    Text(
                        rolLabel(m.rol),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (m.esDueñoCuentaAcademia) {
                        Text(
                            stringResource(R.string.members_badge_academy_owner),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                    Text(
                        if (m.categoriasDesdeHijos.isNotEmpty()) {
                            m.categoriasDesdeHijos.joinToString(", ")
                        } else {
                            stringResource(R.string.members_parent_categories_empty)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (m.categoriasDesdeHijos.isNotEmpty()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                    )
                    mail?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (!m.activo) {
                        Text(
                            stringResource(R.string.members_ficha_status_inactive),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    stringResource(R.string.members_active_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Switch(
                    checked = m.activo,
                    onCheckedChange = onToggleActivo,
                    enabled = !(m.esDueñoCuentaAcademia && m.activo),
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onCambiarRol) {
                        Icon(
                            Icons.Default.ManageAccounts,
                            contentDescription = stringResource(R.string.members_parent_cd_role),
                        )
                    }
                    IconButton(onClick = onVinculosPadre) {
                        Icon(
                            Icons.Default.FamilyRestroom,
                            contentDescription = stringResource(R.string.members_parent_cd_links),
                        )
                    }
                    if (onQuitarDelClub != null) {
                        IconButton(onClick = onQuitarDelClub) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.members_parent_cd_remove),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}
