package com.escuelafutbol.academia.ui.parents.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.parents.MensajeCategoriaUi
import com.escuelafutbol.academia.ui.parents.MensajeCategoriaTipo
import java.text.DateFormat
import java.util.Date

@Composable
fun ParentsInboxMessageCard(m: MensajeCategoriaUi, dateTimeFmt: DateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    m.categoriaNombre,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    parentsInboxTipoEtiqueta(m.tipo),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Text(m.titulo, style = MaterialTheme.typography.titleSmall)
            Text(m.cuerpo, style = MaterialTheme.typography.bodyMedium)
            Text(
                dateTimeFmt.format(Date(m.createdAtMillis)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun parentsInboxTipoEtiqueta(tipo: String): String = when (tipo) {
    MensajeCategoriaTipo.PARTIDO_EVENTO -> stringResource(R.string.msg_type_partido)
    MensajeCategoriaTipo.CONVIVIO_LOGISTICA -> stringResource(R.string.msg_type_convivio)
    MensajeCategoriaTipo.ADMINISTRATIVO -> stringResource(R.string.msg_type_admin)
    else -> stringResource(R.string.msg_type_otro)
}
