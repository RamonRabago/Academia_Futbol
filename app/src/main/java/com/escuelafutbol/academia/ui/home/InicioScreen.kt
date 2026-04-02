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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.Categoria
import com.escuelafutbol.academia.ui.util.coilLogoModel
import com.escuelafutbol.academia.ui.util.coilPortadaCategoriaModel
import com.escuelafutbol.academia.ui.util.coilPortadaModel

@Composable
fun InicioScreen(
    config: AcademiaConfig,
    /** Si hay filtro de categoría y tiene portada propia, se muestra primero; si no, la portada de la academia. */
    categoriaPortada: Categoria? = null,
    categoriaEtiqueta: String,
    onNavigate: (route: String) -> Unit,
) {
    val context = LocalContext.current
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
                    val portadaModel = categoriaPortada?.coilPortadaCategoriaModel(context)
                        ?: config.coilPortadaModel(context)
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
                AccesoRapidoCard(
                    titulo = stringResource(R.string.tab_players),
                    icono = Icons.Default.Group,
                    onClick = { onNavigate("jugadores") },
                )
                AccesoRapidoCard(
                    titulo = stringResource(R.string.tab_attendance),
                    icono = Icons.Default.TaskAlt,
                    onClick = { onNavigate("asistencia") },
                )
                AccesoRapidoCard(
                    titulo = stringResource(R.string.tab_stats),
                    icono = Icons.Default.Assessment,
                    onClick = { onNavigate("estadisticas") },
                )
                AccesoRapidoCard(
                    titulo = stringResource(R.string.tab_parents),
                    icono = Icons.Default.MailOutline,
                    onClick = { onNavigate("padres") },
                )
                AccesoRapidoCard(
                    titulo = stringResource(R.string.tab_academy),
                    icono = Icons.Default.Settings,
                    onClick = { onNavigate("academia") },
                )
            }
        }
    }
}

@Composable
private fun AccesoRapidoCard(
    titulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
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
