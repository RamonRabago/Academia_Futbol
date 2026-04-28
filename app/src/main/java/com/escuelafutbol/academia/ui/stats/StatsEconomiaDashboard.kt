package com.escuelafutbol.academia.ui.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.ChipsGroup
import com.escuelafutbol.academia.ui.design.EmptyState
import java.util.Locale

/** Orden de la lista compacta por categoría. */
enum class EconomiaOrden {
    Estimado,
    Cobrado,
    Adeudo,
}

/** Filtro rápido sobre la lista. */
enum class EconomiaFiltro {
    Todas,
    ConAdeudo,
    MayorIngreso,
}

/** Misma lógica que antes en `remember`; reutilizada para animar la lista con el par orden/filtro correcto. */
private fun calcularFilasMostradas(
    eco: StatsEconomiaResumenUi,
    orden: EconomiaOrden,
    filtro: EconomiaFiltro,
): List<EconomiaPorCategoriaUi> {
    var lista = eco.filasPorCategoria
    lista = when (filtro) {
        EconomiaFiltro.Todas -> lista
        EconomiaFiltro.ConAdeudo -> lista.filter {
            eco.hayCobrosRegistradosEnSistema &&
                (it.adeudoPendientePeriodo ?: 0.0) > 0.001
        }
        EconomiaFiltro.MayorIngreso -> {
            lista
                .sortedByDescending { it.ingresoMensualEstimado }
                .take(3)
        }
    }
    return when (orden) {
        EconomiaOrden.Estimado -> lista.sortedWith(
            compareByDescending<EconomiaPorCategoriaUi> { it.ingresoMensualEstimado }
                .thenBy { it.categoria },
        )
        EconomiaOrden.Cobrado -> lista.sortedWith(
            compareByDescending<EconomiaPorCategoriaUi> { it.ingresoCobradoPeriodo ?: 0.0 }
                .thenBy { it.categoria },
        )
        EconomiaOrden.Adeudo -> lista.sortedWith(
            compareByDescending<EconomiaPorCategoriaUi> { it.adeudoPendientePeriodo ?: 0.0 }
                .thenBy { it.categoria },
        )
    }
}

private val ColorCobradoVerde = Color(0xFF2E7D32)
private val ColorAdeudoRojoSuave = Color(0xFFD84343)
private val ColorAdeudoRojoFuerte = Color(0xFFB71C1C)

/** Anchos fijos compactos para montos; el nombre de categoría usa el `weight` restante. */
private val WColEst = 56.dp
private val WColCob = 56.dp
private val WColPend = 56.dp
private val WColCobranza = 64.dp
private val WColGapMontos = 4.dp
private val WColAnchoBloqueMontos =
    WColEst + WColCob + WColPend + WColCobranza + WColGapMontos + WColGapMontos + WColGapMontos

private fun porcentajeCobranza(fila: EconomiaPorCategoriaUi, hayCobros: Boolean): Float? {
    if (!hayCobros || fila.ingresoMensualEstimado <= 0.001) return null
    return ((fila.ingresoCobradoPeriodo ?: 0.0) / fila.ingresoMensualEstimado * 100.0)
        .toFloat()
        .coerceIn(0f, 100f)
}

private fun filaSinActividadEconomica(fila: EconomiaPorCategoriaUi): Boolean {
    val est = fila.ingresoMensualEstimado <= 0.001
    val cob = (fila.ingresoCobradoPeriodo ?: 0.0) <= 0.001
    val ade = (fila.adeudoPendientePeriodo ?: 0.0) <= 0.001
    return est && cob && ade
}

/** Partes verde y roja de la barra (solo esos dos colores), proporciones que suman 1 si hay algo que mostrar. */
private fun fraccionesBarraSoloVerdeRojo(fila: EconomiaPorCategoriaUi, hayCobros: Boolean): Pair<Float, Float> {
    if (!hayCobros || fila.ingresoMensualEstimado <= 0.001) return 0f to 0f
    val est = fila.ingresoMensualEstimado
    val cob = (fila.ingresoCobradoPeriodo ?: 0.0).coerceAtLeast(0.0)
    val ade = (fila.adeudoPendientePeriodo ?: 0.0).coerceAtLeast(0.0)
    var g = (cob / est).toFloat().coerceIn(0f, 1f)
    var r = (ade / est).toFloat().coerceIn(0f, 1f)
    if (g + r > 1f + 1e-4f) {
        val inv = 1f / (g + r)
        g *= inv
        r *= inv
    }
    val sum = g + r
    if (sum <= 1e-5f) return 0f to 0f
    return (g / sum) to (r / sum)
}

@Composable
private fun colorSemaforoCobranza(pct: Float?): Color {
    if (pct == null) return MaterialTheme.colorScheme.onSurfaceVariant
    return when {
        pct < 50f -> Color(0xFFE53935)
        pct < 80f -> Color(0xFFF9A825)
        else -> ColorCobradoVerde
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StatsEconomiaCategoriasDashboard(
    eco: StatsEconomiaResumenUi,
    orden: EconomiaOrden,
    onOrdenChange: (EconomiaOrden) -> Unit,
    filtro: EconomiaFiltro,
    onFiltroChange: (EconomiaFiltro) -> Unit,
    categoriasExpandidas: Set<String>,
    onToggleCategoria: (String) -> Unit,
    formatImporte: (Double) -> String,
) {
    val haptic = LocalHapticFeedback.current
    val totalCategorias = eco.filasPorCategoria.size
    val filasMostradas = remember(eco.filasPorCategoria, orden, filtro, eco.hayCobrosRegistradosEnSistema) {
        calcularFilasMostradas(eco, orden, filtro)
    }

    val totalEstimadoVisible = remember(filasMostradas) {
        filasMostradas.sumOf { it.ingresoMensualEstimado }
    }
    val totalCobradoVisible = remember(filasMostradas, eco.hayCobrosRegistradosEnSistema) {
        if (!eco.hayCobrosRegistradosEnSistema) null
        else filasMostradas.sumOf { it.ingresoCobradoPeriodo ?: 0.0 }
    }
    val totalAdeudoVisible = remember(filasMostradas, eco.hayCobrosRegistradosEnSistema) {
        if (!eco.hayCobrosRegistradosEnSistema) null
        else filasMostradas.sumOf { it.adeudoPendientePeriodo ?: 0.0 }
    }

    val tops = remember(filasMostradas, eco.hayCobrosRegistradosEnSistema) {
        if (filasMostradas.isEmpty()) {
            Triple<EconomiaPorCategoriaUi?, EconomiaPorCategoriaUi?, EconomiaPorCategoriaUi?>(
                null,
                null,
                null,
            )
        } else {
            val topEst = filasMostradas.maxByOrNull { it.ingresoMensualEstimado }
            val topCob = if (eco.hayCobrosRegistradosEnSistema) {
                filasMostradas.maxByOrNull { it.ingresoCobradoPeriodo ?: 0.0 }
            } else {
                null
            }
            val peorCobranza = if (eco.hayCobrosRegistradosEnSistema) {
                filasMostradas
                    .filter { it.ingresoMensualEstimado > 0.001 }
                    .minWithOrNull(
                        compareBy<EconomiaPorCategoriaUi> { fila ->
                            (fila.ingresoCobradoPeriodo ?: 0.0) / fila.ingresoMensualEstimado
                        }.thenBy { it.ingresoCobradoPeriodo ?: 0.0 },
                    )
            } else {
                null
            }
            Triple(topEst, topCob, peorCobranza)
        }
    }

    val chipOrdenColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
    val chipFiltroColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )

    Text(
        stringResource(R.string.stats_economy_dash_sort_label),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(
            top = AcademiaDimens.gapMicro,
            bottom = AcademiaDimens.gapSm,
        ),
    )
    ChipsGroup {
        FilterChip(
            selected = orden == EconomiaOrden.Estimado,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onOrdenChange(EconomiaOrden.Estimado)
            },
            label = { Text(stringResource(R.string.stats_economy_sort_estimado)) },
            colors = chipOrdenColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = orden == EconomiaOrden.Estimado,
            ),
        )
        FilterChip(
            selected = orden == EconomiaOrden.Cobrado,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onOrdenChange(EconomiaOrden.Cobrado)
            },
            label = { Text(stringResource(R.string.stats_economy_sort_cobrado)) },
            colors = chipOrdenColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = orden == EconomiaOrden.Cobrado,
            ),
        )
        FilterChip(
            selected = orden == EconomiaOrden.Adeudo,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onOrdenChange(EconomiaOrden.Adeudo)
            },
            label = { Text(stringResource(R.string.stats_economy_sort_adeudo)) },
            colors = chipOrdenColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = orden == EconomiaOrden.Adeudo,
            ),
        )
    }

    Text(
        stringResource(R.string.stats_economy_dash_filter_label),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(
            top = AcademiaDimens.gapSm,
            bottom = AcademiaDimens.gapSm,
        ),
    )
    ChipsGroup {
        FilterChip(
            selected = filtro == EconomiaFiltro.Todas,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onFiltroChange(EconomiaFiltro.Todas)
            },
            label = { Text(stringResource(R.string.stats_economy_filter_todas)) },
            colors = chipFiltroColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = filtro == EconomiaFiltro.Todas,
            ),
        )
        FilterChip(
            selected = filtro == EconomiaFiltro.ConAdeudo,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onFiltroChange(EconomiaFiltro.ConAdeudo)
            },
            label = { Text(stringResource(R.string.stats_economy_filter_con_adeudo)) },
            colors = chipFiltroColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = filtro == EconomiaFiltro.ConAdeudo,
            ),
        )
        FilterChip(
            selected = filtro == EconomiaFiltro.MayorIngreso,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onFiltroChange(EconomiaFiltro.MayorIngreso)
            },
            label = { Text(stringResource(R.string.stats_economy_filter_mayor_ingreso)) },
            colors = chipFiltroColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = filtro == EconomiaFiltro.MayorIngreso,
            ),
        )
    }

    Text(
        stringResource(
            R.string.stats_economy_dash_showing_categories,
            filasMostradas.size,
            totalCategorias,
        ),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            top = AcademiaDimens.gapVerticalTight,
            bottom = AcademiaDimens.gapMicro,
        ),
    )
    if (filtro == EconomiaFiltro.MayorIngreso && totalCategorias > 0) {
        Text(
            stringResource(R.string.stats_economy_dash_top3_filter_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = AcademiaDimens.gapSm),
        )
    }

    StatsCardFrame {
        Text(
            stringResource(R.string.stats_economy_dash_global_title),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = AcademiaDimens.gapMd),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top,
        ) {
            ResumenGlobalColumna(
                titulo = stringResource(R.string.stats_economy_dash_total_estimado),
                valor = formatImporte(totalEstimadoVisible),
                colorValor = MaterialTheme.colorScheme.onSurface,
            )
            ResumenGlobalColumna(
                titulo = stringResource(R.string.stats_economy_dash_total_cobrado),
                valor = totalCobradoVisible?.let { formatImporte(it) } ?: "—",
                colorValor = ColorCobradoVerde,
            )
            ResumenGlobalColumna(
                titulo = stringResource(R.string.stats_economy_dash_total_adeudo),
                valor = totalAdeudoVisible?.let { formatImporte(it) } ?: "—",
                colorValor = ColorAdeudoRojoSuave,
            )
        }
    }

    if (filasMostradas.isNotEmpty()) {
        val (topEst, topCob, peor) = tops
        val tituloEst = stringResource(R.string.stats_economy_dash_top_est)
        val tituloCob = stringResource(R.string.stats_economy_dash_top_cob)
        val tituloPeor = stringResource(R.string.stats_economy_dash_top_peor)
        Column(
            modifier = Modifier.padding(top = AcademiaDimens.gapMd),
            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
            ) {
                StatsCardMedium(
                    value = topEst?.let { formatImporte(it.ingresoMensualEstimado) } ?: "—",
                    label = topEst?.let { "$tituloEst · ${it.categoria}" } ?: tituloEst,
                    modifier = Modifier.weight(1f),
                )
                StatsCardMedium(
                    value = if (eco.hayCobrosRegistradosEnSistema && topCob != null) {
                        formatImporte(topCob.ingresoCobradoPeriodo ?: 0.0)
                    } else {
                        "—"
                    },
                    label = topCob?.let { "$tituloCob · ${it.categoria}" } ?: tituloCob,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
            ) {
                StatsCardMedium(
                    value = if (eco.hayCobrosRegistradosEnSistema && peor != null) {
                        String.format(
                            Locale.getDefault(),
                            "%.0f%%",
                            porcentajeCobranza(peor, true) ?: 0f,
                        )
                    } else {
                        "—"
                    },
                    label = peor?.let { "$tituloPeor · ${it.categoria}" } ?: tituloPeor,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(AcademiaDimens.statsMetricTileHeight),
                )
            }
        }
    }

    AnimatedContent(
        targetState = orden to filtro,
        transitionSpec = {
            (
                fadeIn(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                ) + slideInVertically(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 10 },
                )
                ).togetherWith(
                fadeOut(animationSpec = tween(160)) +
                    slideOutVertically(
                        animationSpec = tween(160),
                        targetOffsetY = { -it / 10 },
                    ),
            )
        },
        modifier = Modifier.fillMaxWidth(),
        label = "economia_lista_categorias",
    ) { state ->
        val rows = calcularFilasMostradas(eco, state.first, state.second)
        val maxAdeudoEnFilas = if (!eco.hayCobrosRegistradosEnSistema) {
            0.0
        } else {
            rows.maxOfOrNull { it.adeudoPendientePeriodo ?: 0.0 } ?: 0.0
        }

        if (rows.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AcademiaDimens.gapMd),
                title = if (eco.filasPorCategoria.isEmpty()) {
                    stringResource(R.string.stats_economy_dash_empty_no_categories)
                } else {
                    stringResource(R.string.stats_economy_dash_empty_filter)
                },
            )
        } else {
            Column(modifier = Modifier.padding(top = AcademiaDimens.gapVerticalTight)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = AcademiaDimens.gapSm),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        stringResource(R.string.stats_economy_col_cat),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                    )
                    Row(
                        modifier = Modifier.width(WColAnchoBloqueMontos),
                        horizontalArrangement = Arrangement.spacedBy(WColGapMontos),
                    ) {
                        Text(
                            stringResource(R.string.stats_economy_col_est),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(WColEst),
                            textAlign = TextAlign.End,
                            maxLines = 2,
                        )
                        Text(
                            stringResource(R.string.stats_economy_col_cob),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(WColCob),
                            textAlign = TextAlign.End,
                            maxLines = 2,
                        )
                        Text(
                            stringResource(R.string.stats_economy_col_pendiente),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(WColPend),
                            textAlign = TextAlign.End,
                            maxLines = 2,
                        )
                        Text(
                            stringResource(R.string.stats_economy_col_pct),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(WColCobranza),
                            textAlign = TextAlign.End,
                            maxLines = 2,
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                rows.forEach { fila ->
                    val expandida = fila.categoria in categoriasExpandidas
                    val sinActividad = filaSinActividadEconomica(fila)
                    val pct = porcentajeCobranza(fila, eco.hayCobrosRegistradosEnSistema)
                    val adeudo = fila.adeudoPendientePeriodo ?: 0.0
                    val adeudoAlto = eco.hayCobrosRegistradosEnSistema &&
                        maxAdeudoEnFilas > 0 &&
                        adeudo >= maxAdeudoEnFilas * 0.65 &&
                        adeudo > 0.001
                    val baseSurface = when {
                        sinActividad -> MaterialTheme.colorScheme.surfaceContainerHigh
                        !eco.hayCobrosRegistradosEnSistema || pct == null -> {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                        pct < 50f -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.28f)
                        pct < 80f -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                        else -> ColorCobradoVerde.copy(alpha = 0.12f)
                    }
                    val colorAdeudoTexto = if (adeudoAlto) ColorAdeudoRojoFuerte else ColorAdeudoRojoSuave

                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AcademiaDimens.gapMicro)
                            .clickable(
                                onClickLabel = stringResource(
                                    R.string.stats_economy_row_expand_cd,
                                    fila.categoria,
                                ),
                            ) { onToggleCategoria(fila.categoria) },
                        elevated = false,
                        containerColor = baseSurface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        includeContentPadding = false,
                    ) {
                        Column(Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = AcademiaDimens.gapVerticalTight,
                                        vertical = AcademiaDimens.gapVerticalTight,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapVerticalTight),
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapSm),
                                ) {
                                    if (adeudoAlto) {
                                        Icon(
                                            imageVector = Icons.Outlined.Warning,
                                            contentDescription = stringResource(
                                                R.string.stats_economy_adeudo_alto_cd,
                                                fila.categoria,
                                            ),
                                            modifier = Modifier.size(AcademiaDimens.iconSizeSm),
                                            tint = ColorAdeudoRojoFuerte,
                                        )
                                    }
                                    Text(
                                        fila.categoria,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = if (eco.hayCobrosRegistradosEnSistema) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                    )
                                }
                                Row(
                                    modifier = Modifier.width(WColAnchoBloqueMontos),
                                    horizontalArrangement = Arrangement.spacedBy(WColGapMontos),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        formatImporte(fila.ingresoMensualEstimado),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.width(WColEst),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        if (eco.hayCobrosRegistradosEnSistema) {
                                            formatImporte(fila.ingresoCobradoPeriodo ?: 0.0)
                                        } else {
                                            "—"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.width(WColCob),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        if (eco.hayCobrosRegistradosEnSistema) {
                                            formatImporte(fila.adeudoPendientePeriodo ?: 0.0)
                                        } else {
                                            "—"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (adeudoAlto) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (eco.hayCobrosRegistradosEnSistema) {
                                            colorAdeudoTexto
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.width(WColPend),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Column(
                                        modifier = Modifier.width(WColCobranza),
                                        horizontalAlignment = Alignment.End,
                                    ) {
                                        if (sinActividad) {
                                            Text(
                                                stringResource(R.string.stats_economy_sin_actividad),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.End,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        } else if (pct != null) {
                                            Text(
                                                stringResource(
                                                    R.string.stats_economy_pct_cobranza,
                                                    pct.toInt().coerceIn(0, 100),
                                                ),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = colorSemaforoCobranza(pct),
                                                textAlign = TextAlign.End,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        } else {
                                            Text(
                                                "—",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.End,
                                            )
                                        }
                                    }
                                }
                            }
                BarraCobranzaApilada(
                    fila = fila,
                    hayCobros = eco.hayCobrosRegistradosEnSistema,
                    sinActividad = sinActividad,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = AcademiaDimens.gapMd,
                            end = AcademiaDimens.gapMd,
                            bottom = AcademiaDimens.gapVerticalTight,
                        ),
                )
                AnimatedVisibility(
                    visible = expandida,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = AcademiaDimens.paddingCardCompact,
                                end = AcademiaDimens.paddingCardCompact,
                                bottom = AcademiaDimens.gapMd,
                            ),
                        verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMicro),
                    ) {
                        Text(
                            stringResource(R.string.stats_economy_cat_active, fila.alumnosActivos),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            stringResource(R.string.stats_economy_cat_becados, fila.becados),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            stringResource(R.string.stats_economy_cat_con_cuota, fila.alumnosConCuota),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            stringResource(R.string.stats_economy_cat_sin_cuota, fila.sinCuotaDefinida),
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
    }
}

@Composable
private fun BarraCobranzaApilada(
    fila: EconomiaPorCategoriaUi,
    hayCobros: Boolean,
    sinActividad: Boolean,
    modifier: Modifier = Modifier,
) {
    val forma = RoundedCornerShape(AcademiaDimens.radiusSm)
    val (verde, rojo) = remember(fila, hayCobros) {
        fraccionesBarraSoloVerdeRojo(fila, hayCobros)
    }
    if (sinActividad) {
        Spacer(modifier.height(AcademiaDimens.gapSm))
        return
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AcademiaDimens.gapMd)
            .clip(forma)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
    ) {
        if (hayCobros && fila.ingresoMensualEstimado > 0.001) {
            if (verde > 0f) {
                Box(
                    modifier = Modifier
                        .weight(verde)
                        .fillMaxHeight()
                        .background(ColorCobradoVerde),
                )
            }
            if (rojo > 0f) {
                Box(
                    modifier = Modifier
                        .weight(rojo)
                        .fillMaxHeight()
                        .background(ColorAdeudoRojoSuave),
                )
            }
        }
    }
}

@Composable
private fun ResumenGlobalColumna(
    titulo: String,
    valor: String,
    colorValor: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMicro),
        modifier = Modifier.widthIn(min = 88.dp),
    ) {
        Text(
            valor,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colorValor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            titulo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
