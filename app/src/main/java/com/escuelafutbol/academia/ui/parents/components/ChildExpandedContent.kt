package com.escuelafutbol.academia.ui.parents.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
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
    val dividerColor = scheme.outlineVariant.copy(alpha = 0.38f)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
    ) {
        HorizontalDivider(color = dividerColor)

        ParentExpandedLoadingStrip(rendimientoCargando = rendimientoCargando)

        rendimiento?.proximoPartido?.let { px ->
            ParentNextMatchHeroCard(
                p = px,
                dateFmt = dateFmt,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        ParentCompetitionRendimientoSection(
            rendimiento = rendimiento,
            rendimientoCargando = rendimientoCargando,
            modifier = Modifier.padding(top = 10.dp),
        )

        HorizontalDivider(
            Modifier.padding(vertical = 10.dp),
            color = dividerColor,
        )

        ParentSubsectionTitle(
            title = stringResource(R.string.parent_section_training_title),
            subtitle = stringResource(R.string.parent_section_training_subtitle),
        )
        if (hijo.porcentajeAsistenciaEntrenos != null) {
            Text(
                stringResource(R.string.parent_perf_attendance_hist_pct, hijo.porcentajeAsistenciaEntrenos),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.primary,
                modifier = Modifier.padding(top = 6.dp),
            )
        } else {
            Spacer(Modifier.height(4.dp))
        }
        if (hijo.ultimasAsistencias.isNotEmpty()) {
            Text(
                stringResource(R.string.parent_attendance_recent),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            AttendanceSection(
                lineas = hijo.ultimasAsistencias,
                dateFmt = dateFmt,
                showSectionHeading = false,
                modifier = Modifier.padding(top = 0.dp),
            )
        } else {
            ParentBlockEmptyState(
                stringResource(R.string.parent_no_attendance_yet),
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        HorizontalDivider(
            Modifier.padding(vertical = 10.dp),
            color = dividerColor,
        )

        ParentSubsectionTitle(
            title = stringResource(R.string.parent_section_payments_title),
            subtitle = stringResource(R.string.parent_section_payments_subtitle),
        )
        if (hijo.mesesVencidos.isNotEmpty()) {
            DebtSection(
                mesesVencidos = hijo.mesesVencidos,
                totalAdeudoHijo = hijo.totalAdeudoHijo,
                moneyFmt = moneyFmt,
                embedInUnifiedLayout = true,
                modifier = Modifier.padding(top = 6.dp),
            )
        } else {
            ParentBlockEmptyState(
                stringResource(R.string.parent_payment_none_child),
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Surface(
            color = scheme.surfaceVariant.copy(alpha = 0.28f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
        ) {
            Text(
                stringResource(R.string.parent_child_expanded_hint),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant.copy(alpha = 0.95f),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }

        if (hijo.vinculoId != null) {
            TextButton(
                onClick = onRequestUnlink,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                Text(stringResource(R.string.parent_unlink_short))
            }
        }
    }
}
