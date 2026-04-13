-- Avisos por categoría: staff autorizado publica; padres leen solo si tienen hijo vinculado en esa categoría.

create table if not exists public.academia_mensajes_categoria (
  id uuid primary key default gen_random_uuid(),
  academia_id uuid not null references public.academias (id) on delete cascade,
  categoria_nombre text not null,
  tipo text not null
    check (tipo in (
      'partido_evento',
      'convivio_logistica',
      'administrativo',
      'otro'
    )),
  titulo text not null,
  cuerpo text not null,
  author_user_id uuid not null references auth.users (id) on delete cascade,
  created_at timestamptz not null default now(),
  event_at timestamptz,
  archived_at timestamptz,
  constraint academia_mensajes_cat_titulo_no_vacio check (char_length(trim(titulo)) > 0),
  constraint academia_mensajes_cat_cuerpo_no_vacio check (char_length(trim(cuerpo)) > 0)
);

create index if not exists idx_academia_mensajes_cat_academia_created
  on public.academia_mensajes_categoria (academia_id, created_at desc);

create index if not exists idx_academia_mensajes_cat_academia_categoria
  on public.academia_mensajes_categoria (academia_id, categoria_nombre);

comment on table public.academia_mensajes_categoria is
  'Mensajes de staff a padres filtrados por nombre de categoría (alineado a jugadores.categoria).';

-- Coach asignado a la categoría (por nombre en tabla categorias).
create or replace function public.academia_mensaje_coach_categoria_coincide(
  p_academia_id uuid,
  p_categoria_nombre text
)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1
    from public.academia_miembros m
    join public.academia_miembro_categorias l on l.miembro_id = m.id
    join public.categorias c on c.id = l.categoria_id
    where m.academia_id = p_academia_id
      and m.user_id = auth.uid()
      and m.activo = true
      and lower(trim(m.rol)) = 'coach'
      and lower(trim(c.nombre)) = lower(trim(p_categoria_nombre))
  );
$$;

grant execute on function public.academia_mensaje_coach_categoria_coincide(uuid, text) to authenticated;

alter table public.academia_mensajes_categoria enable row level security;

grant select, insert on public.academia_mensajes_categoria to authenticated;

drop policy if exists "academia_mensajes_cat_select" on public.academia_mensajes_categoria;
create policy "academia_mensajes_cat_select"
  on public.academia_mensajes_categoria
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
          and apa.academia_id = academia_mensajes_categoria.academia_id
          and j.academia_id = academia_mensajes_categoria.academia_id
          and j.activo = true
          and lower(trim(j.categoria)) = lower(trim(academia_mensajes_categoria.categoria_nombre))
      )
      or public.academia_is_owner(academia_mensajes_categoria.academia_id)
      or public.academia_miembro_activo_rol(
        academia_mensajes_categoria.academia_id,
        array['owner', 'admin', 'coordinator']::text[]
      )
      or public.academia_mensaje_coach_categoria_coincide(
        academia_mensajes_categoria.academia_id,
        academia_mensajes_categoria.categoria_nombre
      )
    )
  );

drop policy if exists "academia_mensajes_cat_insert" on public.academia_mensajes_categoria;
create policy "academia_mensajes_cat_insert"
  on public.academia_mensajes_categoria
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
