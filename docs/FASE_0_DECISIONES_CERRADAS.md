# Fase 0 — Decisiones cerradas (membresías y multi-academia)

**Estado:** cerrada.  
**Fecha de cierre:** 2 de abril de 2026.  
**Contexto:** decisiones acordadas con el product owner; alineadas con `PLAN_MEMBRESIA_Y_TENANTS.md` (Anexos A y B) salvo donde aquí se precisa.  
**Implementación:** pendiente (Fases 1 en adelante del plan); este documento **no** implica que el código o SQL ya reflejen estas decisiones.

---

## Resumen ejecutivo

| # | Tema | Decisión |
|---|------|----------|
| 1 | Alta de academia | Self-serve: quien activa/compra queda como dueño/admin del tenant; opcional alta manual para enterprise/pilotos. |
| 2 | Entrada del staff | Código de club corto + registro/login con email; **y** invitación por email desde la app del administrador (ambos canales). |
| 3 | Padres (MVP) | **Una sola vía en la primera versión:** padres **con cuenta** Supabase; unión con código de club + vínculo al hijo (o invitación). Modelo sin cuenta queda para una fase posterior. |
| 4 | Varios clubes por usuario | **Sí.** Tras login: selector de academia (o recordar última); MVP puede implementar selector cuando exista multi-membresía en backend. |
| 5 | Roles de negocio | Definidos en §2 (owner, admin, coordinator, coach, parent). |
| 6 | Dueño técnico vs membresías | **Híbrido (Fase 1):** mantener `academias.user_id` como dueño del tenant; tabla `academia_miembros` (nombre final TBD en migración) para el resto. RLS: dueño **o** membresía activa con permisos. |
| 7 | Límites por plan comercial | Tres planes con techos explícitos en §3; precio en pesos **no** fijado aquí (mercado + costes). |

---

## 1. Roles de negocio (nombres y alcance)

Definición para diseño de RLS, UI y facturación futura. Los nombres en UI pueden traducirse (“Dueño”, “Coordinación”, etc.).

| Rol | Descripción operativa |
|-----|------------------------|
| **owner** | Creador / responsable del tenant (`academias.user_id`). Facturación conceptual, puede destruir/configurar academia, invitar admins, todo el alcance del club salvo restricciones explícitas por plan. |
| **admin** | Miembro con mismos o casi mismos poderes operativos que el dueño en día a día (invitar staff, categorías, jugadores, sync); puede no ser el titular de pago. |
| **coordinator** | Gestión amplia: jugadores, categorías, asistencia, estadísticas, políticas que el plan permita; típicamente sin borrar el tenant ni gestionar facturación del producto. |
| **coach** | Operación acotada a **categorías asignadas** (Fase 3): jugadores, asistencia y vistas permitidas solo en esas categorías. |
| **parent** | Solo datos vinculados a sus hijos (hijos definidos en modelo futuro); sin gestión del club. MVP: cuenta Supabase + código club + vínculo/invitación. |

*Nota:* “Coordinador” y “entrenador” en el **staff local** (Room / `RolStaff`) siguen existiendo para ficha del club; el rol de **membresía** en nube (`coach` vs `coordinator`) gobierna permisos de cuenta.

---

## 2. Punto 6 — Modelo híbrido dueño + membresías (evidencia de diseño)

**Decisión adoptada:**

1. La fila `academias` conserva **`user_id`** como **dueño del tenant** (origen actual en `20260331120000_academia_cloud.sql`).
2. Se añadirá tabla de **miembros** (p. ej. `academia_miembros`): `academia_id`, `user_id` (auth), `rol` (enum/texto acorde a §1), `created_at`, opcional `invited_by`, `activo`.
3. **Autorización:** un usuario accede a datos del tenant si `auth.uid() = academias.user_id` **o** existe membresía activa con rol suficiente para la operación.
4. **Evolución futura opcional:** insertar al dueño también como fila `owner` en miembresías para unificar comprobaciones; no es obligatorio en el primer entregable de Fase 1.

**Motivo:** minimizar riesgo sobre código y RLS existentes; habilitar staff y padres sin que cada registro cree una nueva fila `academias`.

---

## 3. Punto 7 — Límites por plan (para producto y billing futuro)

Techos orientativos para **mensajes comerciales** y **límites técnicos** cuando se implementen. Dejar margen operativo (~10–20 %) entre “marketing” y bloqueo duro si se desea.

| Plan | Jugadores activos (tope) | Cuentas staff (maestro/coord/admin; no cuenta jugador) | Padres con cuenta (Auth) |
|------|--------------------------|--------------------------------------------------------|---------------------------|
| **Esencial** | 120 | 8 | 0 en MVP de padres *(o “no aplica” hasta lanzar padres con cuenta)* |
| **Club** | 350 | 25 | hasta 400 |
| **Academia** | 900 | 60 | hasta 1.500 *(o “alto volumen” con revisión comercial)* |

**Notas:**

- Caso de referencia del interlocutor (~250 jugadores, 10 maestros, 10 coordinadoras, 1 dueño): encaja en plan **Club** (21 cuentas staff < 25).
- Si al lanzar comercial solo se ofrecen **dos** planes: usar **Esencial** + **Club**; **Academia** se activa con clientes grandes.
- Precios en MXN/USD y método de cobro **no** forman parte de esta evidencia; ver `PLAN_MEMBRESIA_Y_TENANTS.md` Anexo B.

---

## 4. Código de club e invitaciones

- **Código:** generado/regenerado por dueño o admin en la app (longitud y formato en especificación de Fase 2).
- **Invitación por email:** enviada desde la app por dueño/admin; enlaza o complementa al código para fijar `academia_id` al aceptar.

---

## 5. Trazabilidad

| Documento | Relación |
|-----------|----------|
| `docs/PLAN_MEMBRESIA_Y_TENANTS.md` | Plan maestro por fases; Anexos A/B son el contexto previo a este cierre. |
| `docs/EVIDENCIA_Y_SEGUIMIENTO.md` | §7 — fila de changelog el día del cierre de Fase 0. |
| `supabase/migrations/20260331120000_academia_cloud.sql` | Estado **antes** de migraciones de miembresías; será necesaria nueva migración (Fase 1). |

---

## 6. Próximo paso formal

**Fase 1:** migración SQL + políticas RLS alineadas a §2 y §3 de este documento (y plan principal).
