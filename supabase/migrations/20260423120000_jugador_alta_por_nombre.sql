-- Nombre visible de quien dio de alta (metadata Auth / correo al momento del registro).
alter table public.jugadores
  add column if not exists alta_por_nombre text;

comment on column public.jugadores.alta_por_nombre is
  'Etiqueta legible de quien registró al jugador (copia al alta; no sustituye alta_por_user_id).';
