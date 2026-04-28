@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.escuelafutbol.academia.ui.competencias

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.AppTintedPanel
import com.escuelafutbol.academia.ui.design.ChipsGroup

/** Chips visibles en la vista compacta de tarjeta. */
private const val CategoriasMaxChipsCompacto = 3

@Composable
private fun ChipNombreCategoria(texto: String) {
    AppTintedPanel(
        modifier = Modifier.widthIn(max = 120.dp),
        shape = RoundedCornerShape(AcademiaDimens.radiusDense),
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
        contentPadding = PaddingValues(
            horizontal = AcademiaDimens.contextBannerHorizontalPadding,
            vertical = AcademiaDimens.paddingChipVerticalDense,
        ),
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun CategoriasChipsCompactas(
    nombres: List<String>,
    onExpandRequest: () -> Unit,
) {
    if (nombres.isEmpty()) return
    val extra = nombres.size - CategoriasMaxChipsCompacto
    ChipsGroup {
        nombres.take(CategoriasMaxChipsCompacto).forEach { ChipNombreCategoria(it) }
        if (extra > 0) {
            val masCd = stringResource(R.string.competitions_card_categories_more_cd, extra)
            AppTintedPanel(
                modifier = Modifier
                    .semantics { contentDescription = masCd }
                    .clickable { onExpandRequest() },
                shape = RoundedCornerShape(AcademiaDimens.radiusDense),
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                contentPadding = PaddingValues(
                    horizontal = AcademiaDimens.contextBannerHorizontalPadding + AcademiaDimens.gapMicro,
                    vertical = AcademiaDimens.paddingChipVerticalDense,
                ),
            ) {
                Text(
                    stringResource(R.string.competitions_card_categories_more, extra),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun LineaDeporteTemporadaLista(item: CompetenciaListaItemUi) {
    val temp = item.competencia.temporada?.trim()?.takeIf { it.isNotEmpty() }
    val texto = if (temp != null) {
        stringResource(R.string.competitions_card_sport_season_dot, item.deporteNombre, temp)
    } else {
        stringResource(R.string.competitions_row_sport, item.deporteNombre)
    }
    Text(
        texto,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun TextoResumenPartidosProximoCompacto(item: CompetenciaListaItemUi) {
    val rival = item.proximoRival
    val fecha = item.proximoFechaCorta
    val proxCorto = when {
        rival != null && fecha != null ->
            stringResource(
                R.string.competitions_card_next_vs_date,
                stringResource(R.string.competitions_card_next_vs, rival),
                fecha,
            )
        rival != null -> stringResource(R.string.competitions_card_next_vs, rival)
        else -> stringResource(R.string.competitions_card_compact_next_none)
    }
    Text(
        stringResource(R.string.competitions_card_compact_matches_next, item.partidosJugados, proxCorto),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun CategoriasTodasChipGroup(nombres: List<String>) {
    if (nombres.isEmpty()) return
    Text(
        stringResource(R.string.competitions_card_categories_section),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(AcademiaDimens.gapVerticalTight))
    ChipsGroup {
        nombres.forEach { nombre -> ChipNombreCategoria(nombre) }
    }
}

@Composable
fun CompetenciaCardBase(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        elevated = true,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapVerticalTight),
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
        horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.radiusSm),
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
    var expandida by remember { mutableStateOf(false) }
    val nombresCat = item.categoriasInscritasNombres
    val hayOverflowCategorias = nombresCat.size > CategoriasMaxChipsCompacto
    CompetenciaCardBase {
        Column(Modifier.animateContentSize(animationSpec = tween(220))) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
            ) {
                Text(
                    item.competencia.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                LineaDeporteTemporadaLista(item)
                TextoResumenPartidosProximoCompacto(item)
                when {
                    nombresCat.isNotEmpty() -> Unit
                    item.numCategoriasInscritas > 0 -> {
                        Text(
                            stringResource(R.string.competitions_card_categories_count, item.numCategoriasInscritas),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    else -> {
                        Text(
                            stringResource(R.string.competitions_card_categories_none),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                if (nombresCat.isNotEmpty()) {
                    Spacer(Modifier.height(AcademiaDimens.radiusSm / 2))
                    CategoriasChipsCompactas(
                        nombres = nombresCat,
                        onExpandRequest = { expandida = true },
                    )
                }
                if (!expandida && hayOverflowCategorias) {
                    TextButton(
                        onClick = { expandida = true },
                        modifier = Modifier.padding(top = AcademiaDimens.radiusSm / 2),
                    ) {
                        Text(stringResource(R.string.competitions_card_see_more))
                    }
                }
            }
            AnimatedVisibility(
                visible = expandida,
                enter = expandVertically(animationSpec = tween(240), expandFrom = Alignment.Top) +
                    fadeIn(tween(200)),
                exit = shrinkVertically(animationSpec = tween(200), shrinkTowards = Alignment.Top) +
                    fadeOut(tween(160)),
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapVerticalTight),
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
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
                        nombresCat.isNotEmpty() -> {
                            CategoriasTodasChipGroup(nombresCat)
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
                    TextButton(
                        onClick = { expandida = false },
                        modifier = Modifier.padding(top = AcademiaDimens.gapVerticalTight),
                    ) {
                        Text(stringResource(R.string.competitions_card_see_less))
                    }
                }
            }
        }
    }
}

@Composable
private fun colorTonoResultadoLista(tono: CompetenciaListaTonResultado): Color {
    val scheme = MaterialTheme.colorScheme
    return when (tono) {
        CompetenciaListaTonResultado.Victoria -> scheme.primary
        CompetenciaListaTonResultado.Empate -> scheme.tertiary
        CompetenciaListaTonResultado.Derrota -> scheme.error
        CompetenciaListaTonResultado.Ninguno -> scheme.onSurfaceVariant
    }
}

@Composable
fun CompetenciaCardPadre(
    item: CompetenciaListaItemUi,
    hijosEnCompetencia: List<Jugador>,
    onClick: () -> Unit,
) {
    var expandida by remember { mutableStateOf(false) }
    val nombresCat = item.categoriasRelacionadas
    val hayOverflowCategorias = nombresCat.size > CategoriasMaxChipsCompacto
    CompetenciaCardBase {
        Column(Modifier.animateContentSize(animationSpec = tween(220))) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
            ) {
                Text(
                    item.competencia.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                LineaDeporteTemporadaLista(item)
                TextoResumenPartidosProximoCompacto(item)
                when {
                    nombresCat.isNotEmpty() -> Unit
                    hijosEnCompetencia.size > 1 -> {
                        val primero = hijosEnCompetencia.first().nombre.trim().ifBlank { "—" }
                        val resto = hijosEnCompetencia.size - 1
                        Text(
                            stringResource(R.string.competitions_card_parent_children_compact, primero, resto),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    hijosEnCompetencia.size == 1 -> {
                        val n = hijosEnCompetencia.first().nombre.trim().ifBlank { "—" }
                        Text(
                            stringResource(R.string.competitions_card_parent_child, n),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (nombresCat.isNotEmpty()) {
                    Spacer(Modifier.height(AcademiaDimens.radiusSm / 2))
                    CategoriasChipsCompactas(
                        nombres = nombresCat,
                        onExpandRequest = { expandida = true },
                    )
                }
                if (!expandida && hayOverflowCategorias) {
                    TextButton(
                        onClick = { expandida = true },
                        modifier = Modifier.padding(top = AcademiaDimens.radiusSm / 2),
                    ) {
                        Text(stringResource(R.string.competitions_card_see_more))
                    }
                }
            }
            AnimatedVisibility(
                visible = expandida,
                enter = expandVertically(animationSpec = tween(240), expandFrom = Alignment.Top) +
                    fadeIn(tween(200)),
                exit = shrinkVertically(animationSpec = tween(200), shrinkTowards = Alignment.Top) +
                    fadeOut(tween(160)),
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapVerticalTight),
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
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
                    if (nombresCat.isNotEmpty()) {
                        CategoriasTodasChipGroup(nombresCat)
                    }
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
                            FilaIconoLista(
                                Icons.Filled.Person,
                                stringResource(R.string.competitions_card_parent_children, nombres),
                            )
                        }
                    }
                    item.padreEquipoTexto?.let { eq ->
                        FilaIconoLista(
                            Icons.Filled.Flag,
                            stringResource(R.string.competitions_card_parent_team, eq),
                            iconTint = MaterialTheme.colorScheme.tertiary,
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
                        AppTintedPanel(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AcademiaDimens.radiusMd),
                            containerColor = colorTonoResultadoLista(tono).copy(alpha = 0.14f),
                            contentPadding = PaddingValues(
                                horizontal = AcademiaDimens.paddingCardCompact,
                                vertical = AcademiaDimens.radiusSm,
                            ),
                        ) {
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
                                modifier = Modifier.padding(top = AcademiaDimens.radiusSm / 2),
                            )
                        }
                    }
                    TextButton(
                        onClick = { expandida = false },
                        modifier = Modifier.padding(top = AcademiaDimens.gapVerticalTight),
                    ) {
                        Text(stringResource(R.string.competitions_card_see_less))
                    }
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
    val alturaCelda = AcademiaDimens.resumenStaffCeldaAltura
    AppCard(
        modifier = modifier.fillMaxWidth(),
        elevated = false,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.chipSpacing),
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
}

@Composable
private fun ResumenCelda(
    icono: ImageVector,
    valor: String,
    etiqueta: String,
    alturaFija: Dp,
    modifier: Modifier = Modifier,
) {
    AppTintedPanel(
        modifier = modifier.height(alturaFija),
        shape = RoundedCornerShape(AcademiaDimens.radiusMd),
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        contentPadding = PaddingValues(
            horizontal = AcademiaDimens.paddingCardCompact,
            vertical = AcademiaDimens.paddingCardCompact,
        ),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            /** Espacio fijo: el bloque superior compacto y la etiqueta al pie con altura real para 2 líneas. */
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMicro),
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    modifier = Modifier.size(AcademiaDimens.iconSizeResumen),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    valor,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
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
