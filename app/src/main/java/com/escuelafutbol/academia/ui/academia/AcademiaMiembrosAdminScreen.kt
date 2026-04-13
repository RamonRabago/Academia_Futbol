package com.escuelafutbol.academia.ui.academia

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.entity.Staff
import com.escuelafutbol.academia.ui.util.InviteClubIntentHelper
import com.escuelafutbol.academia.ui.util.formatearFechaDiaLocal
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademiaMiembrosAdminScreen(
    academiaId: String,
    vm: AcademiaMiembrosViewModel,
    /** Equipo local/nube: se cruza por correo con el miembro para sueldo y teléfono en la ficha del entrenador. */
    staff: List<Staff>,
    /** Dueño de cuenta de la academia (o solo local): puede abrir el formulario de Equipo para sueldo y ficha. */
    puedeDueñoEditarFichaCoachSueldo: Boolean,
    /** Abre el diálogo de Equipo; el padre pasa [staffCoincidente] o null para crear ficha enlazada al correo del entrenador. */
    onEditarEquipoParaCoach: (MiembroAdminUi, Staff?) -> Unit,
    codigoInviteCoach: String?,
    codigoInviteCoordinator: String?,
    codigoInviteParent: String?,
    nombreAcademia: String,
    onCerrar: () -> Unit,
) {
    val context = LocalContext.current
    val ui by vm.uiState.collectAsState()
    val items by vm.items.collectAsState()

    LaunchedEffect(academiaId) {
        vm.cargar(academiaId)
    }

    var dialogoRol by remember { mutableStateOf<MiembroAdminUi?>(null) }
    var dialogoCats by remember { mutableStateOf<MiembroAdminUi?>(null) }
    var dialogoQuitar by remember { mutableStateOf<MiembroAdminUi?>(null) }
    var dialogoVinculosPadre by remember { mutableStateOf<MiembroAdminUi?>(null) }
    var fichaCoach by remember { mutableStateOf<MiembroAdminUi?>(null) }

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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            stringResource(R.string.members_admin_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    item {
                        MiembrosInviteBlock(
                            codigoInviteCoach = codigoInviteCoach,
                            codigoInviteCoordinator = codigoInviteCoordinator,
                            codigoInviteParent = codigoInviteParent,
                            nombreAcademia = nombreAcademia,
                        )
                    }
                    items(items, key = { it.id }) { m ->
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
                            onCategoriasCoach = { dialogoCats = m },
                            onQuitarDelClub = if (m.esDueñoCuentaAcademia) null else {
                                { dialogoQuitar = m }
                            },
                            onVinculosPadre = if (m.rol.equals("parent", ignoreCase = true)) {
                                { dialogoVinculosPadre = m }
                            } else {
                                null
                            },
                            onFichaCoach = if (m.rol.equals("coach", ignoreCase = true)) {
                                { fichaCoach = m }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }

    fichaCoach?.let { m ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            MiembroCoachFichaOverlay(
                m = m,
                staffCoincidente = staffPorEmailCoincidente(staff, m.memberEmail),
                puedeDueñoEditarFichaCoachSueldo = puedeDueñoEditarFichaCoachSueldo,
                onEditarEnEquipo = {
                    onEditarEquipoParaCoach(m, staffPorEmailCoincidente(staff, m.memberEmail))
                    fichaCoach = null
                },
                onCerrar = { fichaCoach = null },
            )
        }
    }
    }

    dialogoVinculosPadre?.let { m ->
        var tickVinculos by remember(m.id) { mutableStateOf(0) }
        var vinculos by remember(m.id) { mutableStateOf<List<PadresVinculoUi>>(emptyList()) }
        var opcionesVinc by remember(m.id) { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
        LaunchedEffect(academiaId, m.userId, tickVinculos) {
            vinculos = vm.listarVinculosPadre(academiaId, m.userId)
            opcionesVinc = vm.jugadoresDisponiblesParaVincular(academiaId, m.userId)
        }
        AlertDialog(
            onDismissRequest = { dialogoVinculosPadre = null },
            title = { Text(stringResource(R.string.members_parent_links_title)) },
            text = {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .heightIn(max = 420.dp),
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
                                            r.onSuccess { tickVinculos++ }
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
                        opcionesVinc.forEach { (remoteId, nombre) ->
                            TextButton(
                                onClick = {
                                    vm.agregarVinculoPadre(academiaId, m.userId, remoteId) { r ->
                                        r.onFailure { e ->
                                            Toast.makeText(
                                                context,
                                                e.message,
                                                Toast.LENGTH_LONG,
                                            ).show()
                                        }
                                        r.onSuccess { tickVinculos++ }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(nombre)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogoVinculosPadre = null }) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
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

    dialogoCats?.let { m ->
        if (!m.rol.equals("coach", ignoreCase = true)) {
            dialogoCats = null
        } else {
            var seleccion by remember(m.id) { mutableStateOf(m.categoriaRemoteIds.toSet()) }
            var opciones by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
            LaunchedEffect(Unit) {
                opciones = vm.categoriasSeleccionables()
            }
            AlertDialog(
                onDismissRequest = { dialogoCats = null },
                title = { Text(stringResource(R.string.members_coach_categories_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (opciones.isEmpty()) {
                            Text(
                                stringResource(R.string.members_no_synced_categories),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        } else {
                            opciones.forEach { (remoteId, nombre) ->
                                val sel = remoteId in seleccion
                                FilterChip(
                                    selected = sel,
                                    onClick = {
                                        seleccion = if (sel) seleccion - remoteId else seleccion + remoteId
                                    },
                                    label = { Text(nombre) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            vm.guardarCategoriasCoach(m.id, academiaId, seleccion.toList()) { res ->
                                res.onFailure { e ->
                                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                                }
                            }
                            dialogoCats = null
                        },
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dialogoCats = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }
    }
}

@Composable
private fun MiembrosInviteBlock(
    codigoInviteCoach: String?,
    codigoInviteCoordinator: String?,
    codigoInviteParent: String?,
    nombreAcademia: String,
) {
    val context = LocalContext.current
    val coach = codigoInviteCoach?.trim().orEmpty()
    val coord = codigoInviteCoordinator?.trim().orEmpty()
    val parent = codigoInviteParent?.trim().orEmpty()
    val anyCode = coach.isNotEmpty() || coord.isNotEmpty() || parent.isNotEmpty()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(R.string.members_invite_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                stringResource(R.string.members_invite_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!anyCode) {
                Text(
                    stringResource(R.string.members_invite_no_code),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                MiembrosInviteRoleRow(
                    label = stringResource(R.string.academy_invite_role_coach),
                    code = coach,
                    academyName = nombreAcademia,
                    target = InviteClubIntentHelper.InviteTarget.COACH,
                )
                MiembrosInviteRoleRow(
                    label = stringResource(R.string.academy_invite_role_coordinator),
                    code = coord,
                    academyName = nombreAcademia,
                    target = InviteClubIntentHelper.InviteTarget.COORDINATOR,
                )
                MiembrosInviteRoleRow(
                    label = stringResource(R.string.academy_invite_role_parent),
                    code = parent,
                    academyName = nombreAcademia,
                    target = InviteClubIntentHelper.InviteTarget.PARENT,
                )
            }
        }
    }
}

@Composable
private fun MiembrosInviteRoleRow(
    label: String,
    code: String,
    academyName: String,
    target: InviteClubIntentHelper.InviteTarget,
) {
    val context = LocalContext.current
    if (code.isEmpty()) return
    Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    Text(code, style = MaterialTheme.typography.titleMedium)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = {
                InviteClubIntentHelper.shareInviteTextForTarget(context, academyName, code, target)
            },
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.academy_club_code_share))
        }
        OutlinedButton(
            onClick = { InviteClubIntentHelper.copyCode(context, code) },
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.academy_club_code_copy))
        }
    }
    OutlinedButton(
        onClick = {
            InviteClubIntentHelper.openEmailDraftForTarget(context, academyName, code, target)
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.academy_club_email_invite))
    }
}

private fun staffPorEmailCoincidente(staff: List<Staff>, email: String?): Staff? {
    val e = email?.trim()?.lowercase(Locale.getDefault())?.takeIf { it.isNotEmpty() } ?: return null
    return staff.firstOrNull { s ->
        s.email?.trim()?.lowercase(Locale.getDefault()) == e
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MiembroCoachFichaOverlay(
    m: MiembroAdminUi,
    staffCoincidente: Staff?,
    puedeDueñoEditarFichaCoachSueldo: Boolean,
    onEditarEnEquipo: () -> Unit,
    onCerrar: () -> Unit,
) {
    val moneyFmt = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.members_coach_ficha_title)) },
                navigationIcon = {
                    IconButton(onClick = onCerrar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            val titulo = m.displayLabel?.takeIf { it.isNotBlank() }
                ?: stringResource(R.string.members_user_id_short, m.userIdCorto)
            Text(titulo, style = MaterialTheme.typography.headlineSmall)
            m.memberEmail?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                rolLabel(m.rol),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                stringResource(
                    if (m.activo) {
                        R.string.members_ficha_status_active
                    } else {
                        R.string.members_ficha_status_inactive
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            HorizontalDivider()
            Text(
                stringResource(R.string.members_ficha_label_club_join),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val altaMs = m.fechaAltaClubMillis
            Text(
                if (altaMs != null) {
                    formatearFechaDiaLocal(altaMs)
                } else {
                    stringResource(R.string.members_ficha_club_join_unknown)
                },
                style = MaterialTheme.typography.bodyLarge,
            )
            HorizontalDivider()
            Text(
                stringResource(R.string.members_ficha_label_categories),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (m.nombresCategoriasCoach.isEmpty()) {
                Text(
                    stringResource(R.string.members_ficha_categories_empty),
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                Text(
                    m.nombresCategoriasCoach.joinToString(", "),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            HorizontalDivider()
            Text(
                stringResource(R.string.members_ficha_staff_email_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.members_ficha_label_monthly_pay),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val sueldo = staffCoincidente?.sueldoMensual
            Text(
                when {
                    sueldo != null && sueldo > 0 -> moneyFmt.format(sueldo)
                    else -> stringResource(R.string.members_ficha_monthly_pay_none)
                },
                style = MaterialTheme.typography.bodyLarge,
            )
            staffCoincidente?.telefono?.trim()?.takeIf { it.isNotEmpty() }?.let { tel ->
                Text(
                    stringResource(R.string.members_ficha_label_phone),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(tel, style = MaterialTheme.typography.bodyLarge)
            }
            if (puedeDueñoEditarFichaCoachSueldo) {
                FilledTonalButton(
                    onClick = onEditarEnEquipo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.members_coach_ficha_edit_equipo))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MiembroAdminCard(
    m: MiembroAdminUi,
    onToggleActivo: (Boolean) -> Unit,
    onCambiarRol: () -> Unit,
    onCategoriasCoach: () -> Unit,
    onQuitarDelClub: (() -> Unit)?,
    onVinculosPadre: (() -> Unit)?,
    onFichaCoach: (() -> Unit)?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    val titulo = m.displayLabel?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.members_user_id_short, m.userIdCorto)
                    Text(
                        titulo,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    val mail = m.memberEmail
                    if (!mail.isNullOrBlank() &&
                        !mail.equals(m.displayLabel?.trim(), ignoreCase = true)
                    ) {
                        Text(
                            mail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(R.string.members_active_label),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Switch(
                        checked = m.activo,
                        onCheckedChange = onToggleActivo,
                        enabled = !(m.esDueñoCuentaAcademia && m.activo),
                    )
                }
            }
            if (m.rol.equals("coach", ignoreCase = true) && m.nombresCategoriasCoach.isNotEmpty()) {
                Text(
                    stringResource(
                        R.string.members_coach_categories_line,
                        m.nombresCategoriasCoach.joinToString(", "),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                TextButton(onClick = onCambiarRol) {
                    Text(stringResource(R.string.members_change_role))
                }
                if (m.rol.equals("coach", ignoreCase = true)) {
                    TextButton(onClick = onCategoriasCoach) {
                        Text(stringResource(R.string.members_edit_categories))
                    }
                }
                if (onFichaCoach != null) {
                    TextButton(onClick = onFichaCoach) {
                        Text(stringResource(R.string.members_coach_ficha_open))
                    }
                }
                if (onVinculosPadre != null) {
                    TextButton(onClick = onVinculosPadre) {
                        Text(stringResource(R.string.members_parent_links))
                    }
                }
            }
            if (onQuitarDelClub != null) {
                TextButton(onClick = onQuitarDelClub) {
                    Text(
                        stringResource(R.string.members_remove_from_club),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
