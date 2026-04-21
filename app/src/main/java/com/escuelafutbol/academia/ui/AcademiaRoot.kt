package com.escuelafutbol.academia.ui

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.data.local.entity.Categoria
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.ui.academia.AcademiaConfigViewModel
import com.escuelafutbol.academia.ui.academia.AcademiaScreen
import com.escuelafutbol.academia.ui.academia.StaffViewModel
import com.escuelafutbol.academia.ui.home.InicioScreen
import com.escuelafutbol.academia.ui.attendance.AttendanceScreen
import com.escuelafutbol.academia.ui.attendance.AttendanceViewModel
import com.escuelafutbol.academia.ui.categoria.CategoriaPickerViewModel
import com.escuelafutbol.academia.ui.finanzas.FinanzasScreen
import com.escuelafutbol.academia.ui.finanzas.FinanzasViewModel
import com.escuelafutbol.academia.ui.categoria.CategoriaSelectionScreen
import com.escuelafutbol.academia.ui.competencias.CompetenciasScreen
import com.escuelafutbol.academia.ui.competencias.CompetenciasViewModel
import com.escuelafutbol.academia.ui.contenido.ContenidoScreen
import com.escuelafutbol.academia.ui.contenido.ContenidoViewModel
import com.escuelafutbol.academia.ui.parents.ParentsScreen
import com.escuelafutbol.academia.ui.parents.ParentsViewModel
import com.escuelafutbol.academia.ui.players.PlayersScreen
import com.escuelafutbol.academia.ui.players.PlayersViewModel
import com.escuelafutbol.academia.ui.stats.StatsScreen
import com.escuelafutbol.academia.ui.stats.StatsViewModel
import com.escuelafutbol.academia.ui.sync.CloudSyncViewModel
import com.escuelafutbol.academia.ui.auth.AcademiaBindingErrorScreen
import com.escuelafutbol.academia.ui.auth.AcademiaBindingLoadingScreen
import com.escuelafutbol.academia.ui.auth.AcademiaBindingUiState
import com.escuelafutbol.academia.ui.auth.AcademiaBindingViewModel
import com.escuelafutbol.academia.ui.auth.AcademiaOnboardingScreen
import com.escuelafutbol.academia.ui.auth.AcademiaPickAcademyScreen
import com.escuelafutbol.academia.ui.auth.AuthViewModel
import com.escuelafutbol.academia.ui.auth.LoginScreen
import com.escuelafutbol.academia.ui.auth.SetNewPasswordScreen
import com.escuelafutbol.academia.ui.auth.SupabaseConfigRequiredScreen
import com.escuelafutbol.academia.ui.auth.isPasswordRecoverySession
import com.escuelafutbol.academia.data.local.model.puedeVerMensualidadEnEsteDispositivo
import com.escuelafutbol.academia.data.local.model.cloudCoachCategoriasPermitidasOperacion
import com.escuelafutbol.academia.data.local.model.categoriaPortadaParaFiltro
import com.escuelafutbol.academia.data.local.model.membresiaNubeAunNoResuelta
import com.escuelafutbol.academia.push.FcmRegistration
import com.escuelafutbol.academia.ui.navigation.rutaPrincipalVisible
import com.escuelafutbol.academia.ui.theme.AcademiaFutbolTheme
import com.escuelafutbol.academia.ui.util.coilLogoModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.combine
import androidx.compose.ui.graphics.vector.ImageVector

private sealed class Tab(
    val route: String,
    val labelRes: Int,
) {
    data object Inicio : Tab("inicio", R.string.tab_home)
    /** Pantalla intermedia: jugadores, asistencia, estadísticas, recursos (no aplica a padre en nube; competencias va directo al menú ☰). */
    data object Equipo : Tab("equipo_hub", R.string.tab_team_hub)
    data object Jugadores : Tab("jugadores", R.string.tab_players)
    data object Asistencia : Tab("asistencia", R.string.tab_attendance)
    data object Estadisticas : Tab("estadisticas", R.string.tab_stats)
    data object Recursos : Tab("contenido", R.string.tab_resources)
    data object Competencias : Tab("competencias", R.string.tab_competitions)
    data object Finanzas : Tab("finanzas", R.string.tab_finances)
    data object Padres : Tab("padres", R.string.tab_parents)
    data object Academia : Tab("academia", R.string.tab_academy)

    companion object {
        val entries = listOf(
            Inicio,
            Equipo,
            Jugadores,
            Asistencia,
            Estadisticas,
            Recursos,
            Competencias,
            Finanzas,
            Padres,
            Academia,
        )

        /** Solo Inicio, Padres y Academia; el resto va al menú superior izquierdo. */
        fun barraInferior(config: AcademiaConfig, uid: String?): List<Tab> = buildList {
            add(Inicio)
            if (rutaPrincipalVisible(Padres.route, config, uid)) add(Padres)
            if (rutaPrincipalVisible(Academia.route, config, uid)) add(Academia)
        }

        /** Rutas del menú desplegable (scroll si hay muchas). */
        fun tabsMenuDesplegable(config: AcademiaConfig, uid: String?): List<Tab> {
            val padreNube =
                config.remoteAcademiaId != null &&
                    config.cloudMembresiaRol?.equals("parent", ignoreCase = true) == true
            return buildList {
                if (!padreNube) {
                    if (rutaPrincipalVisible(Jugadores.route, config, uid)) add(Jugadores)
                    if (rutaPrincipalVisible(Asistencia.route, config, uid)) add(Asistencia)
                    if (rutaPrincipalVisible(Estadisticas.route, config, uid)) add(Estadisticas)
                    if (rutaPrincipalVisible(Recursos.route, config, uid)) add(Recursos)
                    if (rutaPrincipalVisible(Competencias.route, config, uid)) add(Competencias)
                    if (rutaPrincipalVisible(Finanzas.route, config, uid)) add(Finanzas)
                } else {
                    if (rutaPrincipalVisible(Recursos.route, config, uid)) add(Recursos)
                    if (rutaPrincipalVisible(Competencias.route, config, uid)) add(Competencias)
                }
            }
        }
    }
}

@Composable
private fun AcademiaPrincipalNavigationBar(
    tabsVisibles: List<Tab>,
    currentDestination: NavDestination?,
    mostrandoSelectorCategoria: Boolean,
    recursosNoLeidos: Int,
    navController: NavHostController,
    sessionVm: SessionViewModel,
) {
    /** Más baja que [androidx.compose.material3.NavigationBar] (~80 dp) para ganar lista útil en pantalla. */
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(52.dp)
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            tabsVisibles.forEach { tab ->
                val selected =
                    currentDestination?.hierarchy?.any { it.route == tab.route } == true
                val label = stringResource(tab.labelRes)
                val navigate: () -> Unit = {
                    if (mostrandoSelectorCategoria) {
                        sessionVm.cerrarSelectorCategoria()
                    }
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                            } else {
                                Color.Transparent
                            },
                        )
                        .clickable(onClick = navigate)
                        .padding(vertical = 2.dp, horizontal = 2.dp)
                        .semantics { contentDescription = label },
                ) {
                    if (tab == Tab.Recursos && recursosNoLeidos > 0) {
                        val cd = stringResource(
                            R.string.resources_unread_badge_a11y,
                            recursosNoLeidos,
                        )
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(
                                        if (recursosNoLeidos > 9) "9+" else recursosNoLeidos.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            },
                            modifier = Modifier.semantics { contentDescription = cd },
                        ) {
                            Icon(
                                imageVector = iconoVectorTab(tab),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    } else {
                        Icon(
                            imageVector = iconoVectorTab(tab),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

private fun iconoVectorTab(tab: Tab): ImageVector = when (tab) {
    Tab.Inicio -> Icons.Default.Home
    Tab.Equipo -> Icons.Default.Dashboard
    Tab.Jugadores -> Icons.Default.Group
    Tab.Asistencia -> Icons.Default.TaskAlt
    Tab.Estadisticas -> Icons.Default.Assessment
    Tab.Recursos -> Icons.AutoMirrored.Filled.MenuBook
    Tab.Competencias -> Icons.Default.EmojiEvents
    Tab.Finanzas -> Icons.Default.Payments
    Tab.Padres -> Icons.Default.MailOutline
    Tab.Academia -> Icons.Default.Settings
}

@Composable
fun AcademiaRoot(factory: ViewModelProvider.Factory) {
    val context = LocalContext.current
    val app = context.applicationContext as AcademiaApplication
    val authVm: AuthViewModel = viewModel(factory = factory)
    val authSession by authVm.sessionStatus.collectAsState()

    /** Evita desmontar el flujo de academia/binding cuando Auth pasa brevemente a [SessionStatus.Initializing]. */
    var ultimoUserIdAutenticado by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(authSession) {
        when (val s = authSession) {
            is SessionStatus.Authenticated ->
                ultimoUserIdAutenticado = s.session.user?.id?.toString()
            is SessionStatus.NotAuthenticated ->
                ultimoUserIdAutenticado = null
            else -> Unit
        }
    }

    if (app.supabaseClient == null) {
        AcademiaFutbolTheme {
            SupabaseConfigRequiredScreen()
        }
        return
    }

    when {
        authSession is SessionStatus.Authenticated &&
            isPasswordRecoverySession((authSession as SessionStatus.Authenticated).session) -> {
            AcademiaFutbolTheme {
                SetNewPasswordScreen(authVm)
            }
            return
        }
        authSession is SessionStatus.NotAuthenticated -> {
            AcademiaFutbolTheme {
                LoginScreen(authVm)
            }
            return
        }
        authSession is SessionStatus.Initializing && ultimoUserIdAutenticado == null -> {
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
    }

    val bindingVm: AcademiaBindingViewModel = viewModel(factory = factory)
    val bindingState by bindingVm.uiState.collectAsState()
    // Solo al cambiar de usuario. Con sesión «pegada» durante Initializing la clave no se vacía.
    val bindingRefreshKey = when (val s = authSession) {
        is SessionStatus.Authenticated ->
            s.session.user?.id?.toString().orEmpty().ifEmpty { ultimoUserIdAutenticado.orEmpty() }
        else -> ultimoUserIdAutenticado.orEmpty()
    }
    LaunchedEffect(bindingRefreshKey) {
        if (bindingRefreshKey.isNotEmpty()) {
            bindingVm.refresh()
        }
    }

    AcademiaFutbolTheme {
        when (val bs = bindingState) {
            AcademiaBindingUiState.Loading -> AcademiaBindingLoadingScreen()
            is AcademiaBindingUiState.Error -> AcademiaBindingErrorScreen(bs.message) {
                bindingVm.refresh(mostrarPantallaCarga = true)
            }
            AcademiaBindingUiState.NeedsOnboarding -> AcademiaOnboardingScreen(bindingVm)
            is AcademiaBindingUiState.PickAcademy -> AcademiaPickAcademyScreen(bindingVm, bs.options)
            AcademiaBindingUiState.Ready -> {
                AcademiaRootAuthenticatedContent(
                    factory = factory,
                    authVm = authVm,
                    authUserIdKey = bindingRefreshKey,
                )
            }
        }
    }
}

@Composable
private fun AcademiaRootAuthenticatedContent(
    factory: ViewModelProvider.Factory,
    authVm: AuthViewModel,
    /** Mismo criterio que [bindingRefreshKey]: estable aunque Auth esté en [SessionStatus.Initializing]. */
    authUserIdKey: String,
) {
    val context = LocalContext.current
    val app = context.applicationContext as AcademiaApplication
    val application = context.applicationContext as Application
    val scopedFactory = remember(application, authUserIdKey) {
        AcademiaViewModelFactory(
            application,
            app.database,
            sessionAuthUserId = authUserIdKey,
        )
    }
    val sessionVm: SessionViewModel = viewModel(
        key = authUserIdKey.ifEmpty { "session" },
        factory = scopedFactory,
    )
    val configVm: AcademiaConfigViewModel = viewModel(factory = scopedFactory)
    val config by configVm.config.collectAsState()
    val enPrincipal by sessionVm.enMenuPrincipal.collectAsState()
    val filtroCategoria by sessionVm.filtroCategoria.collectAsState()
    val childFactory = remember(sessionVm) {
        AcademiaViewModelFactory(application, app.database, sessionVm)
    }

    LaunchedEffect(
        config.remoteAcademiaId,
        config.cloudMembresiaRol,
        config.cloudCoachCategoriasJson,
        config.remoteAcademiaCuentaUserId,
        authUserIdKey,
    ) {
        val uidSesion = authUserIdKey.takeIf { it.isNotBlank() }
        if (config.membresiaNubeAunNoResuelta(uidSesion)) {
            sessionVm.actualizarRestriccionOperacionCoach(
                permitidas = null,
                esperandoMembresiaNube = true,
            )
        } else {
            sessionVm.actualizarRestriccionOperacionCoach(
                permitidas = config.cloudCoachCategoriasPermitidasOperacion(),
                esperandoMembresiaNube = false,
            )
        }
    }

    AcademiaFutbolTheme(
        colorPrimarioHex = config.temaColorPrimarioHex,
        colorSecundarioHex = config.temaColorSecundarioHex,
    ) {
        AcademiaMainScaffold(
            sessionVm = sessionVm,
            config = config,
            context = context,
            factory = scopedFactory,
            childFactory = childFactory,
            filtroCategoria = filtroCategoria,
            authVm = authVm,
            mostrandoSelectorCategoria = !enPrincipal,
            sessionAuthUserId = authUserIdKey,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AcademiaMainScaffold(
    sessionVm: SessionViewModel,
    config: AcademiaConfig,
    context: android.content.Context,
    factory: ViewModelProvider.Factory,
    childFactory: ViewModelProvider.Factory,
    filtroCategoria: String?,
    authVm: AuthViewModel,
    mostrandoSelectorCategoria: Boolean,
    sessionAuthUserId: String,
) {
    val navController = rememberNavController()
    val appNav = context.applicationContext as AcademiaApplication

    val activity = context as? ComponentActivity
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ -> }

    LaunchedEffect(sessionAuthUserId) {
        if (sessionAuthUserId.isBlank()) return@LaunchedEffect
        FcmRegistration.syncTokenIfPossible(appNav)
        val act = activity ?: return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= 33) {
            val ok = ContextCompat.checkSelfPermission(
                act,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!ok) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    BackHandler(enabled = mostrandoSelectorCategoria) {
        sessionVm.cerrarSelectorCategoria()
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val impideCambiarCategoria by sessionVm.impideVolverASeleccionCategoria.collectAsState()

    val etiquetaCategoria = if (filtroCategoria == null) {
        stringResource(R.string.category_all)
    } else {
        filtroCategoria!!
    }

    val etiquetaCuentaSesion = authVm.cuentaEtiquetaVisible()
    val sessionBarAccountLabel = stringResource(R.string.session_bar_account_label)

    val uidSesionRol = sessionAuthUserId.takeIf { it.isNotBlank() }
    val tabsVisibles = remember(config, uidSesionRol) {
        Tab.barraInferior(config, uidSesionRol)
    }
    val opcionesMenuNavegacion = remember(config, uidSesionRol) {
        Tab.tabsMenuDesplegable(config, uidSesionRol)
    }
    var menuNavegacionExpanded by remember { mutableStateOf(false) }
    val menuNavCd = stringResource(R.string.nav_main_menu_cd)

    LaunchedEffect(navController, config, uidSesionRol) {
        appNav.pendingNavigationRoute.collect { route ->
            if (rutaPrincipalVisible(route, config, uidSesionRol)) {
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    val syncVm: CloudSyncViewModel = viewModel(factory = factory)
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val activityForContenido = LocalContext.current as ComponentActivity
    val contenidoVmGlobal: ContenidoViewModel = viewModel(
        viewModelStoreOwner = activityForContenido,
        key = "contenido_global_$sessionAuthUserId",
        factory = childFactory,
    )
    val recursosNoLeidos by contenidoVmGlobal.recursosNoLeidosCount.collectAsState()

    val enRecursos = currentDestination?.hierarchy?.any { it.route == Tab.Recursos.route } == true
    val cabeceraPrincipalScrollState = rememberTopAppBarState()
    val cabeceraPrincipalScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(cabeceraPrincipalScrollState)

    LaunchedEffect(enRecursos) {
        if (!enRecursos) {
            cabeceraPrincipalScrollState.heightOffset = 0f
        }
    }

    LaunchedEffect(Unit) {
        syncVm.scheduleInitialDelayedSync()
    }

    LaunchedEffect(snackbarHostState) {
        syncVm.userVisibleSyncIssues.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long,
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                syncVm.onLifecycleResume()
                contenidoVmGlobal.refrescar()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        modifier = if (enRecursos) {
            Modifier.nestedScroll(cabeceraPrincipalScrollBehavior.nestedScrollConnection)
        } else {
            Modifier
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                scrollBehavior = if (enRecursos) cabeceraPrincipalScrollBehavior else null,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                navigationIcon = {
                    Box {
                        IconButton(
                            onClick = { menuNavegacionExpanded = true },
                            modifier = Modifier.semantics { contentDescription = menuNavCd },
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = menuNavegacionExpanded,
                            onDismissRequest = { menuNavegacionExpanded = false },
                        ) {
                            // No usar Column+verticalScroll aquí: con altura sin acotar provoca crash
                            // ("scrollable with infinity max height"). Material3 ya limita y desplaza el menú.
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.change_category)) },
                                enabled = !impideCambiarCategoria && !mostrandoSelectorCategoria,
                                onClick = {
                                    menuNavegacionExpanded = false
                                    sessionVm.volverASeleccionCategoria()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null)
                                },
                            )
                            if (opcionesMenuNavegacion.isNotEmpty()) {
                                HorizontalDivider()
                                opcionesMenuNavegacion.forEach { tab ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(stringResource(tab.labelRes))
                                                if (tab == Tab.Recursos && recursosNoLeidos > 0) {
                                                    Spacer(Modifier.width(8.dp))
                                                    Badge {
                                                        Text(
                                                            if (recursosNoLeidos > 9) {
                                                                "9+"
                                                            } else {
                                                                recursosNoLeidos.toString()
                                                            },
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        onClick = {
                                            menuNavegacionExpanded = false
                                            if (mostrandoSelectorCategoria) {
                                                sessionVm.cerrarSelectorCategoria()
                                            }
                                            navController.navigate(tab.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(iconoVectorTab(tab), contentDescription = null)
                                        },
                                    )
                                }
                            }
                        }
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                        Column(
                            modifier = Modifier.weight(1f, fill = true),
                        ) {
                            Text(
                                config.nombreAcademia,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                stringResource(R.string.working_in, etiquetaCategoria),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (!etiquetaCuentaSesion.isNullOrBlank()) {
                                Text(
                                    etiquetaCuentaSesion,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.semantics {
                                        contentDescription =
                                            "$sessionBarAccountLabel: $etiquetaCuentaSesion"
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (enRecursos) {
                val mostrarBarraInferior by remember {
                    derivedStateOf {
                        cabeceraPrincipalScrollState.collapsedFraction < 0.88f
                    }
                }
                AnimatedVisibility(
                    visible = mostrarBarraInferior,
                    enter = slideInVertically(
                        animationSpec = tween(220),
                        initialOffsetY = { it },
                    ) + fadeIn(animationSpec = tween(220)),
                    exit = slideOutVertically(
                        animationSpec = tween(220),
                        targetOffsetY = { it },
                    ) + fadeOut(animationSpec = tween(220)),
                ) {
                    AcademiaPrincipalNavigationBar(
                        tabsVisibles = tabsVisibles,
                        currentDestination = currentDestination,
                        mostrandoSelectorCategoria = mostrandoSelectorCategoria,
                        recursosNoLeidos = recursosNoLeidos,
                        navController = navController,
                        sessionVm = sessionVm,
                    )
                }
            } else {
                AcademiaPrincipalNavigationBar(
                    tabsVisibles = tabsVisibles,
                    currentDestination = currentDestination,
                    mostrandoSelectorCategoria = mostrandoSelectorCategoria,
                    recursosNoLeidos = recursosNoLeidos,
                    navController = navController,
                    sessionVm = sessionVm,
                )
            }
        },
    ) { innerPadding ->
        val syncing by syncVm.syncing.collectAsState()
        val pullRefreshState = rememberPullToRefreshState()
        val pullRefreshCd = stringResource(R.string.pull_to_refresh_cd)
        PullToRefreshBox(
            isRefreshing = syncing,
            onRefresh = { syncVm.requestManualSync() },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .semantics { contentDescription = pullRefreshCd },
        ) {
            if (mostrandoSelectorCategoria) {
                val pickerVm: CategoriaPickerViewModel = viewModel(factory = factory)
                val categoriasCoach by sessionVm.categoriasPermitidasOperacion.collectAsState()
                val esperandoMembresia by sessionVm.esperandoMembresiaNubeParaSelector.collectAsState()
                CategoriaSelectionScreen(
                    sessionVm = sessionVm,
                    pickerVm = pickerVm,
                    config = config,
                    categoriasPermitidasCoach = categoriasCoach,
                    esperandoMembresiaNube = esperandoMembresia,
                    modifier = Modifier.fillMaxSize(),
                    onCategoriaConfirmada = {
                        navController.navigate(Tab.Inicio.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            } else {
                NavHost(
                    navController = navController,
                    startDestination = Tab.Inicio.route,
                    modifier = Modifier.fillMaxSize(),
                ) {
            composable(Tab.Equipo.route) {
                EquipoHubScreen(
                    config = config,
                    uidSesion = uidSesionRol,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Tab.Inicio.route) {
                val appInicio = context.applicationContext as AcademiaApplication
                val categoriaInicio by produceState<Categoria?>(null, filtroCategoria) {
                    val nombreCat = filtroCategoria
                    if (nombreCat == null) {
                        value = null
                        return@produceState
                    }
                    val daoCat = appInicio.database.categoriaDao()
                    val daoJug = appInicio.database.jugadorDao()
                    combine(
                        daoCat.observeAllOrdered(),
                        daoJug.observeCategorias(),
                    ) { tabla, jugadores ->
                        categoriaPortadaParaFiltro(nombreCat, tabla, jugadores)
                    }.collect { value = it }
                }
                InicioScreen(
                    config = config,
                    categoriaPortada = categoriaInicio,
                    categoriaEtiqueta = etiquetaCategoria,
                    accesoRapidoVisible = { route ->
                        rutaPrincipalVisible(route, config, uidSesionRol)
                    },
                    sesionEtiqueta = etiquetaCuentaSesion,
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
                    sessionVm = sessionVm,
                    categoriaFiltro = filtroCategoria,
                    configAcademia = cfg,
                    sessionAuthUserId = sessionAuthUserId,
                )
            }
            composable(Tab.Asistencia.route) {
                val vm: AttendanceViewModel = viewModel(factory = childFactory)
                AttendanceScreen(vm, filtroCategoria)
            }
            composable(Tab.Estadisticas.route) {
                val vm: StatsViewModel = viewModel(factory = childFactory)
                StatsScreen(
                    viewModel = vm,
                    configAcademia = config,
                    sessionAuthUserId = sessionAuthUserId,
                )
            }
            composable(Tab.Recursos.route) {
                ContenidoScreen(
                    viewModel = contenidoVmGlobal,
                    config = config,
                    categoriaFiltro = filtroCategoria,
                )
            }
            composable(Tab.Competencias.route) {
                val vm: CompetenciasViewModel = viewModel(
                    key = "competencias_$sessionAuthUserId",
                    factory = childFactory,
                )
                CompetenciasScreen(viewModel = vm, config = config)
            }
            composable(Tab.Finanzas.route) {
                val vm: FinanzasViewModel = viewModel(factory = childFactory)
                FinanzasScreen(
                    viewModel = vm,
                    puedeVerFinanzas = config.puedeVerMensualidadEnEsteDispositivo(uidSesionRol),
                )
            }
            composable(Tab.Padres.route) {
                val vm: ParentsViewModel = viewModel(factory = childFactory)
                ParentsScreen(
                    viewModel = vm,
                    remoteAcademiaId = config.remoteAcademiaId,
                )
            }
            composable(Tab.Academia.route) {
                val cfg: AcademiaConfigViewModel = viewModel(factory = factory)
                val stf: StaffViewModel = viewModel(factory = factory)
                AcademiaScreen(
                    configVm = cfg,
                    staffVm = stf,
                    viewModelFactory = factory,
                    sessionAuthUserId = sessionAuthUserId,
                    onSignOut = { authVm.signOut() },
                )
            }
                }
            }
        }
    }
}
