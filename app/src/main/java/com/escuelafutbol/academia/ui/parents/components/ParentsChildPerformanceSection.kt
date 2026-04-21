package com.escuelafutbol.academia.ui.parents.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.parents.HijoRendimientoCompPadreUi
import com.escuelafutbol.academia.ui.parents.ProximoPartidoPadreUi
import java.text.DateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

/** Título de bloque + subtítulo opcional para jerarquía en tarjeta expandida del hijo. */
@Composable
fun ParentSubsectionTitle(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth()) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.primary,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
    }
}

/** Estado vacío compacto, mismo acabado en todos los bloques de la tarjeta expandida. */
@Composable
fun ParentBlockEmptyState(
    message: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceVariant.copy(alpha = 0.32f),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.22f)),
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
        )
    }
}

@Composable
fun ParentExpandedLoadingStrip(
    rendimientoCargando: Boolean,
    modifier: Modifier = Modifier,
) {
    if (rendimientoCargando) {
        val scheme = MaterialTheme.colorScheme
        Column(modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = scheme.primary,
                trackColor = scheme.surfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
fun ParentNextMatchHeroCard(
    p: ProximoPartidoPadreUi,
    dateFmt: DateFormat,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val fechaTexto = runCatching {
        val ld = LocalDate.parse(p.fechaIso.trim())
        val ms = ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        dateFmt.format(Date(ms))
    }.getOrElse { p.fechaIso }
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = scheme.primaryContainer.copy(alpha = 0.5f),
        ),
    ) {
        Row(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(scheme.primary),
            )
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(
                    stringResource(R.string.parent_perf_next_match_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                )
                Text(
                    p.competenciaNombre,
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Spacer(Modifier.height(9.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = scheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        stringResource(R.string.parent_perf_vs_rival, p.rival),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = scheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        fechaTexto,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = scheme.onSurface,
                    )
                }
                p.hora?.let { h ->
                    Row(
                        Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = scheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(h, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                    }
                }
                p.sede?.let { s ->
                    Row(
                        Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = scheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(s, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ParentCompetitionRendimientoSection(
    rendimiento: HijoRendimientoCompPadreUi?,
    rendimientoCargando: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ParentSubsectionTitle(
            title = stringResource(R.string.parent_section_competitions_title),
            subtitle = stringResource(R.string.parent_section_competitions_subtitle),
        )
        ParentGoalsOnlyCard(totalGoles = rendimiento?.totalGolesLigas ?: 0)
        rendimiento?.let { r ->
            if (r.ultimosResultadosEquipo.isNotEmpty()) {
                ParentTeamRecentResultsBlock(r)
            } else if (!rendimientoCargando) {
                ParentBlockEmptyState(stringResource(R.string.parent_perf_no_recent_results))
            }
        }
        if (rendimiento == null && !rendimientoCargando) {
            ParentBlockEmptyState(stringResource(R.string.parent_perf_no_comp_data))
        }
    }
}

@Composable
private fun ParentGoalsOnlyCard(totalGoles: Int) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceContainerLow,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.28f)),
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.SportsSoccer,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    stringResource(R.string.parent_perf_goals_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
                Text(
                    totalGoles.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun ParentTeamRecentResultsBlock(r: HijoRendimientoCompPadreUi) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceContainerLow,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.22f)),
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                stringResource(R.string.parent_perf_recent_team_results),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                r.ultimosResultadosEquipo.forEach { letra ->
                    val (bg, fg) = when (letra) {
                        "G" -> scheme.primaryContainer.copy(alpha = 0.92f) to scheme.onPrimaryContainer
                        "E" -> scheme.surfaceVariant.copy(alpha = 0.85f) to scheme.onSurfaceVariant
                        else -> scheme.errorContainer.copy(alpha = 0.92f) to scheme.onErrorContainer
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = bg,
                    ) {
                        Text(
                            letra,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = fg,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        )
                    }
                }
            }
            val ventana = r.ultimosResultadosEquipo.size
            val marco = r.partidosConGolEnVentana
            val textoMarco = when {
                ventana == 1 && marco >= 1 -> stringResource(R.string.parent_perf_scored_last_yes)
                ventana == 1 -> stringResource(R.string.parent_perf_scored_last_no)
                else -> stringResource(R.string.parent_perf_scored_in_window, marco, ventana)
            }
            Text(
                textoMarco,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                stringResource(R.string.parent_perf_results_legend),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant.copy(alpha = 0.82f),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
