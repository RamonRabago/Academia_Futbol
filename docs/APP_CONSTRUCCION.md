# Programa de construcción — Academia Fútbol (app Android)

Documento vivo para alinear **equipo**, **dueños de academias** y **desarrollo**: qué existe, con qué se conecta y dónde documentar cambios.

## Principios

1. **Una fuente de verdad por dominio:** datos operativos en **Room** (local) + **Supabase** (nube) donde aplica; el detalle de sync está en `AcademiaCloudSync` y en cada repositorio remoto.
2. **Registro de cambios obligatorio:** cualquier comportamiento que perciba el usuario o contrato con la nube debe reflejarse en **`CHANGELOG.md`** (sección `[Sin publicar]` o versión en curso).
3. **Simplicidad en UI:** flujos cortos, textos claros en `strings.xml`, permisos alineados con RLS en Postgres.

## Módulos de producto (alto nivel)

| Área | Pantalla / pestaña | Datos locales | Nube (Supabase) |
|------|---------------------|---------------|-----------------|
| Identidad / club | Academia, binding | `academia_config`, categorías | `academias`, `categorias`, miembros |
| Jugadores | Jugadores | `jugadores`, historial | `jugadores`, storage fotos |
| Operación diaria | Asistencia, estadísticas | asistencias, días entreno | `asistencias` (sync) |
| Economía | Finanzas | cobros mensuales, staff | tablas de cobros / staff |
| Comunicación urgente | Padres (avisos) | — (lectura en caliente) | `academia_mensajes_categoria` |
| **Contenido de valor** | **Recursos** | — (lista en memoria + pull) | **`academia_contenido_categoria`**, **`academia_contenido_reaccion`** |

## Recursos para la categoría (nuevo)

- **Objetivo:** noticias, entrenamientos, nutrición, ejercicios, bienestar; visible para **toda la categoría** elegida al publicar (o **todas las categorías** permitidas al usuario: un registro por categoría).
- **Quién publica:** dueño, admin, coordinador, o **coach** solo en categorías asignadas (misma lógica que avisos a padres).
- **Aprobación:** por defecto el borrador queda **`pending_approval`** hasta que el **coach de esa categoría** (o dueño/admin/coordinador) lo apruebe; las familias solo ven **`published`**. Dueño/admin (o dueño de cuenta) pueden activar **visible ya para familias** al publicar.
- **Quién lee:** padres con hijo en esa categoría; staff con acceso a la categoría.
- **Implementación app:** `ContenidoViewModel`, `ContenidoScreen`, pestaña **Recursos**, `AcademiaContenidoCategoriaRepository`; portada y galería en **Storage** (`uploadAcademiaPublicImage`); reacciones con **`AcademiaContenidoReaccionRepository`** (quitar = volver a pulsar la misma reacción).
- **SQL:** `20260501120000_academia_contenido_categoria.sql`; `20260502120000_academia_contenido_imagen_url.sql`; `20260515130000_academia_contenido_cuerpo_imgs_rls_autor.sql`; `20260515140000_academia_contenido_reaccion.sql`; **`20260515150000_archivar_academia_contenido_rpc.sql`** (RPC archivar publicación); **`20260515160000_contenido_estado_aprobacion.sql`** (`estado_publicacion`, RLS padres solo publicados).

## Cómo seguir evolucionando esto

1. Definir el cambio y actualizar **`CHANGELOG.md`** el mismo día del merge.
2. Si toca **esquema Supabase**: nueva migración en `supabase/migrations/`, nombre con timestamp; desplegar en el proyecto antes de publicar la APK que lo use.
3. Si toca **Room**: subir versión en `AcademiaDatabase` y migración local.
4. Probar **rol padre** y **rol coach** en dispositivo real (RLS distinto).

## Referencias rápidas en el repo

- Registro de versiones: `CHANGELOG.md`
- Contratos SQL recientes: `supabase/migrations/`
- Navegación y visibilidad por rol: `AcademiaNavPolicy.kt`, `AcademiaRoot.kt` (barra inferior: Inicio / Padres / Academia; menú ☰ arriba izquierda con el resto de rutas visibles).
