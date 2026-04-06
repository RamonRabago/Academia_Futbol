-- Punto 3 (producto): coordinator = admin en gestión de club en app; RLS alineado con UPDATE en `academias`.
-- Sin esto, coordinator ve UI de gestión pero PostgREST rechaza nombre/código/tema/medios.

drop policy if exists "academias_update" on public.academias;

create policy "academias_update"
  on public.academias
  for update
  to authenticated
  using (
    public.academia_is_owner(id)
    or public.academia_miembro_activo_rol(
      id,
      array['owner', 'admin', 'coordinator']::text[]
    )
  )
  with check (
    public.academia_is_owner(id)
    or public.academia_miembro_activo_rol(
      id,
      array['owner', 'admin', 'coordinator']::text[]
    )
  );
