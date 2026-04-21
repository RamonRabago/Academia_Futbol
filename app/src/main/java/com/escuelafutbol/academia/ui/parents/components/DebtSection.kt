package com.escuelafutbol.academia.ui.parents.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.parents.MesAdeudoPadreUi
import java.text.NumberFormat

@Composable
fun DebtSection(
    mesesVencidos: List<MesAdeudoPadreUi>,
    totalAdeudoHijo: Double,
    moneyFmt: NumberFormat,
    modifier: Modifier = Modifier,
    /** Si es true, no se repite el título de sección ni el divisor (el padre ya mostró cabecera unificada). */
    embedInUnifiedLayout: Boolean = false,
) {
    if (mesesVencidos.isEmpty()) {
        return
    }
    val scheme = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (!embedInUnifiedLayout) {
            HorizontalDivider(Modifier.padding(vertical = 2.dp), color = scheme.outlineVariant.copy(alpha = 0.4f))
            Text(
                stringResource(R.string.parent_payment_section_child),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.primary,
            )
        }
        Text(
            stringResource(R.string.parent_payment_child_owed, moneyFmt.format(totalAdeudoHijo)),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurface,
            modifier = Modifier.padding(top = if (embedInUnifiedLayout) 0.dp else 2.dp),
        )
        mesesVencidos.forEach { mes ->
            Text(
                stringResource(
                    R.string.parent_payment_month_line,
                    mes.etiquetaMes,
                    moneyFmt.format(mes.saldoPendiente),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
