package com.escuelafutbol.academia.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.ui.util.coilFotoModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceAlumnoResumenPicker(
    jugadores: List<Jugador>,
    focoJugadorId: Long?,
    onFocoChange: (Long?) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var expandido by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val todos = stringResource(R.string.attendance_student_all)
    val lista = remember(jugadores) {
        jugadores.distinctBy { it.id }.sortedBy { it.nombre.trim() }
    }
    val texto = focoJugadorId?.let { id -> lista.find { it.id == id }?.nombre } ?: todos
    val puedeAbrir = enabled && lista.isNotEmpty()
    val jugadorSel = remember(focoJugadorId, lista) {
        focoJugadorId?.let { id -> lista.find { it.id == id } }
    }
    val modeloFoto = remember(jugadorSel) {
        jugadorSel?.coilFotoModel(context)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
    ) {
        Text(
            stringResource(R.string.attendance_student_picker_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 120.dp),
        )
        if (jugadorSel != null) {
            Box(
                modifier = Modifier
                    .size(AcademiaDimens.avatarRow)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (modeloFoto != null) {
                    AsyncImage(
                        model = modeloFoto,
                        contentDescription = stringResource(R.string.player_photo_cd),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(AcademiaDimens.iconInset),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        ExposedDropdownMenuBox(
            expanded = expandido && puedeAbrir,
            onExpandedChange = { if (puedeAbrir) expandido = it },
            modifier = Modifier.weight(1f),
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = {},
                readOnly = true,
                enabled = puedeAbrir,
                singleLine = true,
                label = null,
                textStyle = MaterialTheme.typography.bodyMedium,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido && puedeAbrir) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = puedeAbrir),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expandido && puedeAbrir,
                onDismissRequest = { expandido = false },
            ) {
                DropdownMenuItem(
                    text = { Text(todos) },
                    onClick = {
                        onFocoChange(null)
                        expandido = false
                    },
                )
                lista.forEach { j ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                j.nombre,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = if (j.id == focoJugadorId) {
                                    MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                } else {
                                    MaterialTheme.typography.bodyLarge
                                },
                            )
                        },
                        onClick = {
                            onFocoChange(j.id)
                            expandido = false
                        },
                    )
                }
            }
        }
    }
}
