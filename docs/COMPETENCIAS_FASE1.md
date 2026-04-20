# Competencias multideporte — Fase 1 (implementado)

Fase 1 incluye **modelo en Supabase**, **DTOs + repositorio remoto**, **dominio (reglas de puntos + tabla)** y **caso de uso** de orquestación. **Fase 2** añade la ruta `competencias`, `CompetenciasScreen` / `CompetenciasViewModel` y textos en `strings.xml`. **Fase 3** habilita la misma ruta para **padre en nube** en solo lectura (ver `CHANGELOG.md`).

## 1. Resumen de arquitectura

- **Fuente de verdad:** tablas en **Postgres/Supabase** (`catalogo_deporte`, `academia_competencia`, `academia_competencia_categoria`, `academia_competencia_partido`).
- **Cliente Android:** lectura/escritura vía **`AcademiaCompetenciasRepository`** (PostgREST), igual que Recursos u otros módulos nube.
- **Lógica de negocio pura:** paquete **`domain.competencias`** — cálculo de tabla **sin** acoplar a fútbol; usa `score_propio` / `score_rival` y reglas de puntos resueltas desde deporte + overrides de competencia.
- **RLS:** padres leen competencias/inscripciones/partidos si tienen hijo vinculado en la **misma categoría** (`categoria_nombre`); staff owner/admin/coordinator; coach con `academia_mensaje_coach_categoria_coincide` (mismo helper que mensajes/contenido).

## 2. Archivos creados

| Archivo | Rol |
|---------|-----|
| `supabase/migrations/20260520100000_academia_competencias_multideporte_fase1.sql` | Tablas, índices, triggers `updated_at`, RLS, semilla de deportes |
| `supabase/migrations/20260520110000_academia_competencias_coach_select.sql` | Coach: `SELECT` competencias con inscripción en su categoría |
| `supabase/migrations/20260520120000_academia_competencias_coach_insert_borrador.sql` | Coach: `INSERT` competencias; `SELECT` borradores sin inscripciones activas |
| `supabase/migrations/20260520130000_academia_competencia_select_staff_data_access.sql` | `SELECT` competencias: **`academia_competencia_padre_puede_ver`** (SECURITY DEFINER, evita recursión con RLS de categorías) + **`academia_staff_data_access`** |
| `supabase/migrations/20260520140000_academia_competencia_select_fix_recursion.sql` | Misma corrección por si ya se aplicó una versión previa de la 201300 que causaba *infinite recursion* |
| `supabase/migrations/20260520150000_academia_comp_partido_unique_jornada.sql` | **Unicidad de jornada** por categoría inscrita en la competencia: reasigna duplicados históricos y `CREATE UNIQUE INDEX` sobre `(competencia_id, categoria_en_competencia_id, jornada)` |
| `app/.../data/remote/dto/CompetenciasMultideporteDto.kt` | Filas e inserts/patch serializables |
| `app/.../data/remote/AcademiaCompetenciasRepository.kt` | Listar deportes, competencias, inscripciones, partidos; insertar competencia/inscripción/partido; actualizar partido |
| `app/.../domain/competencias/ReglasPuntosResolver.kt` | Resolución de puntos victoria/empate/derrota |
| `app/.../domain/competencias/CompetenciaTablaCalculator.kt` | Agregación y orden de tabla |
| `app/.../domain/competencias/CompetenciasCasosUso.kt` | Orquesta repo + cálculo de tabla |
| `docs/COMPETENCIAS_FASE1.md` | Este documento |
| `docs/COMPETENCIAS_TABLA_EVOLUCION.md` | Tabla, columna **Var.** y líderes ofensivos: criterios actuales y extensiones previstas |

## 3. Archivos modificados

| Archivo | Cambio |
|---------|--------|
| `docs/FUNCIONALIDADES.md` | Sección prevista del módulo |
| `CHANGELOG.md` | Entrada bajo `[Sin publicar]` |

## 4. Modelos nuevos (concepto)

- **`catalogo_deporte`:** catálogo global (semilla: fútbol, básquetbol, béisbol, voleibol, genérico). Etiquetas para UI y flags `maneja_*` para evolución futura.
- **`academia_competencia`:** liga/torneo por `academia_id`, enlace `deporte_id`, fechas, tipo, overrides opcionales de puntos.
- **`academia_competencia_categoria`:** inscripción categoría ↔ competencia (`categoria_nombre` alineado a jugadores), `nombre_equipo_mostrado`, `grupo`.
- **`academia_competencia_partido`:** evento con marcador neutro `score_propio` / `score_rival`, `jugado`, `estado`, `detalle_marcador_json` reservado.

## 5. Cálculo de tabla y estadísticas

- Solo cuentan partidos con **`jugado = true`**, **`estado ≠ cancelado`** y **marcador completo** (ambos scores no nulos).
- Por cada inscripción activa se acumulan: PJ, G, E, P, score a favor/contra, diferencia.
- **Puntos:** `G * pv + E * pe + P * pd` con `pv/pe/pd` de la competencia si vienen informados, si no del deporte (`resolverReglasPuntosTabla`).
- **Orden:** puntos DESC, diferencia DESC, a favor DESC, nombre equipo.

## 6. Texto visible según deporte

- Las etiquetas vienen de **`catalogo_deporte`** (`etiqueta_score_singular`, etc.). La UI (Fase 2) debe leer esa fila según `deporte_id` de la competencia y usar esos textos en labels; **`strings.xml`** puede dar fallback para deportes desconocidos o códigos sin fila.

## 7. Cómo probar (sin pantalla aún)

1. **Aplicar migración** en tu proyecto Supabase (SQL Editor o `supabase db push` según tu flujo).
2. Con **SQL** o **Table editor**: confirma filas en `catalogo_deporte`.
3. Como **usuario staff** en la app (sesión real): desde código temporal o prueba unitaria, instancia `AcademiaCompetenciasRepository(client)` y:
   - `insertarCompetencia` con `deporte_id` de un catálogo existente.
   - `insertarInscripcion` con `categoria_nombre` existente en tu academia.
   - `insertarPartido` y `actualizarPartido` con `jugado=true`, scores y `estado=juguado` (o `jugado` + scores según flujo final).
4. `CompetenciasCasosUso(clienteRepo).calcularTablaPosiciones(academiaId, competenciaId)` debe devolver líneas ordenadas.

*(En Fase 2 se sustituirá la “prueba manual por código” por pantallas y, si aplica, tests JVM del calculador.)*

## 8. Fase 2 (implementado en app)

- Ruta **`competencias`** en **`AcademiaNavPolicy`** y menú ☰ de **`AcademiaRoot`**; **`CompetenciasViewModel`** registrado en **`AcademiaViewModelFactory`** (filtro de categoría + categorías permitidas del coach).
- Pantallas: lista → detalle con pestañas **Partidos**, **Tabla**, **Inscripciones**; diálogos de competencia, inscripción, partido y resultado.
- **Etiquetas de marcador** desde `CatalogoDeporteRow` con fallback en recursos de cadena.
- Pendiente opcional: espejo **Room** + `needsCloudPush` para offline.

## 9. Fase 3 (padre en nube, solo lectura)

- Con academia vinculada a la nube y rol de membresía **parent**, la ruta **`competencias`** es visible (`AcademiaNavPolicy`); en el menú ☰ del padre aparece junto a **Recursos** (`AcademiaRoot.tabsMenuDesplegable`).
- **`puedeCrearCompetencia`** / **`puedeAgregarInscripcionOPartido`** siguen en `false` para padre: sin FAB, sin botones de alta y sin edición de partido; textos de contexto en lista y franja bajo las pestañas en detalle (`CompetenciasScreen`, `strings.xml`).
- Los datos que ve el padre los limita **RLS** (p. ej. competencias con hijo en la misma categoría inscrita).

## 10. Escalabilidad (preparado, no implementado)

- **`detalle_marcador_json`** para sets/periodos/entradas.
- **`grupo`** en inscripción para tablas múltiples.
- **Fases / playoff:** nuevas tablas `competencia_fase` + FK en partido sin romper columnas actuales.
