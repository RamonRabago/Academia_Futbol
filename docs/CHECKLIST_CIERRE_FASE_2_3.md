# Checklist — cierre Fase 2 y 3 (MVP en repo)

**Fase 2 (unión a academia):** cerrada en alcance **MVP** — código de club, onboarding, selector multi-academia, `joinByCode` con código normalizado (`trim` + mayúsculas).

**Fase 3 (permisos en app):** cerrada en alcance **MVP** — roles en nube, coach por categorías, padre con rutas limitadas, gestión de miembros, coordinator alineado con admin (app + RLS tras migraciones), revocar = DELETE en `academia_miembros`, invitación = compartir / copiar / borrador de correo (sin API server-side de invitación Auth).

## Pruebas recomendadas (dispositivo + Supabase actualizado)

1. **Onboarding:** usuario nuevo crea academia o se une con código (coach/coordinator/parent); texto «¿Cómo me uno?» visible.
2. **Código:** `trim` y espacios no rompen el alta; botón Unirme con ≥ 4 caracteres tras trim.
3. **Multi-academia:** pantalla «Elige academia» muestra subtítulo; elegir una entra al flujo principal.
4. **Invitación:** con código generado, desde Academia — compartir, copiar, redactar correo abren apps del sistema con texto coherente.
5. **Miembros:** bloque «Invitar a nuevas cuentas» en pantalla de miembros; quitar del club pide confirmación y borra fila (coach sin categorías en cascada).
6. **Dueño:** no aparece «Quitar del club» en la cuenta dueña de `academias.user_id` (o según UI: dueño de cuenta bloqueado).
7. **Padre en nube:** solo Inicio, Padres, Academia en barra y atajos.
8. **Coach:** solo categorías asignadas en jugadores/asistencia/stats.
9. **Coordinator:** tras SQL `20260416130000` + `20260417100000`, puede actualizar `academias` y gestionar miembros como admin en producto acordado.

## Fuera de este cierre (fases posteriores)

- Invitación vía **Supabase Auth Admin** / Edge Function con `service_role` (no va en el APK).
- **Fase 4** padres: flujo completo vínculo hijo.
- **Fase 5** límites por plan y migración masiva de tenants.
