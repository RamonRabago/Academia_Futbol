package com.escuelafutbol.academia.ui.parents.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.parents.LineaAsistenciaPadreUi
import java.text.DateFormat
import java.util.Date

@Composable
fun AttendanceSection(
    lineas: List<LineaAsistenciaPadreUi>,
    dateFmt: DateFormat,
    modifier: Modifier = Modifier,
    /** Si es false, el padre ya mostró un título de sección arriba (evita duplicar encabezado). */
    showSectionHeading: Boolean = true,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (showSectionHeading) {
            Text(
                stringResource(R.string.parent_attendance_recent),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        lineas.forEach { linea ->
            val fecha = dateFmt.format(Date(linea.fechaDia))
            val presente = linea.presente
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f),
                ) {
                    Text(
                        fecha,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (presente) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f)
                    },
                ) {
                    Text(
                        stringResource(
                            if (presente) R.string.parent_attendance_present else R.string.parent_attendance_absent,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (presente) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}
