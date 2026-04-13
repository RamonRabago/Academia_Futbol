-- Añade fecha de alta del miembro en el club (created_at) al listado de gestión.
-- No basta con CREATE OR REPLACE: si cambian las columnas del RETURNS TABLE, Postgres
-- exige eliminar la función antes (error 42P13).
DROP FUNCTION IF EXISTS public.list_academia_miembros_for_manage(uuid);

create or replace function public.list_academia_miembros_for_manage(p_academia_id uuid)
returns table (
  id uuid,
  academia_id uuid,
  user_id uuid,
  rol text,
  activo boolean,
  display_label text,
  member_email text,
  created_at timestamptz
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
    u.email::text as member_email,
    m.created_at
  from public.academia_miembros m
  join auth.users u on u.id = m.user_id
  where m.academia_id = p_academia_id
  order by m.rol, display_label, m.user_id;
end;
$$;

grant execute on function public.list_academia_miembros_for_manage(uuid) to authenticated;

comment on function public.list_academia_miembros_for_manage(uuid) is
  'Miembros de una academia con display_label, email y created_at (alta en el club), solo para gestores.';
