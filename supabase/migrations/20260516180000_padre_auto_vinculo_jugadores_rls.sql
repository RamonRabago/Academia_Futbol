-- Padre con rol activo en la academia puede:
-- 1) Ver jugadores cuyo email_tutor coincide con su correo de Auth y aún no están vinculados a él.
-- 2) Insertar fila en academia_padres_alumnos solo en ese caso (además de la política existente de staff).

create policy "jugadores_parent_candidate_select"
  on public.jugadores
  for select
  to authenticated
  using (
    coalesce(activo, true)
    and exists (
      select 1 from public.academia_miembros m
      where m.academia_id = jugadores.academia_id
        and m.user_id = auth.uid()
        and coalesce(m.activo, true)
        and lower(trim(m.rol)) = 'parent'
    )
    and lower(trim(coalesce(jugadores.email_tutor, ''))) = lower(trim(coalesce((auth.jwt() ->> 'email')::text, '')))
    and not exists (
      select 1 from public.academia_padres_alumnos p
      where p.jugador_id = jugadores.id
        and p.parent_user_id = auth.uid()
    )
  );

create policy "padres_alumnos_insert_parent_email_match"
  on public.academia_padres_alumnos
  for insert
  to authenticated
  with check (
    parent_user_id = auth.uid()
    and exists (
      select 1 from public.academia_miembros m
      where m.academia_id = academia_padres_alumnos.academia_id
        and m.user_id = auth.uid()
        and coalesce(m.activo, true)
        and lower(trim(m.rol)) = 'parent'
    )
    and exists (
      select 1 from public.jugadores j
      where j.id = academia_padres_alumnos.jugador_id
        and j.academia_id = academia_padres_alumnos.academia_id
        and coalesce(j.activo, true)
        and lower(trim(coalesce(j.email_tutor, ''))) = lower(trim(coalesce((auth.jwt() ->> 'email')::text, '')))
    )
  );
