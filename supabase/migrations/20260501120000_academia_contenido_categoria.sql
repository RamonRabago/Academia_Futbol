-- Recursos por categoría: noticias, entrenamiento, nutrición, ejercicios, etc.
-- Lectura: padres con hijo en la categoría + staff (mismo criterio que academia_mensajes_categoria).

create table if not exists public.academia_contenido_categoria (
  id uuid primary key default gen_random_uuid(),
  academia_id uuid not null references public.academias (id) on delete cascade,
  categoria_nombre text not null,
  tema text not null
    check (tema in (
      'noticia',
      'entrenamiento',
      'nutricion',
      'ejercicio',
      'bienestar',
      'otro'
    )),
  titulo text not null,
  cuerpo text not null,
  author_user_id uuid not null references auth.users (id) on delete cascade,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  archived_at timestamptz,
  constraint academia_contenido_cat_titulo_no_vacio check (char_length(trim(titulo)) > 0),
  constraint academia_contenido_cat_cuerpo_no_vacio check (char_length(trim(cuerpo)) > 0)
);

create index if not exists idx_academia_contenido_cat_academia_created
  on public.academia_contenido_categoria (academia_id, created_at desc);

create index if not exists idx_academia_contenido_cat_academia_categoria
  on public.academia_contenido_categoria (academia_id, categoria_nombre);

comment on table public.academia_contenido_categoria is
  'Contenido educativo por categoría (visible a padres con hijo en esa categoría).';

drop trigger if exists trg_academia_contenido_updated on public.academia_contenido_categoria;
create trigger trg_academia_contenido_updated
  before update on public.academia_contenido_categoria
  for each row execute function public.set_updated_at();

alter table public.academia_contenido_categoria enable row level security;

grant select, insert, update on public.academia_contenido_categoria to authenticated;

drop policy if exists "academia_contenido_cat_select" on public.academia_contenido_categoria;
create policy "academia_contenido_cat_select"
  on public.academia_contenido_categoria
  for select
  to authenticated
  using (
    archived_at is null
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
  );

drop policy if exists "academia_contenido_cat_insert" on public.academia_contenido_categoria;
create policy "academia_contenido_cat_insert"
  on public.academia_contenido_categoria
  for insert
  to authenticated
  with check (
    author_user_id = auth.uid()
    and (
      public.academia_is_owner(academia_id)
      or public.academia_miembro_activo_rol(
        academia_id,
        array['owner', 'admin', 'coordinator']::text[]
      )
      or public.academia_mensaje_coach_categoria_coincide(academia_id, categoria_nombre)
    )
  );

drop policy if exists "academia_contenido_cat_update" on public.academia_contenido_categoria;
create policy "academia_contenido_cat_update"
  on public.academia_contenido_categoria
  for update
  to authenticated
  using (
    public.academia_is_owner(academia_id)
    or public.academia_miembro_activo_rol(
      academia_id,
      array['owner', 'admin', 'coordinator']::text[]
    )
    or public.academia_mensaje_coach_categoria_coincide(academia_id, categoria_nombre)
  )
  with check (
    public.academia_is_owner(academia_id)
    or public.academia_miembro_activo_rol(
      academia_id,
      array['owner', 'admin', 'coordinator']::text[]
    )
    or public.academia_mensaje_coach_categoria_coincide(academia_id, categoria_nombre)
  );
