alter table public.jugadores
  add column if not exists becado boolean not null default false;

comment on column public.jugadores.becado is 'Alumno becado: exento de mensualidad.';
