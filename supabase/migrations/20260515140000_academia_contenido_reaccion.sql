-- Reacciones a publicaciones de Recursos (misma audiencia que el contenido).

create table if not exists public.academia_contenido_reaccion (
  id uuid primary key default gen_random_uuid(),
  academia_id uuid not null references public.academias (id) on delete cascade,
  contenido_id uuid not null references public.academia_contenido_categoria (id) on delete cascade,
  user_id uuid not null references auth.users (id) on delete cascade,
  tipo text not null
    check (tipo in ('like', 'celebrate', 'thanks', 'strong')),
  created_at timestamptz not null default now(),
  unique (contenido_id, user_id)
);

create index if not exists idx_academia_contenido_reaccion_academia
  on public.academia_contenido_reaccion (academia_id);

create index if not exists idx_academia_contenido_reaccion_contenido
  on public.academia_contenido_reaccion (contenido_id);

comment on table public.academia_contenido_reaccion is
  'Una reacción por usuario y publicación (like, celebrate, thanks, strong).';

alter table public.academia_contenido_reaccion enable row level security;

grant select, insert, delete on public.academia_contenido_reaccion to authenticated;

-- Misma visibilidad que el post: padres con hijo en categoría, dueño, admin/coord, coach de categoría.
drop policy if exists "academia_contenido_reaccion_select" on public.academia_contenido_reaccion;
create policy "academia_contenido_reaccion_select"
  on public.academia_contenido_reaccion
  for select
  to authenticated
  using (
    exists (
      select 1
      from public.academia_contenido_categoria c
      where c.id = academia_contenido_reaccion.contenido_id
        and c.academia_id = academia_contenido_reaccion.academia_id
        and c.archived_at is null
        and (
          exists (
            select 1
            from public.academia_padres_alumnos apa
            join public.jugadores j on j.id = apa.jugador_id
            where apa.parent_user_id = auth.uid()
              and apa.academia_id = c.academia_id
              and j.academia_id = c.academia_id
              and j.activo = true
              and lower(trim(j.categoria)) = lower(trim(c.categoria_nombre))
          )
          or public.academia_is_owner(c.academia_id)
          or public.academia_miembro_activo_rol(
            c.academia_id,
            array['owner', 'admin', 'coordinator']::text[]
          )
          or public.academia_mensaje_coach_categoria_coincide(
            c.academia_id,
            c.categoria_nombre
          )
        )
    )
  );

drop policy if exists "academia_contenido_reaccion_insert" on public.academia_contenido_reaccion;
create policy "academia_contenido_reaccion_insert"
  on public.academia_contenido_reaccion
  for insert
  to authenticated
  with check (
    user_id = auth.uid()
    and exists (
      select 1
      from public.academia_contenido_categoria c
      where c.id = contenido_id
        and c.academia_id = academia_id
        and c.archived_at is null
        and (
          exists (
            select 1
            from public.academia_padres_alumnos apa
            join public.jugadores j on j.id = apa.jugador_id
            where apa.parent_user_id = auth.uid()
              and apa.academia_id = c.academia_id
              and j.academia_id = c.academia_id
              and j.activo = true
              and lower(trim(j.categoria)) = lower(trim(c.categoria_nombre))
          )
          or public.academia_is_owner(c.academia_id)
          or public.academia_miembro_activo_rol(
            c.academia_id,
            array['owner', 'admin', 'coordinator']::text[]
          )
          or public.academia_mensaje_coach_categoria_coincide(
            c.academia_id,
            c.categoria_nombre
          )
        )
    )
  );

drop policy if exists "academia_contenido_reaccion_delete" on public.academia_contenido_reaccion;
create policy "academia_contenido_reaccion_delete"
  on public.academia_contenido_reaccion
  for delete
  to authenticated
  using (user_id = auth.uid());
