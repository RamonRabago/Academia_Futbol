-- Lista miembros con etiqueta legible (correo / nombre en metadata) para pantalla de gestión.
-- Requiere tablas y academia_can_manage_members de migraciones anteriores.
-- Ejecutar en SQL Editor de Supabase si aún no aplicaste migraciones vía CLI.

create or replace function public.list_academia_miembros_for_manage(p_academia_id uuid)
returns table (
  id uuid,
  academia_id uuid,
  user_id uuid,
  rol text,
  activo boolean,
  display_label text,
  member_email text
)
language plpgsql
security definer
set search_path = public
as $$
begin
  if not public.academia_can_manage_members(p_academia_id) then
    raise exception 'forbidden' using errcode = 'P0001';
  end if;

  return query
  select
    m.id,
    m.academia_id,
    m.user_id,
    m.rol,
    m.activo,
    coalesce(
      nullif(trim(both from u.raw_user_meta_data->>'full_name'), ''),
      nullif(trim(both from u.raw_user_meta_data->>'name'), ''),
      u.email::text
    ) as display_label,
    u.email::text as member_email
  from public.academia_miembros m
  join auth.users u on u.id = m.user_id
  where m.academia_id = p_academia_id
  order by m.rol, display_label, m.user_id;
end;
$$;

grant execute on function public.list_academia_miembros_for_manage(uuid) to authenticated;

comment on function public.list_academia_miembros_for_manage(uuid) is
  'Miembros de una academia con display_label (nombre en metadata o email) y email, solo para gestores.';
