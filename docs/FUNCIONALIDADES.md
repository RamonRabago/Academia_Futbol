# Funcionalidades — Academia Fútbol (app Android)

Catálogo **por áreas de producto** de lo que la app ofrece hoy. No sustituye el detalle técnico ni el historial de versiones: sirve de **mapa** para equipo, dueños y desarrollo.

**Mantener al día:** cuando cambie un flujo visible para el usuario, actualiza este archivo en la misma entrega (aunque sea un bullet) y registra el cambio en [`CHANGELOG.md`](../CHANGELOG.md).

**Documentos relacionados**

| Documento | Uso |
|------------|-----|
| [`APP_CONSTRUCCION.md`](APP_CONSTRUCCION.md) | Principios, tabla datos locales vs Supabase, evolución del producto |
| [`CHANGELOG.md`](../CHANGELOG.md) | Qué se añadió, cambió o corrigió en cada versión |
| [`EVIDENCIA_Y_SEGUIMIENTO.md`](EVIDENCIA_Y_SEGUIMIENTO.md) | Entorno, auth, evidencias técnicas |
| [`COMPETENCIAS_FASE1.md`](COMPETENCIAS_FASE1.md) | Módulo **Ligas / competencias / partidos** (multideporte): Fase 1 técnica |

---

## Competencias y partidos (en construcción)

- **Fase 1 (hecha en backend/código compartido):** tablas Supabase neutras (`score_propio` / `score_rival`), catálogo `catalogo_deporte`, RLS alineado a padres/staff/coach; repositorio `AcademiaCompetenciasRepository`, dominio de **tabla de posiciones** y **`CompetenciasCasosUso`**. Detalle en [`COMPETENCIAS_FASE1.md`](COMPETENCIAS_FASE1.md).
- **Fase 2 (hecha):** ruta **`competencias`** en menú ☰ (academia en nube; no padre en nube), hub **Equipo** e Inicio; pantallas **`CompetenciasScreen`** + **`CompetenciasViewModel`**: lista, detalle (partidos / tabla / inscripciones), alta de competencia, inscripción, partido y resultado. **Tabla:** columna **Var.** (criterio exacto y evolución prevista en [`COMPETENCIAS_TABLA_EVOLUCION.md`](COMPETENCIAS_TABLA_EVOLUCION.md)) y bloque de **líderes ofensivos** solo con desglose coherente con el marcador; etiquetas según deporte. No hace falta ninguna tabla extra: **`academia_competencia_categoria`** puede estar vacía hasta inscribir categorías; si la liga existe en **`academia_competencia`** pero no sale en la app, suele ser **RLS** (migraciones `20260520120000`, **`20260520130000`** y, si hubo error de recursión, **`20260520140000`** en Supabase).
- **Fase 3 (pendiente):** vistas solo lectura para padres.

---

## Navegación y roles

- **Barra inferior:** **Inicio**, **Padres** (si el rol del dispositivo lo permite) y **Academia**.
- **Menú ☰ (cabecera):** acceso a **Jugadores**, **Asistencia**, **Estadísticas**, **Recursos**, **Competencias** (solo con academia en nube y no como padre en nube), **Finanzas** (según permisos y academia vinculada a la nube). Puede incluir **Cambiar categoría** y otras entradas con scroll si no caben.
- **Padre en nube** (`remoteAcademiaId` + rol `parent`): suele ver **Inicio**, **Recursos**, **Padres** y **Academia**; no **Finanzas** ni el bloque operativo de equipo (jugadores / asistencia / estadísticas) salvo política distinta en código.
- **Filtro de categoría:** afecta listados y resúmenes donde aplique («todas» o una categoría concreta).

Lógica central de visibilidad: `AcademiaNavPolicy.kt`, `AcademiaRoot.kt`.

---

## Inicio

- Pantalla de bienvenida / accesos según lo que el usuario pueda abrir (atajos a rutas como Recursos si están visibles).

---

## Jugadores

- Listado de **alumnos activos** (nombre, categoría, foto si existe).
- **Alta, edición y baja** de jugadores; datos locales en Room con sincronización a Supabase cuando la academia está en nube.
- Campos operativos relevantes para el club (categoría, becado, mensualidad, contacto, etc., según pantalla).
- Fotos: almacenamiento local y/o **Storage** en nube según configuración.

---

## Asistencia

- **Día de trabajo:** flechas o calendario para elegir fecha; título con fecha legible.
- **Día de entrenamiento:** interruptor para marcar si ese día **cuenta como sesión** para resúmenes y estadísticas (tabla `dias_entrenamiento` en Room).
- **Lista del día:** por alumno (foto, nombre, categoría) con interruptor **presente / no presente**; ausencia por defecto sin fila en base hasta que se confirme.
- **Marcar todos presentes** (un toque).
- **Resumen visual:** período **mes del día seleccionado** o **año completo**; porcentaje, presentes, ausentes, total de cupos, días con marcas, barra de progreso; opción **«Ver resumen de»** todo el equipo o **un alumno**.
- El **porcentaje** considera todos los cupos (alumno × día marcado como entrenamiento); quien no asistió y no tiene fila explícita cuenta como **ausente** (misma lógica que el interruptor por defecto).

---

## Estadísticas

- Resumen de plantilla (total jugadores, etc.).
- **Asistencia media global** alineada con el criterio de días de entrenamiento y cupos implícitos (ver Asistencia).
- Indicadores si hay marcas fuera de días marcados como entreno.
- **Cuotas / mensualidades** (si el dispositivo y usuario pueden ver importes según configuración).

---

## Recursos (contenido por categoría)

- **Feed** de publicaciones (noticias, entrenamiento, nutrición, ejercicio, bienestar, otros) con portada, galería, texto breve, tiempo relativo.
- **Filtros** por tema y, para staff, por **estado** (publicado / pendiente / rechazado) en panel inferior.
- **Publicar:** pantalla completa; mensaje (límite de caracteres), portada y hasta varias fotos en el cuerpo; galería y cámara; opción de publicar en **una categoría** o en **todas** las permitidas (varias filas en nube).
- **Moderación:** aprobar / rechazar pendientes (dueño, admin, coordinador, coach en su categoría según reglas).
- **Visible ya para familias** en publicación (roles con permiso).
- **Reacciones** en posts publicados (like, celebrar, gracias, fuerte); repetir la misma reacción la quita.
- **Archivar / quitar del listado**; visor de imagen a pantalla completa con zoom.
- Vista **«todas las categorías»:** una misma publicación multi-categoría se muestra **una sola tarjeta** (categorías en cabecera; acciones de moderación/archivo aplican a todas las filas vinculadas).
- **Badge de no leídos** en pestaña/menú (local, sin depender de push).
- Datos desde **Supabase** (`academia_contenido_categoria`, `academia_contenido_reaccion`); lista en memoria con refresco.

---

## Finanzas

- Gestión de **cobros mensuales** y vista por alumnos según alcance (academia / categoría).
- Prellenado de mes según fichas y reglas de cuota; becados y ajustes coordinados con la ficha del jugador donde aplica.

---

## Padres (avisos y vínculos)

- **Avisos por categoría** desde la nube (mensajes a familias).
- **Vínculo tutor ↔ alumno:** staff puede gestionar miembros; tutores pueden vincular hijos según flujos implementados (incl. candidatos por **correo tutor** en nube cuando aplica).
- Auto-vínculo o listas desde RPC/RLS según migraciones desplegadas.

---

## Academia (configuración del club)

- **Vinculación** con academia en Supabase, identidad (nombre, logo, colores), **invitaciones** por rol (códigos compartir/copiar/correo).
- **Menú de apartados:** invitaciones, identidad, colores, pagos/privacidad, equipo, cuenta; navegación con barra y **atrás** por sección.
- **Gestionar miembros** (cuenta en nube): lista de padres/tutores, roles, vínculos con alumnos, filtros por categoría; sin duplicar el flujo de códigos de invitación en esa pantalla.
- **Equipo técnico (staff)** y datos relacionados según pantallas de Academia.
- **Sincronización** con la nube donde esté implementada (`AcademiaCloudSync` y repositorios).

---

## Autenticación y sesión

- **Login** con email/contraseña (Supabase Auth).
- Confirmación de correo y enlaces profundos según `EVIDENCIA_Y_SEGUIMIENTO.md`.

---

## Otros

- **Hub Equipo** (`equipo_hub`): agrupación lógica de rutas de operación; la app puede enlazarlo desde otros sitios.
- Notificaciones push: guía de montaje en [`PUSH_FCM_SETUP.md`](PUSH_FCM_SETUP.md) si aplica al despliegue.

---

*Última revisión orientativa del alcance: abril 2026. Ajustar esta fecha cuando se actualice el contenido.*
