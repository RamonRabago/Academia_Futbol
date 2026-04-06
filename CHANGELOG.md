# Registro de cambios — Escuela Fútbol / Academia

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/). Las fechas usan el calendario del equipo.

## [Sin publicar]

### Añadido

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

- **`AcademiaMiembrosViewModel`:** `runCatching` sobre `setMiembroActivo` / `setMiembroRol` / `replaceMiembroCategorias` devolvía `Result<PostgrestResult>`; los callbacks esperaban `Result<Unit>` — el bloque ahora termina en `Unit` tras la llamada suspendida.
- **Nombre que volvía a «Mi Academia»**: causado por pull que leía `academias.nombre` en Supabase sin haber subido antes el nombre editado localmente.

---

*Actualizar este archivo en el mismo cambio (o inmediatamente después) cuando se modifique comportamiento visible, API, base de datos local, sync con Supabase o reglas de permisos.*
