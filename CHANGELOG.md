# Registro de cambios — Escuela Fútbol / Academia

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/). Las fechas usan el calendario del equipo.

## [Sin publicar]

### Añadido

- **Competencias — lista por rol:** tarjetas **staff** (partidos jugados, próximo rival y fecha, categorías inscritas) y **padre** (hijos, categoría, equipo, último resultado con tono victoria/empate/derrota); **resumen** superior para staff; selector **«Ver como»** entre hijos si hay varios; textos y vacíos más claros; botón **Nueva competencia** integrado en la columna (`CompetenciaListaCards`, `CompetenciasScreen`, `CompetenciasViewModel`, `strings.xml`).

- **Estadísticas — economía por categoría:** bloque con **ingreso mensual estimado (ficha)**, orden por estimado, barra comparativa, resumen de **cuatro categorías destacadas** (mayor estimado, mayor cobrado del mes, mayor adeudo del mes, más becados) y textos que distinguen estimado vs cobros reales; si no hay filas en `cobros_mensuales_alumno`, aviso claro y sin columnas de cobrado/adeudo (`StatsViewModel`, `StatsEconomia`, `StatsScreen`, `strings.xml`).

### Cambiado

- **Competencias — detalle partidos:** bloque superior con **categoría activa** (chip destacado o selector **Todas** + categorías), **resumen** (jugados / victorias / empates / derrotas / pendientes) y **Registrar partido** como `Button` principal; lista filtrada **solo en UI** por categoría; mensaje si el filtro no tiene partidos (`CompetenciasScreen`, `strings.xml`).

- **Competencias — categorías en lista:** chips con **hasta 5 nombres** o **4 + «+N»** y **expansión** para ver todas; mismo bloque en **tarjeta padre**; resumen superior con **tres celdas alineadas** (icono, cifra, etiqueta, misma altura y `weight(1f)`) (`CompetenciaListaCards`, `CompetenciasViewModel` solo relleno de nombres ya cargados, `strings.xml`).

- **Competencias — alta de competencia:** el flujo pasa de **diálogo** a **pantalla completa** (formulario con scroll, secciones, tarjetas para elegir deporte, validación en campos y **Guardar** fijo abajo con estado de carga; sin cambios en ViewModel ni backend) (`CompetenciasScreen`, `strings.xml`).

- **Estadísticas — mes del bloque económico:** `YearMonth` en `StatsViewModel` (anterior / siguiente / mes actual / lista de meses, sin meses futuros); **cobrado y adeudo** y tops asociados recalculan con el mes elegido; **ficha** (estimado y conteos) sin cambio al cambiar mes; UI con secciones «Según ficha actual» vs «Cobros del mes elegido» y colores más claros (`StatsScreen`, `strings.xml`).

- **Estadísticas — selector de mes:** «Mes actual» y flecha a mes siguiente deshabilitados si ya estás en el mes corriente; divisores y tipografía para jerarquizar ficha vs cobros (`StatsScreen`).

- **Estadísticas — dashboard por categoría:** lista compacta expandible, chips de orden (estimado / cobrado / adeudo) y filtros (todas / con adeudo / top ingreso), resumen global de totales y colores verde/rojo suave para cobrado y adeudo (`StatsEconomiaDashboard`, `StatsScreen`, `strings.xml`).

- **Estadísticas — dashboard económico:** totales y «tops» solo sobre categorías visibles; texto «Mostrando X de Y»; % cobranza y barra verde/rojo por fila; alertas por cobranza baja y adeudo alto; sección compacta mayor estimado / mayor cobrado / peor cobranza (`StatsEconomiaDashboard`, `strings.xml`).

- **Asistencia — día de entrenamiento inteligente:** detección automática según **días de la semana** en `AcademiaConfig.diasEntrenoSemanaIsoJson` (por defecto martes y jueves, ISO 1–7); prioridad **override manual** por día y filtro de categoría en tabla **`dias_entreno_override`**; migración Room **v33**; textos «Hoy es un día habitual de entrenamiento» / «Detectado automáticamente» cuando no hay override (`AttendanceViewModel`, `AttendanceScreen`, `DiaEntrenamientoReglas`, `strings.xml`).

## [1.0.1] — 2026-04-21

Revisión lista para compartir por WhatsApp: APK **release** (`assembleRelease`) con firma *debug* del SDK (instalable fuera de Play Store; ver `app/build.gradle.kts`). Para **portadas de categoría en cuenta padre**, desplegar en Supabase la migración **`20260422180000_list_my_parent_categorias_portadas_rpc.sql`**.

### Cambiado

- **Nombre visible de la app:** `app_name` pasa a **«Mi Academia»** (inicio del sistema, login y `AndroidManifest`) (`strings.xml`).

- **Asistencia (entrenador):** flujo tipo check-in: **fecha** + **Marcar todos / Limpiar** + **buscador y chips** compactos; **barra contextual** «Es día de entrenamiento» **encima de la lista** (no antes de los botones); **lista** con fila ancha pulsable + **Switch** visible y colores presente/ausente; **resumen** más discreto (**colapsado** por defecto con línea y barra fina; detalle al expandir) (`AttendanceScreen`, `AttendanceSummaryCard`, `strings.xml`).

- **Academia (dueño / gestión):** en el menú de ajustes con tarjetas se **omite la segunda barra** «Academia» (ya existe cabecera global con logo y nombre del club); **lista más densa** (padding, separación y filas compactas); **texto de ayuda corto**; **cabecera de bienvenida** «Bienvenido a tu academia» + nombre del club para dueño de dispositivo (`AcademiaScreen`, `strings.xml`).

- **Identidad del club:** vista **compacta tipo editor** (tres `OutlinedCard`, 8–10 dp entre bloques, menos padding superior de lista); portada con vista previa más baja y **Elegir / Quitar en una fila**; logo **64 dp** con acciones en **fila**; nombre con **Guardar** al lado; textos de ayuda **cortos** (`AcademiaIdentidadClubSeccionCompacta`, `strings.xml`).

- **Mensualidad y privacidad:** mismo **padding compacto** del `LazyColumn` que identidad; **sin título duplicado**; dos **`OutlinedCard`** (permisos + PIN / día límite); textos de ayuda **cortos**; menos espacio entre interruptores y bloques (`AcademiaMensualidadPrivacidadSeccionCompacta`, `strings.xml`).

- **Jugadores:** **buscador** por nombre; **chips** (Todos, por categoría en lista, Adeudo, Pagado, Becados); **agrupación** por categoría con encabezados; tarjeta más **compacta** (avatar 36 dp, menos padding) y **badge** de estado; deuda total por alumno en **solo lectura** desde `cobros_mensuales_alumno` (`PlayersScreen`, `PlayersViewModel.JugadorListaUi`, `strings.xml`).

- **Binding / Supabase:** reintentos automáticos (hasta 3, con espera breve) en lecturas PostgREST críticas de **academias** / **academia_miembros** ante **timeout** u otros fallos de red transitorios (`AcademiaCloudSync.withPostgrestNetworkRetry`).

- **Inicio (padre en nube) — portada por categoría del hijo:** la imagen ancha usa la misma resolución que staff (`categoriaPortadaParaFiltro`) con hijos ordenados por nombre y preferencia por **`jugador_remote_id`** en **`SessionViewModel`** (`parentInicioPortadaJugadorRemoteId`, `reconciliarPortadaPadreConHijos`); reconciliación al cambiar la lista; **`HijoResumenUi.jugadorRemoteId`**; tests de lógica en `SessionParentPortadaReconciliacionTest` (`ParentInicioPortada.kt`, `ParentsViewModel`, `SessionViewModel`, `AcademiaRoot`). **Persistencia Room:** tabla **`session_parent_portada_jugador`** + DAO; migración v**32**; lectura/escritura en `SessionViewModel`; borrado en `signOut` (`AuthViewModel`).

- **Inicio (padre) — selector de portada por hijo:** si hay **más de un hijo** con `remoteId`, fila compacta «Portada» + menú con avatar y nombre; llama a **`setParentInicioPortadaJugadorRemoteId`** (`InicioScreen`, `InicioPadrePortadaOpcion`, `strings.xml`, `AcademiaRoot`).

- **Barra inferior (Inicio / Padres / Academia):** el fondo del tab activo usa **`primaryContainer`**, derivado del **color primario** de la academia en la paleta, en lugar del tinte sobre **`secondaryContainer`** (`AcademiaRoot`).

- **Padres — densidad vertical (solo layout):** menos padding/márgenes en contenedor de pestaña, `LazyColumn`, tarjeta de hijo expandida y bloques (próximo partido, competiciones, entrenos, adeudos, hint); filas de asistencia y chips algo más compactos (`ParentsScreen`, `ParentsPadreConHijosContent`, `LinkedChildCard`, `ChildExpandedContent`, `ParentsChildPerformanceSection`, `AttendanceSection`, `DebtSection`, `ChildHeaderRow`).

- **Padres — insets anidados y cabecera:** el `Scaffold` interno usa **`contentWindowInsets` vacíos** (como Recursos) para no duplicar hueco respecto al `Scaffold` de `AcademiaRoot`; título de pestaña **«PADRES»** centrado en mayúsculas; `LazyColumn` con menos **padding superior/inferior** (`ParentsScreen`, `ParentsPadreConHijosContent`).

- **Copy padres (MX):** competiciones «rendimiento reciente del equipo»; «Entrenamientos» en títulos y asistencia; frase de goles con singular en ventana de 1 partido (`strings.xml`, `ParentsChildPerformanceSection`).

- **Padres — tarjeta expandida (acabado visual):** tipografía y espaciado alineados entre bloques; **estados vacíos** unificados (`ParentBlockEmptyState`) en competiciones, resultados recientes, entrenos y pagos; sección **Pagos** con cabecera propia; chips de asistencia y superficies algo más suaves; hint final más ligero (`ParentsChildPerformanceSection`, `ChildExpandedContent`, `AttendanceSection`, `DebtSection`, `strings.xml`).

- **Padres — tarjeta expandida del hijo (solo UX):** orden fijo **próximo partido → competiciones → entrenos → adeudos → desvincular**; **Próximo partido** en `ElevatedCard` con barra de acento y más jerarquía; secciones con **títulos y subtítulos**; entrenos separados de ligas; hint en contenedor suave al final (`ParentsChildPerformanceSection`, `ChildExpandedContent`, `AttendanceSection`, `DebtSection`, `strings.xml`).

- **Padres (rol tutor en nube):** al expandir un hijo, bloque de **rendimiento**: **próximo partido** (rival, fecha, hora, sede, liga), **resumen compacto** (goles en ligas desde Supabase + **% asistencia a entrenos** desde Room), **últimos resultados del equipo** (G/E/P, máx. 5) y texto **«marcó en X de los últimos Y»**; lista de entrenos recortada a **5** fechas con leyenda de histórico. Cálculo solo lectura vía `AcademiaCompetenciasRepository` + `ParentsRendimientoComputo` (`ParentsViewModel`, `ParentsScreen`, `ParentsPadreConHijosContent`, `ParentsChildPerformanceSection`, `ChildExpandedContent`, `LinkedChildCard`, `strings.xml`).

- **Competencias — Inscripciones (padre):** pestaña con **tarjetas por hijo** vinculado: nombre del alumno, texto «Tu hijo juega en:», **equipo**, **categoría** y **liga o competencia** con etiquetas claras; **staff** conserva la lista técnica anterior. Asociación hijo ↔ inscripción solo por **presentación**: categoría del `Jugador` en Room normalizada = `categoria_nombre` de la inscripción (`CompetenciasScreen`, `CompetenciasViewModel`, `strings.xml`).

### Corregido

- **Asistencia — «Día de entrenamiento» al volver a una fecha:** si la marca se guardó con **categoría concreta** en el menú y después el filtro global pasa a **«todas las categorías»**, el interruptor y el resumen ignoraban filas con `scopeKey` no vacío. Ahora la vista «todas» trata como entreno cualquier marca de ese día de calendario (`DiaEntrenamientoReglas.diaMarcadoComoEntrenamiento`, `AttendanceViewModel.esDiaEntrenamientoMarcado`).

- **Asistencia — resumen al final y aviso:** la lista y el resumen van en **un solo** `LazyColumn` (scroll continuo), `navigationBarsPadding` y aviso si hay **presentes** pero el día **no** está como entreno; **«Marcar todos presentes»** (y variante visible) **activa también** el día de entrenamiento para que el resumen cuente (`AttendanceScreen`, `AttendanceViewModel`, `strings.xml`).

- **Logo del club en la cabecera tras binding con reintentos:** Coil podía no volver a descargar la misma URL si la primera petición falló o quedó «atascada» hasta un pull manual. Tras cada binding exitoso se incrementa una generación y la petición del logo usa otra clave de memoria (`AcademiaBrandingImageReload`, `CoilAcademyImages.kt`, `AcademiaBindingViewModel`).

- **Inicio (padre) — portada de categoría sin imagen:** el rol **parent** no pasa RLS de `categorias` (`academia_staff_data_access`), así que el pull dejaba Room **sin `portada_url`** aunque existiera en Supabase. Nueva RPC **`list_my_parent_categorias_portadas`** (categorías de hijos vinculados) y llamada en **`AcademiaCloudSync.pullCategorias`** junto al refuerzo del coach (`20260422180000_list_my_parent_categorias_portadas_rpc.sql`).

- **Inicio (padre) — selector de portada:** `ExposedDropdownMenuBox` usaba **`DropdownMenu`** en lugar de **`ExposedDropdownMenu`**, lo que provocaba cierre al abrir el menú; se alinea con Material 3 (`ExposedDropdownMenu`, **`menuAnchor`** en la superficie ancla) (`InicioScreen`).

- **Competencias — lista de anotadores (vista padre/partido):** el `Row` de cada anotador dejaba el **nombre con ancho 0** porque la columna de goles usaba `fillMaxWidth()` dentro de un `Row` sin ancho fijo; se usa **ancho ajustado al contenido** para el bloque de cantidad. **Nombre** con respaldo desde `Jugador` en Room si `nombre_mostrado` viene vacío y texto **«Anotador»** si no hay datos; el filtro de filas admite líneas con **`jugador_remote_id`** aunque falte nombre. **Foto:** `SubcomposeAsyncImage` con placeholder mientras carga o si falla la URL (`CompetenciasScreen`, `strings.xml`).

- **UI por rol (padre vs staff):** Inicio sin observar categorías/jugadores globales si es padre en nube; chip «Vista familia» e indicación a «Padres»; `AcademiaScreen` sustituido por vista mínima para padre; `LaunchedEffect` redirige a Inicio si la ruta actual no pasa `rutaPrincipalVisible`; guardas en `NavHost` (Equipo, Jugadores, Asistencia, Estadísticas, Finanzas); early-return en `PlayersScreen`, `AttendanceScreen`, `StatsScreen` y `FinanzasScreen` (`AcademiaRoot`, `InicioScreen`, `AcademiaScreen`, `strings.xml`).

- **Padre en nube y selector de categoría:** el selector de staff ya no tapa el `NavHost` (p. ej. pestaña Padres); «Cambiar categoría» queda deshabilitado para padre; con restricción vacía no se ofrece la tarjeta «Todas las categorías» en el selector (`AcademiaRoot`, `CategoriaSelectionScreen`).

### Añadido

- **Aislamiento por sesión (auth):** `AcademiaSessionManager` + purga transaccional de tablas espejo en Room (`SessionOperationalMirrorDao`); al cerrar sesión y al detectar cambio de usuario se vacían jugadores, categorías, asistencias, staff, cobros, etc.; `AcademiaRoot` purga antes del binding, reinicia árbol con `key(uid)` y claves de `ViewModel` por usuario; padre en nube fuerza cierre del selector de categoría y restricción vacía (no «todas») (`AcademiaApplication`, `AuthViewModel`, `AcademiaRoot`).

- **Competencias — resultado del partido:** registro de **anotadores** (jugador de la categoría o nombre libre + cantidad) guardado en **`detalle_marcador_json`** con modelo `DetalleMarcadorPayload` / `AnotadorMarcadorLinea` (`DetalleMarcadorMarcadorJson.kt`); resumen en la tarjeta del listado para padres; aviso si la suma de anotaciones no coincide con el marcador propio; **fecha del encuentro** editable con calendario en la pantalla de resultado (`CompetenciasScreen`, `CompetenciasViewModel`, `strings.xml`).

- **Competencias — Fase 3 (padre en nube):** acceso **solo lectura** a la misma pantalla de competencias (menú ☰ junto a Recursos, atajo Inicio si aplica); textos de contexto y franja «solo lectura» en detalle; sin crear competencia, partido, inscripción ni editar resultado (`AcademiaNavPolicy`, `AcademiaRoot`, `CompetenciasScreen`, `strings.xml`, `FUNCIONALIDADES.md`, `COMPETENCIAS_FASE1.md`).

- **Competencias (padre):** categorías desde **hijos vinculados** en Room (no categoría de sesión de staff); **filtro local** «Todas mis categorías» + **chips** si hay más de una; lista y detalle filtran inscripciones/partidos/tabla por nombre normalizado; línea de categorías en la tarjeta solo si hay **varias** categorías en la competencia (`CompetenciasViewModel`, `CompetenciasScreen`, `strings.xml`).

### Cambiado

- **Competencias (padre):** vacío en lista distingue **sin vínculos en nube** vs **vínculos sin categorías en Room**; en detalle, aviso cuando el filtrado deja **sin inscripciones** aplicables (`CompetenciasViewModel`, `CompetenciasScreen`, `strings.xml`).

- **Competencias — lista de partidos:** tarjeta con **jornada / fecha vs rival / marcador destacado / chip de estado** (jugado, programado, cancelado, pospuesto) y bloque **Anotadores** con avatar (Coil o placeholder), nombre y goles alineados a la derecha (`CompetenciasScreen`, `strings.xml`).

- **Competencias — lista de partidos (refinamiento):** cabecera **jornada+rival / fecha**, marcador **centrado** más grande, chip bajo el marcador, **tarjeta entera** para expandir, resumen **goles/anotadores** con plurales y filas de anotación más limpias (`CompetenciasScreen`, `strings.xml`).

- **Competencias — partidos:** cabecera en **un solo string** (`vs %2$s`) para evitar **«vsFENIX»** por trim de espacio en recursos; tarjeta **más compacta** y colores **verde / rojo / gris** según victoria, derrota o empate (`CompetenciasScreen`, `strings.xml`).

- **Competencias — fotos de anotadores:** carga de plantilla por **categoría normalizada** y unión por **`jugador_remote_id`** (incluye hijos/padre sin roster completo); comparación de UUID **sin distinguir mayúsculas** (`CompetenciasViewModel`, `CompetenciasScreen`).

- **Competencias — fotos padre / detalle marcador:** en cuenta **padre** Room solo tiene **hijos vinculados** (RLS + `pruneJugadoresNoAutorizadosParaPadre`); el detalle del marcador guarda opcionalmente **`foto_url`** por anotador para que Coil muestre miniatura **sin** depender de tener al compañero en Room; al **re-guardar** se conserva `foto_url` del JSON vía estado de edición; placeholder de lista más claro (`DetalleMarcadorMarcadorJson.kt`, `CompetenciasScreen`, `strings.xml`).

- **Padres — avisos del club (padre con hijos):** módulo **`ParentsClubNoticesSection`** en `OutlinedCard` con cabecera (título, subtítulo «Mensajes de tus categorías», **actualizar con icono**), chips en `FlowRow`, carga con texto, estado vacío con icono y dos líneas, y **feed de tarjetas** dentro del mismo contenedor (`ParentsClubNoticesSection.kt`, `ParentsPadreConHijosContent.kt`, `strings.xml`).

- **Padres — acción única:** solo **«Agregar hijo»** en la tarjeta resumen (icono **+**); eliminado el botón duplicado «+ Vincular hijo» del panel inferior; el panel de candidatos queda **debajo del resumen** para que el toque abra siempre el bloque correcto (`ParentsSummaryCard`, `ParentsLinkChildPanel`, `ParentsPadreConHijosContent`, `ParentsScreen`).

- **Padres — modularización UI:** composables en `ui/parents/components/` (`ParentsSummaryCard`, `LinkedChildCard`, `ChildHeaderRow`, `ChildExpandedContent`, `AttendanceSection`, `DebtSection`, `EmptyChildrenState`, `ParentsLinkChildPanel`, `ParentsInboxMessageCard`) y `ParentsPadreConHijosContent.kt`; tarjetas de hijo **colapsadas por defecto** y **solo una expandida** a la vez; `ParentsScreen` delega el flujo padre con hijos (`ParentsPadreConHijosContent`).

- **Padres — UX «Mis hijos»:** un solo subtítulo breve, botón **«+ Vincular hijo»** y panel desplegable con candidatos; **un único mensaje de vacío** solo si no hay hijos vinculados ni candidatos; sin repetir bloques «Vincular otro hijo» / «no encontramos…» cuando ya hay hijos en lista; orden: título → hijos → aviso de pagos (si aplica) → vincular (`ParentsScreen`, `strings.xml`).

- **Padres — vincular hijo:** texto mientras **consulta la nube**; si hay **candidatos por correo**, el panel se **abre solo** (salvo que el usuario haya pulsado **Ocultar**); texto introductorio y aviso cuando **no hay más candidatos** con hijos ya vinculados (`ParentsScreen`, `strings.xml`).

- **Padres — tarjeta de cada hijo:** **foto circular** (Coil: URL Supabase o archivo local) y **nombre** en hasta dos líneas con **ellipsis**; la acción **Desvincular** pasa a una **segunda fila** alineada a la derecha para no comprimir el texto (`ParentsScreen`, `ParentsViewModel`, `coilFotoJugadorModel` en `CoilAcademyImages.kt`).

- **Área útil en pestañas (cabecera global):** **Finanzas**, **Asistencia**, **Estadísticas**, **Jugadores** y **Padres** dejan de duplicar **`TopAppBar`** respecto a **`AcademiaMainScaffold`**; título compacto en el contenido, menos padding vertical y listas con **`contentPadding`** ajustado (FAB en **Jugadores** alineado en el contenedor). Más filas visibles sin scroll (`FinanzasScreen`, `AttendanceScreen`, `StatsScreen`, `PlayersScreen`, `ParentsScreen`).

- **Finanzas — más lista visible:** en una sola fila **título + navegación de mes**; botón «Prellenar…» y chips con menos aire; pestañas en **fila compacta** sustituyendo `SecondaryTabRow`; menos separación en la lista y en cada tarjeta de alumno (`FinanzasScreen`). **Barra inferior principal** (Inicio / Padres / Academia): sustituye el `NavigationBar` de Material por una barra **más baja** manteniendo icono, etiqueta e insignia de no leídos (`AcademiaRoot`).

- **Academia (modo padre/tutor):** en la tarjeta «Gestión del club» ya no se muestra el párrafo largo de solo lectura; quedan título y nombre de la academia (`AcademiaScreen`).

- **Padres — resumen de adeudos:** en cuenta **padre** ya no se usan todos los jugadores de Room (p. ej. datos previos del dueño en el mismo dispositivo); solo alumnos cuyo `remoteId` está en **`academia_padres_alumnos`** para la sesión actual (`ParentsViewModel`).

- **Padres (Fase 4 — vínculo tutor ↔ alumno):** política RLS **`padres_alumnos_delete_parent_own`** para que el tutor elimine **solo su fila** en `academia_padres_alumnos` (varios tutores por alumno sin límite en esta fase); pantalla **Padres** con **desvincular** por hijo (confirmación), **vincular otro hijo** reutilizando candidatos por email, y refresco de ids de vínculo (`ParentsViewModel`, `ParentsScreen`, `PadresAlumnosRepository`, migración `20260521140000_padres_alumnos_delete_parent_own.sql`, `strings.xml`).

- **Asistencia — más clara y compacta:** cabecera con **título y fecha en una fila**; interruptor de entreno con **icono de ayuda** (textos largos en diálogo); selector **«Ver resumen de»** en línea con el desplegable y avatar más pequeño; **resumen** con chips Mes/Año, **% y barra de progreso en la misma zona visual**, textos de alcance y nota al pie tras **información** en diálogo (`AttendanceScreen`, `AttendanceSummaryCard`, `AttendanceAlumnoResumenPicker`, `strings.xml`).

- **Build release para compartir APK:** el tipo `release` usa la misma **firma *debug*** del SDK (`signingConfig = debug`), de modo que `assembleRelease` genera un **`app-release.apk` firmado** e instalable fuera de Play Store (p. ej. WhatsApp). **No** sustituye un keystore de publicación en tienda (`app/build.gradle.kts`).

- **Competencias — nuevo partido:** **pantalla completa** (`Dialog` + `DialogProperties(usePlatformDefaultWidth = false)`), `Scaffold` con barra superior y **Cancelar / Guardar** abajo; lista con aire; jornada y fecha a **ancho completo**; ayuda de fecha bajo el campo; tarjetas de categoría más amplias; se elimina el párrafo introductorio **redundante** (sin inscripciones no se abre el flujo y la categoría ya se elige en la lista) (`DialogoNuevoPartido`, `CompetenciasScreen`, `strings.xml`).

- **Tema (modo claro):** el esquema de color ya no deja al **Material 3** rellenar superficies con sus valores por defecto (suelen verse **rosados/violetas**); `background`, `surface`, `surfaceVariant` y `surfaceContainer*` pasan a **blanco y grises neutros**; contornos neutros. Modo oscuro: superficies y contenedores **gris carbón** coherentes (`Theme.kt`, `Color.kt`).

- **Competencias — lista de partidos:** cada tarjeta muestra de forma **rápida** victoria, empate o derrota (marcador propio vs rival) con **fondo tonal**, **borde** e **icono** junto al resultado, más **banda vertical** de color; los pendientes conservan estilo neutro (`TabPartidos`, `strings.xml`).

- **Competencias — tabla:** cabecera y filas con la **misma** repartición de `weight` (PJ…Pts + **Var.** compacta); **sin** párrafos introductorios de leyenda en pantalla (la cabecera basta); nombres en una línea con ellipsis y columnas numéricas con **alineación a la derecha** y cifras tabulares (`tnum`); tabla principal en **`Card`** elevado y bloque de líderes en **`OutlinedCard`** más ligero; **líderes ofensivos** solo como ranking cuando el desglose es **coherente** con el marcador propio (si no, mensaje vacío o de inconsistencia); en ranking, **icono de balón** (o básquet) en círculo tonal junto a cada anotador y subtítulo acortado. Dominio: `CompetenciaTablaCalculator.kt`, `CompetenciaLideresOfensivos.kt` (`construirLideresOfensivosTabla`), `CompetenciasViewModel` / `CompetenciasDetalleUi`, `CompetenciasScreen`, `strings.xml`. Guía de evolución: **`docs/COMPETENCIAS_TABLA_EVOLUCION.md`** (referencia también en `COMPETENCIAS_FASE1.md`).

- **Competencias — lista de partidos:** al tocar la fila del partido se **despliega** el bloque de **anotadores** (foto si coincide con la plantilla local, icono de balón, nombres legibles) con **animación** de entrada escalonada; el personal con permiso dispone de **Editar** aparte para abrir el resultado. Ya no se muestra el párrafo corrido de anotadores en la tarjeta colapsada (`TabPartidos`, `strings.xml`).

- **Competencias — resultado del partido (UI):** de diálogo modal a **pantalla completa** con `Scaffold`, cabecera con contexto del encuentro, marcador destacado, anotadores en tarjetas y **selector de jugador en bottom sheet** con búsqueda y **foto** (Coil); barra inferior **Cancelar / Guardar** (`PantallaResultadoPartido`, `FilaAnotadorResultadoUi`). Regla de proyecto **`.cursor/rules/ui-compose-pantallas-calidad.mdc`** para priorizar layout amplio y listas legibles en nuevas pantallas Compose. Ajuste de **espacio en anotadores**: jugador a **ancho completo** y cantidad/borrar en segunda fila; textos con `fillMaxWidth` y ellipsis; barra superior y lista más compactas (`CompetenciasScreen`, `strings.xml`). El formulario de resultado pasa a **`Dialog` a nivel ventana** (`DialogProperties(usePlatformDefaultWidth = false)`) desde **`CompetenciaDetalleScaffold`**, encima de pestañas y barra de competencia, para recuperar área útil.

- **Competencias — nuevo partido:** mismo criterio visual que nueva competencia (`Dialog` + `Surface`), secciones con jerarquía de color, **tarjetas** para elegir categoría inscrita (icono + borde al seleccionar), jornada/fecha en fila, **selector de fecha** con `DatePickerDialog` / icono de calendario (campo solo lectura, ISO para Supabase) y mensajes de error en contenedor tonal (`CompetenciasScreen`, `strings.xml`).

- **Competencias — nueva competencia:** el formulario pasa a un **diálogo a tarjeta** con scroll, **filas de deporte** con icono grande (32 dp), borde resaltado y altura táctil mínima; texto de ayuda bajo «Deporte» y tipo con **placeholder** en lugar del párrafo bajo el campo (`CompetenciasScreen`, `strings.xml`).

### Corregido

- **Competencias — jornada duplicada:** no se puede **registrar** un partido con la **misma jornada** que otro de la **misma categoría inscrita** en la competencia (validación en `CompetenciasViewModel` + mensaje `competitions_error_duplicate_matchday`); al abrir **Registrar partido**, el campo **jornada** se **sugiere** como la siguiente libre por categoría (`CompetenciasScreen`). En **Postgres/Supabase**, migración **`20260520150000_academia_comp_partido_unique_jornada.sql`**: normaliza duplicados ya existentes y crea índice único `(competencia_id, categoria_en_competencia_id, jornada)`.

- **Competencias — resultado:** si el partido queda **jugado** con marcador, el **estado** ya no puede quedar como «programado» (se normaliza al guardar y en la lista si llegan datos incoherentes). Al activar **Jugado** **antes** de la fecha programada del encuentro se abre el **calendario** para fijar la fecha real; si activas **Jugado** el **mismo día** que la fecha programada (calendario local del dispositivo), no se fuerza ese paso (`PantallaResultadoPartido`, `TabPartidos`, `strings.xml`).

- **Competencias — guardar / listar:** el tipo se **normaliza** a `liga` / `copa` / `torneo` / `amistoso` / `otro` antes del `INSERT` (el check SQL rechazaba textos libres o mayúsculas). **Coach** puede crear competencias y ver **borradores sin inscripciones** vía migración **`20260520120000_academia_competencias_coach_insert_borrador.sql`**; el listado en app ya no filtra en cliente las competencias vacías (la visibilidad la marca RLS). Mensaje genérico si Supabase no devuelve detalle (`CompetenciasViewModel`, `CompetenciasScreen`, `strings.xml`).

- **Competencias — RLS `SELECT`:** migración **`20260520130000_academia_competencia_select_staff_data_access.sql`** — staff con **`academia_staff_data_access`** ve todas las competencias de la academia; padres con función **`academia_competencia_padre_puede_ver`** (`SECURITY DEFINER`) para **no encadenar** la RLS de `academia_competencia_categoria` (evita *infinite recursion detected in policy*). **`20260520140000_academia_competencia_select_fix_recursion.sql`** repite el arreglo si ya se aplicó una 201300 antigua. **`AcademiaCompetenciasRepository.listarCompetencias`** devuelve **`Result`** y la UI muestra errores de red/RLS.

- **Asistencia — porcentaje del resumen y estadísticas:** el cálculo ya usa **todos los cupos** (cada alumno en cada día marcado como entrenamiento); quien queda con el interruptor en **no asistió** sin guardar fila en Room cuenta como **ausente**, no se ignora (antes solo entraban quienes tenían registro explícito y el % podía quedar en 100 % de más) (`DiaEntrenamientoReglas`, `AttendanceViewModel`, `StatsViewModel`).

- **Recursos — vista «todas las categorías»:** si una misma publicación se envió a **varias categorías**, el listado ya **no repite** la tarjeta; se muestra **una sola** entrada (categorías en cabecera separadas por coma), reacciones **sumadas** por fila, y **aprobar / rechazar / archivar** actúan sobre **todas** las filas vinculadas (`ContenidoViewModel`, `ContenidoScreen`).

### Añadido

- **Competencias multideporte (Fase 2 — UI staff):** ruta **`competencias`** en **`AcademiaRoot`** (menú ☰), **`AcademiaNavPolicy`** (requiere academia en nube; oculta a padre en nube), atajos en **Inicio** y **Equipo**; pantalla **`CompetenciasScreen`**, **`CompetenciasViewModel`** en **`AcademiaViewModelFactory`**; textos en **`strings.xml`**. RLS coach **`20260520110000_academia_competencias_coach_select.sql`** (lectura de competencias con inscripción en categoría asignada al coach).

- **Competencias multideporte (Fase 1):** migración **`20260520100000_academia_competencias_multideporte_fase1.sql`** (`catalogo_deporte`, `academia_competencia`, `academia_competencia_categoria`, `academia_competencia_partido`, RLS, semilla de deportes); DTOs `CompetenciasMultideporteDto.kt`, **`AcademiaCompetenciasRepository`**, dominio **`ReglasPuntosResolver`**, **`CompetenciaTablaCalculator`**, **`CompetenciasCasosUso`**; guía **`docs/COMPETENCIAS_FASE1.md`** y apunte en **`docs/FUNCIONALIDADES.md`**.

- **Documentación:** catálogo de funcionalidades por áreas de producto en **`docs/FUNCIONALIDADES.md`** (enlazado desde **`docs/APP_CONSTRUCCION.md`**).

- **Recursos — publicar:** el flujo **+** pasa de un diálogo a una **pantalla completa** con tarjetas de contexto, **más espacio para el mensaje** (área de texto ampliada), **Galería** y **Cámara** para portada y fotos del artículo (permiso `CAMERA` + `TakePicture` como en otras pantallas), barra inferior **Cancelar / Publicar** e indicador de envío (`ContenidoScreen`, `strings.xml` `resources_publish_editor_intro`, `resources_publish_close_cd`).

### Cambiado

- **Gestionar miembros (padres y tutores):** la lista muestra **solo** cuentas con rol padre/tutor (sin entrenadores duplicados respecto a Equipo); **chips por categoría** según alumnos vinculados (un tutor con hijos en varias categorías aparece al filtrar cualquiera de ellas); al cambiar vínculos se **recarga** la lista para actualizar categorías (`AcademiaMiembrosViewModel`, `AcademiaMiembrosAdminScreen`, `AcademiaScreen`, `strings.xml`, `members_manage_menu_subtitle`).

- **Gestionar miembros — UI:** tarjetas alineadas al estilo **Equipo técnico** (`StaffCard`): elevación ligera, **avatar con iniciales**, misma jerarquía tipográfica (rol en color primario, categorías en `labelSmall`, contacto), acciones con **iconos** (rol, vínculos, quitar) y **estados vacíos** con icono; bloques de ayuda y filtro en **Surface** con forma grande (`AcademiaMiembrosAdminScreen`, `strings.xml`).

- **Academia (engranaje):** la configuración del club pasa a un **menú de apartados** (invitaciones, identidad, colores, pagos/privacidad, equipo, cuenta); al elegir uno se abre **solo esa sección** con barra superior y **volver atrás**, para reducir la sensación de pantalla amontonada (`AcademiaScreen`, `strings.xml` `academy_settings_hub_intro`, `nav_back_cd`). En el menú inicial también aparece **Gestionar miembros** (academia en nube), con el mismo acceso que desde códigos de invitación (`members_manage_menu_subtitle`).

- **Academia — códigos de invitación:** dentro de ese apartado, **menú en tarjetas** (renovar los tres códigos en una tarjeta, una tarjeta por rol y acceso a miembros); al elegir un rol se abre la **pantalla solo de ese código** (compartir / copiar / correo) y la barra superior muestra el rol con **atrás** al menú de códigos (`InvitacionesSubPantalla`, `academy_invite_hub_intro`, `academy_invite_role_subtitle_code`).

- **Gestionar miembros:** se quita el bloque **«Invitar a nuevas cuentas»** y los códigos por rol de esta pantalla (evita duplicar lo de **Academia → Códigos de invitación**); la lista de miembros queda como contenido principal (`AcademiaMiembrosAdminScreen`, llamada desde `AcademiaScreen`). El texto de ayuda (`members_admin_hint`) indica dónde generar o compartir códigos.

### Añadido

- **Padres — auto-vínculo por correo tutor:** en la pestaña **Padres**, si aún no hay hijos en el dispositivo, el tutor puede **filtrar por categoría** y **vincular** alumnos cuyo `email_tutor` en la nube coincide con su correo de sesión; migración **`20260516180000_padre_auto_vinculo_jugadores_rls.sql`** (RLS `SELECT` candidatos + `INSERT` del vínculo). Tras vincular se ejecuta **sync pull** (`ParentsViewModel`, `PadresAlumnosRepository`, `ParentsScreen`).

### Cambiado

- **Gestionar miembros — vínculo tutor ↔ alumno:** en el diálogo **«Vínculo tutor ↔ alumno»**, chips por **categoría** y candidatos desde la **nube** (no solo Room); la lista usa la RPC **`list_jugadores_para_vinculo_padre_staff`** (migración **`20260516190000_list_jugadores_vinculo_padre_staff_rpc.sql`**) para devolver **todos** los jugadores activos no vinculados, con **fallback** al `SELECT` si la función aún no está en Supabase; el filtro por categoría **normaliza** espacios y mayúsculas y cada fila muestra **nombre — categoría** (`PadresAlumnosRepository`, `AcademiaMiembrosAdminScreen`, `AcademiaMiembrosViewModel`, `strings.xml`). El cuerpo del diálogo pasa a **`BasicAlertDialog`** con **`heightIn` + `verticalScroll`** en orden correcto y altura máx. ~55 % pantalla para poder **desplazar** toda la lista de alumnos.

- **Recursos — cabecera con menú al desplazar:** en la pestaña **Recursos**, la barra superior de la app (☰, logo, academia y categoría) usa **`TopAppBar` + `enterAlwaysScrollBehavior`** enlazada al `nestedScroll` del `Scaffold` principal, de modo que **se oculta al bajar** el listado y **vuelve al subir** (`AcademiaMainScaffold` en `AcademiaRoot`).

- **Recursos — barra inferior al desplazar:** en **Recursos**, la **NavigationBar** (Inicio / Padres / Academia) se **oculta al bajar** el feed y **vuelve al subir**, enlazada al mismo **`collapsedFraction`** de la cabecera principal (`AnimatedVisibility`, `AcademiaPrincipalNavigationBar` en `AcademiaRoot`).

- **Recursos — separación entre publicaciones:** franja **`surfaceVariant`** detrás del listado y **`HorizontalDivider`** gris entre cada post para marcar bien el corte entre publicaciones (`ContenidoScreen`).

- **Recursos — sin hueco superior al desplazar:** se quita la **segunda** `TopAppBar` de la pantalla de recursos (solo queda la del `AcademiaRoot`); título **Recursos** y **filtros** pasan a la **primera fila del `LazyColumn`** (scroll con el feed) y el `Scaffold` interno usa **`contentWindowInsets` vacíos** para no duplicar insets con el padre (`ContenidoScreen`).

- **Recursos — feed a ancho de pantalla:** las publicaciones van **edge-to-edge** (sin margen lateral en la lista), tarjetas con esquinas rectas, carrusel de fotos a **ancho completo** y **menos hueco** entre ítems (`ContenidoScreen`).

- **Recursos — pantalla más limpia:** el texto largo de ayuda pasa a **una línea** (distinta para padre en nube vs staff); los filtros por **tema** y **estado** van a un **panel inferior** (icono embudo en la barra superior) con chips en `FlowRow` y botón **Listo**; más altura útil para el feed (`ContenidoScreen`, `strings.xml`).
- **Recursos — scroll tipo feed:** la intro, cabecera «Publicaciones / Actualizar» y errores van **dentro del `LazyColumn`** (se ocultan al bajar); la **TopAppBar** usa `enterAlwaysScrollBehavior` para ocultarse al desplazar hacia abajo y **volver al subir** (`nestedScroll`, `ContenidoScreen`).

- **Cabecera:** **Cambiar categoría** pasa al menú ☰ (primera opción, icono categoría); se quita el botón de la barra superior para ganar espacio (`AcademiaRoot`, `strings.xml` descripción del menú).

- **Navegación:** barra inferior solo **Inicio**, **Padres** (si aplica) y **Academia**; el resto (jugadores, asistencia, estadísticas, recursos, finanzas o solo recursos para padre en nube) va a un **menú ☰** arriba a la izquierda con **scroll** si no cabe (altura máx. ~55 % pantalla). Sigue existiendo la ruta `equipo_hub` / `EquipoHubScreen` por si se enlaza desde otro sitio (`AcademiaRoot`, `strings.xml`, `AcademiaNavPolicy`).

### Añadido

- **Recursos — badge de no leídos (sin push):** contador local con `recursosUltimaVistaAtMillis` en Room; al volver a la app se refresca el listado y se muestra un **numerito** (máx. 9+) en la pestaña o en el ítem **Recursos** del menú ☰; al abrir Recursos se marca como visto (`ContenidoViewModel`, `AcademiaRoot`, `ContenidoScreen`, `AcademiaConfig`, BD v**31**).

- **Recursos — publicar en todas y moderación:** selector **«Todas las categorías»** (varias categorías → varios inserts), **filtros por estado** (visible / pendiente / rechazado) para staff, interruptor **visible ya para familias** (dueño de cuenta, rol owner o admin en nube) y acciones **Aprobar / Rechazar** (menú de tarjeta, detalle y snackbars). Columna y RLS en migración **`20260515160000_contenido_estado_aprobacion.sql`** (`ContenidoViewModel`, `ContenidoScreen`, `strings.xml`).

- **Recursos — foto a pantalla completa:** al tocar una imagen del **carrusel** (lista o detalle) se abre **`FullscreenImageViewerDialog`** con el título de la publicación (`ContenidoScreen`).

- **Recursos — feed tipo red social:** tarjetas **OutlinedCard** con cabecera (categoría, tema, tiempo relativo), **carrusel** de fotos (portada + galería), texto corto y barra de **reacciones** (❤️ 👏 🙏 💪); publicación con un solo campo de mensaje (**500** caracteres) y título derivado automáticamente. Tabla Supabase **`academia_contenido_reaccion`** (una reacción por usuario y post, RLS alineado a la visibilidad del contenido), repositorio `AcademiaContenidoReaccionRepository`, `ContenidoViewModel` (`alternarReaccionDesdeUi`), migración `20260515140000_academia_contenido_reaccion.sql`.

- **Recursos — fotos dentro del artículo:** hasta **12** imágenes adicionales al texto (misma subida pública a `academia-media`, ruta `…/contenido/cuerpo/…`); columna **`cuerpo_imagenes_urls`** (JSON array en texto); miniaturas en tarjeta y galería al leer el detalle (`ContenidoScreen`, `ContenidoViewModel`, `encodeContenidoCuerpoImagenesUrls`, migración `20260515130000_academia_contenido_cuerpo_imgs_rls_autor.sql`).

- **Recursos — imágenes tipo blog:** foto de **portada opcional** al publicar (galería → subida a Storage `academia-media` con ruta `{uid}/{academiaId}/contenido/…`), columna **`imagen_url`** en Supabase; tarjetas y detalle con vista tipo **artículo** (`ContenidoScreen`, `ContenidoViewModel`, migración `20260502120000_academia_contenido_imagen_url.sql`).

- **Recursos por categoría:** pestaña **Recursos** (contenido educativo: noticias, entreno, nutrición, ejercicio, bienestar, otros), filtros por **tema**, publicación con categoría destino y **quitar del listado** (archivo en nube). Tabla Supabase **`academia_contenido_categoria`**, RLS alineado con avisos a padres (`AcademiaContenidoCategoriaRepository`, `ContenidoScreen`, `AcademiaRoot`). Documento de seguimiento **`docs/APP_CONSTRUCCION.md`**.

- **Asistencia — resumen visual del período:** modo **Mes** / **Año** con botones **relleno + contorno** (queda claro cuál está activo), texto que explica el alcance (mes del día de la cabecera vs año natural), **días distintos con lista**, porcentaje grande y **barra de progreso lineal** Material; año en franja con flechas (`AsistenciaResumenUi.diasConRegistro`, `AttendanceSummaryCard`).
- **Asistencia — resumen por alumno:** lista desplegable **«Ver resumen de»** (todo el equipo o un jugador); el mismo resumen mes/año se filtra a sus marcas (`AttendanceAlumnoResumenPicker`, `focoResumenJugadorId`, `AsistenciaResumenUi.nombreAlumnoFoco`).

- **Asistencia — foto y calendario:** al elegir alumno para el resumen se muestra su **foto** (local o Supabase vía `coilFotoModel`); la **etiqueta del campo** va fuera del `OutlinedTextField` para evitar solapamiento. **Tocar la fecha** abre el **DatePicker** Material (`seleccionarFechaCalendario`, `AttendanceScreen`).

- **Asistencia — lista del día:** cada fila de **Tomar asistencia** muestra **foto circular** del alumno (misma carga que el resumen) además de nombre y categoría (`AttendanceListaDiaFila`, `AttendanceScreen`).

- **Asistencia — día de entrenamiento:** interruptor bajo la fecha para **marcar o quitar** si ese día cuenta como sesión; el resumen mes/año y **Estadísticas** solo usan marcas en días marcados (ámbito = filtro de categoría actual: «todas» o una categoría). Tabla Room **`dias_entrenamiento`** (BD v**30**); al migrar se **prellenan** días que ya tenían asistencias con marca global para no perder el histórico.

### Cambiado

- **Recursos — visibilidad:** las publicaciones nuevas de coordinador/entrenador pasan por **pendiente de aprobación** salvo que dueño/admin elijan publicación directa a familias; en lista y detalle las **reacciones** solo aparecen si el post está **publicado** (`ContenidoScreen`, RLS en migración citada arriba).

- **Visor de imagen a pantalla completa:** **pellizco** para acercar/alejar (1×–5×), **arrastre** para mover la foto ampliada y **doble toque** para restablecer zoom; el fondo oscuro cierra al tocar fuera del contenido central sin bloquear los gestos sobre la imagen (`FullscreenImageViewerDialog`).

- **Icono del launcher:** primer plano del icono adaptativo con el **arte tipo clipboard / deportes** aportado (PNG en `drawable-nodpi/ic_launcher_foreground.png`); fondo **azul oscuro** (`ic_launcher_background`); se elimina el vector genérico anterior.

- **Asistencia y estadísticas — conteo de sesiones:** textos de **días con lista** / **días de entreno** y **asistencia media** aclaran que el criterio son los días marcados como entrenamiento (`strings.xml`, `AttendanceSummaryCard`, `StatsScreen`).

- **Finanzas — prellenado de cobros:** el mes visible se **rellena solo** con las cuotas de ficha (misma regla que el botón) al cambiar **mes**, **alcance** (toda la academia / categoría) o **ficha de jugadores** (alta, becado, mensualidad, etc.); el botón sigue sirviendo para **forzar** una pasada manual (`FinanzasViewModel`).

### Corregido

- **Recursos — publicar como entrenador o coordinador:** las publicaciones pasaban a **pendiente de aprobación** para el dueño porque la app solo enviaba `published` con el interruptor exclusivo de dueño/admin y el INSERT en Supabase solo aceptaba `published` para esos roles. Ahora **coach** (en sus categorías asignadas) y **coordinador** insertan en **publicado** de forma directa; migración **`20260516130000_contenido_insert_published_coach_coordinator.sql`** y `ContenidoViewModel.resolverEstadoInicialPublicacion`.

- **Entrenador — portadas en «Cambiar categoría»:** las miniaturas salen de Room tras `pullCategorias`; si el coach no recibía filas de `categorias` por RLS, solo veía nombres sin foto (mientras **Recursos** sí filtraba por categoría con otra política). Añadidos **SELECT** por categorías asignadas (`categorias_coach_assigned_read`) y RPC **`list_my_coach_categorias_portadas`**; la app fusiona ese resultado en Room y **pull de academia** va antes del pull de categorías (`20260516120000_coach_categorias_portadas_rpc_rls.sql`, `AcademiaCloudSync`).

- **Selector de categoría:** la cabecera (menú ☰) y la **barra inferior** siguen visibles al elegir categoría; al tocar una pestaña o una ruta del menú se cierra el selector con `cerrarSelectorCategoria()` (`AcademiaRoot`). «Cambiar categoría» en el menú queda deshabilitado mientras el selector está abierto.

- **Menú ☰ cabecera:** al abrirlo la app podía cerrarse por un `Column` con `verticalScroll` dentro de `DropdownMenu` (altura máxima infinita). Se elimina ese contenedor y se confía el scroll al `DropdownMenu` de Material3; icono de «Cambiar categoría» → `SwapHoriz` (`AcademiaRoot`).

- **Inicio — icono Recursos:** sustitución de `Icons.Filled.MenuBook` por `Icons.AutoMirrored.Filled.MenuBook` en acceso rápido (quita deprecación en `compileDebugKotlin`).

- **Recursos — «Quitar del listado»:** RPC **`archivar_academia_contenido_categoria(p_id, p_academia_id)`** (`SECURITY DEFINER`, permisos = autor, dueño, admin/coord, coach de categoría); la app **usa primero la RPC** y **fallback al UPDATE** si la función no está desplegada. Snackbar de error con **detalle del mensaje** de la API. Complemento histórico: RLS UPDATE con autor en **`20260515130000_academia_contenido_cuerpo_imgs_rls_autor.sql`** (`20260515150000_archivar_academia_contenido_rpc.sql`, `AcademiaContenidoCategoriaRepository`, `ContenidoScreen`).

- **Asistencia — día de entrenamiento:** al desactivar el interruptor con **filtro de categoría** activo no se borraba la marca migrada con `scopeKey` vacío, el estado seguía «activo»; al apagar se eliminan **todas** las marcas de ese día (`deleteAllForDay`).

- **Asistencia — resumen:** el porcentaje usaba `displaySmall` pegado a la barra de progreso y se veía **empalmado**; ahora `headlineLarge`, columna sin `Alignment.Bottom` y **espaciadores** antes/después del `LinearProgressIndicator` (`AttendanceSummaryCard`).

- **Asistencia — lista del día:** la pantalla volvía a un `Column` + `LazyColumn` con `weight(1f)`; con el resumen alto la lista podía quedar **sin altura útil** y parecía que no se podía marcar asistencia. Todo el contenido va en un **`LazyColumn`** (fecha, resumen, botón y tarjetas con interruptor) para **desplazar** y tomar lista del día (`attendance_day_list_title`, `AttendanceScreen`).

- **Jugador → becado y finanzas:** al marcar **becado** en la ficha, los cobros mensuales con saldo pendiente pasan a **esperado = cobrado** en ese mes (sin deuda), con **sync** a Supabase si aplica; en **Finanzas → Alumnos** se sigue viendo importe y **Editar** si hay registro de mes aunque el alumno sea becado (`PlayersViewModel`, `CobroMensualDao.getByJugadorId`, `CobroMensualPushHelper` compartido con `FinanzasViewModel`).

- **Gestionar miembros — botón «Ficha técnica»:** en tarjeta de entrenador, el `Row` de acciones comprimía el texto a un ancho mínimo y **«Ficha técnica»** se veía letra a letra en vertical; las acciones van en **`FlowRow`** con ancho completo para que pasen de línea con normalidad (`AcademiaMiembrosAdminScreen`).

- **Migración `list_academia_miembros_for_manage`:** incluye `DROP FUNCTION …` antes de recrear la RPC al añadir `created_at`, porque Postgres **no permite** cambiar el tipo de fila devuelto solo con `CREATE OR REPLACE` (error `42P13` en el SQL Editor de Supabase).

- **Selector de categoría — navegación y toques:** la barra inferior seguía visible sobre el selector; un toque cerca de «Academia» cerraba el selector y abría esa pestaña. La barra se **oculta** mientras eliges categoría; al **confirmar** categoría (o «todas») se **vuelve a Inicio** para no quedar en la pestaña anterior. La fila de categoría: miniatura vacía también **confirma** al tocar; con imagen, el recuadro abre el visor y el texto confirma (`CategoriaSelectionScreen`, `AcademiaRoot`).

- **Imágenes Supabase en Coil:** URLs de logo/portada (academia y categoría) pasan por `ImageRequest` con **crossfade** en lugar de `String` crudo (`CoilAcademyImages.kt`), para carga más fiable en miniaturas.

- **Dueño de academia también en `academia_miembros` como padre/tutor:** `resolveMembresiaCloud` tomaba la primera membresía y podía dejar `cloudMembresiaRol = parent` aunque la sesión fuera **dueño de cuenta** (`academias.user_id`), activando sync modo padre (menos `pull`, recorte de jugadores, sin categorías/portadas vía RLS). Ahora **dueño de cuenta tiene prioridad** sobre la fila de miembro (`AcademiaCloudSync`).

- **Inicio — portada de categoría:** la cabecera usaba solo `observeByNombre` sobre Room; el selector ya fusionaba tabla + jugadores y enriquecía portadas por **clave normalizada**. Eso hacía que en Inicio se viera casi siempre la **portada de la academia** aunque la categoría activa tuviera URL en otra fila o nombre ligeramente distinto. Ahora Inicio resuelve la categoría con la misma lógica (`mergeCategoriasParaUi` / `categoriaPortadaParaFiltro`, `AcademiaRoot`, `CategoriaPickerViewModel`, `CategoriaListaUiMerge.kt`).

- **Selector de categoría / portadas con academia en nube:** si `cloudMembresiaRol` aún no estaba en Room pero la sesión es **dueño de cuenta** (`remoteAcademiaCuentaUserId`), la app dejaba `esperandoMembresiaNube` y el modo restrictivo; ahora se considera membresía resuelta para el dueño y `rolDispositivoEfectivo` / mensualidad / pestañas usan el UID de sesión (`AcademiaMembresiaUi`, `RolDispositivo`, `AcademiaNavPolicy`, `AcademiaRoot`, pantallas que consultan mensualidad).

### Añadido

- **Dueño — sueldo y ficha Equipo desde miembros:** en la ficha técnica del entrenador, el **dueño de cuenta** (`puedeMutarDiaLimitePagoMes`, misma regla que día límite de pago) ve **«Editar ficha en Equipo (sueldo y datos)»**: abre el formulario de **Academia → Equipo** (editar fila existente por correo o **alta rellenada** con nombre, correo y categorías del coach en nube). `StaffFormDialog` acepta `prefillCoachMember` (`AcademiaScreen`, `AcademiaMiembrosAdminScreen`).

- **Gestionar miembros — ficha del entrenador:** pantalla a pantalla completa desde **Ficha técnica** en cada entrenador; muestra alta en el club (`created_at` vía RPC), categorías, estado de membresía y **sueldo/teléfono** si hay coincidencia por correo con **Academia → Equipo** (`AcademiaMiembrosAdminScreen`, `MiembroAdminUi`, migración SQL `list_academia_miembros_for_manage` con `created_at`).

- **Push FCM:** dependencias Firebase Messaging, `google-services` (copia automática desde `google-services.json.example` si no existe `google-services.json`), `AcademiaFcmMessagingService`, registro de token vía RPC `register_fcm_token`, permiso `POST_NOTIFICATIONS`, navegación a pestaña **Padres** al tocar la notificación. Edge Function **`send-academia-mensaje-push`** (FCM HTTP v1) + guía **`docs/PUSH_FCM_SETUP.md`** (Firebase, secretos, webhook INSERT en `academia_mensajes_categoria`). SQL **`user_fcm_tokens`**, RPC **`parent_user_ids_for_categoria_mensaje`** (solo `service_role`).

- **Padres — avisos por categoría (nube):** tabla Supabase **`academia_mensajes_categoria`**, RLS para que **padres** lean solo mensajes de categorías donde tienen hijo vinculado (`academia_padres_alumnos` + `jugadores.categoria`); **dueño/admin/coordinador** publican a cualquier categoría; **coach** solo a categorías asignadas (`academia_miembro_categorias`). Tipos: partido/entreno, convivio/logística, administrativo, otro. App: envío y lista en pestaña **Padres** (staff); bandeja con filtros y **Actualizar** para padres (`20260428120000_academia_mensajes_categoria.sql`, `AcademiaMensajesCategoriaRepository`, `ParentsViewModel`, `ParentsScreen`, `AcademiaRoot`).

- **Pull-to-refresh (deslizar hacia abajo):** en el contenido principal (pestañas y selector de categoría) dispara **sincronización con Supabase** al instante, sin esperar el intervalo automático de 70 s (`CloudSyncViewModel.requestManualSync`, `PullToRefreshBox` en `AcademiaRoot`, `pull_to_refresh_cd` en `strings.xml`).

- **Academia → Tu cuenta:** muestra el **rol en el club** según la sesión (`cloudMembresiaRol`: padre/tutor, entrenador, coordinador, administrador, dueño) o textos de **academia local** / **sincronizando rol** si aplica (`AcademiaScreen`, `strings.xml`).

- **Día límite de pago y adeudo visible a padres:** en **Academia → Mensualidad y privacidad** se configura el día del mes (1–28) o se deja sin regla; se guarda en Room **`diaLimitePagoMes`** (BD v**28**) y **`remoteAcademiaCuentaUserId`** (v**29**, UUID de `academias.user_id` para saber quién es el dueño de cuenta). **Solo ese dueño** puede editar el día límite en app (lectura para coordinadores/admin); **`pushAcademiaDiaLimitePago`** y **`guardarDiaLimitePagoMes`** lo comprueban. Supabase: trigger **`20260426150000_academias_dia_limite_solo_dueno.sql`** impide cambiar **`dia_limite_pago_mes`** si no es `academia_is_owner`. Sync **`merge`/`pullAcademiaConfig`** rellenan el UUID del dueño. En **Padres**, si la regla aplica y hay saldo vencido, recordatorio por hijo (`ParentsViewModel`, `ParentsScreen`, `PagoPlazoUtil`). SQL previo **`20260426120000_academia_dia_limite_pago_y_padre_cobros.sql`** (columna + RLS cobros padres). **`CobroMensualDao.observeTodos`**.

- **Finanzas (cobros mensuales y nómina staff):** pestaña **Finanzas** con mes navegable (`YYYY-MM`), resumen (adeudo histórico sumando pendientes de meses registrados, totales del mes, desglose por categoría), lista de alumnos con registro de cobro o **Registrar mes**, **Prellenar mes con cuotas de fichas** (alumnos activos no becados con mensualidad &gt; 0). Room **`cobros_mensuales_alumno`** (v**27**), **`CobroMensualDao`**, sync **`jugador_cobros_mensual`** en Supabase (`20260425140000_jugador_cobros_y_staff_sueldo.sql`). Repos **`CobroMensualRemoteRepository`**; visibilidad alineada con mensualidades; padres en nube sin pestaña.
- **Staff — sueldo mensual:** columna Room **`sueldoMensual`**, formulario en **Academia → Equipo**, tarjeta y sync **`equipo_staff.sueldo_mensual`**; **`StaffRemoteRepository`** actualiza nube al guardar si hay `remoteId`.

- **Asistencia → nube:** columna Room **`needsCloudPush`** (migración **25 → 26**) y **`pushAsistencias`** hace **UPDATE** en `asistencias` cuando el registro ya tiene `remoteId` y hubo cambios locales (`AsistenciaUpdatePatch`). **`pullAsistencias`** deja `needsCloudPush` en falso al fusionar.

- **Visor de imágenes a pantalla completa** (`FullscreenImageViewerDialog`): reutilizable para branding y jugadores. **Portada** y **logo** de la academia son pulsables en **Academia**, **Inicio** (cabecera con solape del avatar) y en el **selector de categoría** (barra, franja superior y miniatura por categoría si hay portada). En Inicio, si la portada visible es la de la categoría en curso, el título del visor usa `category_cover_viewer_title`.

- **Editar jugador:** botón **Editar** en la tarjeta expandida; formulario compartido con el alta (`FormularioJugadorUi`, `edit_player` / `player_edit`). `actualizarJugador` en Room y **`JugadorRemoteRepository`** + **`JugadorRemoteUpdatePatch`** en Supabase (campos editables; no cambia `fecha_alta_ms` ni auditoría de alta). Si no hay permiso para ver mensualidad en el dispositivo, al guardar se mantienen **becado** y **mensualidad** del registro.

- **Auditoría de alta de jugador:** columnas **`alta_por_user_id`** y **`alta_por_nombre`** en Supabase (`jugadores`); en Room **`altaPorUserId`** (v24) y **`altaPorNombre`** (v25). Al guardar, se persisten el UUID de Auth y la **etiqueta visible** (metadata o correo) vía `etiquetaVisibleDesdeAuthMetadata`. La ficha muestra **«Alta por: [nombre]»**. SQL `20260422130000_jugador_alta_por_user.sql` y `20260423120000_jugador_alta_por_nombre.sql`.
- **RPC `alta_por_user_labels_for_academia`** + **UPDATE** de backfill en `20260424103000_alta_por_nombre_backfill_and_rpc.sql`: rellena `alta_por_nombre` en filas antiguas desde `auth.users`; la app llama al RPC cuando hay jugadores con UUID de alta pero sin nombre en Room (`PlayersViewModel.etiquetasAltaPorUid`, `AcademiaMiembrosRepository.etiquetasAltaPorUsuario`, DTO `AltaPorUserLabelRow`).

- **Recordar categoría de trabajo:** tabla Room **`session_categoria_reciente`** (v23) guarda la última categoría del selector por **`userId`** de Auth; **`SessionViewModel`** restaura al entrar y persiste en **`confirmarSeleccion`**; al restringir coach, ajusta el nombre al conjunto permitido y actualiza disco. Se borra la fila del usuario en **`signOut`** (`AuthViewModel`).

- **Identidad de sesión en la app:** `AuthViewModel.cuentaEtiquetaVisible()`; tercera línea en la barra superior (`AcademiaRoot`) con nombre o correo; saludo «Hola, …» en `InicioScreen` (`home_welcome_user`, `session_bar_account_label`).

- **Pestaña Academia → Tu cuenta:** bloque con **nombre** y **correo** leídos de la sesión (`editableProfileSnapshot`); strings `auth_account_label_*`, `auth_account_no_profile_data`.

- **`academia_padres_alumnos` al guardar jugadores:** la tabla no se rellenaba sola al dar de alta un alumno (solo existía el vínculo manual en «Hijos vinculados»). Tras subir jugadores a la nube, **`AcademiaCloudSync`** intenta **auto-vínculo** cuando el **correo del tutor** del jugador coincide (sin distinguir mayúsculas) con el **correo de un miembro activo con rol `parent`** (`list_academia_miembros_for_manage`). Al insertar cualquier jugador nuevo también se reintenta para alumnos ya remotos con el mismo criterio. Texto de ayuda actualizado en `members_parent_links_hint`.

- **Fase 4 MVP (padres con cuenta):** `PadresAlumnosRepository` + DTOs `AcademiaPadresAlumnoRow`/`Insert`; diálogo **Hijos vinculados** en miembros con rol `parent` (alta/baja de vínculos); `ParentsViewModel` con base de datos y `ParentsScreen` según `esPadreMembresiaNube()` (hijos + asistencia reciente vs borrador para staff); `AcademiaRoot` pasa `config` a Padres.
- **SQL** `20260418120000_parent_read_asistencias_historial.sql`: políticas `asistencias_parent_select` y `historial_parent_select` para tutores.
- **`AcademiaConfig.esPadreMembresiaNube()`** en `AcademiaMembresiaUi.kt`.
- **Sync:** si la membresía en nube es `parent`, se omiten los `push*` operativos y tras `pullJugadores` se eliminan locales con `remoteId` fuera del conjunto permitido por RLS.

- **Cierre Fase 2 y 3 (MVP):** `InviteClubIntentHelper` — compartir texto de invitación, copiar código, abrir cliente de correo con asunto y cuerpo; mismas acciones en tarjeta **Código** (`AcademiaScreen`) y bloque **Invitar** en `AcademiaMiembrosAdminScreen`.
- **Revocar membresía:** `AcademiaMiembrosRepository.deleteMiembro` + `eliminarMiembro` en VM + diálogo «Quitar del club» (no aplica al dueño de cuenta de la academia).
- **Onboarding / multi-academia:** textos `onboarding_join_steps_*`, `onboarding_join_divider`, `pick_academy_subtitle`; `joinByCode` normaliza código (`trim` + mayúsculas).
- **`docs/CHECKLIST_CIERRE_FASE_2_3.md`** — pruebas manuales sugeridas; **`PLAN_MEMBRESIA_Y_TENANTS.md`** Fases 2 y 3 marcadas **CERRADA (MVP)**.

- **SQL Supabase** `20260417100000_academias_update_coordinator.sql`: política **`academias_update`** permite también miembro activo **`coordinator`** (igual que `admin`), alineada con `academiaGestionNubePermitida` en app. **Ejecutar en SQL Editor** tras migraciones previas.

- **Opción A — gestión de miembros en app:** pantalla admin `AcademiaMiembrosAdminScreen` (lista, activo/inactivo, cambio de rol, categorías coach vía `academia_miembro_categorias`); botón en `AcademiaScreen` cuando hay academia en nube y permiso de gestión; `AcademiaMiembrosViewModel` + `AcademiaMiembrosRepository` (PostgREST); DTOs `AcademiaMiembroActivoPatch`, `AcademiaMiembroRolPatch`, `AcademiaMiembroCategoriaInsert`; registro en fábrica de ViewModels. Identificador en UI: últimos 8 caracteres de `user_id`; no se desactiva ni cambia rol al dueño de cuenta (`academias.user_id`).
- **SQL Supabase** `20260416130000_coordinator_manage_members.sql`: `academia_can_manage_members` incluye rol **`coordinator`** (misma capacidad que owner/admin para gestionar miembros en RLS). **Aplicar en SQL Editor** tras migraciones anteriores.
- **`rutaPrincipalVisible`** (`AcademiaNavPolicy.kt`): pestañas y accesos rápidos de Inicio alineados (padre en nube: solo rutas `inicio`, `padres`, `academia`).
- **Strings** `members_*` para la UI de miembros.

- **Fase 3 (parcial):** `cloudMembresiaRol` y `cloudCoachCategoriasJson` en Room v21; sync en `merge`/`pullAcademiaConfig` vía `academia_miembros` + `academia_miembro_categorias`; `SessionViewModel.categoriasPermitidasOperacion`; filtrado jugadores/asistencia/stats con `jugadoresActivosFlow` y `JugadorDao.observeByCategorias`.
- **Pestañas para rol `parent` en nube:** solo Inicio, Padres y Academia (`AcademiaRoot`).
- **Selector de categoría:** coach restringido sin opción «Todas», lista filtrada, sin FAB alta categoría ni edición de portada si no es owner/admin en nube (`puedeEditarCategoriasEnSelector`).
- **DTO** `AcademiaMiembroCategoriaLinkRow`; helpers `AcademiaMembresiaUi.kt`.
- **Strings** `pick_category_coach_hint`, `pick_category_coach_empty`.

- **`docs/PLAN_MEMBRESIA_Y_TENANTS.md` §Fase 3:** actualizado con opción A (gestión miembros en app + RLS coordinador) y pendientes (invitar/revocar explícito, invitación email).
- **Snackbar y feedback al guardar nombre de academia**: al pulsar «Guardar nombre» se oculta teclado y foco, vibración háptica en éxito, mensaje «Nombre guardado» o aviso si no hay permiso (`AcademiaScreen`, `strings.xml`).
- **Campo Room `academiaGestionNubePermitida`** y migración **19 → 20**: indica si el usuario en sesión puede administrar la academia en la nube (dueño de `academias.user_id` o miembro `owner`/`admin`/`coordinator` en `academia_miembros`, según lógica en sync).
- **`AcademiaNombrePatch`**: actualización parcial del nombre en Supabase.
- **Strings** `academy_readonly_no_admin_*`, `academy_name_saved`, `academy_name_save_denied`.

### Cambiado

- **Finanzas:** alcance propio **Toda la academia** vs **una categoría** (selector + diálogo), sin depender del filtro global de sesión; totales y adeudo histórico del mes se calculan solo sobre alumnos visibles en ese alcance. Vista en **pestañas** Resumen / Alumnos / Nómina (esta última solo con vista general). Resumen compacto en una tarjeta; desglose por categoría con acción «Ver solo esta categoría» (`FinanzasViewModel`, `FinanzasScreen`, `AcademiaViewModelFactory`).

- **Pull-to-refresh:** el gesto **solo descarga** datos desde Supabase (`syncAll(skipPush = true)`), sin ejecutar subidas antes. Así un fallo en push (RLS, red al subir, etc.) no impide actualizar listas y portadas al deslizar. El sync completo (push + pull) sigue en arranque y al reanudar la app (`AcademiaCloudSync`, `CloudSyncViewModel`).

- **Modo dispositivo eliminado en UI:** ya no hay selector en Academia; el comportamiento (pestañas, mensualidades, vista familia) sale de **`rolDispositivoEfectivo()`** según `cloudMembresiaRol` y academia en nube. Sync persiste `rolDispositivo` en Room alineado al rol; alta de academia propia inserta `DUENO_ACADEMIA`. **Cambiar PIN** del staff queda bajo **Mensualidad y privacidad**. `rutaPrincipalVisible` solo recibe `config`; retirados `guardarRolDispositivo` y `PendienteTrasPin.Rol`.

- **Pestaña Padres:** **`ParentsScreen`** deja de recibir `AcademiaConfig`; el estado (incl. día límite y cobros) se deriva de **`ParentsViewModel`** vía **`academiaConfigDao.observe()`** y flujos de jugadores/asistencias/cobros.

- **Gradle wrapper:** `distributionUrl` vuelve a **HTTPS** (`services.gradle.org`, Gradle **8.9**), `validateDistributionUrl=true`, para que **sync funcione en cualquier PC** sin copiar `gradle-*-bin.zip` al repo. La descarga se cachea en `GRADLE_USER_HOME/wrapper/dists`. Sigue `networkTimeout` **120 s**. (El `.gitignore` de ZIP locales en `gradle/wrapper/` puede quedarse por si alguien prueba distribución local.)

- **Asistencia:** al marcar presente/ausente se **fusiona** con el registro existente (se mantiene `remoteId`) y se marca **`needsCloudPush`** para subir cambios; «Marcar todos presentes» usa **`jugadoresActivosSnapshot`** (misma lista que en pantalla, p. ej. coach con categorías asignadas). Texto del botón en `attendance_mark_all_present`.

- **Foto en tarjeta de jugador:** tocar la **miniatura** abre el mismo visor compartido (`FullscreenImageViewerDialog`); el resto de la fila sigue expandiendo/contrayendo detalles (strings `player_photo_tap_to_expand`, `player_photo_viewer_close`).

- **Lista de jugadores (`PlayersScreen`):** filas **compactas** (foto 40dp, nombre, categoría, fecha de alta y mensualidad resumida); al **tocar la fila** se **expande** con animación el bloque de detalles (CURP, documentos, contacto, notas, acciones). Solo **un jugador expandido** a la vez; chevron animado y tono de tarjeta algo más alto al expandir. Strings de accesibilidad `player_card_*`.

- **Inicio — Punto 2:** atajos con lista única `ACCESOS_RAPIDOS_INICIO` (mismo orden de rutas que pestañas operativas); texto *Accesos rápidos* y tarjetas solo si hay al menos una ruta visible (`rutaPrincipalVisible` vía `accesoRapidoVisible`).

- **`computeAcademiaGestionNubePermitida`** (`AcademiaCloudSync`): rol de membresía **`coordinator`** cuenta como gestión de academia en nube (coherente con RLS y opción A).
- **`AcademiaRoot`:** `tabsVisibles` y `InicioScreen.accesoRapidoVisible` usan `rutaPrincipalVisible`; `AcademiaScreen` recibe `viewModelFactory` para `AcademiaMiembrosViewModel`.
- **Sincronización del nombre**: `pushAcademiaNombre` al inicio de `syncAll` y desde `guardarNombre` cuando hay `remoteAcademiaId`, para que el pull no sobrescriba el nombre local con el valor antiguo de la nube (`AcademiaCloudSync`, `AcademiaConfigViewModel`).
- **`pullAcademiaConfig` / merge**: `nombreAcademia` con `row.nombre.ifBlank { cfg.nombreAcademia }`; se persiste y refresca `academiaGestionNubePermitida`.
- **Permisos en la pestaña Academia**: si `academiaGestionNubePermitida` es falsa (sync), la UI de academia queda restringida (sin código club, marca, tema, mensualidades ni staff); con permiso incluye owner/admin/**coordinator** en membresía activa (`AcademiaScreen`).
- **`AcademiaConfigViewModel`**: operaciones sensibles (nombre, colores, medios, mensualidades, código club) comproban `puedeMutarConfigAcademiaAdmin()`; `guardarNombre(nombre, onResult?)`.
- **`StaffViewModel`**: recibe `AcademiaConfigDao`; alta/edición/baja de staff bloqueada si la academia está en nube y `academiaGestionNubePermitida` es falsa.
- **Push condicionado**: `pushAcademiaNombre`, `pushAcademiaThemeColors` y `pushAcademiaMedia` no llaman a PostgREST si el usuario no puede gestionar la academia en nube.
- **Enlace roto a academia**: al limpiar `remoteAcademiaId` se restablece `academiaGestionNubePermitida = true`.

### Corregido

- **Selector «¿con qué categoría trabajas?» sin miniatura de portada:** la lista mezclaba `categorias` y nombres de `jugadores` con clave de nombre **exacto**; si el texto no coincidía (espacios, mayúsculas), se mostraba una fila **sintética** sin `portadaUrlSupabase` / ruta local. **`CategoriaPickerViewModel`** unifica por **`normalizarClaveCategoriaNombre`** (NFC, guiones tipográficos, espacios), prioriza la fila con portada y **enriquece** filas sin foto si en Room hay otra fila equivalente con URL/archivo; **`CategoriaSelectionScreen`** usa la misma clave para coach y **`LazyColumn` key** incluye portada para recargar Coil.

- **Día límite de pago:** al guardar o quitar la regla se muestra **snackbar** (éxito o sin permiso), vibración de confirmación y se oculta el teclado, alineado con «Guardar nombre» (`AcademiaScreen`, `AcademiaConfigViewModel.guardarDiaLimitePagoMes` con callback).
- **Snackbar «No se pudo actualizar los datos en la nube» en exceso:** el sync al **reanudar** la actividad podía ejecutarse cuando **`currentUserOrNull()`** aún era null o fallaba por red; ahora se **omite** el sync sin usuario y los fallos genéricos **no** muestran snackbar en el sync automático (siguen los avisos de onboarding / elegir academia). `CloudSyncViewModel.runAutoSyncLocked(mostrarErrorGenericoSiFalla)`.

- **Alta de jugador + documento CURP (o acta):** los `ActivityResult` de **CURP y acta** se registran siempre en **`PlayersScreen`** (no dentro del formulario condicional); copia vía `copiarUriAdjuntoAJugador` + `lifecycleScope`. **`BackHandler`** del alta vive en la pantalla y se **desactiva** mientras hay selector/cámara/recorte/permiso (`awaitingExternalActivityResult`) para que el “atrás” al cerrar el gestor no cierre el registro. El formulario sigue siendo capa en `Box` sobre el `Scaffold`. **Visibilidad del alta y rutas de adjuntos** viven en **`PlayersViewModel`** (`StateFlow`) para que no se pierdan al recrear la actividad al volver del selector; la pantalla usa `abrirAltaJugador` / `cerrarAltaJugador` / `aplicarRuta*Copiada` y `guardarJugador` cierra el alta al terminar.

- **Alta de jugador y selector de categoría:** el overlay del registro queda **debajo** de la barra superior de `AcademiaMainScaffold`; al volver del gestor de archivos un **toque residual** podía pulsar **«Cambiar categoría»** y abrir el selector. Mientras el alta está abierta, **`SessionViewModel`** marca `impideVolverASeleccionCategoria` (desde `PlayersScreen`), se **deshabilita** ese botón y **`volverASeleccionCategoria()`** no hace nada si el bloqueo está activo.

- **Pantalla de error «Request timeout» al reanudar la app:** `LaunchedEffect(authSession)` volvía a ejecutar `bindingVm.refresh()` en cada nueva emisión de sesión (mismo usuario), dejando el flujo en **Loading** y, si la red iba lenta, en **Error** con timeout de **10 s** en la petición a `academias`. Ahora el efecto depende solo del **id de usuario** autenticado; en `AcademiaApplication` el cliente Supabase usa **`requestTimeout` 45 s**.

- **«Comprobando academia…» al volver del CURP / al desbloquear pantalla:** `refresh()` ponía siempre **Loading** y desmontaba el menú; además, un **`SessionStatus.Initializing`** breve hacía **return** antes del `LaunchedEffect` y al volver a **Authenticated** el efecto se **reiniciaba** y volvía a llamar a `refresh()`. **Cambios:** `AcademiaBindingViewModel.refresh(mostrarPantallaCarga)` — si ya estás en **Ready**, la comprobación va en **segundo plano** sin pantalla de carga ni error por timeout puntual; **Reintentar** usa `mostrarPantallaCarga = true`. En **`AcademiaRoot`**, sesión **«pegada»** (`ultimoUserIdAutenticado`) para no sustituir toda la UI por el spinner de Auth durante **Initializing**; **`AcademiaRootAuthenticatedContent`** recibe **`authUserIdKey`** para no perder el `SessionViewModel` en ese intervalo.

- **Recorte de foto de jugador (uCrop):** el módulo **uCrop 2.2.8** pasa a ser **`:ucrop` local** (parche). Se quitó `setImageToWrapCropBounds()` al **soltar la rueda Escala** y al **ACTION_UP** del gesto en la imagen, que devolvían el zoom al mínimo (~240%) justo al soltar. Activado **recorte libre**, decode ampliado y multiplicador de zoom como antes; texto de ayuda actualizado.

- **«Este dispositivo lo usa» (padre / profesor / …):** al reabrir la app, el sync volvía a dejar **Padre o tutor** porque `mergeAcademiaRowIntoLocal` copiaba `rol_dispositivo` desde Supabase (valor único por academia, casi siempre por defecto). Ahora se **conserva siempre el valor guardado en Room** en ese merge.

- **Cambiar categoría:** el selector ya no sustituye todo el `Scaffold` principal; se muestra dentro de `AcademiaMainScaffold` con la **barra inferior** visible. **Atrás del sistema** cierra el selector y vuelve al menú **sin cambiar** la categoría (`SessionViewModel.cerrarSelectorCategoria`, `BackHandler` en `AcademiaRoot`). Al pulsar una pestaña con el selector abierto, primero se cierra el selector y luego se navega.

- **Coach no ve categorías aunque gestión de miembros sí:** `resolveMembresiaCloud` prioriza `academia_miembros` antes del atajo `academias.user_id` (si la cuenta es dueña y además tiene fila `coach`, antes se trataba solo como dueño y se ignoraban `academia_miembro_categorias`). Nueva RPC Supabase **`list_my_coach_category_names`** (`20260421120000_list_my_coach_category_names.sql`) en **security definer** para devolver nombres sin depender solo de RLS en el cliente; la app intenta RPC y hace fallback a PostgREST. **Ejecutar el SQL en el proyecto Supabase.**

- **Nombres de categorías asignadas al coach:** al resolver `academia_miembro_categorias` → nombres, si no hay fila con `id` + `academia_id`, se reintenta solo por `id` (`nombreCategoriaCloudParaCoach`) por si en `categorias` el `academia_id` no coincide con la academia enlazada.

- **Parpadeo «todas las categorías» y luego «sin asignar» al cambiar de cuenta:** si el `remoteAcademiaId` seguía en caché, `resolveAcademiaBinding` devolvía OK **sin** volver a ejecutar `mergeAcademiaRowIntoLocal`, y el rol/categorías coach no llegaban a Room hasta el sync retardado. Ahora siempre se fusiona la fila de academia al validar acceso. El selector muestra carga mientras `membresiaNubeAunNoResuelta()` y `SessionViewModel` distingue «esperando membresía» de «sin restricción coach».

- **Mismo móvil, admin y profesor (cuentas distintas):** `pullAcademiaConfig` / `mergeAcademiaRowIntoLocal` ya no reutilizan `cloudMembresiaRol` ni `cloudCoachCategoriasJson` del usuario anterior al resolver membresía; al **cerrar sesión** se borran esos campos en Room. `SessionViewModel` usa clave por **id de usuario** de Auth para no arrastrar restricción de categorías entre cuentas.

- **Coach sin categorías visibles:** RLS `miembro_cat_select` permitía solo `academia_staff_data_access`; si el rol en BD no coincidía exactamente con el array SQL, el entrenador no leía `academia_miembro_categorias`. Migración `20260420100000_miembro_cat_select_coach_self.sql`: también se permite SELECT cuando el vínculo es del propio `user_id` activo; `academia_miembro_activo_rol` compara roles con `lower(trim(...))`. Tras aplicar el SQL en Supabase, sincronizar en el móvil del profesor.

- **Compilación Compose:** `stringResource` no puede llamarse dentro de `Modifier.semantics { }`; etiqueta de sesión en barra resuelta fuera (`AcademiaRoot`). Saludo en inicio precalculado en el cuerpo de `InicioScreen`.

- **`AcademiaMiembrosViewModel`:** `runCatching` sobre `setMiembroActivo` / `setMiembroRol` / `replaceMiembroCategorias` devolvía `Result<PostgrestResult>`; los callbacks esperaban `Result<Unit>` — el bloque ahora termina en `Unit` tras la llamada suspendida.
- **Nombre que volvía a «Mi Academia»**: causado por pull que leía `academias.nombre` en Supabase sin haber subido antes el nombre editado localmente.

## [Sin publicar]

Cambios posteriores a la revisión **1.0.1** (2026-04-21).

### Cambiado

- **Jugadores (staff) — correo tutor:** en el formulario de alta/edición, **ayuda** al vínculo con la app padres, **teclado de correo** y **validación suave** (si hay texto, debe parecer un email con @ y dominio con punto; hasta entonces el botón Guardar queda deshabilitado) (`PlayersScreen`, `strings.xml`). Fase 4 ticket 10.

- **Padres — vincular hijo:** antes de crear el vínculo en nube, **diálogo de confirmación** con nombre y categoría del alumno; botón de acción con estado de carga (`ParentsLinkChildPanel`, `strings.xml`). Complementa la Fase 4 (ticket confirmación al vincular).

### Documentación / Supabase (Fase 4)

- **RLS `padres_alumnos_delete_parent_own`:** verificado en **producción** (proyecto `escuela-futbol-correcaminos`): al intentar crear la política desde el SQL Editor, Postgres respondió que **ya existía** (`42710`) — alineado con `supabase/migrations/20260521140000_padres_alumnos_delete_parent_own.sql`. Registro en `docs/FASE4_PADRES_PROGRESO.md` §Registro de acciones.

---

*Actualizar este archivo en el mismo cambio (o inmediatamente después) cuando se modifique comportamiento visible, API, base de datos local, sync con Supabase o reglas de permisos.*
