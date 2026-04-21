# Fase 4 — Padres: seguimiento de tickets

**Objetivo de producto (cerrado):** el tutor puede **desvincularse solo** de un alumno; **varios tutores** por alumno; **sin límite** de tutores en esta fase; al desvincular deja de ver datos del alumno; no puede quitar vínculos de otros.

**Última actualización del checklist:** 2026-04-21 (sesión implementación base + UI).

---

## Tickets (orden sugerido) y estado

| # | Ticket | Tamaño | Estado |
|---|--------|--------|--------|
| 1 | Decisión de producto por escrito | S | **Hecho** (reglas acordadas en chat; reflejadas en `FUNCIONALIDADES.md`) |
| 2 | Migración Supabase: `padres_alumnos_delete_parent_own` | M | **Código listo** — falta **aplicar migración** en tu proyecto Supabase (`20260521140000_padres_alumnos_delete_parent_own.sql`) |
| 3 | Opcional: columnas `source` / `created_by` en `academia_padres_alumnos` | S–M | **Pendiente** |
| 4 | `PadresAlumnosRepository` (delete padre documentado) | S | **Hecho** |
| 5 | `ParentsViewModel`: vínculos, desvincular, refresco | M | **Hecho** (base) |
| 6 | `PadreConHijos`: «Vincular otro hijo» + candidatos | M | **Hecho** |
| 7 | Confirmaciones / textos vacíos (vincular con diálogo opcional, etc.) | S–M | **Parcial** (desvincular con confirmación; vincular sigue en un toque) |
| 8 | UI desvincular padre en tarjeta hijo | M | **Hecho** |
| 9 | `AcademiaMiembrosAdminScreen` / VM alineados (mensajes, `source` si existe) | S | **Pendiente** |
| 10 | Ficha jugador (staff): `email_tutor` visible / validación suave | S | **Pendiente** |
| 11 | `CHANGELOG.md` + `FUNCIONALIDADES.md` | S | **Hecho** (ajustar en cada entrega) |
| 12 | QA por rol en staging (matriz padre / staff / regresión) | M | **En curso contigo** (pruebas reales) |

---

## Qué tocar en el código (referencia rápida)

- **SQL:** `supabase/migrations/20260521140000_padres_alumnos_delete_parent_own.sql`
- **App:** `ParentsViewModel.kt`, `ParentsScreen.kt`, `PadresAlumnosRepository.kt`
- **Copy:** `strings.xml` (cadenas `parent_unlink*`, `parent_add_child_*`)

---

## Siguiente paso inmediato (para ti)

1. **Desplegar** la migración `20260521140000` en Supabase (CLI o SQL Editor).
2. En dispositivo con cuenta **padre**: probar **Desvincular** y **Vincular otro hijo**; revisar estética y textos y anotar cambios (los vamos aplicando en siguientes commits).

---

## Notas de estética / UX (rellenar al probar)

*(Espacio para bullets cuando pruebes: pantalla X, cambiar padding, texto Y…)*
