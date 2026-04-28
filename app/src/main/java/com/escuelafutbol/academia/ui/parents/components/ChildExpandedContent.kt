package com.escuelafutbol.academia.ui.parents.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.design.AcademiaContextBanner
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.AppTintedPanel
import com.escuelafutbol.academia.ui.design.SectionHeader
import com.escuelafutbol.academia.ui.parents.HijoRendimientoCompPadreUi
import com.escuelafutbol.academia.ui.parents.HijoResumenUi
import java.text.DateFormat
import java.text.NumberFormat

@Composable
fun ChildExpandedContent(
    hijo: HijoResumenUi,
    rendimiento: HijoRendimientoCompPadreUi?,
    rendimientoCargando: Boolean,
    moneyFmt: NumberFormat,
    dateFmt: DateFormat,
    onRequestUnlink: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = AcademiaDimens.gapSm),
        verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingListSection),
    ) {
        ParentExpandedLoadingStrip(rendimientoCargando = rendimientoCargando)

        AcademiaContextBanner(
            contextText = stringResource(
                R.string.parent_padres_context_line,
                hijo.nombre,
                hijo.categoria,
            ),
            emphasize = true,
            modifier = Modifier.fillMaxWidth(),
        )

        AppCard(
            modifier = Modifier.fillMaxWidth(),
            elevated = false,
        ) {
            SectionHeader(
                title = stringResource(R.string.parent_padres_metrics_title),
                subtitle = stringResource(R.string.parent_padres_metrics_subtitle),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = AcademiaDimens.gapMd),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val asistenciaTexto = hijo.porcentajeAsistenciaEntrenos?.let { pct ->
                    stringResource(R.string.parent_perf_training_pct_value, pct)
                } ?: stringResource(R.string.parent_perf_no_training_data)
                val golesTexto = rendimiento?.totalGolesLigas?.toString()
                    ?: stringResource(R.string.parent_perf_no_training_data)
                val partidosTexto = rendimiento?.ultimosResultadosEquipo
                    ?.takeIf { it.isNotEmpty() }
                    ?.size
                    ?.toString()
                    ?: stringResource(R.string.parent_perf_no_training_data)
                ParentMetricaCelda(
                    etiqueta = stringResource(R.string.parent_padres_metric_label_attendance),
                    valor = asistenciaTexto,
                    modifier = Modifier.weight(1f),
                )
                ParentMetricaCelda(
                    etiqueta = stringResource(R.string.parent_padres_metric_label_goals),
                    valor = golesTexto,
                    modifier = Modifier.weight(1f),
                )
                ParentMetricaCelda(
                    etiqueta = stringResource(R.string.parent_padres_metric_label_matches),
                    valor = partidosTexto,
                    hint = stringResource(R.string.parent_padres_metric_matches_hint),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        rendimiento?.proximoPartido?.let { px ->
            ParentNextMatchHeroCard(
                p = px,
                dateFmt = dateFmt,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AppCard(
            modifier = Modifier.fillMaxWidth(),
            elevated = false,
            containerColor = scheme.surfaceContainerLow.copy(alpha = 0.45f),
            borderColor = scheme.outline.copy(alpha = 0.2f),
        ) {
            SectionHeader(
                title = stringResource(R.string.parent_section_performance_visual_title),
                subtitle = stringResource(R.string.parent_section_performance_visual_subtitle),
                modifier = Modifier.fillMaxWidth(),
            )
            ParentCompetitionRendimientoSection(
                rendimiento = rendimiento,
                rendimientoCargando = rendimientoCargando,
                mostrarTarjetaGoles = false,
                mostrarCabeceraSubseccion = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AcademiaDimens.gapMd),
            )
        }

        SectionHeader(
            title = stringResource(R.string.parent_section_training_title),
            subtitle = stringResource(R.string.parent_section_training_subtitle),
            modifier = Modifier.fillMaxWidth(),
        )
        AppTintedPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AcademiaDimens.radiusMd),
            containerColor = scheme.surfaceVariant.copy(alpha = 0.35f),
            contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
        ) {
            if (hijo.porcentajeAsistenciaEntrenos != null) {
                Text(
                    stringResource(R.string.parent_perf_attendance_hist_pct, hijo.porcentajeAsistenciaEntrenos),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface,
                )
            }
            if (hijo.ultimasAsistencias.isNotEmpty()) {
                Text(
                    stringResource(R.string.parent_attendance_recent),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = AcademiaDimens.gapSm),
                )
                AttendanceSection(
                    lineas = hijo.ultimasAsistencias,
                    dateFmt = dateFmt,
                    showSectionHeading = false,
                    modifier = Modifier.padding(top = AcademiaDimens.gapMicro),
                )
            } else {
                ParentBlockEmptyState(
                    stringResource(R.string.parent_no_attendance_yet),
                    modifier = Modifier.padding(top = AcademiaDimens.gapSm),
                )
            }
        }

        SectionHeader(
            title = stringResource(R.string.parent_section_payments_title),
            subtitle = stringResource(R.string.parent_section_payments_subtitle),
            modifier = Modifier.fillMaxWidth(),
        )
        if (hijo.mesesVencidos.isNotEmpty()) {
            DebtSection(
                mesesVencidos = hijo.mesesVencidos,
                totalAdeudoHijo = hijo.totalAdeudoHijo,
                moneyFmt = moneyFmt,
                embedInUnifiedLayout = true,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            ParentBlockEmptyState(
                stringResource(R.string.parent_payment_none_child),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AppTintedPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AcademiaDimens.radiusMd),
            containerColor = scheme.surfaceVariant.copy(alpha = 0.28f),
            contentPadding = PaddingValues(
                horizontal = AcademiaDimens.paddingCardCompact,
                vertical = AcademiaDimens.gapMd,
            ),
        ) {
            Text(
                stringResource(R.string.parent_child_expanded_hint),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant.copy(alpha = 0.95f),
            )
        }

        if (hijo.vinculoId != null) {
            TextButton(
                onClick = onRequestUnlink,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.parent_unlink_short))
            }
        }
    }
}

@Composable
private fun ParentMetricaCelda(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier.padding(horizontal = AcademiaDimens.gapMicro),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            valor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = AcademiaDimens.gapMicro),
            maxLines = 2,
        )
        if (hint != null) {
            Text(
                hint,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 2,
            )
        }
    }
}
