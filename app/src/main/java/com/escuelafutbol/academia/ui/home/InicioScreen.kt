package com.escuelafutbol.academia.ui.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.Categoria
import com.escuelafutbol.academia.ui.util.FullscreenImageViewerDialog
import com.escuelafutbol.academia.ui.util.coilLogoModel
import com.escuelafutbol.academia.ui.util.coilPortadaCategoriaModel
import com.escuelafutbol.academia.ui.util.coilPortadaModel

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
    onNavigate: (route: String) -> Unit,
) {
    val context = LocalContext.current
    var imageViewer by remember { mutableStateOf<InicioImageViewer?>(null) }
    val portadaCategoriaModel = categoriaPortada?.coilPortadaCategoriaModel(context)
    val portadaAcademiaModel = config.coilPortadaModel(context)
    val portadaMostrada = portadaCategoriaModel ?: portadaAcademiaModel
    val textoBienvenidaSesion =
        if (sesionEtiqueta.isNullOrBlank()) null
        else stringResource(R.string.home_welcome_user, sesionEtiqueta)
    val atajosVisibles = ACCESOS_RAPIDOS_INICIO.filter { accesoRapidoVisible(it.route) }
    val coverH = 152.dp
    val avatarSize = 96.dp
    val overlap = 48.dp
    val headerTotal = coverH + overlap

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
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
                        .align(Alignment.TopCenter),
                ) {
                    if (portadaMostrada != null) {
                        AsyncImage(
                            model = portadaMostrada,
                            contentDescription = stringResource(R.string.academy_cover),
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    imageViewer = if (portadaCategoriaModel != null) {
                                        InicioImageViewer.PortadaCategoria(
                                            checkNotNull(categoriaPortada).nombre,
                                        )
                                    } else {
                                        InicioImageViewer.PortadaAcademia
                                    }
                                },
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                        )
                    }
                }
                val logoModel = config.coilLogoModel(context)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(avatarSize)
                        .clip(CircleShape)
                        .border(
                            width = 4.dp,
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
                                modifier = Modifier.padding(8.dp),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                config.nombreAcademia,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                textAlign = TextAlign.Center,
            )
            if (textoBienvenidaSesion != null) {
                Text(
                    textoBienvenidaSesion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 6.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        stringResource(R.string.working_in, categoriaEtiqueta),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        if (atajosVisibles.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.home_quick_actions_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                )
            }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    for (def in atajosVisibles) {
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
                categoriaPortada?.takeIf { it.nombre == v.nombre }
                    ?.coilPortadaCategoriaModel(context)
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
    AccesoRapidoDef("finanzas", R.string.tab_finances, Icons.Default.Payments),
    AccesoRapidoDef("padres", R.string.tab_parents, Icons.Default.MailOutline),
    AccesoRapidoDef("academia", R.string.tab_academy, Icons.Default.Settings),
)

@Composable
private fun AccesoRapidoCard(
    titulo: String,
    icono: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(titulo, style = MaterialTheme.typography.titleMedium)
        }
    }
}
