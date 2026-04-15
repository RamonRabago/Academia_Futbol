-- Estados de publicación: pendiente de aprobación del entrenador de categoría vs visible a familias.

alter table public.academia_contenido_categoria
  add column if not exists estado_publicacion text not null default 'published';

alter table public.academia_contenido_categoria
  drop constraint if exists academia_contenido_cat_estado_chk;

alter table public.academia_contenido_categoria
  add constraint academia_contenido_cat_estado_chk
  check (estado_publicacion in ('published', 'pending_approval', 'rejected'));

alter table public.academia_contenido_categoria
  add column if not exists approved_at timestamptz;

alter table public.academia_contenido_categoria
  add column if not exists approved_by_user_id uuid references auth.users (id) on delete set null;

update public.academia_contenido_categoria
set estado_publicacion = 'published'
where estado_publicacion is null;

comment on column public.academia_contenido_categoria.estado_publicacion is
  'published = visible a padres; pending_approval = staff ve, entrenador de categoría aprueba; rejected = no se muestra a padres.';

create index if not exists idx_academia_contenido_cat_academia_estado
  on public.academia_contenido_categoria (academia_id, estado_publicacion);

-- SELECT: padres solo ven publicados; staff ve publicados + pendientes/rechazados en su ámbito.
drop policy if exists "academia_contenido_cat_select" on public.academia_contenido_categoria;
create policy "academia_contenido_cat_select"
  on public.academia_contenido_categoria
  for select
  to authenticated
  using (
    archived_at is null
    and (
      (
        estado_publicacion = 'published'
        and (
          exists (
            select 1
            from public.academia_padres_alumnos apa
            join public.jugadores j on j.id = apa.jugador_id
            where apa.parent_user_id = auth.uid()
              and apa.academia_id = academia_contenido_categoria.academia_id
              and j.academia_id = academia_contenido_categoria.academia_id
              and j.activo = true
              and lower(trim(j.categoria)) = lower(trim(academia_contenido_categoria.categoria_nombre))
          )
          or public.academia_is_owner(academia_contenido_categoria.academia_id)
          or public.academia_miembro_activo_rol(
            academia_contenido_categoria.academia_id,
            array['owner', 'admin', 'coordinator']::text[]
          )
          or public.academia_mensaje_coach_categoria_coincide(
            academia_contenido_categoria.academia_id,
            academia_contenido_categoria.categoria_nombre
          )
        )
      )
      or (
        estado_publicacion in ('pending_approval', 'rejected')
        and (
          author_user_id = auth.uid()
          or public.academia_is_owner(academia_contenido_categoria.academia_id)
          or public.academia_miembro_activo_rol(
            academia_contenido_categoria.academia_id,
            array['owner', 'admin', 'coordinator']::text[]
          )
          or public.academia_mensaje_coach_categoria_coincide(
            academia_contenido_categoria.academia_id,
            academia_contenido_categoria.categoria_nombre
          )
        )
      )
    )
  );

-- INSERT: publicado solo dueño de cuenta o miembro admin; pendiente también coordinador o coach de la categoría.
drop policy if exists "academia_contenido_cat_insert" on public.academia_contenido_categoria;
create policy "academia_contenido_cat_insert"
  on public.academia_contenido_categoria
  for insert
  to authenticated
  with check (
    author_user_id = auth.uid()
    and (
      (
        estado_publicacion = 'published'
        and (
          public.academia_is_owner(academia_id)
          or public.academia_miembro_activo_rol(academia_id, array['admin']::text[])
        )
      )
      or (
        estado_publicacion = 'pending_approval'
        and (
          public.academia_is_owner(academia_id)
          or public.academia_miembro_activo_rol(academia_id, array['admin', 'coordinator']::text[])
          or public.academia_mensaje_coach_categoria_coincide(academia_id, categoria_nombre)
        )
      )
    )
  );

drop policy if exists "academia_contenido_cat_update" on public.academia_contenido_categoria;
create policy "academia_contenido_cat_update"
  on public.academia_contenido_categoria
  for update
  to authenticated
  using (
    author_user_id = auth.uid()
    or public.academia_is_owner(academia_id)
    or public.academia_miembro_activo_rol(
      academia_id,
      array['owner', 'admin', 'coordinator']::text[]
    )
    or public.academia_mensaje_coach_categoria_coincide(academia_id, categoria_nombre)
  )
  with check (
    author_user_id = auth.uid()
    or public.academia_is_owner(academia_id)
    or public.academia_miembro_activo_rol(
      academia_id,
      array['owner', 'admin', 'coordinator']::text[]
    )
    or public.academia_mensaje_coach_categoria_coincide(academia_id, categoria_nombre)
  );

-- Reacciones: padres solo si el post está publicado; staff igual que contenido.
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
          (
            c.estado_publicacion = 'published'
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
          or (
            c.estado_publicacion in ('pending_approval', 'rejected')
            and (
              c.author_user_id = auth.uid()
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
          (
            c.estado_publicacion = 'published'
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
          or (
            c.estado_publicacion in ('pending_approval', 'rejected')
            and (
              c.author_user_id = auth.uid()
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
        )
    )
  );
