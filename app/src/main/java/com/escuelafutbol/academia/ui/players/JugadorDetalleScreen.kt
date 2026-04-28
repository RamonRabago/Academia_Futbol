package com.escuelafutbol.academia.ui.players

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.PrimaryButton
import com.escuelafutbol.academia.ui.design.SectionHeader
import com.escuelafutbol.academia.ui.util.coilFotoModel
import com.escuelafutbol.academia.ui.util.formatearFechaCalendarioUtc
import com.escuelafutbol.academia.ui.util.formatearFechaDiaLocal
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneOffset

/** Rutas del `NavHost` principal (deben coincidir con `AcademiaRoot` Tab.route). */
private const val RUTA_TAB_ASISTENCIA = "asistencia"
private const val RUTA_TAB_FINANZAS = "finanzas"
private const val RUTA_TAB_ESTADISTICAS = "estadisticas"

/** Edad en años a partir de fecha de nacimiento UTC o, si falta, del año de nacimiento. */
private fun edadJugadorAnios(jugador: Jugador): Int? {
    jugador.fechaNacimientoMillis?.let { ms ->
        val birth = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()
        val today = LocalDate.now(ZoneOffset.UTC)
        if (birth.isAfter(today)) return null
        return Period.between(birth, today).years
    }
    val y = jugador.anioNacimiento ?: return null
    val currentYear = LocalDate.now(ZoneOffset.UTC).year
    val age = currentYear - y
    return if (age in 0..120) age else null
}

/** Solo dígitos para `https://wa.me/{numero}` (incluye lada). */
private fun digitosSoloParaWaMe(texto: String): String? {
    val d = texto.filter { it.isDigit() }
    return d.takeIf { it.isNotEmpty() }
}

private fun abrirWhatsAppWaMe(context: Context, telefonoTutor: String) {
    val digitos = digitosSoloParaWaMe(telefonoTutor) ?: return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digitos"))
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
    }
}

private fun abrirEmailTutor(context: Context, email: String) {
    val limpio = email.trim()
    if (limpio.isEmpty()) return
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.fromParts("mailto", limpio, null)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JugadorDetalleScreen(
    jugador: Jugador,
    onBack: () -> Unit,
    onEditar: () -> Unit,
    onNavigateToTab: (route: String) -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scroll = rememberScrollState()
    val fotoModel = remember(jugador.fotoUrlSupabase, jugador.fotoRutaAbsoluta) {
        jugador.coilFotoModel(context)
    }
    val edad = remember(jugador.fechaNacimientoMillis, jugador.anioNacimiento) {
        edadJugadorAnios(jugador)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        jugador.nombre,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.player_detail_back_cd),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AcademiaDimens.paddingScreenHorizontal)
                .verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingListSection),
        ) {
            Spacer(Modifier.padding(top = AcademiaDimens.gapSm))
            Box(
                modifier = Modifier
                    .size(AcademiaDimens.avatarFormHero + AcademiaDimens.gapMd * 2)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (fotoModel != null) {
                    AsyncImage(
                        model = fotoModel,
                        contentDescription = stringResource(R.string.player_photo_cd),
                        modifier = Modifier
                            .size(AcademiaDimens.avatarFormHero)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(AcademiaDimens.iconSizeMd * 2),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = jugador.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = jugador.categoria,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            edad?.let { años ->
                Text(
                    text = stringResource(R.string.player_detail_age_years, años),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            PrimaryButton(
                text = stringResource(R.string.player_detail_edit),
                onClick = onEditar,
                modifier = Modifier.fillMaxWidth(),
            )
            SectionHeader(title = stringResource(R.string.player_detail_quick_actions))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapSm),
            ) {
                OutlinedButton(
                    onClick = {
                        onNavigateToTab(RUTA_TAB_ASISTENCIA)
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AcademiaDimens.gapMicro),
                ) {
                    Text(stringResource(R.string.player_detail_action_attendance))
                }
                OutlinedButton(
                    onClick = {
                        onNavigateToTab(RUTA_TAB_FINANZAS)
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AcademiaDimens.gapMicro),
                ) {
                    Text(stringResource(R.string.player_detail_action_finance))
                }
                OutlinedButton(
                    onClick = {
                        onNavigateToTab(RUTA_TAB_ESTADISTICAS)
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AcademiaDimens.gapMicro),
                ) {
                    Text(stringResource(R.string.player_detail_action_stats))
                }
            }
            SectionHeader(title = stringResource(R.string.player_detail_info_section))
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd)) {
                    val telefonoTutor =
                        jugador.telefonoTutor?.trim()?.takeIf { it.isNotEmpty() }
                    val digitosWa = telefonoTutor?.let(::digitosSoloParaWaMe)
                    val emailTutor =
                        jugador.emailTutor?.trim()?.takeIf { it.isNotEmpty() }
                    DetalleCampo(
                        label = stringResource(R.string.birth_date),
                        value = jugador.fechaNacimientoMillis?.let { formatearFechaCalendarioUtc(it) }
                            ?: stringResource(R.string.player_detail_not_available),
                    )
                    DetalleCampo(
                        label = stringResource(R.string.player_curp),
                        value = jugador.curp?.trim()?.takeIf { it.isNotEmpty() }
                            ?: stringResource(R.string.player_detail_not_available),
                    )
                    DetalleCampo(
                        label = stringResource(R.string.parent_phone),
                        value = telefonoTutor
                            ?: stringResource(R.string.player_detail_not_available),
                        trailing = if (telefonoTutor != null && digitosWa != null) {
                            {
                                IconButton(
                                    onClick = { abrirWhatsAppWaMe(context, telefonoTutor) },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Phone,
                                        contentDescription = stringResource(R.string.player_detail_whatsapp_cd),
                                        modifier = Modifier.size(AcademiaDimens.iconSizeSm),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        } else {
                            null
                        },
                    )
                    DetalleCampo(
                        label = stringResource(R.string.parent_email),
                        value = emailTutor
                            ?: stringResource(R.string.player_detail_not_available),
                        trailing = if (emailTutor != null) {
                            {
                                IconButton(
                                    onClick = { abrirEmailTutor(context, emailTutor) },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Email,
                                        contentDescription = stringResource(R.string.player_detail_email_cd),
                                        modifier = Modifier.size(AcademiaDimens.iconSizeSm),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        } else {
                            null
                        },
                    )
                }
            }
            SectionHeader(title = stringResource(R.string.player_detail_registration_section))
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd)) {
                    val fechaAltaOk = jugador.fechaAltaMillis > 0L
                    DetalleCampo(
                        label = stringResource(R.string.player_detail_registration_join_date),
                        value = if (fechaAltaOk) {
                            formatearFechaDiaLocal(jugador.fechaAltaMillis)
                        } else {
                            stringResource(R.string.player_detail_not_available)
                        },
                    )
                    val nombreAlta =
                        jugador.altaPorNombre?.trim()?.takeIf { it.isNotEmpty() }
                    val tieneAltaPorUid = !jugador.altaPorUserId.isNullOrBlank()
                    if (nombreAlta != null || tieneAltaPorUid) {
                        DetalleCampo(
                            label = stringResource(R.string.player_detail_registered_by_label),
                            value = nombreAlta
                                ?: stringResource(R.string.player_detail_registered_by_legacy),
                        )
                    }
                    val fechaBajaMs = jugador.fechaBajaMillis
                    val estadoValor = when {
                        jugador.activo ->
                            stringResource(R.string.player_detail_status_active)
                        fechaBajaMs != null && fechaBajaMs > 0L ->
                            stringResource(
                                R.string.player_detail_status_discharged_from,
                                formatearFechaDiaLocal(fechaBajaMs),
                            )
                        else ->
                            stringResource(R.string.player_detail_status_discharged)
                    }
                    DetalleCampo(
                        label = stringResource(R.string.player_detail_registration_status),
                        value = estadoValor,
                    )
                }
            }
            val notas = jugador.notas?.trim().orEmpty()
            if (notas.isNotEmpty()) {
                AppCard {
                    SectionHeader(title = stringResource(R.string.notes))
                    Text(
                        notas,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(Modifier.padding(bottom = AcademiaDimens.paddingCard))
        }
    }
}

@Composable
private fun DetalleCampo(
    label: String,
    value: String,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMicro),
        ) {
            Text(
                value,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            trailing?.invoke(this)
        }
    }
}
