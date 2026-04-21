-- Fase 4 padres: el tutor puede eliminar solo su propia fila en academia_padres_alumnos.
-- Reglas de negocio: varios tutores por alumno (unique solo parent_user_id + jugador_id);
-- no se limita el número de tutores por alumno en esta fase.
-- Convive con la política existente padres_alumnos_delete (staff); en RLS varias políticas DELETE se combinan con OR.

create policy "padres_alumnos_delete_parent_own"
  on public.academia_padres_alumnos
  for delete
  to authenticated
  using (
    parent_user_id = auth.uid()
    and exists (
      select 1
      from public.academia_miembros m
      where m.academia_id = academia_padres_alumnos.academia_id
        and m.user_id = auth.uid()
        and coalesce(m.activo, true)
        and lower(trim(m.rol)) = 'parent'
    )
  );

comment on policy "padres_alumnos_delete_parent_own" on public.academia_padres_alumnos is
  'El padre elimina únicamente su vínculo (parent_user_id = auth.uid()); no puede desvincular a otros tutores.';
