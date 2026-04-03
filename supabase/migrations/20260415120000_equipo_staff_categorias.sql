-- Asignación equipo_staff ↔ categorías (entrenadores / coordinadores por categoría)

create table if not exists public.equipo_staff_categorias (
  id uuid primary key default gen_random_uuid(),
  academia_id uuid not null references public.academias (id) on delete cascade,
  staff_id uuid not null references public.equipo_staff (id) on delete cascade,
  categoria_id uuid not null references public.categorias (id) on delete cascade,
  updated_at timestamptz not null default now(),
  unique (staff_id, categoria_id)
);

drop trigger if exists trg_equipo_staff_categorias_updated on public.equipo_staff_categorias;
create trigger trg_equipo_staff_categorias_updated
  before update on public.equipo_staff_categorias
  for each row execute function public.set_updated_at();

alter table public.equipo_staff_categorias enable row level security;

drop policy if exists "equipo_staff_categorias_by_academia" on public.equipo_staff_categorias;
create policy "equipo_staff_categorias_by_academia"
  on public.equipo_staff_categorias
  for all
  to authenticated
  using (public.academia_staff_data_access(academia_id))
  with check (public.academia_staff_data_access(academia_id));
