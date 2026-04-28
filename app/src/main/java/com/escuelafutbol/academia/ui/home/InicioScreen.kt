package com.escuelafutbol.academia.ui.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.Categoria
import com.escuelafutbol.academia.data.local.model.normalizarClaveCategoriaNombre
import com.escuelafutbol.academia.ui.util.FullscreenImageViewerDialog
import com.escuelafutbol.academia.ui.util.coilFotoJugadorModel
import com.escuelafutbol.academia.ui.util.coilLogoModel
import com.escuelafutbol.academia.ui.util.coilPortadaCategoriaModel
import com.escuelafutbol.academia.ui.util.coilPortadaModel
import com.escuelafutbol.academia.ui.design.AcademiaContextBanner
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.AppTintedPanel
import com.escuelafutbol.academia.ui.design.PrimaryButton
import com.escuelafutbol.academia.ui.design.SectionHeader
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Opción del selector de portada por hijo (Inicio, rol padre). */
data class InicioPadrePortadaOpcion(
    val jugadorRemoteId: String,
    val nombre: String,
    val fotoUrlSupabase: String?,
    val fotoRutaAbsoluta: String?,
)

data class InicioCumpleaneroUi(
    val nombre: String,
    val categoria: String,
    val fechaNacimientoMillis: Long?,
    val telefonoContacto: String? = null,
    val fotoUrlSupabase: String? = null,
    val fotoRutaAbsoluta: String? = null,
)

private sealed class InicioImageViewer {
    data object Logo : InicioImageViewer()
    data object PortadaAcademia : InicioImageViewer()
    data class PortadaCategoria(val nombre: String) : InicioImageViewer()
}

@Composable
fun InicioScreen(
    config: AcademiaConfig,
    /** Si hay filtro de categoría y tiene portada propia, se muestra primero; si no, la portada de la academia. */
    categoriaPortada: Categoria? = null,
    categoriaEtiqueta: String,
    /** Si devuelve false, no se muestra la tarjeta de acceso rápido a esa ruta (p. ej. padre en nube). */
    accesoRapidoVisible: (route: String) -> Boolean = { true },
    /** Nombre o correo de la cuenta (Supabase); null si no hay dato. */
    sesionEtiqueta: String? = null,
    /** Padre/tutor en nube: sin chip de categoría de staff ni datos operativos de categoría en cabecera. */
    vistaPadreEnNube: Boolean = false,
    /** Solo si hay más de un hijo con `remoteId`; vacío = sin selector. */
    parentPortadaSelectorOpciones: List<InicioPadrePortadaOpcion> = emptyList(),
    /** Valor actual de [SessionViewModel.parentInicioPortadaJugadorRemoteId]. */
    parentPortadaJugadorRemoteIdSeleccionado: String? = null,
    onParentPortadaJugadorRemoteIdChange: (String) -> Unit = {},
    /** Staff: sin filas en la tabla de categorías (Room). */
    mostrarGuiaStaffSinCategorias: Boolean = false,
    /** Staff: hay categorías pero aún no hay jugadores (Room). */
    mostrarGuiaStaffSinJugadores: Boolean = false,
    cumpleanerosHoy: List<InicioCumpleaneroUi> = emptyList(),
    onOpenCumpleanosClub: () -> Unit = {},
    onNavigate: (route: String) -> Unit,
) {
    val context = LocalContext.current
    var imageViewer by remember { mutableStateOf<InicioImageViewer?>(null) }
    val portadaCategoriaModel = categoriaPortada?.coilPortadaCategoriaModel(context)
    val portadaAcademiaModel = config.coilPortadaModel(context)
    val portadaMostrada = portadaCategoriaModel ?: portadaAcademiaModel
    val saludoSesion =
        if (sesionEtiqueta.isNullOrBlank()) null
        else stringResource(R.string.home_welcome_user, sesionEtiqueta)
    val atajosVisibles = ACCESOS_RAPIDOS_INICIO.filter { accesoRapidoVisible(it.route) }
    val primaryPadresVisible = vistaPadreEnNube && atajosVisibles.any { it.route == "padres" }
    val atajosLista = if (primaryPadresVisible) {
        atajosVisibles.filter { it.route != "padres" }
    } else {
        atajosVisibles
    }
    val coverH = AcademiaDimens.homeHeroCoverHeight
    val avatarSize = AcademiaDimens.homeHeroLogoSize
    val overlap = AcademiaDimens.homeHeroLogoOverlap
    val headerTotal = coverH + overlap
    val bienvenidaEnHero = vistaPadreEnNube && saludoSesion != null
    val abrirVisorPortada: () -> Unit = {
        imageViewer = if (portadaCategoriaModel != null) {
            InicioImageViewer.PortadaCategoria(
                checkNotNull(categoriaPortada).nombre,
            )
        } else {
            InicioImageViewer.PortadaAcademia
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = AcademiaDimens.paddingCard + AcademiaDimens.spacingDialogBlock,
        ),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerTotal),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(coverH)
                        .align(Alignment.TopCenter)
                        .clickable(
                            enabled = portadaMostrada != null,
                            onClick = abrirVisorPortada,
                        ),
                ) {
                    if (portadaMostrada != null) {
                        AsyncImage(
                            model = portadaMostrada,
                            contentDescription = stringResource(R.string.academy_cover),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                if (portadaMostrada != null) {
                                    Color.Black.copy(alpha = 0.30f)
                                } else {
                                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f)
                                },
                            ),
                    )
                    if (bienvenidaEnHero) {
                        AppTintedPanel(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .padding(
                                    horizontal = AcademiaDimens.paddingScreenHorizontal,
                                    vertical = AcademiaDimens.paddingCardCompact,
                                ),
                            shape = RoundedCornerShape(AcademiaDimens.radiusMd),
                            containerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.52f),
                            contentPadding = PaddingValues(
                                horizontal = AcademiaDimens.paddingCardCompact,
                                vertical = AcademiaDimens.gapMd,
                            ),
                        ) {
                            Text(
                                text = checkNotNull(saludoSesion),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
                val logoModel = config.coilLogoModel(context)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(avatarSize)
                        .clip(CircleShape)
                        .border(
                            width = AcademiaDimens.gapSm,
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape,
                        ),
                ) {
                    if (logoModel != null) {
                        AsyncImage(
                            model = logoModel,
                            contentDescription = stringResource(R.string.academy_logo_profile),
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { imageViewer = InicioImageViewer.Logo },
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
                                stringResource(R.string.academy_logo_placeholder_short),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(AcademiaDimens.gapMd),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(AcademiaDimens.gapMd))
            if (primaryPadresVisible) {
                PrimaryButton(
                    text = stringResource(R.string.home_parent_open_padres_button),
                    onClick = { onNavigate("padres") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
                )
            }
            Spacer(
                Modifier.height(
                    if (primaryPadresVisible) {
                        AcademiaDimens.paddingCard + AcademiaDimens.gapSm
                    } else {
                        AcademiaDimens.chipSpacing + AcademiaDimens.gapMicro
                    },
                ),
            )
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
                elevated = false,
                containerColor = if (vistaPadreEnNube) {
                    MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.65f)
                } else {
                    null
                },
                borderColor = if (vistaPadreEnNube) {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                } else {
                    null
                },
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AcademiaDimens.chipSpacing),
                ) {
                    if (vistaPadreEnNube) {
                        SectionHeader(
                            title = stringResource(R.string.home_inicio_padre_title),
                            subtitle = stringResource(R.string.home_inicio_padre_subtitle),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        SectionHeader(
                            title = stringResource(R.string.home_inicio_staff_title),
                            subtitle = stringResource(R.string.home_inicio_staff_subtitle, categoriaEtiqueta),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Text(
                        config.nombreAcademia,
                        style = if (vistaPadreEnNube) {
                            MaterialTheme.typography.titleLarge
                        } else {
                            MaterialTheme.typography.headlineSmall
                        },
                        fontWeight = if (vistaPadreEnNube) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (saludoSesion != null && !bienvenidaEnHero) {
                        Text(
                            saludoSesion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = AcademiaDimens.gapVerticalTight),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (vistaPadreEnNube) {
                        AcademiaContextBanner(
                            contextText = stringResource(R.string.home_parent_scope_badge),
                            emphasize = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            AppTintedPanel(
                                modifier = Modifier.wrapContentWidth(),
                                shape = RoundedCornerShape(AcademiaDimens.radiusXl),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                                contentPadding = PaddingValues(
                                    horizontal = AcademiaDimens.paddingCardCompact,
                                    vertical = AcademiaDimens.gapMd,
                                ),
                            ) {
                                Text(
                                    text = stringResource(R.string.working_in, categoriaEtiqueta),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                    if (vistaPadreEnNube && parentPortadaSelectorOpciones.size > 1) {
                        InicioParentPortadaSelector(
                            opciones = parentPortadaSelectorOpciones,
                            remoteIdSeleccionado = parentPortadaJugadorRemoteIdSeleccionado,
                            onRemoteIdChange = onParentPortadaJugadorRemoteIdChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = AcademiaDimens.gapSm),
                        )
                    }
                    if (vistaPadreEnNube) {
                        AppTintedPanel(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AcademiaDimens.radiusMd),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                            contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
                        ) {
                            Text(
                                stringResource(R.string.home_parent_quick_hint_padres),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            val cumpleanerosHoyFiltrados = remember(cumpleanerosHoy) {
                cumpleanerosHoy
                    .filter { esCumpleHoy(it.fechaNacimientoMillis) }
                    .sortedBy { it.nombre.lowercase() }
            }
            val proximosCumpleaneros = remember(cumpleanerosHoy) {
                val hoy = LocalDate.now(ZoneId.systemDefault())
                cumpleanerosHoy
                    .filter { !esCumpleHoy(it.fechaNacimientoMillis) }
                    .mapNotNull { item ->
                        val dias = diasHastaCumple(item.fechaNacimientoMillis, hoy) ?: return@mapNotNull null
                        if (dias in 1..7) item to dias else null
                    }
                    .sortedBy { it.second }
                    .map { it.first }
            }
            if (cumpleanerosHoyFiltrados.isNotEmpty()) {
                Spacer(Modifier.height(AcademiaDimens.spacingListSection))
                InicioCumpleanosCard(
                    cumpleanerosHoy = cumpleanerosHoyFiltrados,
                    vistaPadreEnNube = vistaPadreEnNube,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
                )
            }
            if (proximosCumpleaneros.isNotEmpty()) {
                Spacer(Modifier.height(AcademiaDimens.spacingListSection))
                InicioProximosCumpleanosCard(
                    proximosCumpleaneros = proximosCumpleaneros,
                    vistaPadreEnNube = vistaPadreEnNube,
                    onOpenCumpleanosClub = onOpenCumpleanosClub,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
                )
            }
        }
        if (!vistaPadreEnNube && mostrarGuiaStaffSinCategorias) {
            item {
                InicioStaffOnboardingSinCategoriasPanel(
                    onNavigate = onNavigate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
                )
            }
        }
        if (!vistaPadreEnNube && mostrarGuiaStaffSinJugadores) {
            item {
                InicioStaffOnboardingSinJugadoresPanel(
                    onNavigate = onNavigate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
                )
            }
        }
        if (atajosLista.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(R.string.home_shortcuts_section_title),
                    subtitle = if (vistaPadreEnNube) {
                        stringResource(R.string.home_shortcuts_section_subtitle_parent)
                    } else {
                        stringResource(R.string.home_shortcuts_section_subtitle_staff)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AcademiaDimens.paddingScreenHorizontal)
                        .padding(
                            top = if (primaryPadresVisible) {
                                AcademiaDimens.paddingCard + AcademiaDimens.gapMd
                            } else {
                                AcademiaDimens.paddingCard
                            },
                            bottom = AcademiaDimens.gapSm,
                        ),
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(AcademiaDimens.chipSpacing),
                ) {
                    for (def in atajosLista) {
                        AccesoRapidoCard(
                            titulo = stringResource(def.titleRes),
                            icono = def.icon,
                            onClick = { onNavigate(def.route) },
                        )
                    }
                }
            }
        }
    }

    imageViewer?.let { v ->
        val model = when (v) {
            InicioImageViewer.Logo -> config.coilLogoModel(context)
            InicioImageViewer.PortadaAcademia -> config.coilPortadaModel(context)
            is InicioImageViewer.PortadaCategoria ->
                categoriaPortada?.takeIf {
                    normalizarClaveCategoriaNombre(it.nombre) ==
                        normalizarClaveCategoriaNombre(v.nombre)
                }?.coilPortadaCategoriaModel(context)
        }
        val titulo = when (v) {
            InicioImageViewer.Logo -> stringResource(R.string.academy_logo_section)
            InicioImageViewer.PortadaAcademia -> stringResource(R.string.academy_cover_section)
            is InicioImageViewer.PortadaCategoria -> stringResource(
                R.string.category_cover_viewer_title,
                v.nombre,
            )
        }
        val cd = when (v) {
            InicioImageViewer.Logo -> stringResource(R.string.academy_logo_profile)
            InicioImageViewer.PortadaAcademia -> stringResource(R.string.academy_cover)
            is InicioImageViewer.PortadaCategoria ->
                stringResource(R.string.player_photo_tap_to_expand)
        }
        if (model != null) {
            FullscreenImageViewerDialog(
                titulo = titulo,
                imageModel = model,
                contentDescription = cd,
                onDismiss = { imageViewer = null },
            )
        } else {
            LaunchedEffect(v) { imageViewer = null }
        }
    }
}

@Composable
private fun InicioCumpleanosCard(
    cumpleanerosHoy: List<InicioCumpleaneroUi>,
    vistaPadreEnNube: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hoy = remember { LocalDate.now(ZoneId.systemDefault()) }
    AppCard(
        modifier = modifier,
        elevated = false,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.32f),
        borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.24f),
    ) {
        SectionHeader(
            title = if (vistaPadreEnNube) {
                stringResource(R.string.birthday_parent_title)
            } else {
                stringResource(R.string.birthday_staff_title)
            },
            subtitle = if (vistaPadreEnNube) {
                stringResource(R.string.birthday_parent_subtitle)
            } else {
                stringResource(R.string.birthday_staff_subtitle)
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(AcademiaDimens.gapMd))
        if (vistaPadreEnNube) {
            val principal = cumpleanerosHoy.first()
            AppTintedPanel(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AcademiaDimens.radiusMd),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
            ) {
                Text(
                    text = stringResource(R.string.birthday_parent_message_title, principal.nombre),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.birthday_parent_message_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = AcademiaDimens.gapSm),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = {
                            if (!abrirWhatsappCumple(
                                    context = context,
                                    telefonoRaw = principal.telefonoContacto,
                                    nombre = principal.nombre,
                                )
                            ) {
                                Toast.makeText(
                                    context,
                                    context.getString(
                                        R.string.birthday_action_message_prefilled,
                                        principal.nombre,
                                    ),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        },
                    ) {
                        Text(text = stringResource(R.string.birthday_parent_action_send))
                    }
                }
                if (cumpleanerosHoy.size > 1) {
                    Text(
                        text = stringResource(
                            R.string.birthday_parent_more_children,
                            cumpleanerosHoy.size - 1,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = AcademiaDimens.gapSm),
                    )
                }
            }
        } else {
            cumpleanerosHoy.forEach { alumno ->
                val edad = calcularEdad(alumnoNacimientoMillis = alumno.fechaNacimientoMillis, hoy = hoy)
                val detalle = if (edad != null && edad >= 0) {
                    stringResource(
                        R.string.birthday_staff_item_with_age,
                        alumno.nombre,
                        alumno.categoria,
                        edad,
                    )
                } else {
                    stringResource(
                        R.string.birthday_staff_item_without_age,
                        alumno.nombre,
                        alumno.categoria,
                    )
                }
                AppTintedPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = AcademiaDimens.gapSm),
                    shape = RoundedCornerShape(AcademiaDimens.radiusMd),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.70f),
                    contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
                ) {
                    Text(
                        text = detalle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = {
                                if (!abrirWhatsappCumple(
                                        context = context,
                                        telefonoRaw = alumno.telefonoContacto,
                                        nombre = alumno.nombre,
                                    )
                                ) {
                                    Toast.makeText(
                                        context,
                                        context.getString(
                                            R.string.birthday_action_message_prefilled,
                                            alumno.nombre,
                                        ),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            },
                        ) {
                            Text(text = stringResource(R.string.birthday_staff_action))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InicioProximosCumpleanosCard(
    proximosCumpleaneros: List<InicioCumpleaneroUi>,
    vistaPadreEnNube: Boolean,
    onOpenCumpleanosClub: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hoy = remember { LocalDate.now(ZoneId.systemDefault()) }
    var rangoDias by remember { mutableStateOf(7) }
    val proximosFiltrados = remember(proximosCumpleaneros, rangoDias, hoy) {
        proximosCumpleaneros
            .mapNotNull { alumno ->
                val proximoCumple = proximaFechaCumple(alumno.fechaNacimientoMillis, hoy) ?: return@mapNotNull null
                val dias = java.time.temporal.ChronoUnit.DAYS.between(hoy, proximoCumple).toInt()
                if (dias !in 1..rangoDias) return@mapNotNull null
                val edad = edadQueCumplira(alumno.fechaNacimientoMillis, proximoCumple)
                Triple(alumno, proximoCumple, edad)
            }
            .sortedBy { it.second }
    }
    if (proximosFiltrados.isEmpty()) return
    val maxVisible = 4
    val itemsVisibles = proximosFiltrados.take(maxVisible)
    val restantes = (proximosFiltrados.size - itemsVisibles.size).coerceAtLeast(0)
    AppCard(
        modifier = modifier,
        elevated = false,
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.26f),
        borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.20f),
    ) {
        SectionHeader(
            title = stringResource(R.string.birthday_upcoming_title_days, rangoDias),
            subtitle = if (vistaPadreEnNube) {
                stringResource(R.string.birthday_upcoming_subtitle_parent)
            } else {
                stringResource(R.string.birthday_upcoming_subtitle_staff)
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(AcademiaDimens.gapMd))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = { rangoDias = 7 }, enabled = rangoDias != 7) {
                Text(text = stringResource(R.string.birthday_upcoming_range_7))
            }
            TextButton(onClick = { rangoDias = 30 }, enabled = rangoDias != 30) {
                Text(text = stringResource(R.string.birthday_upcoming_range_30))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AcademiaDimens.gapSm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onOpenCumpleanosClub) {
                Text(text = stringResource(R.string.birthday_upcoming_open_calendar))
            }
            if (restantes > 0) {
                TextButton(onClick = onOpenCumpleanosClub) {
                    Text(text = stringResource(R.string.birthday_upcoming_more_count, restantes))
                }
            }
        }
        itemsVisibles.forEach { (alumno, fechaCumple, edadCumplira) ->
            val fechaTexto = remember(fechaCumple) {
                val meses = listOf(
                    "ene", "feb", "mar", "abr", "may", "jun",
                    "jul", "ago", "sep", "oct", "nov", "dic",
                )
                "${fechaCumple.dayOfMonth} ${meses[fechaCumple.monthValue - 1]}"
            }
            AppTintedPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AcademiaDimens.gapSm),
                shape = RoundedCornerShape(AcademiaDimens.radiusMd),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
                contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                ) {
                    val fotoModel = coilFotoJugadorModel(
                        context,
                        alumno.fotoUrlSupabase,
                        alumno.fotoRutaAbsoluta,
                    )
                    Box(
                        modifier = Modifier
                            .size(AcademiaDimens.avatarResultRow)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (fotoModel != null) {
                            AsyncImage(
                                model = fotoModel,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(AcademiaDimens.iconSizeSm),
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alumno.nombre,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = alumno.categoria,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = fechaTexto,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        edadCumplira?.let { edad ->
                            Text(
                                text = stringResource(R.string.birthday_upcoming_turning_age, edad),
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

private fun esCumpleHoy(fechaNacimientoMillis: Long?): Boolean {
    if (fechaNacimientoMillis == null) return false
    val zone = ZoneId.systemDefault()
    val nacimiento = Instant.ofEpochMilli(fechaNacimientoMillis).atZone(zone).toLocalDate()
    val hoy = LocalDate.now(zone)
    return nacimiento.month == hoy.month && nacimiento.dayOfMonth == hoy.dayOfMonth
}

private fun diasHastaCumple(fechaNacimientoMillis: Long?, hoy: LocalDate): Int? {
    if (fechaNacimientoMillis == null) return null
    val nacimiento = Instant.ofEpochMilli(fechaNacimientoMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    var siguiente = nacimiento.withYear(hoy.year)
    if (siguiente.isBefore(hoy) || siguiente.isEqual(hoy)) {
        siguiente = siguiente.plusYears(1)
    }
    return java.time.temporal.ChronoUnit.DAYS.between(hoy, siguiente).toInt()
}

private fun proximaFechaCumple(fechaNacimientoMillis: Long?, hoy: LocalDate): LocalDate? {
    if (fechaNacimientoMillis == null) return null
    val nacimiento = Instant.ofEpochMilli(fechaNacimientoMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    var siguiente = nacimiento.withYear(hoy.year)
    if (!siguiente.isAfter(hoy)) siguiente = siguiente.plusYears(1)
    return siguiente
}

private fun edadQueCumplira(fechaNacimientoMillis: Long?, proximoCumple: LocalDate): Int? {
    if (fechaNacimientoMillis == null) return null
    val nacimiento = Instant.ofEpochMilli(fechaNacimientoMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val edad = proximoCumple.year - nacimiento.year
    return edad.takeIf { it >= 0 }
}

private fun calcularEdad(alumnoNacimientoMillis: Long?, hoy: LocalDate): Int? {
    if (alumnoNacimientoMillis == null) return null
    val nacimiento = Instant.ofEpochMilli(alumnoNacimientoMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return hoy.year - nacimiento.year
}

private fun abrirWhatsappCumple(
    context: android.content.Context,
    telefonoRaw: String?,
    nombre: String,
): Boolean {
    val digitos = telefonoRaw
        ?.filter { it.isDigit() }
        ?.takeIf { it.length >= 8 }
        ?: return false
    val mensaje = context.getString(R.string.birthday_action_message_prefilled, nombre)
    val url = "https://wa.me/$digitos?text=${Uri.encode(mensaje)}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val puedeAbrir = intent.resolveActivity(context.packageManager) != null
    if (!puedeAbrir) return false
    return runCatching {
        context.startActivity(intent)
        true
    }.getOrDefault(false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InicioParentPortadaSelector(
    opciones: List<InicioPadrePortadaOpcion>,
    remoteIdSeleccionado: String?,
    onRemoteIdChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val seleccionActual = opciones.firstOrNull { op ->
        op.jugadorRemoteId.equals(remoteIdSeleccionado, ignoreCase = true)
    } ?: opciones.first()
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.semantics {
            contentDescription = context.getString(R.string.home_parent_portada_dropdown_cd)
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingListSection),
        ) {
            Text(
                stringResource(R.string.home_parent_portada_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppCard(
                modifier = Modifier
                    .weight(1f)
                    .menuAnchor(
                        type = MenuAnchorType.PrimaryNotEditable,
                        enabled = opciones.size > 1,
                    ),
                elevated = false,
                includeContentPadding = false,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = AcademiaDimens.contextBannerHorizontalPadding,
                            vertical = AcademiaDimens.gapVerticalTight,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
                        modifier = Modifier.weight(1f).padding(end = AcademiaDimens.gapSm),
                    ) {
                        val fotoModel = coilFotoJugadorModel(
                            context,
                            seleccionActual.fotoUrlSupabase,
                            seleccionActual.fotoRutaAbsoluta,
                        )
                        Box(
                            modifier = Modifier
                                .size(AcademiaDimens.iconSizeMd)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (fotoModel != null) {
                                AsyncImage(
                                    model = fotoModel,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(AcademiaDimens.iconSizeSm - AcademiaDimens.gapMicro),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Text(
                            seleccionActual.nombre,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            opciones.forEach { op ->
                val foto = coilFotoJugadorModel(context, op.fotoUrlSupabase, op.fotoRutaAbsoluta)
                DropdownMenuItem(
                    text = {
                        Text(
                            op.nombre,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        onRemoteIdChange(op.jugadorRemoteId)
                        expanded = false
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(AcademiaDimens.iconSizeMd)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (foto != null) {
                                AsyncImage(
                                    model = foto,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(AcademiaDimens.iconSizeSm - AcademiaDimens.gapMicro),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                )
            }
        }
    }
}

/** Misma lista de rutas que las pestañas operativas (excluye `inicio`). Orden fijo = coherente con barra. */
private data class AccesoRapidoDef(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector,
)

private val ACCESOS_RAPIDOS_INICIO = listOf(
    AccesoRapidoDef("jugadores", R.string.tab_players, Icons.Default.Group),
    AccesoRapidoDef("asistencia", R.string.tab_attendance, Icons.Default.TaskAlt),
    AccesoRapidoDef("estadisticas", R.string.tab_stats, Icons.Default.Assessment),
    AccesoRapidoDef("competencias", R.string.tab_competitions, Icons.Default.EmojiEvents),
    AccesoRapidoDef("contenido", R.string.tab_resources, Icons.AutoMirrored.Filled.MenuBook),
    AccesoRapidoDef("finanzas", R.string.tab_finances, Icons.Default.Payments),
    AccesoRapidoDef("padres", R.string.tab_parents, Icons.Default.MailOutline),
    AccesoRapidoDef("academia", R.string.tab_academy, Icons.Default.Settings),
)

@Composable
private fun InicioStaffOnboardingSinCategoriasPanel(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    AppTintedPanel(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AcademiaDimens.radiusMd),
        containerColor = scheme.secondaryContainer.copy(alpha = 0.72f),
        contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd)) {
            Text(
                stringResource(R.string.onboarding_home_hint_no_categories),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSecondaryContainer,
            )
            PrimaryButton(
                text = stringResource(R.string.onboarding_home_cta_academy),
                onClick = { onNavigate("academia") },
            )
        }
    }
}

@Composable
private fun InicioStaffOnboardingSinJugadoresPanel(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    AppTintedPanel(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AcademiaDimens.radiusMd),
        containerColor = scheme.primaryContainer.copy(alpha = 0.65f),
        contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd)) {
            Text(
                stringResource(R.string.onboarding_home_hint_no_players),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onPrimaryContainer,
            )
            PrimaryButton(
                text = stringResource(R.string.onboarding_home_cta_players),
                onClick = { onNavigate("jugadores") },
            )
        }
    }
}

@Composable
private fun AccesoRapidoCard(
    titulo: String,
    icono: ImageVector,
    onClick: () -> Unit,
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevated = true,
        includeContentPadding = false,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AcademiaDimens.buttonMinHeight)
                .padding(
                    horizontal = AcademiaDimens.paddingCard,
                    vertical = AcademiaDimens.gapMd,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.paddingCardCompact + AcademiaDimens.gapSm),
        ) {
            Box(
                modifier = Modifier
                    .size(AcademiaDimens.homeShortcutIconContainer)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AcademiaDimens.iconSizeMd),
                )
            }
            Text(
                titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
