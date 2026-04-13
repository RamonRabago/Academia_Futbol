-- Tokens FCM por usuario (dispositivos). La app inserta/actualiza con la sesión; Edge Function usa service role para leer al enviar push.

create table if not exists public.user_fcm_tokens (
  user_id uuid not null references auth.users (id) on delete cascade,
  token text not null,
  platform text not null default 'android',
  updated_at timestamptz not null default now(),
  primary key (user_id, token)
);

create index if not exists idx_user_fcm_tokens_user on public.user_fcm_tokens (user_id);

comment on table public.user_fcm_tokens is 'Tokens Firebase Cloud Messaging por usuario; usados para avisos a padres (p. ej. academia_mensajes_categoria).';

alter table public.user_fcm_tokens enable row level security;

drop policy if exists "user_fcm_tokens_select_own" on public.user_fcm_tokens;
create policy "user_fcm_tokens_select_own"
  on public.user_fcm_tokens
  for select
  to authenticated
  using (user_id = auth.uid());

drop policy if exists "user_fcm_tokens_insert_own" on public.user_fcm_tokens;
create policy "user_fcm_tokens_insert_own"
  on public.user_fcm_tokens
  for insert
  to authenticated
  with check (user_id = auth.uid());

drop policy if exists "user_fcm_tokens_update_own" on public.user_fcm_tokens;
create policy "user_fcm_tokens_update_own"
  on public.user_fcm_tokens
  for update
  to authenticated
  using (user_id = auth.uid())
  with check (user_id = auth.uid());

drop policy if exists "user_fcm_tokens_delete_own" on public.user_fcm_tokens;
create policy "user_fcm_tokens_delete_own"
  on public.user_fcm_tokens
  for delete
  to authenticated
  using (user_id = auth.uid());

grant select, insert, update, delete on public.user_fcm_tokens to authenticated;

-- Upsert desde la app con una sola llamada (evita detalles de PostgREST upsert en clientes).
create or replace function public.register_fcm_token(p_token text, p_platform text default 'android')
returns void
language plpgsql
security invoker
set search_path = public
as $$
begin
  insert into public.user_fcm_tokens (user_id, token, platform, updated_at)
  values (
    auth.uid(),
    trim(p_token),
    coalesce(nullif(trim(p_platform), ''), 'android'),
    now()
  )
  on conflict (user_id, token) do update
    set updated_at = now(),
        platform = excluded.platform;
end;
$$;

revoke all on function public.register_fcm_token(text, text) from public;
grant execute on function public.register_fcm_token(text, text) to authenticated;

-- Solo service_role (p. ej. Edge Function): padres con hijo activo en la categoría del aviso.
create or replace function public.parent_user_ids_for_categoria_mensaje(
  p_academia_id uuid,
  p_categoria_nombre text
)
returns table (parent_user_id uuid)
language sql
stable
security definer
set search_path = public
as $$
  select distinct apa.parent_user_id
  from public.academia_padres_alumnos apa
  join public.jugadores j on j.id = apa.jugador_id
  where apa.academia_id = p_academia_id
    and j.academia_id = p_academia_id
    and j.activo = true
    and lower(trim(j.categoria)) = lower(trim(p_categoria_nombre));
$$;

revoke all on function public.parent_user_ids_for_categoria_mensaje(uuid, text) from public;
grant execute on function public.parent_user_ids_for_categoria_mensaje(uuid, text) to service_role;
