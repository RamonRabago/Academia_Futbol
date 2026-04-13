# Registro de cambios — Escuela Fútbol / Academia

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/). Las fechas usan el calendario del equipo.

## [Sin publicar]

### Añadido

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

---

*Actualizar este archivo en el mismo cambio (o inmediatamente después) cuando se modifique comportamiento visible, API, base de datos local, sync con Supabase o reglas de permisos.*
