package com.escuelafutbol.academia.ui

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.data.local.entity.Categoria
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.academia.AcademiaConfigViewModel
import com.escuelafutbol.academia.ui.academia.AcademiaScreen
import com.escuelafutbol.academia.ui.academia.StaffViewModel
import com.escuelafutbol.academia.ui.home.InicioScreen
import com.escuelafutbol.academia.ui.attendance.AttendanceScreen
import com.escuelafutbol.academia.ui.attendance.AttendanceViewModel
import com.escuelafutbol.academia.ui.categoria.CategoriaPickerViewModel
import com.escuelafutbol.academia.ui.categoria.CategoriaSelectionScreen
import com.escuelafutbol.academia.ui.parents.ParentsScreen
import com.escuelafutbol.academia.ui.parents.ParentsViewModel
import com.escuelafutbol.academia.ui.players.PlayersScreen
import com.escuelafutbol.academia.ui.players.PlayersViewModel
import com.escuelafutbol.academia.ui.stats.StatsScreen
import com.escuelafutbol.academia.ui.stats.StatsViewModel
import com.escuelafutbol.academia.ui.auth.AuthViewModel
import com.escuelafutbol.academia.ui.auth.LoginScreen
import com.escuelafutbol.academia.ui.auth.SupabaseConfigRequiredScreen
import com.escuelafutbol.academia.data.local.model.RolDispositivo
import com.escuelafutbol.academia.ui.theme.AcademiaFutbolTheme
import com.escuelafutbol.academia.ui.util.coilLogoModel
import io.github.jan.supabase.auth.status.SessionStatus

private sealed class Tab(
    val route: String,
    val labelRes: Int,
) {
    data object Inicio : Tab("inicio", R.string.tab_home)
    data object Jugadores : Tab("jugadores", R.string.tab_players)
    data object Asistencia : Tab("asistencia", R.string.tab_attendance)
    data object Estadisticas : Tab("estadisticas", R.string.tab_stats)
    data object Padres : Tab("padres", R.string.tab_parents)
    data object Academia : Tab("academia", R.string.tab_academy)

    companion object {
        val entries = listOf(Inicio, Jugadores, Asistencia, Estadisticas, Padres, Academia)
    }
}

@Composable
fun AcademiaRoot(factory: ViewModelProvider.Factory) {
    val context = LocalContext.current
    val app = context.applicationContext as AcademiaApplication
    val application = context.applicationContext as Application
    val sessionVm: SessionViewModel = viewModel(factory = factory)
    val authVm: AuthViewModel = viewModel(factory = factory)
    val authSession by authVm.sessionStatus.collectAsState()

    if (app.supabaseClient == null) {
        AcademiaFutbolTheme {
            SupabaseConfigRequiredScreen()
        }
        return
    }

    when (authSession) {
        SessionStatus.Initializing -> {
            AcademiaFutbolTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            return
        }
        is SessionStatus.Authenticated -> { /* continúa abajo */ }
        else -> {
            AcademiaFutbolTheme {
                LoginScreen(authVm)
            }
            return
        }
    }

    val configVm: AcademiaConfigViewModel = viewModel(factory = factory)
    val config by configVm.config.collectAsState()
    val enPrincipal by sessionVm.enMenuPrincipal.collectAsState()
    val filtroCategoria by sessionVm.filtroCategoria.collectAsState()
    val childFactory = remember(sessionVm) {
        AcademiaViewModelFactory(application, app.database, sessionVm)
    }

    AcademiaFutbolTheme(
        colorPrimarioHex = config.temaColorPrimarioHex,
        colorSecundarioHex = config.temaColorSecundarioHex,
    ) {
        if (!enPrincipal) {
            val pickerVm: CategoriaPickerViewModel = viewModel(factory = factory)
            CategoriaSelectionScreen(
                sessionVm = sessionVm,
                pickerVm = pickerVm,
                config = config,
            )
        } else {
            AcademiaMainScaffold(
                sessionVm = sessionVm,
                config = config,
                context = context,
                factory = factory,
                childFactory = childFactory,
                filtroCategoria = filtroCategoria,
                authVm = authVm,
            )
        }
    }
}

@Composable
private fun AcademiaMainScaffold(
    sessionVm: SessionViewModel,
    config: com.escuelafutbol.academia.data.local.entity.AcademiaConfig,
    context: android.content.Context,
    factory: ViewModelProvider.Factory,
    childFactory: ViewModelProvider.Factory,
    filtroCategoria: String?,
    authVm: AuthViewModel,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val etiquetaCategoria = if (filtroCategoria == null) {
        stringResource(R.string.category_all)
    } else {
        filtroCategoria!!
    }

    val rolDispositivo = remember(config.rolDispositivo) {
        RolDispositivo.fromStored(config.rolDispositivo)
    }
    val tabsVisibles = remember(rolDispositivo) {
        Tab.entries.filter { tab ->
            when (tab) {
                Tab.Padres -> rolDispositivo.puedeVerPestañaPadres()
                else -> true
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        val logoModel = config.coilLogoModel(context)
                        if (logoModel != null) {
                            AsyncImage(
                                model = logoModel,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        Column {
                            Text(
                                config.nombreAcademia,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                stringResource(R.string.working_in, etiquetaCategoria),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { sessionVm.volverASeleccionCategoria() },
                        modifier = Modifier.padding(start = 4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text(
                            stringResource(R.string.change_category),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar {
                tabsVisibles.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    Tab.Inicio -> Icons.Default.Home
                                    Tab.Jugadores -> Icons.Default.Group
                                    Tab.Asistencia -> Icons.Default.TaskAlt
                                    Tab.Estadisticas -> Icons.Default.Assessment
                                    Tab.Padres -> Icons.Default.MailOutline
                                    Tab.Academia -> Icons.Default.Settings
                                },
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Inicio.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Tab.Inicio.route) {
                val appInicio = context.applicationContext as AcademiaApplication
                val categoriaInicio by produceState<Categoria?>(null, filtroCategoria) {
                    val nombreCat = filtroCategoria
                    if (nombreCat == null) {
                        value = null
                        return@produceState
                    }
                    appInicio.database.categoriaDao().observeByNombre(nombreCat).collect {
                        value = it
                    }
                }
                InicioScreen(
                    config = config,
                    categoriaPortada = categoriaInicio,
                    categoriaEtiqueta = etiquetaCategoria,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Tab.Jugadores.route) {
                val vm: PlayersViewModel = viewModel(factory = childFactory)
                val cfgVm: AcademiaConfigViewModel = viewModel(factory = factory)
                val cfg by cfgVm.config.collectAsState()
                PlayersScreen(
                    viewModel = vm,
                    categoriaFiltro = filtroCategoria,
                    configAcademia = cfg,
                )
            }
            composable(Tab.Asistencia.route) {
                val vm: AttendanceViewModel = viewModel(factory = childFactory)
                AttendanceScreen(vm, filtroCategoria)
            }
            composable(Tab.Estadisticas.route) {
                val vm: StatsViewModel = viewModel(factory = childFactory)
                StatsScreen(viewModel = vm, configAcademia = config)
            }
            composable(Tab.Padres.route) {
                val vm: ParentsViewModel = viewModel(factory = childFactory)
                ParentsScreen(vm)
            }
            composable(Tab.Academia.route) {
                val cfg: AcademiaConfigViewModel = viewModel(factory = factory)
                val stf: StaffViewModel = viewModel(factory = factory)
                AcademiaScreen(
                    configVm = cfg,
                    staffVm = stf,
                    onSignOut = { authVm.signOut() },
                    viewModelFactory = factory,
                )
            }
        }
    }
}
