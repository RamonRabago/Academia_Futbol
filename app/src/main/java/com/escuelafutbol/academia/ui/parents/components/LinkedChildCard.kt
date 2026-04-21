package com.escuelafutbol.academia.ui.parents.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
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
