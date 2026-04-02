-- Fase 1: membresías por academia + RLS híbrido (dueño en academias.user_id O miembro activo).
-- Ver docs/FASE_0_DECISIONES_CERRADAS.md y docs/PLAN_MEMBRESIA_Y_TENANTS.md.
-- Ejecutar en Supabase SQL Editor después de las migraciones anteriores.

-- ---------------------------------------------------------------------------
-- Columna opcional: código corto de club (lookup en Fase 2; puede quedar null)
-- ---------------------------------------------------------------------------

alter table public.academias
  add column if not exists codigo_club text;

create unique index if not exists idx_academias_codigo_club
  on public.academias (codigo_club)
  where codigo_club is not null;

-- ---------------------------------------------------------------------------
-- Tablas nuevas
-- ---------------------------------------------------------------------------

create table if not exists public.academia_miembros (
  id uuid primary key default gen_random_uuid(),
  academia_id uuid not null references public.academias (id) on delete cascade,
  user_id uuid not null references auth.users (id) on delete cascade,
  rol text not null
    check (rol in ('owner', 'admin', 'coordinator', 'coach', 'parent')),
  activo boolean not null default true,
  invited_by uuid references auth.users (id) on delete set null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (academia_id, user_id)
);

create index if not exists idx_academia_miembros_user
  on public.academia_miembros (user_id)
  where activo = true;

create index if not exists idx_academia_miembros_academia
  on public.academia_miembros (academia_id)
  where activo = true;

create table if not exists public.academia_miembro_categorias (
  id uuid primary key default gen_random_uuid(),
  miembro_id uuid not null references public.academia_miembros (id) on delete cascade,
  categoria_id uuid not null references public.categorias (id) on delete cascade,
  unique (miembro_id, categoria_id)
);

create index if not exists idx_miembro_cat_categoria
  on public.academia_miembro_categorias (categoria_id);

-- Vínculo padre (Auth) ↔ jugador; RLS de jugadores usa esta tabla para rol parent.
create table if not exists public.academia_padres_alumnos (
  id uuid primary key default gen_random_uuid(),
  academia_id uuid not null references public.academias (id) on delete cascade,
  parent_user_id uuid not null references auth.users (id) on delete cascade,
  jugador_id uuid not null references public.jugadores (id) on delete cascade,
  created_at timestamptz not null default now(),
  unique (parent_user_id, jugador_id)
);

create index if not exists idx_padres_alumnos_parent
  on public.academia_padres_alumnos (parent_user_id);

create index if not exists idx_padres_alumnos_jugador
  on public.academia_padres_alumnos (jugador_id);

-- ---------------------------------------------------------------------------
-- Triggers updated_at
-- ---------------------------------------------------------------------------

drop trigger if exists trg_academia_miembros_updated on public.academia_miembros;
create trigger trg_academia_miembros_updated
  before update on public.academia_miembros
  for each row execute function public.set_updated_at();

-- ---------------------------------------------------------------------------
-- Funciones helper (SECURITY DEFINER: leen tablas sin depender de RLS recursiva)
-- ---------------------------------------------------------------------------

create or replace function public.academia_is_owner(p_academia_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1 from public.academias a
    where a.id = p_academia_id and a.user_id = auth.uid()
  );
$$;

create or replace function public.academia_miembro_activo_rol(p_academia_id uuid, p_roles text[])
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1 from public.academia_miembros m
    where m.academia_id = p_academia_id
      and m.user_id = auth.uid()
      and m.activo = true
      and m.rol = any (p_roles)
  );
$$;

-- Staff con acceso a datos operativos (dueño de fila academias o miembro coach+).
create or replace function public.academia_staff_data_access(p_academia_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select public.academia_is_owner(p_academia_id)
    or public.academia_miembro_activo_rol(
      p_academia_id,
      array['owner', 'admin', 'coordinator', 'coach']::text[]
    );
$$;

-- Cualquier miembro activo (incl. parent): branding / pertenencia al club.
create or replace function public.academia_any_access(p_academia_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select public.academia_is_owner(p_academia_id)
    or exists (
      select 1 from public.academia_miembros m
      where m.academia_id = p_academia_id
        and m.user_id = auth.uid()
        and m.activo = true
    );
$$;

-- Quién puede gestionar altas/bajas de miembros e invitaciones.
create or replace function public.academia_can_manage_members(p_academia_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select public.academia_is_owner(p_academia_id)
    or public.academia_miembro_activo_rol(
      p_academia_id,
      array['owner', 'admin']::text[]
    );
$$;

grant execute on function public.academia_is_owner(uuid) to authenticated;
grant execute on function public.academia_miembro_activo_rol(uuid, text[]) to authenticated;
grant execute on function public.academia_staff_data_access(uuid) to authenticated;
grant execute on function public.academia_any_access(uuid) to authenticated;
grant execute on function public.academia_can_manage_members(uuid) to authenticated;

-- ---------------------------------------------------------------------------
-- RLS nuevas tablas
-- ---------------------------------------------------------------------------

alter table public.academia_miembros enable row level security;
alter table public.academia_miembro_categorias enable row level security;
alter table public.academia_padres_alumnos enable row level security;

drop policy if exists "academia_miembros_select" on public.academia_miembros;
drop policy if exists "academia_miembros_insert" on public.academia_miembros;
drop policy if exists "academia_miembros_update" on public.academia_miembros;
drop policy if exists "academia_miembros_delete" on public.academia_miembros;

create policy "academia_miembros_select"
  on public.academia_miembros
  for select
  to authenticated
  using (
    user_id = auth.uid()
    or public.academia_staff_data_access(academia_id)
  );

create policy "academia_miembros_insert"
  on public.academia_miembros
  for insert
  to authenticated
  with check (public.academia_can_manage_members(academia_id));

create policy "academia_miembros_update"
  on public.academia_miembros
  for update
  to authenticated
  using (public.academia_can_manage_members(academia_id))
  with check (public.academia_can_manage_members(academia_id));

create policy "academia_miembros_delete"
  on public.academia_miembros
  for delete
  to authenticated
  using (public.academia_can_manage_members(academia_id));

-- Asignación coach ↔ categoría (Fase 3 filtrará lecturas; hoy staff ve todo).
drop policy if exists "miembro_cat_all" on public.academia_miembro_categorias;
create policy "miembro_cat_select"
  on public.academia_miembro_categorias
  for select
  to authenticated
  using (
    exists (
      select 1 from public.academia_miembros m
      where m.id = academia_miembro_categorias.miembro_id
        and public.academia_staff_data_access(m.academia_id)
    )
  );

create policy "miembro_cat_insert"
  on public.academia_miembro_categorias
  for insert
  to authenticated
  with check (
    exists (
      select 1 from public.academia_miembros m
      where m.id = academia_miembro_categorias.miembro_id
        and public.academia_can_manage_members(m.academia_id)
    )
    or exists (
      select 1 from public.academia_miembros m
      where m.id = academia_miembro_categorias.miembro_id
        and m.user_id = auth.uid()
        and public.academia_miembro_activo_rol(
          m.academia_id,
          array['coordinator']::text[]
        )
    )
  );

create policy "miembro_cat_update"
  on public.academia_miembro_categorias
  for update
  to authenticated
  using (
    exists (
      select 1 from public.academia_miembros m
      where m.id = academia_miembro_categorias.miembro_id
        and (
          public.academia_can_manage_members(m.academia_id)
          or (
            m.user_id = auth.uid()
            and public.academia_miembro_activo_rol(
              m.academia_id,
              array['coordinator']::text[]
            )
          )
        )
    )
  )
  with check (
    exists (
      select 1 from public.academia_miembros m
      where m.id = academia_miembro_categorias.miembro_id
        and (
          public.academia_can_manage_members(m.academia_id)
          or (
            m.user_id = auth.uid()
            and public.academia_miembro_activo_rol(
              m.academia_id,
              array['coordinator']::text[]
            )
          )
        )
    )
  );

create policy "miembro_cat_delete"
  on public.academia_miembro_categorias
  for delete
  to authenticated
  using (
    exists (
      select 1 from public.academia_miembros m
      where m.id = academia_miembro_categorias.miembro_id
        and (
          public.academia_can_manage_members(m.academia_id)
          or (
            m.user_id = auth.uid()
            and public.academia_miembro_activo_rol(
              m.academia_id,
              array['coordinator']::text[]
            )
          )
        )
    )
  );

drop policy if exists "padres_alumnos_select" on public.academia_padres_alumnos;
drop policy if exists "padres_alumnos_insert" on public.academia_padres_alumnos;
drop policy if exists "padres_alumnos_update" on public.academia_padres_alumnos;
drop policy if exists "padres_alumnos_delete" on public.academia_padres_alumnos;

create policy "padres_alumnos_select"
  on public.academia_padres_alumnos
  for select
  to authenticated
  using (
    parent_user_id = auth.uid()
    or public.academia_staff_data_access(academia_id)
  );

create policy "padres_alumnos_insert"
  on public.academia_padres_alumnos
  for insert
  to authenticated
  with check (public.academia_staff_data_access(academia_id));

create policy "padres_alumnos_update"
  on public.academia_padres_alumnos
  for update
  to authenticated
  using (public.academia_staff_data_access(academia_id))
  with check (public.academia_staff_data_access(academia_id));

create policy "padres_alumnos_delete"
  on public.academia_padres_alumnos
  for delete
  to authenticated
  using (public.academia_staff_data_access(academia_id));

-- ---------------------------------------------------------------------------
-- Sustituir políticas en tablas existentes
-- ---------------------------------------------------------------------------

drop policy if exists "academias_own" on public.academias;

create policy "academias_select"
  on public.academias
  for select
  to authenticated
  using (public.academia_any_access(id));

create policy "academias_insert"
  on public.academias
  for insert
  to authenticated
  with check (auth.uid() = user_id);

create policy "academias_update"
  on public.academias
  for update
  to authenticated
  using (
    public.academia_is_owner(id)
    or public.academia_miembro_activo_rol(
      id,
      array['owner', 'admin']::text[]
    )
  )
  with check (
    public.academia_is_owner(id)
    or public.academia_miembro_activo_rol(
      id,
      array['owner', 'admin']::text[]
    )
  );

create policy "academias_delete"
  on public.academias
  for delete
  to authenticated
  using (public.academia_is_owner(id));

drop policy if exists "categorias_by_academia" on public.categorias;
create policy "categorias_by_academia"
  on public.categorias
  for all
  to authenticated
  using (public.academia_staff_data_access(academia_id))
  with check (public.academia_staff_data_access(academia_id));

drop policy if exists "jugadores_by_academia" on public.jugadores;
create policy "jugadores_staff"
  on public.jugadores
  for all
  to authenticated
  using (public.academia_staff_data_access(academia_id))
  with check (public.academia_staff_data_access(academia_id));

create policy "jugadores_parent_select"
  on public.jugadores
  for select
  to authenticated
  using (
    exists (
      select 1 from public.academia_padres_alumnos p
      where p.jugador_id = jugadores.id
        and p.parent_user_id = auth.uid()
    )
  );

drop policy if exists "historial_by_academia" on public.jugador_historial;
create policy "historial_by_academia"
  on public.jugador_historial
  for all
  to authenticated
  using (public.academia_staff_data_access(academia_id))
  with check (public.academia_staff_data_access(academia_id));

drop policy if exists "asistencias_by_academia" on public.asistencias;
create policy "asistencias_by_academia"
  on public.asistencias
  for all
  to authenticated
  using (public.academia_staff_data_access(academia_id))
  with check (public.academia_staff_data_access(academia_id));

drop policy if exists "equipo_staff_by_academia" on public.equipo_staff;
create policy "equipo_staff_by_academia"
  on public.equipo_staff
  for all
  to authenticated
  using (public.academia_staff_data_access(academia_id))
  with check (public.academia_staff_data_access(academia_id));
