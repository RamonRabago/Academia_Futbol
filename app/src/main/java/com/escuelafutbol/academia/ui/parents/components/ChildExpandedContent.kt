package com.escuelafutbol.academia.ui.parents.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.parents.HijoResumenUi
import java.text.DateFormat
import java.text.NumberFormat

@Composable
fun ChildExpandedContent(
    hijo: HijoResumenUi,
    moneyFmt: NumberFormat,
    dateFmt: DateFormat,
    onRequestUnlink: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        HorizontalDivider()
        Text(
            stringResource(R.string.parent_child_expanded_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp),
        )
        if (hijo.ultimasAsistencias.isNotEmpty()) {
            AttendanceSection(
                lineas = hijo.ultimasAsistencias,
                dateFmt = dateFmt,
                modifier = Modifier.padding(top = 12.dp),
            )
        } else {
            Text(
                stringResource(R.string.parent_no_attendance_yet),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        DebtSection(
            mesesVencidos = hijo.mesesVencidos,
            totalAdeudoHijo = hijo.totalAdeudoHijo,
            moneyFmt = moneyFmt,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (hijo.vinculoId != null) {
            TextButton(
                onClick = onRequestUnlink,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text(stringResource(R.string.parent_unlink_short))
            }
        }
    }
}
