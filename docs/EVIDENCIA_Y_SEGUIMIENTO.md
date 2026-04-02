# Academia Fútbol — Evidencia y seguimiento técnico

Documento vivo para auditoría y trazabilidad de decisiones e implementación.  
**Actualizado:** 2 de abril de 2026.

---

## 1. Resumen del producto

- **App Android:** `com.escuelafutbol.academia` (Kotlin, Jetpack Compose, Material 3, Room, Navigation).
- **Datos locales:** SQLite vía Room (jugadores, asistencias, categorías, configuración de academia, staff, historial de altas/bajas).
- **Nube:** Supabase (Auth con email/contraseña + PostgREST + **Storage** para imágenes públicas en bucket `academia-media`).
- **Seguridad:** clave publicable (Publishable / anon) solo en cliente; nunca la clave **secret** / **service_role** en la app.

---

## 2. Configuración de entorno (evidencia operativa)

| Elemento | Ubicación en el repo | Notas |
|----------|----------------------|--------|
| Credenciales Supabase (no versionadas) | `local.properties` (raíz del proyecto) | Entrada en `.gitignore`. |
| Variables leídas al compilar | `SUPABASE_URL`, `SUPABASE_ANON_KEY` | Se inyectan en `BuildConfig`. |
| Definición Gradle | `app/build.gradle.kts` | Lee `local.properties` y define `BuildConfig.SUPABASE_URL` y `BuildConfig.SUPABASE_ANON_KEY`. |

**Valores en Supabase (panel):**

- **URL del proyecto:** p. ej. *Project URL* / *Data API* → `https://<ref>.supabase.co`.
- **Clave de cliente:** **Publishable key** (`sb_publishable_…`) o clave **anon** (pestaña Legacy). Van en `SUPABASE_ANON_KEY`.
- **No usar en la app:** **Secret** / **service_role**.

---

## 3.1 Mensaje al confirmar el correo (dos opciones)

1. **Dentro del correo / página web (sin depender de la app)**  
   En Supabase: **Authentication → Email Templates → Confirm signup** (o “Confirmar registro”). Edita el HTML o el texto y añade el aviso que quieras (por ejemplo: *“Después de confirmar, abre Academia Fútbol e inicia sesión”*). Eso lo ve el usuario antes o después del clic, según la plantilla.

2. **Al volver a la app (enlace profundo)**  
   La app declara el esquema `academiafutbol` y el host `auth` (`AndroidManifest` + `Auth` en `AcademiaApplication`). Si Supabase redirige tras confirmar a `academiafutbol://auth/...`, se importa la sesión y se muestra un **Toast**: *«¡Listo! Correo confirmado…»* (`auth_email_confirmed_toast`).  
   En Supabase: **Authentication → URL Configuration → Redirect URLs** debe incluirse exactamente: `academiafutbol://auth/callback` (y si el panel lo permite como **Site URL** para flujos de email, según tu versión del proyecto). Si el enlace del correo sigue abriendo solo el navegador, usa la opción 1 o una página `https` de “éxito” con tu mensaje.

---

## 3. Autenticación (login con email)

| Aspecto | Detalle |
|---------|---------|
| Objetivo | Identificar al usuario contra Supabase Auth (email + contraseña). |
| Cliente | `SupabaseClient` en `AcademiaApplication`, con plugins `Auth` y `Postgrest`. |
| Archivo aplicación | `app/src/main/java/com/escuelafutbol/academia/AcademiaApplication.kt` |
| UI login / registro | `app/src/main/java/com/escuelafutbol/academia/ui/auth/LoginScreen.kt` |
| Estado de sesión | `app/src/main/java/com/escuelafutbol/academia/ui/auth/AuthViewModel.kt` |
| Flujo principal | `app/src/main/java/com/escuelafutbol/academia/ui/AcademiaRoot.kt` — sin credenciales en `BuildConfig` → pantalla de configuración; sin sesión → login; con sesión → flujo normal (categoría + pestañas). |
| Cerrar sesión | `AcademiaScreen` — bloque “Cuenta en la nube” con callback `onSignOut`. |

**Comportamiento documentado:**

- Si el proyecto Supabase exige **confirmación por correo**, el alta puede requerir abrir el enlace del email antes de poder iniciar sesión (mensaje informativo en la UI tras registro).

---

## 4. Base de datos en Supabase (Postgres + RLS)

| Artefacto | Ruta |
|-----------|------|
| Script de creación de tablas, triggers `updated_at` y políticas RLS | `supabase/migrations/20260331120000_academia_cloud.sql` |

**Tablas creadas (schema `public`):**

- `academias` — una fila por usuario (`user_id` → `auth.users`), datos alineados con configuración local; columnas opcionales `color_primario_hex` / `color_secundario_hex` para tema de la app.
- `categorias` — ligadas a `academias`; columna opcional `portada_url` (URL pública en Storage).
- `jugadores` — ligadas a `academias`.
- `jugador_historial` — eventos ALTA/BAJA ligados a `jugadores`.
- `asistencias` — ligadas a `jugadores` y `academias`; unicidad `(jugador_id, fecha_dia_ms)`.
- `equipo_staff` — personal del club ligado a `academias`.

**Seguridad (RLS):** políticas para que cada usuario solo acceda a su academia y a filas cuyo `academia_id` pertenezca a esa academia.

**Ejecución:** Supabase Dashboard → **SQL Editor** → pegar y ejecutar el script completo (re-ejecución parcial: incluye `DROP POLICY IF EXISTS` donde aplica).

---

## 5. Sincronización app ↔ Supabase

| Componente | Ruta |
|--------------|------|
| Lógica de sync (push sin `remoteId`, subida de imágenes a Storage, pull y fusión por UUID) | `app/src/main/java/com/escuelafutbol/academia/data/sync/AcademiaCloudSync.kt` |
| Subida binaria a Storage (`suspend`) | `app/src/main/java/com/escuelafutbol/academia/data/sync/AcademiaStorageUpload.kt` |
| DTOs / mapeo Kotlin ↔ columnas SQL | `app/src/main/java/com/escuelafutbol/academia/data/remote/dto/CloudTables.kt` |
| ViewModel del botón de sync | `app/src/main/java/com/escuelafutbol/academia/ui/sync/CloudSyncViewModel.kt` |
| UI “Sincronizar ahora” | `app/src/main/java/com/escuelafutbol/academia/ui/academia/AcademiaScreen.kt` (sección “Copia en la nube”) |
| Cadenas | `app/src/main/res/values/strings.xml` (`sync_cloud_*`) |

**Room — versiones y columnas relevantes:**

- `9 → 10`: `remoteId` en varias tablas y `remoteAcademiaId` en `academia_config`.
- `10 → 11`: URLs Supabase para fotos (`fotoUrlSupabase` jugadores/staff; `logoUrlSupabase` / `portadaUrlSupabase` en config).
- `11 → 12`: `temaColorPrimarioHex` / `temaColorSecundarioHex` en `academia_config`.
- `12 → 13`: `portadaRutaAbsoluta` / `portadaUrlSupabase` en `categorias`.

**Dependencias relevantes (catálogo + app):**

- `gradle/libs.versions.toml` — BOM Supabase, `auth-kt-android`, `postgrest-kt-android`, `storage-kt-android`, Ktor, kotlinx-serialization.
- `app/build.gradle.kts` — plugin `kotlin.serialization`, `install(Storage)` en `AcademiaApplication.kt`, sustitución de artefactos JVM por variantes `-android` donde aplica.

**Comportamiento actual (mediados 2026):**

- **Imágenes:** logo/portada de academia, fotos de jugadores/staff, portadas por categoría — subida a bucket **`academia-media`** con rutas bajo `auth.uid()`, actualización de columnas `*_url` en Postgres. Coil prioriza URL remota cuando existe.
- **Tema:** colores de interfaz opcionales por academia (hex en Room + columnas en `academias`); `AcademiaFutbolTheme` en sesión autenticada (`Theme.kt`, `BrandColorHex.kt`).
- **Portada por categoría:** al filtrar categoría en Inicio se muestra la portada de esa categoría si existe; edición desde la pantalla de selección de categoría (iconos imagen / papelera).
- **Conflictos multi-dispositivo:** sigue siendo merge simple por UUID; sin CRDT formal.

---

## 6. Mapa rápido de archivos clave

```
escuela_futbol_api/
├── .git/                         # control de versiones (ver §7 changelog Git)
├── local.properties              # SUPABASE_URL, SUPABASE_ANON_KEY (no commitear)
├── gradle/libs.versions.toml
├── app/build.gradle.kts
├── app/src/main/java/com/escuelafutbol/academia/
│   ├── AcademiaApplication.kt
│   ├── MainActivity.kt
│   ├── data/local/               # Room: entidades, DAOs, AcademiaDatabase
│   ├── data/remote/dto/          # DTOs Supabase
│   ├── data/sync/                # AcademiaCloudSync, AcademiaStorageUpload
│   └── ui/                       # Compose, Auth, AcademiaRoot, etc.
├── supabase/migrations/
│   └── 20260331120000_academia_cloud.sql
└── docs/
    ├── EVIDENCIA_Y_SEGUIMIENTO.md        # este archivo
    ├── PLAN_MEMBRESIA_Y_TENANTS.md       # plan por fases + anexos A/B
    └── FASE_0_DECISIONES_CERRADAS.md     # Fase 0 cerrada: roles, híbrido dueño+miembros, planes/límites
```

---

## 7. Registro de cambios (changelog de evidencia)

| Fecha | Cambio |
|-------|--------|
| 2026-04-02 | **Git:** repositorio inicializado en la raíz (`git init`), `.gitignore` ampliado (`.kotlin/`, keystores, `google-services.json`), primer commit `08bcaca` — *Commit inicial: app Academia Fútbol, docs y migraciones Supabase*. `local.properties` y `.gradle/` excluidos. **Remoto:** crear repo en GitHub/GitLab y `git remote add origin …` + `git push -u origin master` (o `main`). |
| 2026-04-02 | **Fase 0 CERRADA:** creado `docs/FASE_0_DECISIONES_CERRADAS.md` — self-serve + código club + invitación email; padres MVP **con cuenta**; multi-academia por usuario; roles owner/admin/coordinator/coach/parent; **punto 6:** modelo **híbrido** (`academias.user_id` + tabla miembros + RLS dueño O membresía); **punto 7:** planes Esencial (120/8), Club (350/25/400 padres), Academia (900/60/1500 padres). `PLAN_MEMBRESIA_Y_TENANTS.md` §Fase 0 enlaza a este archivo. Implementación técnica pendiente (Fase 1+). |
| 2026-04-02 | **Plan membresías:** `PLAN_MEMBRESIA_Y_TENANTS.md` ampliado con **Anexo A** (recomendaciones Fase 0, producto genérico multi-club) y **Anexo B** (modelo de negocio: unidad de cobro, palancas de precio, planes, alineación técnica). |
| 2026-04-02 | **Plan multi-academia / membresías:** creado `docs/PLAN_MEMBRESIA_Y_TENANTS.md` (fases 0–5, criterios de cierre, evidencia y enlace en §6). Pendiente ejecutar fases; estado actual sigue siendo 1 academia ⇄ 1 `user_id` dueño. |
| 2026-04-02 | **Evidencia:** este documento actualizado (Storage, Room 11–13, colores de tema, portada por categoría). La IA no escribe aquí sola; conviene revisar §7 tras cada entrega. |
| 2026-04-02 | **Storage + sync de fotos:** bucket `academia-media`, políticas en SQL; `uploadAcademiaPublicImage` como `suspend`; URLs en `academias`, `jugadores`, `equipo_staff`; Room v11. |
| 2026-04-02 | **Colores de interfaz:** hex opcionales en `academia_config` y columnas `color_*_hex` en `academias`; UI en `AcademiaScreen`; tema dinámico en `AcademiaRoot` / `Theme.kt`; Room v12. |
| 2026-04-02 | **Portada por categoría:** columnas en `categorias` (Postgres + Room v13), edición en `CategoriaSelectionScreen`, `InicioScreen` según `filtroCategoria`, sync y subida `…/categorias/{uuid}/portada.*`. |
| 2026-04-01 | Auth: deep link `academiafutbol://auth` + `handleDeeplinks` en `MainActivity` y Toast al confirmar correo; plantilla de email documentada en §3.1. |
| 2026-04-01 | `AndroidManifest.xml`: permiso `INTERNET` para peticiones a Supabase Auth/PostgREST (evita “Permission denied (missing INTERNET permission?)”). |
| 2026-04-01 | Gradle: `dependencySubstitution` en `app/build.gradle.kts` para forzar `auth-kt-android` y `supabase-kt-android` en lugar de los artefactos JVM homónimos y evitar `Duplicate class` en `checkDebugDuplicateClasses`. |
| 2026-03-31 | Documento inicial: auth email, SQL Supabase, RLS, sync PostgREST, Room v10, UI de sincronización y referencia de rutas. |

---

## 8. Cómo mantener esta documentación

Tras cada entrega relevante:

1. Añadir una fila en la **sección 7** con fecha y breve descripción.
2. Si se crean tablas, endpoints o pantallas nuevas, actualizar **secciones 4–6** y el **mapa de archivos**.
3. Si cambia el procedimiento de despliegue o credenciales, actualizar **sección 2**.

---

## 9. Contacto / contexto del proyecto

- Nombre mostrado en la app: **Academia Fútbol** (`app_name` en recursos).
- Proyecto Supabase de referencia (usuario): **escuela-futbol-correcaminos** / región East US (Ohio) — la URL concreta va en `local.properties`, no en este documento versionado.
