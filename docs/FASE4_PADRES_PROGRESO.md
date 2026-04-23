# Fase 4 — Padres: seguimiento de tickets

**Objetivo de producto (cerrado):** el tutor puede **desvincularse solo** de un alumno; **varios tutores** por alumno; **sin límite** de tutores en esta fase; al desvincular deja de ver datos del alumno; no puede quitar vínculos de otros.

**Última actualización del checklist:** 2026-04-22 (migración verificada en prod + confirmación al vincular + ficha jugador `email_tutor`).

---

## Registro de acciones (Fase 4)

| Fecha | Qué |
|--------|-----|
| 2026-04-22 | **Ticket 2 (Supabase):** en proyecto **producción** `escuela-futbol-correcaminos` (rama `main`), al ejecutar el SQL del repo apareció `42710 — policy "padres_alumnos_delete_parent_own" already exists` → la política **ya estaba desplegada**; no hace falta repetir el `CREATE POLICY` en ese entorno. |
| 2026-04-22 | **Ticket 7 (app):** diálogo de confirmación antes de **Vincular** candidato (`ParentsLinkChildPanel` + cadenas `parent_self_link_confirm_*`). Ver `CHANGELOG.md` [Sin publicar]. |
| 2026-04-22 | **Ticket 10 (app):** ficha jugador (staff) — campo **correo tutor** con ayuda al vínculo padre, teclado email y **validación suave** (guardar deshabilitado si el texto no está vacío y no parece correo) (`PlayersScreen` / `JugadorFormDialog`, `strings.xml`). |

---

## Tickets (orden sugerido) y estado

| # | Ticket | Tamaño | Estado |
|---|--------|--------|--------|
| 1 | Decisión de producto por escrito | S | **Hecho** (reglas acordadas en chat; reflejadas en `FUNCIONALIDADES.md`) |
| 2 | Migración Supabase: `padres_alumnos_delete_parent_own` | M | **Hecho** — verificado en **producción** (política ya existía; archivo `20260521140000_padres_alumnos_delete_parent_own.sql` en repo como referencia). **Otros proyectos Supabase** (p. ej. staging): comprobar aparte si aún no se aplicó. |
| 3 | Opcional: columnas `source` / `created_by` en `academia_padres_alumnos` | S–M | **Pendiente** |
| 4 | `PadresAlumnosRepository` (delete padre documentado) | S | **Hecho** |
| 5 | `ParentsViewModel`: vínculos, desvincular, refresco | M | **Hecho** (base) |
| 6 | `PadreConHijos`: «Vincular otro hijo» + candidatos | M | **Hecho** |
| 7 | Confirmaciones / textos vacíos (vincular con diálogo opcional, etc.) | S–M | **Hecho** (desvincular con confirmación; vincular con diálogo de confirmación en `ParentsLinkChildPanel`) |
| 8 | UI desvincular padre en tarjeta hijo | M | **Hecho** |
| 9 | `AcademiaMiembrosAdminScreen` / VM alineados (mensajes, `source` si existe) | S | **Pendiente** |
| 10 | Ficha jugador (staff): `email_tutor` visible / validación suave | S | **Hecho** |
| 11 | `CHANGELOG.md` + `FUNCIONALIDADES.md` | S | **Hecho** (ajustar en cada entrega) |
| 12 | QA por rol en staging (matriz padre / staff / regresión) | M | **En curso contigo** (pruebas reales) |

---

## Qué tocar en el código (referencia rápida)

- **SQL:** `supabase/migrations/20260521140000_padres_alumnos_delete_parent_own.sql`
- **App:** `ParentsViewModel.kt`, `ParentsScreen.kt`, `PadresAlumnosRepository.kt`
- **Copy:** `strings.xml` (cadenas `parent_unlink*`, `parent_summary_add_child`, `parent_children_empty_state`, etc.)

---

## Siguiente paso inmediato (para ti)

1. Si tenéis **otro** proyecto Supabase (staging / otro club), ejecutar allí el SQL **solo si** al comprobar no existe la política (si devuelve el mismo `already exists`, igual que prod: listo).
2. En dispositivo con cuenta **padre**: probar **Desvincular**, **Vincular** (nuevo diálogo de confirmación) y **Vincular otro hijo**; anotar en «Notas de estética / UX» lo que surja.

---

## Notas de estética / UX (rellenar al probar)

*(Espacio para bullets cuando pruebes: pantalla X, cambiar padding, texto Y…)*
