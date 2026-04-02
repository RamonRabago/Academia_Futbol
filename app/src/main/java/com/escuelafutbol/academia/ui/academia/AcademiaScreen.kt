package com.escuelafutbol.academia.ui.academia

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.sync.CloudSyncViewModel
import com.escuelafutbol.academia.ui.util.coilFotoModel
import com.escuelafutbol.academia.ui.util.coilLogoModel
import com.escuelafutbol.academia.ui.util.coilPortadaModel
import com.escuelafutbol.academia.data.local.entity.Staff
import com.escuelafutbol.academia.data.local.model.RolDispositivo
import com.escuelafutbol.academia.data.local.model.RolStaff
import com.escuelafutbol.academia.ui.theme.normalizeBrandColorHex
import com.escuelafutbol.academia.ui.theme.parseBrandColorOrNull
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademiaScreen(
    configVm: AcademiaConfigViewModel,
    staffVm: StaffViewModel,
    onSignOut: (() -> Unit)? = null,
    viewModelFactory: ViewModelProvider.Factory,
) {
    val syncVm: CloudSyncViewModel = viewModel(factory = viewModelFactory)
    val syncing by syncVm.syncing.collectAsState()
    val syncMsg by syncVm.syncMessage.collectAsState()

    val config by configVm.config.collectAsState()
    val staff by staffVm.staff.collectAsState()
    val rolDispositivo = RolDispositivo.fromStored(config.rolDispositivo)
    val esPersonalClub = rolDispositivo.esPersonalClub()
    val context = LocalContext.current

    var nombreAcademiaEdit by remember(config.nombreAcademia) { mutableStateOf(config.nombreAcademia) }
    var dialogoStaff by remember { mutableStateOf(false) }
    var staffEditar by remember { mutableStateOf<Staff?>(null) }
    var staffFormInstance by remember { mutableStateOf(0) }
    var staffEliminar by remember { mutableStateOf<Staff?>(null) }
    var modoDialogoPin by remember { mutableStateOf<ModoDialogoPin?>(null) }
    var pendienteTrasPin by remember { mutableStateOf<PendienteTrasPin?>(null) }

    fun solicitarCambioRol(nuevo: RolDispositivo) {
        val actual = RolDispositivo.fromStored(config.rolDispositivo)
        if (nuevo == actual) return
        pendienteTrasPin = PendienteTrasPin.Rol(nuevo)
        modoDialogoPin =
            if (config.pinStaffHash == null) ModoDialogoPin.CREAR else ModoDialogoPin.INGRESAR
    }

    fun solicitarCambioPermisos(prof: Boolean, coord: Boolean, dueno: Boolean) {
        if (prof == config.mensualidadVisibleProfesor &&
            coord == config.mensualidadVisibleCoordinador &&
            dueno == config.mensualidadVisibleDueno
        ) {
            return
        }
        pendienteTrasPin = PendienteTrasPin.Permisos(prof, coord, dueno)
        modoDialogoPin =
            if (config.pinStaffHash == null) ModoDialogoPin.CREAR else ModoDialogoPin.INGRESAR
    }

    val elegirLogo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) configVm.guardarLogo(uri)
    }
    val elegirPortada = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) configVm.guardarPortada(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.tab_academy)) })
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SeccionModoDispositivo(
                    rolDispositivo = rolDispositivo,
                    pinStaffConfigurado = config.pinStaffHash != null,
                    solicitarCambioRol = { solicitarCambioRol(it) },
                    onAbrirCambiarPin = {
                        pendienteTrasPin = null
                        modoDialogoPin = ModoDialogoPin.CAMBIAR
                    },
                )
            }
            if (esPersonalClub) {
            item {
                Text(
                    stringResource(R.string.sync_cloud_section),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(R.string.sync_cloud_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = {
                        syncVm.clearMessage()
                        syncVm.syncNow()
                    },
                    enabled = !syncing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                ) {
                    Text(
                        if (syncing) {
                            stringResource(R.string.sync_cloud_running)
                        } else {
                            stringResource(R.string.sync_cloud_button)
                        },
                    )
                }
                syncMsg?.let { m ->
                    Text(
                        m,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            item {
                if (config.remoteAcademiaId != null) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        ),
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                stringResource(R.string.academy_club_code_section),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                stringResource(R.string.academy_club_code_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                config.codigoClubRemoto
                                    ?: stringResource(R.string.academy_club_code_none),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Button(
                                onClick = {
                                    configVm.regenerarCodigoClub { r ->
                                        r.onFailure { e ->
                                            Toast.makeText(
                                                context,
                                                e.message,
                                                Toast.LENGTH_LONG,
                                            ).show()
                                        }
                                        r.onSuccess { code ->
                                            Toast.makeText(
                                                context,
                                                code,
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.academy_club_code_generate))
                            }
                        }
                    }
                }
            }
            } else {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        ),
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                stringResource(R.string.academy_readonly_family_title),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                stringResource(R.string.academy_readonly_family_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                config.nombreAcademia,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            if (esPersonalClub) {
            item {
                Text(
                    stringResource(R.string.academy_branding_section),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(R.string.academy_branding_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Text(
                    stringResource(R.string.academy_cover_section),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    stringResource(R.string.academy_cover_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val portadaModel = config.coilPortadaModel(context)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(120.dp),
                ) {
                    if (portadaModel != null) {
                        AsyncImage(
                            model = portadaModel,
                            contentDescription = stringResource(R.string.academy_cover),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                stringResource(R.string.academy_cover_empty),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = { elegirPortada.launch("image/*") }) {
                        Text(stringResource(R.string.academy_pick_cover))
                    }
                    if (config.portadaRutaAbsoluta != null || !config.portadaUrlSupabase.isNullOrBlank()) {
                        TextButton(onClick = { configVm.quitarPortada() }) {
                            Text(stringResource(R.string.academy_remove_cover))
                        }
                    }
                }
            }
            item {
                Text(
                    stringResource(R.string.academy_logo_section),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    stringResource(R.string.academy_logo_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    val logoModel = config.coilLogoModel(context)
                    if (logoModel != null) {
                        AsyncImage(
                            model = logoModel,
                            contentDescription = stringResource(R.string.academy_logo_profile),
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Card(
                            modifier = Modifier.size(88.dp),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ) {
                            Column(
                                Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    stringResource(R.string.academy_logo_placeholder_short),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { elegirLogo.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.academy_pick_logo_profile))
                        }
                        if (config.logoRutaAbsoluta != null || !config.logoUrlSupabase.isNullOrBlank()) {
                            TextButton(onClick = { configVm.quitarLogo() }) {
                                Text(stringResource(R.string.academy_remove_logo))
                            }
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = nombreAcademiaEdit,
                    onValueChange = { nombreAcademiaEdit = it },
                    label = { Text(stringResource(R.string.academy_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Button(
                    onClick = { configVm.guardarNombre(nombreAcademiaEdit) },
                    enabled = nombreAcademiaEdit.isNotBlank(),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.save_academy_name))
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.academy_theme_section),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(R.string.academy_theme_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                var primHex by remember { mutableStateOf("") }
                var secHex by remember { mutableStateOf("") }
                LaunchedEffect(config.temaColorPrimarioHex, config.temaColorSecundarioHex) {
                    primHex =
                        config.temaColorPrimarioHex?.removePrefix("#")?.uppercase(Locale.ROOT) ?: ""
                    secHex =
                        config.temaColorSecundarioHex?.removePrefix("#")?.uppercase(Locale.ROOT) ?: ""
                }
                fun fieldValid(s: String): Boolean = s.isBlank() || normalizeBrandColorHex(s) != null
                val canSave = fieldValid(primHex) && fieldValid(secHex)
                val scrollPrim = rememberScrollState()
                val scrollSec = rememberScrollState()
                val presetsPrim = listOf(
                    "1B4332" to R.string.theme_preset_green,
                    "003566" to R.string.theme_preset_blue,
                    "C1121F" to R.string.theme_preset_red,
                    "6F1D1B" to R.string.theme_preset_maroon,
                )
                val presetsSec = listOf(
                    "E9C46A" to R.string.theme_preset_gold,
                    "FFB703" to R.string.theme_preset_yellow,
                    "F77F00" to R.string.theme_preset_orange,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                parseBrandColorOrNull(primHex)
                                    ?: MaterialTheme.colorScheme.surfaceVariant,
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(8.dp),
                            ),
                    )
                    OutlinedTextField(
                        value = primHex,
                        onValueChange = { v ->
                            primHex = v.replace("#", "")
                                .uppercase(Locale.ROOT)
                                .filter { it in "0123456789ABCDEF" }
                                .take(6)
                        },
                        label = { Text(stringResource(R.string.academy_theme_primary_label)) },
                        placeholder = { Text(stringResource(R.string.academy_theme_hex_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    stringResource(R.string.academy_theme_presets_primary),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollPrim),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    presetsPrim.forEach { (hex, labelRes) ->
                        FilterChip(
                            selected = primHex == hex,
                            onClick = { primHex = hex },
                            label = { Text(stringResource(labelRes)) },
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                parseBrandColorOrNull(secHex)
                                    ?: MaterialTheme.colorScheme.surfaceVariant,
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(8.dp),
                            ),
                    )
                    OutlinedTextField(
                        value = secHex,
                        onValueChange = { v ->
                            secHex = v.replace("#", "")
                                .uppercase(Locale.ROOT)
                                .filter { it in "0123456789ABCDEF" }
                                .take(6)
                        },
                        label = { Text(stringResource(R.string.academy_theme_secondary_label)) },
                        placeholder = { Text(stringResource(R.string.academy_theme_hex_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    stringResource(R.string.academy_theme_presets_secondary),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollSec),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    presetsSec.forEach { (hex, labelRes) ->
                        FilterChip(
                            selected = secHex == hex,
                            onClick = { secHex = hex },
                            label = { Text(stringResource(labelRes)) },
                        )
                    }
                }
                if (!canSave) {
                    Text(
                        stringResource(R.string.academy_theme_invalid_hex),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(onClick = { configVm.restaurarColoresTemaPorDefecto() }) {
                        Text(stringResource(R.string.academy_theme_reset))
                    }
                    Button(
                        onClick = { configVm.guardarColoresTema(primHex, secHex) },
                        enabled = canSave,
                    ) {
                        Text(stringResource(R.string.academy_theme_save))
                    }
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.academy_fee_privacy_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(R.string.academy_fee_privacy_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    stringResource(R.string.academy_fee_pin_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    stringResource(R.string.academy_fee_who_sees),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.academy_fee_switch_profesor))
                    Switch(
                        checked = config.mensualidadVisibleProfesor,
                        onCheckedChange = {
                            solicitarCambioPermisos(
                                it,
                                config.mensualidadVisibleCoordinador,
                                config.mensualidadVisibleDueno,
                            )
                        },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.academy_fee_switch_coordinador))
                    Switch(
                        checked = config.mensualidadVisibleCoordinador,
                        onCheckedChange = {
                            solicitarCambioPermisos(
                                config.mensualidadVisibleProfesor,
                                it,
                                config.mensualidadVisibleDueno,
                            )
                        },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.academy_fee_switch_dueno))
                    Switch(
                        checked = config.mensualidadVisibleDueno,
                        onCheckedChange = {
                            solicitarCambioPermisos(
                                config.mensualidadVisibleProfesor,
                                config.mensualidadVisibleCoordinador,
                                it,
                            )
                        },
                    )
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.staff_section),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(R.string.staff_section_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = {
                        staffEditar = null
                        staffFormInstance++
                        dialogoStaff = true
                    },
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.add_staff))
                }
            }
            items(staff, key = { it.id }) { m ->
                StaffCard(
                    staff = m,
                    onEdit = {
                        dialogoStaff = false
                        staffFormInstance++
                        staffEditar = m
                    },
                    onDelete = { staffEliminar = m },
                )
            }
            }
            if (onSignOut != null) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        stringResource(R.string.auth_account_section),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        stringResource(R.string.auth_account_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    ) {
                        Text(stringResource(R.string.auth_sign_out))
                    }
                }
            }
        }
    }

    if (dialogoStaff || staffEditar != null) {
        key(staffFormInstance) {
            StaffFormDialog(
                staffExistente = staffEditar,
                onDismiss = {
                    dialogoStaff = false
                    staffEditar = null
                },
                onGuardar = { nombre, rol, tel, email, fotoPath, quitarFoto ->
                    val editando = staffEditar
                    if (editando != null) {
                        staffVm.actualizar(editando, nombre, rol, tel, email, fotoPath, quitarFoto)
                    } else {
                        staffVm.agregar(nombre, rol, tel, email, fotoPath)
                    }
                    dialogoStaff = false
                    staffEditar = null
                },
            )
        }
    }

    staffEliminar?.let { m ->
        AlertDialog(
            onDismissRequest = { staffEliminar = null },
            title = { Text(stringResource(R.string.delete_staff_title)) },
            text = { Text(stringResource(R.string.delete_staff_message, m.nombre)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        staffVm.eliminar(m)
                        staffEliminar = null
                    },
                ) { Text(stringResource(R.string.eliminar)) }
            },
            dismissButton = {
                TextButton(onClick = { staffEliminar = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    modoDialogoPin?.let { modo ->
        DialogoPinStaff(
            modo = modo,
            pendiente = pendienteTrasPin,
            configVm = configVm,
            onEjecutarPendiente = { p ->
                when (p) {
                    is PendienteTrasPin.Rol -> configVm.guardarRolDispositivo(p.valor)
                    is PendienteTrasPin.Permisos -> configVm.guardarPermisosMensualidad(
                        p.prof,
                        p.coord,
                        p.dueno,
                    )
                }
            },
            onCerrar = {
                modoDialogoPin = null
                pendienteTrasPin = null
            },
        )
    }
}

@Composable
private fun SeccionModoDispositivo(
    rolDispositivo: RolDispositivo,
    pinStaffConfigurado: Boolean,
    solicitarCambioRol: (RolDispositivo) -> Unit,
    onAbrirCambiarPin: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(R.string.academy_device_role_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                stringResource(R.string.academy_device_role_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.academy_device_who_uses),
                style = MaterialTheme.typography.titleSmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = rolDispositivo == RolDispositivo.PADRE_TUTOR,
                    onClick = { solicitarCambioRol(RolDispositivo.PADRE_TUTOR) },
                    label = { Text(stringResource(R.string.rol_padre_tutor)) },
                )
                FilterChip(
                    selected = rolDispositivo == RolDispositivo.PROFESOR,
                    onClick = { solicitarCambioRol(RolDispositivo.PROFESOR) },
                    label = { Text(stringResource(R.string.rol_profesor)) },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = rolDispositivo == RolDispositivo.COORDINADOR,
                    onClick = { solicitarCambioRol(RolDispositivo.COORDINADOR) },
                    label = { Text(stringResource(R.string.rol_coordinador)) },
                )
                FilterChip(
                    selected = rolDispositivo == RolDispositivo.DUENO_ACADEMIA,
                    onClick = { solicitarCambioRol(RolDispositivo.DUENO_ACADEMIA) },
                    label = { Text(stringResource(R.string.rol_dueno_academia)) },
                )
            }
            Text(
                stringResource(R.string.academy_device_pin_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.academy_permissions_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                stringResource(
                    when (rolDispositivo) {
                        RolDispositivo.PADRE_TUTOR -> R.string.academy_permissions_padre
                        RolDispositivo.PROFESOR -> R.string.academy_permissions_profesor
                        RolDispositivo.COORDINADOR -> R.string.academy_permissions_coordinador
                        RolDispositivo.DUENO_ACADEMIA -> R.string.academy_permissions_dueno
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (pinStaffConfigurado && rolDispositivo.esPersonalClub()) {
                TextButton(
                    onClick = onAbrirCambiarPin,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(stringResource(R.string.pin_change_action))
                }
            }
        }
    }
}

@Composable
private fun StaffCard(
    staff: Staff,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val rol = RolStaff.fromStored(staff.rol)
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val context = LocalContext.current
            val fotoModel = staff.coilFotoModel(context)
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (fotoModel != null) {
                    AsyncImage(
                        model = fotoModel,
                        contentDescription = stringResource(R.string.staff_photo_cd),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(staff.nombre, style = MaterialTheme.typography.titleSmall)
                    Text(
                        when (rol) {
                            RolStaff.COORDINADOR -> stringResource(R.string.rol_coordinador)
                            RolStaff.PROFESOR -> stringResource(R.string.rol_profesor)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    listOfNotNull(staff.telefono, staff.email).joinToString(" · ").takeIf { it.isNotEmpty() }
                        ?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_staff))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.eliminar))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StaffFormDialog(
    staffExistente: Staff?,
    onDismiss: () -> Unit,
    onGuardar: (
        nombre: String,
        rol: RolStaff,
        telefono: String?,
        email: String?,
        fotoRutaAbsoluta: String?,
        quitarFoto: Boolean,
    ) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var nombre by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf(RolStaff.PROFESOR) }
    var tel by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fotoPath by remember { mutableStateOf<String?>(null) }
    var sinFoto by remember { mutableStateOf(false) }
    var cameraFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(staffExistente) {
        val s = staffExistente
        sinFoto = false
        if (s != null) {
            nombre = s.nombre
            rol = RolStaff.fromStored(s.rol)
            tel = s.telefono.orEmpty()
            email = s.email.orEmpty()
            fotoPath = s.fotoRutaAbsoluta
        } else {
            nombre = ""
            rol = RolStaff.PROFESOR
            tel = ""
            email = ""
            fotoPath = null
        }
    }

    fun staffPhotoDest(): File {
        val dir = File(context.filesDir, "staff_photos").apply { mkdirs() }
        return File(dir, "${UUID.randomUUID()}.jpg")
    }

    val pickGallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            val dest = staffPhotoDest()
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { out -> input.copyTo(out) }
                }
            }
            withContext(Dispatchers.Main) {
                if (dest.exists() && dest.length() > 0) {
                    fotoPath?.let { runCatching { File(it).delete() } }
                    fotoPath = dest.absolutePath
                    sinFoto = false
                }
            }
        }
    }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val f = cameraFile
        if (!success || f == null || !f.exists()) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            val dest = staffPhotoDest()
            runCatching { f.copyTo(dest, overwrite = true) }
            f.delete()
            withContext(Dispatchers.Main) {
                if (dest.exists() && dest.length() > 0) {
                    fotoPath?.let { runCatching { File(it).delete() } }
                    fotoPath = dest.absolutePath
                    sinFoto = false
                }
            }
        }
    }

    fun launchCameraCapture() {
        val f = File(context.cacheDir, "staff_cam_${System.currentTimeMillis()}.jpg")
        runCatching {
            if (!f.exists()) f.createNewFile()
        }
        cameraFile = f
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            f,
        )
        takePicture.launch(uri)
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) launchCameraCapture()
    }

    fun openCamera() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> launchCameraCapture()
            else -> requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    val configuration = LocalConfiguration.current
    val scrollMaxHeight = configuration.screenHeightDp.dp * 0.58f
    val formScroll = rememberScrollState()

    BasicAlertDialog(
        onDismissRequest = onDismiss,
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
                    stringResource(
                        if (staffExistente != null) R.string.edit_staff else R.string.add_staff,
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = scrollMaxHeight)
                        .verticalScroll(formScroll),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                stringResource(R.string.staff_photo_section),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                stringResource(R.string.staff_photo_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val path = fotoPath
                                val previewModel: Any? = if (sinFoto) {
                                    null
                                } else {
                                    when {
                                        path != null && File(path).exists() ->
                                            ImageRequest.Builder(context)
                                                .data(File(path))
                                                .crossfade(true)
                                                .build()
                                        staffExistente != null -> staffExistente.coilFotoModel(context)
                                        else -> null
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .clickable { pickGallery.launch("image/*") },
                                ) {
                                    if (previewModel != null) {
                                        AsyncImage(
                                            model = previewModel,
                                            contentDescription = stringResource(R.string.staff_photo_cd),
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = stringResource(R.string.staff_pick_gallery),
                                                modifier = Modifier.size(26.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        OutlinedButton(
                                            onClick = { pickGallery.launch("image/*") },
                                            modifier = Modifier
                                                .weight(1f)
                                                .defaultMinSize(minHeight = 44.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                Icon(
                                                    Icons.Default.Image,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                                Text(
                                                    stringResource(R.string.staff_pick_gallery),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    maxLines = 1,
                                                )
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { openCamera() },
                                            modifier = Modifier
                                                .weight(1f)
                                                .defaultMinSize(minHeight = 44.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                Icon(
                                                    Icons.Default.PhotoCamera,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                                Text(
                                                    stringResource(R.string.staff_take_photo),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    maxLines = 1,
                                                )
                                            }
                                        }
                                    }
                                    if (!sinFoto && (
                                            fotoPath != null || staffExistente?.let {
                                                it.fotoRutaAbsoluta != null || !it.fotoUrlSupabase.isNullOrBlank()
                                            } == true
                                        )
                                    ) {
                                        TextButton(
                                            modifier = Modifier.align(Alignment.End),
                                            onClick = {
                                                fotoPath?.let { runCatching { File(it).delete() } }
                                                fotoPath = null
                                                sinFoto = true
                                            },
                                        ) {
                                            Text(stringResource(R.string.staff_remove_photo))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text(stringResource(R.string.name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = rol == RolStaff.PROFESOR,
                            onClick = { rol = RolStaff.PROFESOR },
                            label = { Text(stringResource(R.string.rol_profesor)) },
                        )
                        FilterChip(
                            selected = rol == RolStaff.COORDINADOR,
                            onClick = { rol = RolStaff.COORDINADOR },
                            label = { Text(stringResource(R.string.rol_coordinador)) },
                        )
                    }
                    OutlinedTextField(
                        value = tel,
                        onValueChange = { tel = it },
                        label = { Text(stringResource(R.string.staff_phone)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.staff_email)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = { onGuardar(nombre, rol, tel, email, fotoPath, sinFoto) },
                        enabled = nombre.isNotBlank(),
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}
