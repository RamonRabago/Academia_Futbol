-- Academia Fútbol: esquema para sincronización con la app Android.
-- Ejecuta TODO este script en Supabase → SQL Editor → New query → Run.
-- Requiere que los usuarios existan en auth.users (login con email en la app).

-- ---------------------------------------------------------------------------
-- Tablas
-- ---------------------------------------------------------------------------

create table if not exists public.academias (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users (id) on delete cascade,
  nombre text not null default 'Mi Academia',
  logo_url text,
  portada_url text,
  mensualidad_visible_profesor boolean not null default true,
  mensualidad_visible_coordinador boolean not null default true,
  mensualidad_visible_dueno boolean not null default true,
  rol_dispositivo text not null default 'PADRE_TUTOR',
  pin_staff_hash text,
  updated_at timestamptz not null default now(),
  unique (user_id)
);

create table if not exists public.categorias (
  id uuid primary key default gen_random_uuid(),
  academia_id uuid not null references public.academias (id) on delete cascade,
  nombre text not null,
  updated_at timestamptz not null default now(),
  unique (academia_id, nombre)
);

create table if not exists public.jugadores (
  id uuid primary key default gen_random_uuid(),
  academia_id uuid not null references public.academias (id) on delete cascade,
  nombre text not null,
  categoria text not null,
  anio_nacimiento integer,
  telefono_tutor text,
  email_tutor text,
  notas text,
  foto_url text,
  fecha_alta_ms bigint not null,
  activo boolean not null default true,
  fecha_baja_ms bigint,
  mensualidad double precision,
  updated_at timestamptz not null default now()
);

create table if not exists public.jugador_historial (
  id uuid primary key default gen_random_uuid(),
  academia_id uuid not null references public.academias (id) on delete cascade,
  jugador_id uuid not null references public.jugadores (id) on delete cascade,
  tipo text not null,
  fecha_ms bigint not null,
  detalle text,
  updated_at timestamptz not null default now()
);

create table if not exists public.asistencias (
  id uuid primary key default gen_random_uuid(),
  academia_id uuid not null references public.academias (id) on delete cascade,
  jugador_id uuid not null references public.jugadores (id) on delete cascade,
  fecha_dia_ms bigint not null,
  presente boolean not null,
  updated_at timestamptz not null default now(),
  unique (jugador_id, fecha_dia_ms)
);

create table if not exists public.equipo_staff (
  id uuid primary key default gen_random_uuid(),
  academia_id uuid not null references public.academias (id) on delete cascade,
  nombre text not null,
  rol text not null,
  telefono text,
  email text,
  foto_url text,
  updated_at timestamptz not null default now()
);

-- ---------------------------------------------------------------------------
-- updated_at automático
-- ---------------------------------------------------------------------------

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

drop trigger if exists trg_academias_updated on public.academias;
create trigger trg_academias_updated
  before update on public.academias
  for each row execute function public.set_updated_at();

drop trigger if exists trg_categorias_updated on public.categorias;
create trigger trg_categorias_updated
  before update on public.categorias
  for each row execute function public.set_updated_at();

drop trigger if exists trg_jugadores_updated on public.jugadores;
create trigger trg_jugadores_updated
  before update on public.jugadores
  for each row execute function public.set_updated_at();

drop trigger if exists trg_jugador_historial_updated on public.jugador_historial;
create trigger trg_jugador_historial_updated
  before update on public.jugador_historial
  for each row execute function public.set_updated_at();

drop trigger if exists trg_asistencias_updated on public.asistencias;
create trigger trg_asistencias_updated
  before update on public.asistencias
  for each row execute function public.set_updated_at();

drop trigger if exists trg_equipo_staff_updated on public.equipo_staff;
create trigger trg_equipo_staff_updated
  before update on public.equipo_staff
  for each row execute function public.set_updated_at();

-- ---------------------------------------------------------------------------
-- RLS: cada usuario solo ve su academia y datos ligados
-- ---------------------------------------------------------------------------

alter table public.academias enable row level security;
alter table public.categorias enable row level security;
alter table public.jugadores enable row level security;
alter table public.jugador_historial enable row level security;
alter table public.asistencias enable row level security;
alter table public.equipo_staff enable row level security;

drop policy if exists "academias_own" on public.academias;
drop policy if exists "categorias_by_academia" on public.categorias;
drop policy if exists "jugadores_by_academia" on public.jugadores;
drop policy if exists "historial_by_academia" on public.jugador_historial;
drop policy if exists "asistencias_by_academia" on public.asistencias;
drop policy if exists "equipo_staff_by_academia" on public.equipo_staff;

create policy "academias_own"
  on public.academias
  for all
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

create policy "categorias_by_academia"
  on public.categorias
  for all
  using (
    exists (
      select 1 from public.academias a
      where a.id = categorias.academia_id and a.user_id = auth.uid()
    )
  )
  with check (
    exists (
      select 1 from public.academias a
      where a.id = categorias.academia_id and a.user_id = auth.uid()
    )
  );

create policy "jugadores_by_academia"
  on public.jugadores
  for all
  using (
    exists (
      select 1 from public.academias a
      where a.id = jugadores.academia_id and a.user_id = auth.uid()
    )
  )
  with check (
    exists (
      select 1 from public.academias a
      where a.id = jugadores.academia_id and a.user_id = auth.uid()
    )
  );

create policy "historial_by_academia"
  on public.jugador_historial
  for all
  using (
    exists (
      select 1 from public.academias a
      where a.id = jugador_historial.academia_id and a.user_id = auth.uid()
    )
  )
  with check (
    exists (
      select 1 from public.academias a
      where a.id = jugador_historial.academia_id and a.user_id = auth.uid()
    )
  );

create policy "asistencias_by_academia"
  on public.asistencias
  for all
  using (
    exists (
      select 1 from public.academias a
      where a.id = asistencias.academia_id and a.user_id = auth.uid()
    )
  )
  with check (
    exists (
      select 1 from public.academias a
      where a.id = asistencias.academia_id and a.user_id = auth.uid()
    )
  );

create policy "equipo_staff_by_academia"
  on public.equipo_staff
  for all
  using (
    exists (
      select 1 from public.academias a
      where a.id = equipo_staff.academia_id and a.user_id = auth.uid()
    )
  )
  with check (
    exists (
      select 1 from public.academias a
      where a.id = equipo_staff.academia_id and a.user_id = auth.uid()
    )
  );

-- ---------------------------------------------------------------------------
-- Storage: imágenes (logo, portada, jugadores, staff). Bucket público para lectura (Coil).
-- Rutas: {auth.uid()}/{academia_uuid}/logo.ext, portada.ext, jugadores/{jugador_uuid}.ext, staff/{staff_uuid}.ext
-- ---------------------------------------------------------------------------

insert into storage.buckets (id, name, public)
values ('academia-media', 'academia-media', true)
on conflict (id) do update set public = excluded.public;

drop policy if exists "academia_media_public_read" on storage.objects;
create policy "academia_media_public_read"
  on storage.objects
  for select
  to anon, authenticated
  using (bucket_id = 'academia-media');

drop policy if exists "academia_media_insert_own" on storage.objects;
create policy "academia_media_insert_own"
  on storage.objects
  for insert
  to authenticated
  with check (
    bucket_id = 'academia-media'
    and (storage.foldername(name))[1] = auth.uid()::text
  );

drop policy if exists "academia_media_update_own" on storage.objects;
create policy "academia_media_update_own"
  on storage.objects
  for update
  to authenticated
  using (
    bucket_id = 'academia-media'
    and (storage.foldername(name))[1] = auth.uid()::text
  )
  with check (
    bucket_id = 'academia-media'
    and (storage.foldername(name))[1] = auth.uid()::text
  );

drop policy if exists "academia_media_delete_own" on storage.objects;
create policy "academia_media_delete_own"
  on storage.objects
  for delete
  to authenticated
  using (
    bucket_id = 'academia-media'
    and (storage.foldername(name))[1] = auth.uid()::text
  );

-- Colores de la interfaz (#RRGGBB), opcionales; la app los sincroniza.
alter table public.academias add column if not exists color_primario_hex text;
alter table public.academias add column if not exists color_secundario_hex text;

alter table public.categorias add column if not exists portada_url text;
