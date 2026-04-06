-- Quién registró al jugador (Supabase Auth). Nullable: filas anteriores a esta migración.
alter table public.jugadores
  add column if not exists alta_por_user_id uuid references auth.users (id) on delete set null;

comment on column public.jugadores.alta_por_user_id is
  'Usuario autenticado que dio de alta el jugador (auditoría).';
