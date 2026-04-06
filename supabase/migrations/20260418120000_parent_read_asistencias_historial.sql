-- Fase 4: padres con cuenta pueden leer asistencias e historial solo de jugadores vinculados en academia_padres_alumnos.

drop policy if exists "asistencias_parent_select" on public.asistencias;
create policy "asistencias_parent_select"
  on public.asistencias
  for select
  to authenticated
  using (
    exists (
      select 1 from public.academia_padres_alumnos p
      where p.jugador_id = asistencias.jugador_id
        and p.parent_user_id = auth.uid()
    )
  );

drop policy if exists "historial_parent_select" on public.jugador_historial;
create policy "historial_parent_select"
  on public.jugador_historial
  for select
  to authenticated
  using (
    exists (
      select 1 from public.academia_padres_alumnos p
      where p.jugador_id = jugador_historial.jugador_id
        and p.parent_user_id = auth.uid()
    )
  );
