# Registro de cambios — Escuela Fútbol / Academia

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/). Las fechas usan el calendario del equipo.

## [Sin publicar]

### Añadido

- **Fase 3 (parcial):** `cloudMembresiaRol` y `cloudCoachCategoriasJson` en Room v21; sync en `merge`/`pullAcademiaConfig` vía `academia_miembros` + `academia_miembro_categorias`; `SessionViewModel.categoriasPermitidasOperacion`; filtrado jugadores/asistencia/stats con `jugadoresActivosFlow` y `JugadorDao.observeByCategorias`.
- **Pestañas para rol `parent` en nube:** solo Inicio, Padres y Academia (`AcademiaRoot`).
- **Selector de categoría:** coach restringido sin opción «Todas», lista filtrada, sin FAB alta categoría ni edición de portada si no es owner/admin en nube (`puedeEditarCategoriasEnSelector`).
- **DTO** `AcademiaMiembroCategoriaLinkRow`; helpers `AcademiaMembresiaUi.kt`.
- **Strings** `pick_category_coach_hint`, `pick_category_coach_empty`.

- **`docs/PLAN_MEMBRESIA_Y_TENANTS.md` §Fase 3:** bloque *Estado (repo)* con avance parcial y pendientes explícitos (mapa de pestañas por rol miembro, filtrado coach en listas, UI invitar/revocar).
- **Snackbar y feedback al guardar nombre de academia**: al pulsar «Guardar nombre» se oculta teclado y foco, vibración háptica en éxito, mensaje «Nombre guardado» o aviso si no hay permiso (`AcademiaScreen`, `strings.xml`).
- **Campo Room `academiaGestionNubePermitida`** y migración **19 → 20**: indica si el usuario en sesión puede administrar la academia en la nube (dueño de `academias.user_id` o miembro `owner`/`admin` en `academia_miembros`).
- **`AcademiaNombrePatch`**: actualización parcial del nombre en Supabase.
- **Strings** `academy_readonly_no_admin_*`, `academy_name_saved`, `academy_name_save_denied`.

### Cambiado

- **Sincronización del nombre**: `pushAcademiaNombre` al inicio de `syncAll` y desde `guardarNombre` cuando hay `remoteAcademiaId`, para que el pull no sobrescriba el nombre local con el valor antiguo de la nube (`AcademiaCloudSync`, `AcademiaConfigViewModel`).
- **`pullAcademiaConfig` / merge**: `nombreAcademia` con `row.nombre.ifBlank { cfg.nombreAcademia }`; se persiste y refresca `academiaGestionNubePermitida`.
- **Permisos en la pestaña Academia**: usuarios sin rol owner/admin en nube ven solo lectura (tarjeta informativa + nombre); no ven código club, marca, tema, mensualidades ni gestión de staff (`AcademiaScreen`).
- **`AcademiaConfigViewModel`**: operaciones sensibles (nombre, colores, medios, mensualidades, código club) comproban `puedeMutarConfigAcademiaAdmin()`; `guardarNombre(nombre, onResult?)`.
- **`StaffViewModel`**: recibe `AcademiaConfigDao`; alta/edición/baja de staff bloqueada si la academia está en nube y `academiaGestionNubePermitida` es falsa.
- **Push condicionado**: `pushAcademiaNombre`, `pushAcademiaThemeColors` y `pushAcademiaMedia` no llaman a PostgREST si el usuario no puede gestionar la academia en nube.
- **Enlace roto a academia**: al limpiar `remoteAcademiaId` se restablece `academiaGestionNubePermitida = true`.

### Corregido

- **Nombre que volvía a «Mi Academia»**: causado por pull que leía `academias.nombre` en Supabase sin haber subido antes el nombre editado localmente.

---

*Actualizar este archivo en el mismo cambio (o inmediatamente después) cuando se modifique comportamiento visible, API, base de datos local, sync con Supabase o reglas de permisos.*
