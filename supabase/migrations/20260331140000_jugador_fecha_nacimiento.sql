-- Fecha de nacimiento completa (ms UTC inicio de día) además de anio_nacimiento legado.
alter table public.jugadores
  add column if not exists fecha_nacimiento_ms bigint;

comment on column public.jugadores.fecha_nacimiento_ms is
  'Epoch ms, medianoche UTC del día de nacimiento (misma convención que Material DatePicker).';
