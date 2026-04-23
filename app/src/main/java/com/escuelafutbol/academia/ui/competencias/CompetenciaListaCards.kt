package com.escuelafutbol.academia.ui.competencias

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.entity.Jugador

/** Máximo de nombres visibles sin chip «+X». A partir de 6 categorías se muestran 4 + overflow. */
private const val CategoriasMaxVisiblesSinColapsar = 5
private const val CategoriasNombresCuandoHayOverflow = 4

@Composable
private fun ChipNombreCategoria(texto: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
    ) {
        Text(
            texto,
            modifier = Modifier
                .widthIn(max = 140.dp)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BloqueCategoriasTarjetaLista(nombres: List<String>) {
    if (nombres.isEmpty()) return
    var expandido by remember { mutableStateOf(false) }
    val hayOverflow = nombres.size > CategoriasMaxVisiblesSinColapsar
    val restantes = (nombres.size - CategoriasNombresCuandoHayOverflow).coerceAtLeast(0)

    Text(
        stringResource(R.string.competitions_card_categories_section),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(6.dp))
    if (expandido) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            nombres.forEach { nombre ->
                ChipNombreCategoria(nombre)
            }
        }
        TextButton(
            onClick = { expandido = false },
            modifier = Modifier.padding(top = 4.dp),
        ) {
            Text(stringResource(R.string.competitions_card_categories_show_less))
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!hayOverflow) {
                nombres.forEach { ChipNombreCategoria(it) }
            } else {
                val masCd = stringResource(R.string.competitions_card_categories_more_cd, restantes)
                nombres.take(CategoriasNombresCuandoHayOverflow).forEach { ChipNombreCategoria(it) }
                Surface(
                    modifier = Modifier
                        .semantics { contentDescription = masCd }
                        .clickable { expandido = true },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                ) {
                    Text(
                        stringResource(R.string.competitions_card_categories_more, restantes),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
fun CompetenciaCardBase(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun FilaIconoLista(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    iconTint: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconTint,
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun CompetenciaCardStaff(
    item: CompetenciaListaItemUi,
    onClick: () -> Unit,
) {
    CompetenciaCardBase(onClick = onClick) {
        Text(
            item.competencia.nombre,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            stringResource(R.string.competitions_row_sport, item.deporteNombre),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        item.competencia.temporada?.takeIf { it.isNotBlank() }?.let { t ->
            Text(
                stringResource(R.string.competitions_row_season, t),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        Spacer(Modifier.height(2.dp))
        FilaIconoLista(
            Icons.Filled.SportsSoccer,
            stringResource(R.string.competitions_card_matches_played, item.partidosJugados),
        )
        val rival = item.proximoRival
        if (rival != null) {
            val prox = stringResource(R.string.competitions_card_next_vs, rival)
            val fecha = item.proximoFechaCorta
            FilaIconoLista(
                Icons.Filled.CalendarMonth,
                if (fecha != null) {
                    stringResource(R.string.competitions_card_next_vs_date, prox, fecha)
                } else {
                    prox
                },
            )
        } else {
            FilaIconoLista(
                Icons.Filled.CalendarMonth,
                stringResource(R.string.competitions_card_next_none),
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        when {
            item.categoriasInscritasNombres.isNotEmpty() -> {
                BloqueCategoriasTarjetaLista(item.categoriasInscritasNombres)
            }
            item.numCategoriasInscritas > 0 -> {
                FilaIconoLista(
                    Icons.Filled.Groups,
                    stringResource(R.string.competitions_card_categories_count, item.numCategoriasInscritas),
                )
            }
            else -> {
                FilaIconoLista(
                    Icons.Filled.Groups,
                    stringResource(R.string.competitions_card_categories_none),
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun colorTonoResultadoLista(tono: CompetenciaListaTonResultado): Color {
    val dark = isSystemInDarkTheme()
    return when (tono) {
        CompetenciaListaTonResultado.Victoria ->
            if (dark) Color(0xFFC8E6C9) else Color(0xFF1B5E20)
        CompetenciaListaTonResultado.Empate ->
            if (dark) Color(0xFFFFF59D) else Color(0xFF6D4C00)
        CompetenciaListaTonResultado.Derrota ->
            if (dark) Color(0xFFFFCDD2) else Color(0xFFB71C1C)
        CompetenciaListaTonResultado.Ninguno -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun CompetenciaCardPadre(
    item: CompetenciaListaItemUi,
    hijosEnCompetencia: List<Jugador>,
    onClick: () -> Unit,
) {
    CompetenciaCardBase(onClick = onClick) {
        Text(
            item.competencia.nombre,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            stringResource(R.string.competitions_parent_context_emotional),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        when (hijosEnCompetencia.size) {
            0 -> Unit
            1 -> {
                val n = hijosEnCompetencia.first().nombre.trim().ifBlank { "—" }
                FilaIconoLista(Icons.Filled.Person, stringResource(R.string.competitions_card_parent_child, n))
            }
            else -> {
                val nombres = hijosEnCompetencia.joinToString(" · ") { it.nombre.trim().ifBlank { "—" } }
                FilaIconoLista(Icons.Filled.Person, stringResource(R.string.competitions_card_parent_children, nombres))
            }
        }
        if (item.categoriasRelacionadas.isNotEmpty()) {
            BloqueCategoriasTarjetaLista(item.categoriasRelacionadas)
        }
        item.padreEquipoTexto?.let { eq ->
            FilaIconoLista(
                Icons.Filled.Flag,
                stringResource(R.string.competitions_card_parent_team, eq),
                iconTint = MaterialTheme.colorScheme.tertiary,
            )
        }
        Spacer(Modifier.height(2.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        Spacer(Modifier.height(2.dp))
        FilaIconoLista(
            Icons.Filled.SportsSoccer,
            stringResource(R.string.competitions_card_matches_played, item.partidosJugados),
        )
        val rival = item.proximoRival
        if (rival != null) {
            val prox = stringResource(R.string.competitions_card_next_vs, rival)
            val fecha = item.proximoFechaCorta
            FilaIconoLista(
                Icons.Filled.CalendarMonth,
                if (fecha != null) {
                    stringResource(R.string.competitions_card_next_vs_date, prox, fecha)
                } else {
                    prox
                },
            )
        } else {
            FilaIconoLista(
                Icons.Filled.CalendarMonth,
                stringResource(R.string.competitions_card_next_none_parent),
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val gf = item.padreUltimoGolesPropio
        val gc = item.padreUltimoGolesRival
        val tono = item.padreUltimoTono
        if (gf != null && gc != null && tono != CompetenciaListaTonResultado.Ninguno) {
            val rivalUlt = item.padreUltimoRival?.takeIf { it.isNotBlank() } ?: "—"
            val texto = when (tono) {
                CompetenciaListaTonResultado.Victoria ->
                    stringResource(R.string.competitions_card_last_win, gf, gc, rivalUlt)
                CompetenciaListaTonResultado.Empate ->
                    stringResource(R.string.competitions_card_last_draw, gf, gc, rivalUlt)
                CompetenciaListaTonResultado.Derrota ->
                    stringResource(R.string.competitions_card_last_loss, gf, gc, rivalUlt)
                CompetenciaListaTonResultado.Ninguno -> ""
            }
            Spacer(Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colorTonoResultadoLista(tono).copy(alpha = 0.14f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        stringResource(R.string.competitions_card_last_result_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        texto,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colorTonoResultadoLista(tono),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun CompetenciasListaResumenStaff(
    totalCompetencias: Int,
    totalPartidosJugados: Int,
    totalCategoriasInscritas: Int,
    modifier: Modifier = Modifier,
) {
    val alturaCelda = 112.dp
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ResumenCelda(
            icono = Icons.Filled.EmojiEvents,
            valor = totalCompetencias.toString(),
            etiqueta = stringResource(R.string.competitions_summary_competitions),
            alturaFija = alturaCelda,
            modifier = Modifier.weight(1f),
        )
        ResumenCelda(
            icono = Icons.Filled.SportsSoccer,
            valor = totalPartidosJugados.toString(),
            etiqueta = stringResource(R.string.competitions_summary_matches_played),
            alturaFija = alturaCelda,
            modifier = Modifier.weight(1f),
        )
        ResumenCelda(
            icono = Icons.Filled.Groups,
            valor = totalCategoriasInscritas.toString(),
            etiqueta = stringResource(R.string.competitions_summary_categories),
            alturaFija = alturaCelda,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ResumenCelda(
    icono: ImageVector,
    valor: String,
    etiqueta: String,
    alturaFija: Dp,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(alturaFija),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        tonalElevation = 0.dp,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                valor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
