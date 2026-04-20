-- Coach: puede ver competencias donde exista al menos una inscripción en una categoría asignada.

drop policy if exists "academia_competencia_select" on public.academia_competencia;
create policy "academia_competencia_select"
  on public.academia_competencia
  for select
  to authenticated
  using (
    exists (
      select 1
      from public.academia_competencia_categoria cc
      join public.academia_padres_alumnos apa on apa.academia_id = academia_competencia.academia_id
      join public.jugadores j on j.id = apa.jugador_id
      where cc.competencia_id = academia_competencia.id
        and apa.parent_user_id = auth.uid()
        and j.academia_id = academia_competencia.academia_id
        and j.activo = true
        and lower(trim(j.categoria)) = lower(trim(cc.categoria_nombre))
    )
    or public.academia_is_owner(academia_competencia.academia_id)
    or public.academia_miembro_activo_rol(
      academia_competencia.academia_id,
      array['owner', 'admin', 'coordinator']::text[]
    )
    or exists (
      select 1
      from public.academia_competencia_categoria cc2
      where cc2.competencia_id = academia_competencia.id
        and public.academia_mensaje_coach_categoria_coincide(
          academia_competencia.academia_id,
          cc2.categoria_nombre
        )
    )
  );
