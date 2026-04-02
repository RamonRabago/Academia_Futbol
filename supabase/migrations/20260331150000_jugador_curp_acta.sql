alter table public.jugadores
  add column if not exists curp text;

alter table public.jugadores
  add column if not exists acta_nacimiento_url text;

comment on column public.jugadores.curp is 'CURP del menor (México).';
comment on column public.jugadores.acta_nacimiento_url is 'URL pública del acta (PDF o imagen) en Storage.';
