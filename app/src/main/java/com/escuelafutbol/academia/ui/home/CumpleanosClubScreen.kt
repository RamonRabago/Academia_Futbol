package com.escuelafutbol.academia.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.AppTintedPanel
import com.escuelafutbol.academia.ui.design.SectionHeader
import com.escuelafutbol.academia.ui.util.coilFotoJugadorModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private data class CumpleProximoUi(
    val item: InicioCumpleaneroUi,
    val fechaCumple: LocalDate,
    val edad: Int?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CumpleanosClubScreen(
    items: List<InicioCumpleaneroUi>,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val hoy = remember { LocalDate.now(ZoneId.systemDefault()) }
    val cumpleHoy = remember(items, hoy) {
        items.filter { esCumpleHoyScreen(it.fechaNacimientoMillis, hoy) }
            .sortedBy { it.nombre.lowercase() }
    }
    val cumpleProximos7 = remember(items, hoy) {
        items.mapNotNull { item ->
            val fecha = proximaFechaCumpleScreen(item.fechaNacimientoMillis, hoy) ?: return@mapNotNull null
            val dias = ChronoUnit.DAYS.between(hoy, fecha).toInt()
            if (dias !in 1..7) return@mapNotNull null
            CumpleProximoUi(item, fecha, edadQueCumpliraScreen(item.fechaNacimientoMillis, fecha))
        }.sortedBy { it.fechaCumple }
    }
    val cumpleEsteMes = remember(items, hoy) {
        items.mapNotNull { item ->
            val fecha = proximaFechaCumpleScreen(item.fechaNacimientoMillis, hoy) ?: return@mapNotNull null
            if (fecha.month != hoy.month) return@mapNotNull null
            CumpleProximoUi(item, fecha, edadQueCumpliraScreen(item.fechaNacimientoMillis, fecha))
        }.sortedBy { it.fechaCumple }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.birthday_club_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.player_detail_back_cd),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AcademiaDimens.paddingScreenHorizontal),
            contentPadding = PaddingValues(bottom = AcademiaDimens.paddingCard),
            verticalArrangement = Arrangement.spacedBy(AcademiaDimens.spacingListSection),
        ) {
            item {
                CumpleSeccionHoy(cumpleHoy = cumpleHoy)
            }
            item {
                CumpleSeccionProximos(
                    title = stringResource(R.string.birthday_club_next_7_title),
                    subtitle = stringResource(R.string.birthday_club_next_7_subtitle),
                    items = cumpleProximos7,
                )
            }
            item {
                CumpleSeccionProximos(
                    title = stringResource(R.string.birthday_club_month_title),
                    subtitle = stringResource(R.string.birthday_club_month_subtitle),
                    items = cumpleEsteMes,
                )
            }
        }
    }
}

@Composable
private fun CumpleSeccionHoy(cumpleHoy: List<InicioCumpleaneroUi>) {
    if (cumpleHoy.isEmpty()) return
    AppCard {
        SectionHeader(
            title = stringResource(R.string.birthday_club_today_title),
            subtitle = stringResource(R.string.birthday_club_today_subtitle),
        )
        Spacer(Modifier.padding(top = AcademiaDimens.gapMd))
        cumpleHoy.forEach { item ->
            CumpleItemRow(item = item, fecha = LocalDate.now(ZoneId.systemDefault()), edad = calcularEdadHoy(item.fechaNacimientoMillis))
        }
    }
}

@Composable
private fun CumpleSeccionProximos(
    title: String,
    subtitle: String,
    items: List<CumpleProximoUi>,
) {
    if (items.isEmpty()) return
    AppCard {
        SectionHeader(title = title, subtitle = subtitle)
        Spacer(Modifier.padding(top = AcademiaDimens.gapMd))
        items.forEach { row ->
            CumpleItemRow(item = row.item, fecha = row.fechaCumple, edad = row.edad)
        }
    }
}

@Composable
private fun CumpleItemRow(
    item: InicioCumpleaneroUi,
    fecha: LocalDate,
    edad: Int?,
) {
    val context = LocalContext.current
    val meses = remember {
        listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    }
    val fechaTexto = remember(fecha) { "${fecha.dayOfMonth} ${meses[fecha.monthValue - 1]}" }
    AppTintedPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AcademiaDimens.gapSm),
        shape = RoundedCornerShape(AcademiaDimens.radiusMd),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentPadding = PaddingValues(AcademiaDimens.paddingCardCompact),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AcademiaDimens.gapMd),
        ) {
            val fotoModel = coilFotoJugadorModel(context, item.fotoUrlSupabase, item.fotoRutaAbsoluta)
            Box(
                modifier = Modifier
                    .size(AcademiaDimens.avatarResultRow)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (fotoModel != null) {
                    AsyncImage(
                        model = fotoModel,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(AcademiaDimens.iconSizeSm),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.categoria,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = fechaTexto,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                edad?.let {
                    Text(
                        text = stringResource(R.string.birthday_upcoming_turning_age, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun esCumpleHoyScreen(ms: Long?, hoy: LocalDate): Boolean {
    if (ms == null) return false
    val nacimiento = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
    return nacimiento.month == hoy.month && nacimiento.dayOfMonth == hoy.dayOfMonth
}

private fun proximaFechaCumpleScreen(ms: Long?, hoy: LocalDate): LocalDate? {
    if (ms == null) return null
    val nacimiento = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
    var next = nacimiento.withYear(hoy.year)
    if (!next.isAfter(hoy)) next = next.plusYears(1)
    return next
}

private fun edadQueCumpliraScreen(ms: Long?, proximoCumple: LocalDate): Int? {
    if (ms == null) return null
    val nacimiento = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
    val edad = proximoCumple.year - nacimiento.year
    return edad.takeIf { it >= 0 }
}

private fun calcularEdadHoy(ms: Long?): Int? {
    if (ms == null) return null
    val hoy = LocalDate.now(ZoneId.systemDefault())
    val nacimiento = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
    val edad = hoy.year - nacimiento.year
    return edad.takeIf { it >= 0 }
}
