@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.escuelafutbol.academia.ui.competencias

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.remote.dto.CatalogoDeporteRow
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.AppTintedPanel
import com.escuelafutbol.academia.ui.design.ChipsGroup
import com.escuelafutbol.academia.ui.design.PrimaryButton
import com.escuelafutbol.academia.ui.design.SectionHeader

private data class TipoCompetenciaChip(
    val codigo: String,
    val labelRes: Int,
)

@Composable
fun NuevaCompetenciaScreen(
    viewModel: CompetenciasViewModel,
    onBack: () -> Unit,
    onCreada: () -> Unit,
) {
    val context = LocalContext.current
    val catalogo by viewModel.catalogoDeportes.collectAsState()
    var nombre by remember { mutableStateOf("") }
    var temporada by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("liga") }
    var deporteSel by remember { mutableStateOf<CatalogoDeporteRow?>(null) }
    var err by remember { mutableStateOf<String?>(null) }
    var nombreError by remember { mutableStateOf<String?>(null) }
    var deporteError by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }

    val tiposUi = remember {
        listOf(
            TipoCompetenciaChip("liga", R.string.competitions_type_liga),
            TipoCompetenciaChip("copa", R.string.competitions_type_copa),
            TipoCompetenciaChip("torneo", R.string.competitions_type_torneo),
            TipoCompetenciaChip("amistoso", R.string.competitions_type_amistoso),
            TipoCompetenciaChip("otro", R.string.competitions_type_otro),
        )
    }

    LaunchedEffect(catalogo) {
        if (deporteSel == null && catalogo.isNotEmpty()) {
            deporteSel = catalogo.first()
        }
    }

    fun validar(): Boolean {
        nombreError = null
        deporteError = null
        err = null
        var ok = true
        if (nombre.isBlank()) {
            nombreError = context.getString(R.string.competitions_error_name_required)
            ok = false
        }
        if (deporteSel == null) {
            deporteError = context.getString(R.string.competitions_error_pick_sport)
            ok = false
        }
        return ok
    }

    BackHandler(enabled = !guardando) { onBack() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.competitions_dialog_new_title))
                },
                navigationIcon = {
                    IconButton(
                        onClick = { if (!guardando) onBack() },
                        enabled = !guardando,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back_cd),
                        )
                    }
                },
            )
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .imePadding()
                        .padding(
                            horizontal = AcademiaDimens.paddingScreenHorizontal,
                            vertical = AcademiaDimens.gapSm,
                        ),
                    verticalArrangement = Arrangement.spacedBy(AcademiaDimens.chipSpacing),
                ) {
                    err?.let { msg ->
                        AppTintedPanel(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AcademiaDimens.radiusMd),
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentPadding = PaddingValues(
                                horizontal = AcademiaDimens.paddingCardCompact,
                                vertical = AcademiaDimens.paddingCardCompact + AcademiaDimens.gapMicro,
                            ),
                        ) {
                            Text(
                                msg,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    PrimaryButton(
                        text = stringResource(R.string.competitions_create_submit),
                        onClick = crear@{
                            if (!validar()) return@crear
                            val d = deporteSel!!
                            guardando = true
                            err = null
                            viewModel.crearCompetencia(
                                nombre = nombre,
                                deporteId = d.id,
                                tipoCompetencia = tipo,
                                temporada = temporada.takeIf { it.isNotBlank() },
                            ) { r ->
                                guardando = false
                                if (r.isSuccess) {
                                    onCreada()
                                } else {
                                    err = r.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                                        ?: context.getString(R.string.competitions_error_save_generic)
                                }
                            }
                        },
                        loading = guardando,
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = AcademiaDimens.paddingScreenHorizontal,
                    vertical = AcademiaDimens.paddingCardCompact,
                ),
            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.chipSpacing + AcademiaDimens.radiusSm),
        ) {
            Text(
                stringResource(R.string.competitions_new_screen_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SectionHeader(
                title = stringResource(R.string.competitions_new_section_general),
                subtitle = null,
            )
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    nombreError = null
                    err = null
                },
                label = { Text(stringResource(R.string.competitions_field_name)) },
                supportingText = nombreError?.let { msg -> { Text(msg) } },
                isError = nombreError != null,
                singleLine = true,
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(R.string.competitions_field_sport_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (catalogo.isEmpty()) {
                Text(
                    stringResource(R.string.competitions_new_catalog_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                // Si el catálogo crece mucho, alternativas compactas (sin implementar aún): ExposedDropdownMenuBox,
                // LazyColumn con Modifier.heightIn(max = …), o fila de chips/segmented con búsqueda.
                catalogo.forEach { d ->
                    val sel = deporteSel?.id == d.id
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !guardando) {
                                deporteSel = d
                                deporteError = null
                                err = null
                            },
                        elevated = false,
                        includeContentPadding = false,
                        containerColor = if (sel) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        border = BorderStroke(
                            width = if (sel) 2.dp else 1.dp,
                            color = if (sel) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                        ),
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = AcademiaDimens.buttonMinHeight + AcademiaDimens.gapMicro)
                                .padding(
                                    horizontal = AcademiaDimens.paddingCard,
                                    vertical = AcademiaDimens.paddingCardCompact + AcademiaDimens.gapMicro,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.chipSpacing + AcademiaDimens.gapSm),
                        ) {
                            Icon(
                                imageVector = if (sel) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = null,
                                modifier = Modifier.size(AcademiaDimens.iconSizeMd),
                                tint = if (sel) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                },
                            )
                            Text(
                                d.nombre,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            deporteError?.let { msg ->
                Text(
                    msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            OutlinedTextField(
                value = temporada,
                onValueChange = {
                    temporada = it
                    err = null
                },
                label = { Text(stringResource(R.string.competitions_field_season_optional)) },
                singleLine = true,
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider(
                Modifier.padding(vertical = AcademiaDimens.radiusSm / 2),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            SectionHeader(
                title = stringResource(R.string.competitions_new_section_competition_type),
                subtitle = stringResource(R.string.competitions_field_type_allowed_hint),
            )
            ChipsGroup {
                tiposUi.forEach { item ->
                    FilterChip(
                        selected = tipo == item.codigo,
                        onClick = {
                            if (!guardando) {
                                tipo = item.codigo
                                err = null
                            }
                        },
                        enabled = !guardando,
                        label = { Text(stringResource(item.labelRes)) },
                    )
                }
            }

            HorizontalDivider(
                Modifier.padding(vertical = AcademiaDimens.radiusSm / 2),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            SectionHeader(
                title = stringResource(R.string.competitions_new_section_optional_config),
                subtitle = null,
            )
            Text(
                stringResource(R.string.competitions_new_config_placeholder),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(
                Modifier.padding(vertical = AcademiaDimens.radiusSm / 2),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            SectionHeader(
                title = stringResource(R.string.competitions_new_section_categories),
                subtitle = null,
            )
            Text(
                stringResource(R.string.competitions_new_categories_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Hueco extra para que, al hacer scroll al final, nada quede bajo la barra fija del botón (y mensaje de error).
            Spacer(
                Modifier.height(
                    AcademiaDimens.buttonMinHeight +
                        AcademiaDimens.paddingCard * 2 +
                        AcademiaDimens.chipSpacing * 2,
                ),
            )
        }
    }
}
