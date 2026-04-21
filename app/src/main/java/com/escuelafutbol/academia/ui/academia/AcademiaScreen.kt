package com.escuelafutbol.academia.ui.academia

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.auth.AuthProfileSnapshot
import com.escuelafutbol.academia.ui.auth.AuthViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import com.escuelafutbol.academia.ui.util.coilFotoModel
import com.escuelafutbol.academia.ui.util.coilLogoModel
import com.escuelafutbol.academia.ui.util.coilPortadaModel
import com.escuelafutbol.academia.ui.util.FullscreenImageViewerDialog
import com.escuelafutbol.academia.ui.util.InviteClubIntentHelper
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.Staff
import com.escuelafutbol.academia.data.local.model.esPadreMembresiaNube
import com.escuelafutbol.academia.data.local.model.rolDispositivoEfectivo
import com.escuelafutbol.academia.data.local.model.puedeMutarDiaLimitePagoMes
import com.escuelafutbol.academia.data.local.model.RolStaff
import com.escuelafutbol.academia.ui.theme.normalizeBrandColorHex
import com.escuelafutbol.academia.ui.theme.parseBrandColorOrNull
import java.io.File
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed class AcademiaBrandingImageViewer {
    data object Portada : AcademiaBrandingImageViewer()
    data object Logo : AcademiaBrandingImageViewer()
}

/** Pantallas de detalle bajo el menú de ajustes de Academia (engranaje). */
private sealed class AcademiaDestinoAjuste {
    data object InvitacionesYAcceso : AcademiaDestinoAjuste()
    data object IdentidadClub : AcademiaDestinoAjuste()
    data object TemaColores : AcademiaDestinoAjuste()
    data object PagosPrivacidad : AcademiaDestinoAjuste()
    data object EquipoTecnico : AcademiaDestinoAjuste()
    data object CuentaUsuario : AcademiaDestinoAjuste()
}

private fun tituloDestinoAjusteRes(destino: AcademiaDestinoAjuste?): Int = when (destino) {
    null -> R.string.tab_academy
    AcademiaDestinoAjuste.InvitacionesYAcceso -> R.string.academy_club_code_section
    AcademiaDestinoAjuste.IdentidadClub -> R.string.academy_branding_section
    AcademiaDestinoAjuste.TemaColores -> R.string.academy_theme_section
    AcademiaDestinoAjuste.PagosPrivacidad -> R.string.academy_fee_privacy_title
    AcademiaDestinoAjuste.EquipoTecnico -> R.string.staff_section
    AcademiaDestinoAjuste.CuentaUsuario -> R.string.auth_account_section
}

/** null = menú de tarjetas; resto = detalle de un rol. */
private sealed class InvitacionesSubPantalla {
    data object RolEntrenador : InvitacionesSubPantalla()
    data object RolCoordinador : InvitacionesSubPantalla()
    data object RolFamilias : InvitacionesSubPantalla()
}

private fun tituloInvitacionesSubRes(sub: InvitacionesSubPantalla?): Int? = when (sub) {
    null -> null
    InvitacionesSubPantalla.RolEntrenador -> R.string.academy_invite_role_coach
    InvitacionesSubPantalla.RolCoordinador -> R.string.academy_invite_role_coordinator
    InvitacionesSubPantalla.RolFamilias -> R.string.academy_invite_role_parent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademiaScreen(
    configVm: AcademiaConfigViewModel,
    staffVm: StaffViewModel,
    viewModelFactory: ViewModelProvider.Factory,
    sessionAuthUserId: String = "",
    onSignOut: (() -> Unit)? = null,
) {
    val config by configVm.config.collectAsState()
    if (config.esPadreMembresiaNube()) {
        AcademiaPadreNubeSimpleScreen(
            config = config,
            onSignOut = onSignOut,
        )
        return
    }
    val staff by staffVm.staff.collectAsState()
    val rolDispositivo = config.rolDispositivoEfectivo(sessionAuthUserId.takeIf { it.isNotBlank() })
    val esPersonalClub = rolDispositivo.esPersonalClub()
    val puedeGestionarAcademiaCabecera =
        config.remoteAcademiaId == null || config.academiaGestionNubePermitida
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val viewRoot = LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val nombreGuardadoMsg = stringResource(R.string.academy_name_saved)
    val nombreDenegadoMsg = stringResource(R.string.academy_name_save_denied)
    val diaLimiteGuardadoMsg = stringResource(R.string.academy_payment_deadline_saved)
    val diaLimiteQuitadoMsg = stringResource(R.string.academy_payment_deadline_cleared)
    val diaLimiteDenegadoMsg = stringResource(R.string.academy_payment_deadline_save_denied)

    var nombreAcademiaEdit by remember(config.nombreAcademia) { mutableStateOf(config.nombreAcademia) }
    var dialogoStaff by remember { mutableStateOf(false) }
    var staffEditar by remember { mutableStateOf<Staff?>(null) }
    /** Si no hay fila en Equipo, rellena el alta desde la ficha del entrenador en miembros. */
    var staffPrefillCoachMiembro by remember { mutableStateOf<MiembroAdminUi?>(null) }
    var staffFormInstance by remember { mutableStateOf(0) }
    var staffEliminar by remember { mutableStateOf<Staff?>(null) }
    var modoDialogoPin by remember { mutableStateOf<ModoDialogoPin?>(null) }
    var pendienteTrasPin by remember { mutableStateOf<PendienteTrasPin?>(null) }
    val miembrosVm: AcademiaMiembrosViewModel = viewModel(factory = viewModelFactory)
    val authVm: AuthViewModel = viewModel(factory = viewModelFactory)
    val authBusy by authVm.busy.collectAsState()
    val authSession by authVm.sessionStatus.collectAsState()
    val cuentaPerfil = remember(authSession, authBusy) { authVm.editableProfileSnapshot() }
    var pantallaMiembros by remember { mutableStateOf(false) }
    var destinoAjusteAcademia by remember { mutableStateOf<AcademiaDestinoAjuste?>(null) }
    var invitacionesSub by remember { mutableStateOf<InvitacionesSubPantalla?>(null) }
    var brandingImageViewer by remember { mutableStateOf<AcademiaBrandingImageViewer?>(null) }
    var dialogoPerfil by remember { mutableStateOf(false) }
    var perfilNombre by remember { mutableStateOf("") }
    var perfilApellido by remember { mutableStateOf("") }
    var perfilEmail by remember { mutableStateOf<String?>(null) }
    val perfilGuardadoMsg = stringResource(R.string.auth_profile_saved)

    LaunchedEffect(dialogoPerfil) {
        if (dialogoPerfil) {
            val s = authVm.editableProfileSnapshot()
            perfilNombre = s?.nombre.orEmpty()
            perfilApellido = s?.apellido.orEmpty()
            perfilEmail = s?.email
        }
    }

    LaunchedEffect(destinoAjusteAcademia) {
        if (destinoAjusteAcademia != AcademiaDestinoAjuste.InvitacionesYAcceso) {
            invitacionesSub = null
        }
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

    Box(Modifier.fillMaxSize()) {
        BackHandler(enabled = destinoAjusteAcademia != null && !pantallaMiembros) {
            when {
                destinoAjusteAcademia == AcademiaDestinoAjuste.InvitacionesYAcceso &&
                    invitacionesSub != null -> {
                    invitacionesSub = null
                }
                else -> destinoAjusteAcademia = null
            }
        }
    Scaffold(
        topBar = {
            if (destinoAjusteAcademia != null && esPersonalClub && puedeGestionarAcademiaCabecera) {
                val tituloInvSub = tituloInvitacionesSubRes(invitacionesSub)
                val tituloRes = if (
                    destinoAjusteAcademia == AcademiaDestinoAjuste.InvitacionesYAcceso &&
                    tituloInvSub != null
                ) {
                    tituloInvSub
                } else {
                    tituloDestinoAjusteRes(destinoAjusteAcademia)
                }
                TopAppBar(
                    title = { Text(stringResource(tituloRes)) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                when {
                                    destinoAjusteAcademia == AcademiaDestinoAjuste.InvitacionesYAcceso &&
                                        invitacionesSub != null -> {
                                        invitacionesSub = null
                                    }
                                    else -> destinoAjusteAcademia = null
                                }
                            },
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.nav_back_cd),
                            )
                        }
                    },
                )
            } else {
                TopAppBar(title = { Text(stringResource(R.string.tab_academy)) })
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val puedeGestionarAcademia =
                config.remoteAcademiaId == null || config.academiaGestionNubePermitida
            if (!esPersonalClub) {
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
                                config.nombreAcademia,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            } else if (!puedeGestionarAcademia) {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        ),
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                stringResource(R.string.academy_readonly_no_admin_title),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                stringResource(R.string.academy_readonly_no_admin_body),
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
            } else {
                if (destinoAjusteAcademia == null) {
                    item {
                        Text(
                            stringResource(R.string.academy_settings_hub_intro),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (config.remoteAcademiaId != null) {
                        item {
                            AcademiaAjusteNavegacionFila(
                                icon = Icons.Filled.Share,
                                titulo = stringResource(R.string.academy_club_code_section),
                                subtitulo = stringResource(R.string.academy_club_code_hint),
                                onClick = { destinoAjusteAcademia = AcademiaDestinoAjuste.InvitacionesYAcceso },
                            )
                        }
                        item {
                            AcademiaAjusteNavegacionFila(
                                icon = Icons.Filled.Group,
                                titulo = stringResource(R.string.members_manage_button),
                                subtitulo = stringResource(R.string.members_manage_menu_subtitle),
                                onClick = {
                                    destinoAjusteAcademia = null
                                    pantallaMiembros = true
                                },
                            )
                        }
                    }
                    item {
                        AcademiaAjusteNavegacionFila(
                            icon = Icons.Filled.Image,
                            titulo = stringResource(R.string.academy_branding_section),
                            subtitulo = stringResource(R.string.academy_branding_hint),
                            onClick = { destinoAjusteAcademia = AcademiaDestinoAjuste.IdentidadClub },
                        )
                    }
                    item {
                        AcademiaAjusteNavegacionFila(
                            icon = Icons.Filled.Edit,
                            titulo = stringResource(R.string.academy_theme_section),
                            subtitulo = stringResource(R.string.academy_theme_hint),
                            onClick = { destinoAjusteAcademia = AcademiaDestinoAjuste.TemaColores },
                        )
                    }
                    item {
                        AcademiaAjusteNavegacionFila(
                            icon = Icons.Filled.Lock,
                            titulo = stringResource(R.string.academy_fee_privacy_title),
                            subtitulo = stringResource(R.string.academy_fee_privacy_hint),
                            onClick = { destinoAjusteAcademia = AcademiaDestinoAjuste.PagosPrivacidad },
                        )
                    }
                    item {
                        AcademiaAjusteNavegacionFila(
                            icon = Icons.Filled.ManageAccounts,
                            titulo = stringResource(R.string.staff_section),
                            subtitulo = stringResource(R.string.staff_section_hint),
                            onClick = { destinoAjusteAcademia = AcademiaDestinoAjuste.EquipoTecnico },
                        )
                    }
                    if (onSignOut != null) {
                        item {
                            AcademiaAjusteNavegacionFila(
                                icon = Icons.Filled.Email,
                                titulo = stringResource(R.string.auth_account_section),
                                subtitulo = stringResource(R.string.auth_account_hint),
                                onClick = { destinoAjusteAcademia = AcademiaDestinoAjuste.CuentaUsuario },
                            )
                        }
                    }
                } else {
                    when (destinoAjusteAcademia!!) {
                        AcademiaDestinoAjuste.InvitacionesYAcceso -> {
                            when (invitacionesSub) {
                                null -> {
                                    item {
                                        if (config.remoteAcademiaId != null) {
                                            Text(
                                                stringResource(R.string.academy_invite_hub_intro),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    item {
                                        if (config.remoteAcademiaId != null) {
                                            OutlinedCard(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(16.dp),
                                            ) {
                                                Column(
                                                    Modifier.padding(16.dp),
                                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                                ) {
                                                    Text(
                                                        stringResource(R.string.academy_club_code_generate),
                                                        style = MaterialTheme.typography.titleSmall,
                                                    )
                                                    Text(
                                                        stringResource(R.string.academy_club_code_hint),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 5,
                                                    )
                                                    FilledTonalButton(
                                                        onClick = {
                                                            configVm.regenerarCodigosInvitacion { r ->
                                                                r.onFailure { e ->
                                                                    Toast.makeText(
                                                                        context,
                                                                        e.message,
                                                                        Toast.LENGTH_LONG,
                                                                    ).show()
                                                                }
                                                                r.onSuccess {
                                                                    Toast.makeText(
                                                                        context,
                                                                        R.string.academy_invite_codes_regenerated,
                                                                        Toast.LENGTH_SHORT,
                                                                    ).show()
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp),
                                                    ) {
                                                        Icon(
                                                            Icons.Filled.Refresh,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(20.dp),
                                                        )
                                                        Spacer(Modifier.width(10.dp))
                                                        Text(stringResource(R.string.academy_club_code_generate))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    item {
                                        if (config.remoteAcademiaId != null) {
                                            val c = config.codigoInviteCoachRemoto?.trim().orEmpty()
                                            val sub = if (c.isEmpty()) {
                                                stringResource(R.string.academy_invite_code_missing)
                                            } else {
                                                stringResource(R.string.academy_invite_role_subtitle_code, c)
                                            }
                                            AcademiaAjusteNavegacionFila(
                                                icon = Icons.Filled.SportsSoccer,
                                                titulo = stringResource(R.string.academy_invite_role_coach),
                                                subtitulo = sub,
                                                onClick = { invitacionesSub = InvitacionesSubPantalla.RolEntrenador },
                                            )
                                        }
                                    }
                                    item {
                                        if (config.remoteAcademiaId != null) {
                                            val c = config.codigoInviteCoordinatorRemoto?.trim().orEmpty()
                                            val sub = if (c.isEmpty()) {
                                                stringResource(R.string.academy_invite_code_missing)
                                            } else {
                                                stringResource(R.string.academy_invite_role_subtitle_code, c)
                                            }
                                            AcademiaAjusteNavegacionFila(
                                                icon = Icons.Filled.ManageAccounts,
                                                titulo = stringResource(R.string.academy_invite_role_coordinator),
                                                subtitulo = sub,
                                                onClick = { invitacionesSub = InvitacionesSubPantalla.RolCoordinador },
                                            )
                                        }
                                    }
                                    item {
                                        if (config.remoteAcademiaId != null) {
                                            val c = config.codigoInviteParentRemoto?.trim().orEmpty()
                                            val sub = if (c.isEmpty()) {
                                                stringResource(R.string.academy_invite_code_missing)
                                            } else {
                                                stringResource(R.string.academy_invite_role_subtitle_code, c)
                                            }
                                            AcademiaAjusteNavegacionFila(
                                                icon = Icons.Filled.FamilyRestroom,
                                                titulo = stringResource(R.string.academy_invite_role_parent),
                                                subtitulo = sub,
                                                onClick = { invitacionesSub = InvitacionesSubPantalla.RolFamilias },
                                            )
                                        }
                                    }
                                    item {
                                        if (config.remoteAcademiaId != null) {
                                            AcademiaAjusteNavegacionFila(
                                                icon = Icons.Filled.Group,
                                                titulo = stringResource(R.string.members_manage_button),
                                                subtitulo = stringResource(R.string.members_manage_menu_subtitle),
                                                onClick = {
                                                    destinoAjusteAcademia = null
                                                    pantallaMiembros = true
                                                },
                                            )
                                        }
                                    }
                                }
                                InvitacionesSubPantalla.RolEntrenador -> {
                                    item {
                                        if (config.remoteAcademiaId != null) {
                                            InviteRoleCodeRow(
                                                label = stringResource(R.string.academy_invite_role_coach),
                                                code = config.codigoInviteCoachRemoto,
                                                academyName = config.nombreAcademia,
                                                target = InviteClubIntentHelper.InviteTarget.COACH,
                                                leadingIcon = Icons.Filled.SportsSoccer,
                                            )
                                        }
                                    }
                                }
                                InvitacionesSubPantalla.RolCoordinador -> {
                                    item {
                                        if (config.remoteAcademiaId != null) {
                                            InviteRoleCodeRow(
                                                label = stringResource(R.string.academy_invite_role_coordinator),
                                                code = config.codigoInviteCoordinatorRemoto,
                                                academyName = config.nombreAcademia,
                                                target = InviteClubIntentHelper.InviteTarget.COORDINATOR,
                                                leadingIcon = Icons.Filled.ManageAccounts,
                                            )
                                        }
                                    }
                                }
                                InvitacionesSubPantalla.RolFamilias -> {
                                    item {
                                        if (config.remoteAcademiaId != null) {
                                            InviteRoleCodeRow(
                                                label = stringResource(R.string.academy_invite_role_parent),
                                                code = config.codigoInviteParentRemoto,
                                                academyName = config.nombreAcademia,
                                                target = InviteClubIntentHelper.InviteTarget.PARENT,
                                                leadingIcon = Icons.Filled.FamilyRestroom,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        AcademiaDestinoAjuste.IdentidadClub -> {
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
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    brandingImageViewer = AcademiaBrandingImageViewer.Portada
                                },
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
                                .clip(CircleShape)
                                .clickable {
                                    brandingImageViewer = AcademiaBrandingImageViewer.Logo
                                },
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
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        configVm.guardarNombre(nombreAcademiaEdit) { ok ->
                            if (ok) {
                                viewRoot.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            }
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = if (ok) nombreGuardadoMsg else nombreDenegadoMsg,
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        }
                    },
                    enabled = nombreAcademiaEdit.isNotBlank(),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.save_academy_name))
                }
            }
                        }
                        AcademiaDestinoAjuste.TemaColores -> {
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
                        }
                        AcademiaDestinoAjuste.PagosPrivacidad -> {
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
                if (config.pinStaffHash != null && rolDispositivo.esPersonalClub()) {
                    TextButton(
                        onClick = {
                            pendienteTrasPin = null
                            modoDialogoPin = ModoDialogoPin.CAMBIAR
                        },
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text(stringResource(R.string.pin_change_action))
                    }
                }
                val puedeEditarDiaLimite = config.puedeMutarDiaLimitePagoMes(
                    sessionAuthUserId.takeIf { it.isNotBlank() },
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.academy_payment_deadline_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    stringResource(R.string.academy_payment_deadline_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                if (!puedeEditarDiaLimite) {
                    Text(
                        stringResource(R.string.academy_payment_deadline_owner_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    Text(
                        run {
                            val dia = config.diaLimitePagoMes
                            if (dia != null) {
                                stringResource(R.string.academy_payment_deadline_readonly_value, dia)
                            } else {
                                stringResource(R.string.academy_payment_deadline_readonly_none)
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                } else {
                    var diaLimiteTxt by remember { mutableStateOf("") }
                    LaunchedEffect(config.diaLimitePagoMes) {
                        diaLimiteTxt = config.diaLimitePagoMes?.toString().orEmpty()
                    }
                    OutlinedTextField(
                        value = diaLimiteTxt,
                        onValueChange = { v -> diaLimiteTxt = v.filter { it.isDigit() }.take(2) },
                        label = { Text(stringResource(R.string.academy_payment_deadline_label)) },
                        supportingText = { Text(stringResource(R.string.academy_payment_deadline_support)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                    )
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                diaLimiteTxt = ""
                                configVm.guardarDiaLimitePagoMes(null) { ok ->
                                    if (ok) {
                                        viewRoot.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    }
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = if (ok) diaLimiteQuitadoMsg else diaLimiteDenegadoMsg,
                                            duration = SnackbarDuration.Short,
                                        )
                                    }
                                }
                            },
                        ) {
                            Text(stringResource(R.string.academy_payment_deadline_clear))
                        }
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                when {
                                    diaLimiteTxt.isBlank() -> {
                                        configVm.guardarDiaLimitePagoMes(null) { ok ->
                                            if (ok) {
                                                viewRoot.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                            }
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = if (ok) {
                                                        diaLimiteQuitadoMsg
                                                    } else {
                                                        diaLimiteDenegadoMsg
                                                    },
                                                    duration = SnackbarDuration.Short,
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        val d = diaLimiteTxt.toIntOrNull()
                                        if (d != null && d in 1..28) {
                                            configVm.guardarDiaLimitePagoMes(d) { ok ->
                                                if (ok) {
                                                    viewRoot.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                }
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = if (ok) {
                                                            diaLimiteGuardadoMsg
                                                        } else {
                                                            diaLimiteDenegadoMsg
                                                        },
                                                        duration = SnackbarDuration.Short,
                                                    )
                                                }
                                            }
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = context.getString(R.string.academy_payment_deadline_invalid),
                                                    duration = SnackbarDuration.Short,
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                        ) {
                            Text(stringResource(R.string.academy_payment_deadline_save))
                        }
                    }
                }
            }
                        }
                        AcademiaDestinoAjuste.EquipoTecnico -> {
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
                        staffPrefillCoachMiembro = null
                        staffFormInstance++
                        dialogoStaff = true
                    },
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.add_staff))
                }
            }
            items(staff, key = { it.id }) { m ->
                val categoriasStaff by staffVm.categoriasFlowPara(m.id)
                    .collectAsState(initial = emptyList())
                StaffCard(
                    staff = m,
                    categorias = categoriasStaff,
                    onEdit = {
                        dialogoStaff = false
                        staffPrefillCoachMiembro = null
                        staffFormInstance++
                        staffEditar = m
                    },
                    onDelete = { staffEliminar = m },
                )
            }
                        }
                        AcademiaDestinoAjuste.CuentaUsuario -> {
                            val cerrarSesion = onSignOut
                            if (cerrarSesion != null) {
                                item {
                                    Column(Modifier.fillMaxWidth()) {
                                        AcademiaSeccionCuentaUsuario(
                                            config = config,
                                            authSession = authSession,
                                            cuentaPerfil = cuentaPerfil,
                                            authBusy = authBusy,
                                            mostrarDividerSuperior = false,
                                            onEditarPerfil = { dialogoPerfil = true },
                                            onSignOut = cerrarSesion,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (onSignOut != null && (!esPersonalClub || !puedeGestionarAcademia)) {
                item {
                    AcademiaSeccionCuentaUsuario(
                        config = config,
                        authSession = authSession,
                        cuentaPerfil = cuentaPerfil,
                        authBusy = authBusy,
                        mostrarDividerSuperior = true,
                        onEditarPerfil = { dialogoPerfil = true },
                        onSignOut = onSignOut,
                    )
                }
            }
        }
    }

        val aid = config.remoteAcademiaId
        if (pantallaMiembros && aid != null) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                AcademiaMiembrosAdminScreen(
                    academiaId = aid,
                    vm = miembrosVm,
                    onCerrar = { pantallaMiembros = false },
                )
            }
        }
        if (dialogoPerfil) {
            AlertDialog(
                onDismissRequest = { if (!authBusy) dialogoPerfil = false },
                title = { Text(stringResource(R.string.auth_edit_profile_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!perfilEmail.isNullOrBlank()) {
                            Text(
                                perfilEmail.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Text(
                            stringResource(R.string.auth_edit_profile_email_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedTextField(
                            value = perfilNombre,
                            onValueChange = { perfilNombre = it },
                            label = { Text(stringResource(R.string.auth_given_name)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Words,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = perfilApellido,
                            onValueChange = { perfilApellido = it },
                            label = { Text(stringResource(R.string.auth_family_name)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Words,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (authBusy) return@TextButton
                            authVm.updateProfile(perfilNombre, perfilApellido) { r ->
                                r.onSuccess {
                                    dialogoPerfil = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar(perfilGuardadoMsg)
                                    }
                                }
                                r.onFailure { e ->
                                    Toast.makeText(
                                        context,
                                        e.message ?: context.getString(R.string.auth_error_unexpected),
                                        Toast.LENGTH_LONG,
                                    ).show()
                                }
                            }
                        },
                        enabled = perfilNombre.isNotBlank() && perfilApellido.isNotBlank(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (authBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                            }
                            Text(stringResource(R.string.auth_profile_save))
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { dialogoPerfil = false },
                        enabled = !authBusy,
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }
    }

    if (dialogoStaff || staffEditar != null) {
        val categoriasDisponibles by staffVm.categoriasDisponibles.collectAsState()
        val categoriasIniciales by produceState(
            emptySet<String>(),
            staffEditar?.id,
            staffFormInstance,
            dialogoStaff,
        ) {
            val s = staffEditar
            value = if (s == null) emptySet() else staffVm.getCategoriasForStaffOnce(s.id).toSet()
        }
        key(staffFormInstance, staffEditar?.id, staffPrefillCoachMiembro?.id) {
            StaffFormDialog(
                staffExistente = staffEditar,
                categoriasDisponibles = categoriasDisponibles,
                initialCategoriasSeleccionadas = categoriasIniciales,
                prefillCoachMember = staffPrefillCoachMiembro.takeIf { staffEditar == null },
                onDismiss = {
                    dialogoStaff = false
                    staffEditar = null
                    staffPrefillCoachMiembro = null
                },
                onGuardar = { nombre, rol, tel, email, fotoPath, quitarFoto, sueldoMensual, categorias ->
                    val editando = staffEditar
                    if (editando != null) {
                        staffVm.actualizar(
                            editando,
                            nombre,
                            rol,
                            tel,
                            email,
                            fotoPath,
                            quitarFoto,
                            sueldoMensual,
                            categorias,
                        )
                    } else {
                        staffVm.agregar(nombre, rol, tel, email, fotoPath, sueldoMensual, categorias)
                    }
                    dialogoStaff = false
                    staffEditar = null
                    staffPrefillCoachMiembro = null
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

    brandingImageViewer?.let { kind ->
        val model = when (kind) {
            AcademiaBrandingImageViewer.Portada -> config.coilPortadaModel(context)
            AcademiaBrandingImageViewer.Logo -> config.coilLogoModel(context)
        }
        val titulo = when (kind) {
            AcademiaBrandingImageViewer.Portada ->
                stringResource(R.string.academy_cover_section)
            AcademiaBrandingImageViewer.Logo ->
                stringResource(R.string.academy_logo_section)
        }
        val cd = when (kind) {
            AcademiaBrandingImageViewer.Portada ->
                stringResource(R.string.academy_cover)
            AcademiaBrandingImageViewer.Logo ->
                stringResource(R.string.academy_logo_profile)
        }
        if (model != null) {
            FullscreenImageViewerDialog(
                titulo = titulo,
                imageModel = model,
                contentDescription = cd,
                onDismiss = { brandingImageViewer = null },
            )
        } else {
            LaunchedEffect(kind) { brandingImageViewer = null }
        }
    }
}

@Composable
private fun AcademiaAjusteNavegacionFila(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        ListItem(
            headlineContent = {
                Text(titulo, style = MaterialTheme.typography.titleSmall)
            },
            supportingContent = {
                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            },
            leadingContent = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            },
            trailingContent = {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }
}

@Composable
private fun AcademiaSeccionCuentaUsuario(
    config: AcademiaConfig,
    authSession: SessionStatus,
    cuentaPerfil: AuthProfileSnapshot?,
    authBusy: Boolean,
    mostrarDividerSuperior: Boolean,
    onEditarPerfil: () -> Unit,
    onSignOut: () -> Unit,
) {
    if (mostrarDividerSuperior) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
    }
    Text(
        stringResource(R.string.auth_account_section),
        style = MaterialTheme.typography.titleSmall,
    )
    if (authSession is SessionStatus.Authenticated) {
        Text(
            stringResource(R.string.auth_account_label_role),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp),
        )
        Text(
            stringResource(config.authAccountRoleLabelRes()),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
    if (authSession is SessionStatus.Authenticated && cuentaPerfil != null) {
        val nombreCompleto =
            "${cuentaPerfil.nombre} ${cuentaPerfil.apellido}".trim()
        if (nombreCompleto.isNotBlank()) {
            Text(
                stringResource(R.string.auth_account_label_name),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                nombreCompleto,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (!cuentaPerfil.email.isNullOrBlank()) {
            Text(
                stringResource(R.string.auth_account_label_email),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                cuentaPerfil.email.trim(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (nombreCompleto.isBlank() && cuentaPerfil.email.isNullOrBlank()) {
            Text(
                stringResource(R.string.auth_account_no_profile_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
    Text(
        stringResource(R.string.auth_account_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp),
    )
    OutlinedButton(
        onClick = onEditarPerfil,
        enabled = !authBusy,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        Text(stringResource(R.string.auth_edit_profile))
    }
    OutlinedButton(
        onClick = onSignOut,
        enabled = !authBusy,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Text(stringResource(R.string.auth_sign_out))
    }
}

/** Texto para «Tu cuenta» según `cloudMembresiaRol` y si la academia está enlazada en la nube. */
private fun AcademiaConfig.authAccountRoleLabelRes(): Int =
    when (cloudMembresiaRol?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotEmpty() }) {
        "parent" -> R.string.rol_padre_tutor
        "coach" -> R.string.members_rol_coach
        "coordinator" -> R.string.rol_coordinador
        "admin" -> R.string.members_rol_admin
        "owner" -> R.string.rol_dueno_academia
        null -> if (remoteAcademiaId == null) R.string.auth_account_role_local else R.string.auth_account_role_pending
        else -> R.string.auth_account_role_unknown
    }

@Composable
private fun StaffCard(
    staff: Staff,
    categorias: List<String>,
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
                    Text(
                        if (categorias.isNotEmpty()) {
                            categorias.joinToString(", ")
                        } else {
                            stringResource(R.string.staff_categories_card_empty)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (categorias.isNotEmpty()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                    )
                    staff.sueldoMensual?.takeIf { it > 0 }?.let { sm ->
                        Text(
                            NumberFormat.getCurrencyInstance(Locale.getDefault()).format(sm),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
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
    categoriasDisponibles: List<String>,
    initialCategoriasSeleccionadas: Set<String>,
    /** Relleno al crear Equipo desde la ficha del entrenador (miembro nube sin fila local). */
    prefillCoachMember: MiembroAdminUi?,
    onDismiss: () -> Unit,
    onGuardar: (
        nombre: String,
        rol: RolStaff,
        telefono: String?,
        email: String?,
        fotoRutaAbsoluta: String?,
        quitarFoto: Boolean,
        sueldoMensual: Double?,
        categorias: Set<String>,
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
    var sueldoTxt by remember { mutableStateOf("") }
    var cameraFile by remember { mutableStateOf<File?>(null) }
    var seleccionCategorias by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(staffExistente, prefillCoachMember, initialCategoriasSeleccionadas, categoriasDisponibles) {
        sinFoto = false
        val s = staffExistente
        val coach = prefillCoachMember
        val catsDisponibles = categoriasDisponibles.toSet()
        when {
            s != null -> {
                nombre = s.nombre
                rol = RolStaff.fromStored(s.rol)
                tel = s.telefono.orEmpty()
                email = s.email.orEmpty()
                fotoPath = s.fotoRutaAbsoluta
                sueldoTxt = s.sueldoMensual?.takeIf { it > 0 }?.toString().orEmpty()
                seleccionCategorias = initialCategoriasSeleccionadas.toSet()
            }
            coach != null -> {
                nombre = coach.displayLabel?.trim()?.takeIf { it.isNotEmpty() }
                    ?: coach.memberEmail?.substringBefore("@")?.trim().orEmpty()
                rol = RolStaff.PROFESOR
                tel = ""
                email = coach.memberEmail?.trim().orEmpty()
                fotoPath = null
                sueldoTxt = ""
                seleccionCategorias = coach.nombresCategoriasCoach
                    .filter { it in catsDisponibles }
                    .toSet()
                    .ifEmpty { initialCategoriasSeleccionadas.toSet() }
            }
            else -> {
                nombre = ""
                rol = RolStaff.PROFESOR
                tel = ""
                email = ""
                fotoPath = null
                sueldoTxt = ""
                seleccionCategorias = initialCategoriasSeleccionadas.toSet()
            }
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
                        when {
                            staffExistente != null -> R.string.edit_staff
                            prefillCoachMember != null -> R.string.members_coach_ficha_dialog_title
                            else -> R.string.add_staff
                        },
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
                    Text(
                        stringResource(R.string.staff_categories_label),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        stringResource(R.string.staff_categories_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val chipScroll = rememberScrollState()
                    if (categoriasDisponibles.isEmpty()) {
                        Text(
                            stringResource(R.string.staff_categories_none_created),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(chipScroll),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            categoriasDisponibles.forEach { cat ->
                                FilterChip(
                                    selected = cat in seleccionCategorias,
                                    onClick = {
                                        seleccionCategorias =
                                            if (cat in seleccionCategorias) {
                                                seleccionCategorias - cat
                                            } else {
                                                seleccionCategorias + cat
                                            }
                                    },
                                    label = { Text(cat, maxLines = 1) },
                                )
                            }
                        }
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
                    OutlinedTextField(
                        value = sueldoTxt,
                        onValueChange = { sueldoTxt = it },
                        label = { Text(stringResource(R.string.staff_monthly_salary)) },
                        placeholder = { Text(stringResource(R.string.staff_monthly_salary_hint)) },
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
                        onClick = {
                            val sueldo = sueldoTxt.trim().replace(',', '.').toDoubleOrNull()
                            onGuardar(
                                nombre,
                                rol,
                                tel,
                                email,
                                fotoPath,
                                sinFoto,
                                sueldo,
                                seleccionCategorias,
                            )
                        },
                        enabled = nombre.isNotBlank(),
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

private val InviteRoleButtonPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)

@Composable
private fun InviteRoleCodeRow(
    label: String,
    code: String?,
    academyName: String,
    target: InviteClubIntentHelper.InviteTarget,
    leadingIcon: ImageVector,
) {
    val context = LocalContext.current
    val c = code?.trim().orEmpty()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            leadingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (c.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                    Text(
                        text = c,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            InviteClubIntentHelper.shareInviteTextForTarget(
                                context,
                                academyName,
                                c,
                                target,
                            )
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = InviteRoleButtonPadding,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.academy_club_code_share),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                        )
                    }
                    OutlinedButton(
                        onClick = { InviteClubIntentHelper.copyCode(context, c) },
                        modifier = Modifier.weight(1f),
                        contentPadding = InviteRoleButtonPadding,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.academy_club_code_copy),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                        )
                    }
                }
                OutlinedButton(
                    onClick = {
                        InviteClubIntentHelper.openEmailDraftForTarget(
                            context,
                            academyName,
                            c,
                            target,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = InviteRoleButtonPadding,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(
                        Icons.Filled.Email,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.academy_club_email_invite))
                }
            } else {
                Text(
                    stringResource(R.string.academy_invite_code_missing),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Academia en pestaña inferior para padre/tutor en nube: sin staff, invitaciones ni ajustes del club. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AcademiaPadreNubeSimpleScreen(
    config: AcademiaConfig,
    onSignOut: (() -> Unit)?,
) {
    val scroll = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.tab_academy)) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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
                        stringResource(R.string.academy_readonly_family_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        config.nombreAcademia,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        stringResource(R.string.academy_parent_cloud_tab_intro),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (onSignOut != null) {
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.auth_sign_out))
                }
            }
        }
    }
}
