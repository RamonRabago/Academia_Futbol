-- Cobros mensuales por alumno (staff) y sueldo mensual en equipo_staff

alter table public.equipo_staff
  add column if not exists sueldo_mensual double precision;

create table if not exists public.jugador_cobros_mensual (
  id uuid primary key default gen_random_uuid(),
  academia_id uuid not null references public.academias (id) on delete cascade,
  jugador_id uuid not null references public.jugadores (id) on delete cascade,
  periodo_yyyy_mm text not null,
  importe_esperado double precision not null default 0,
  importe_pagado double precision not null default 0,
  notas text,
  updated_at timestamptz not null default now(),
  unique (jugador_id, periodo_yyyy_mm)
);

drop trigger if exists trg_jugador_cobros_mensual_updated on public.jugador_cobros_mensual;
create trigger trg_jugador_cobros_mensual_updated
  before update on public.jugador_cobros_mensual
  for each row execute function public.set_updated_at();

alter table public.jugador_cobros_mensual enable row level security;

drop policy if exists "jugador_cobros_mensual_staff" on public.jugador_cobros_mensual;
create policy "jugador_cobros_mensual_staff"
  on public.jugador_cobros_mensual
  for all
  to authenticated
  using (public.academia_staff_data_access(academia_id))
  with check (public.academia_staff_data_access(academia_id));
