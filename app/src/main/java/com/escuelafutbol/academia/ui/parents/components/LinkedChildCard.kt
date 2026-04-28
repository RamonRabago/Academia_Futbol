package com.escuelafutbol.academia.ui.parents.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.parents.HijoRendimientoCompPadreUi
import com.escuelafutbol.academia.ui.parents.HijoResumenUi
import java.text.DateFormat
import java.text.NumberFormat

@Composable
fun LinkedChildCard(
    hijo: HijoResumenUi,
    rendimiento: HijoRendimientoCompPadreUi?,
    rendimientoCargando: Boolean,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    moneyFmt: NumberFormat,
    dateFmt: DateFormat,
    reglaLimitePagoActiva: Boolean,
    onRequestUnlink: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        elevated = false,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.92f),
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
    ) {
        Column(Modifier.fillMaxWidth()) {
            ChildHeaderRow(
                hijo = hijo,
                expanded = expanded,
                moneyFmt = moneyFmt,
                showBalance = reglaLimitePagoActiva,
                onClick = onExpandToggle,
            )
            AnimatedVisibility(visible = expanded) {
                ChildExpandedContent(
                    hijo = hijo,
                    rendimiento = rendimiento,
                    rendimientoCargando = rendimientoCargando,
                    moneyFmt = moneyFmt,
                    dateFmt = dateFmt,
                    onRequestUnlink = onRequestUnlink,
                )
            }
        }
    }
}
