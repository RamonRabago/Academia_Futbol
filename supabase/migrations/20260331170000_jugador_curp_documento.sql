alter table public.jugadores
  add column if not exists curp_documento_url text;

comment on column public.jugadores.curp_documento_url is 'URL pública del archivo CURP (PDF o imagen) en Storage.';
