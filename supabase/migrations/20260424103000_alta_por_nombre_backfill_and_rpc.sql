-- Rellena alta_por_nombre en filas antiguas (solo UUID de alta).
update public.jugadores j
set alta_por_nombre = coalesce(
  nullif(trim(both from u.raw_user_meta_data->>'full_name'), ''),
  nullif(trim(both from u.raw_user_meta_data->>'name'), ''),
  u.email::text
)
from auth.users u
where j.alta_por_user_id = u.id
  and (j.alta_por_nombre is null or trim(both from j.alta_por_nombre) = '');

-- Etiquetas para usuarios que figuran como alta_por_user_id en la academia (lectura para miembros activos).
create or replace function public.alta_por_user_labels_for_academia(p_academia_id uuid)
returns table (
  user_id uuid,
  display_label text
)
language plpgsql
security definer
set search_path = public
as $$
begin
  if not exists (
    select 1
    from public.academias a
    where a.id = p_academia_id
      and (
        a.user_id = auth.uid()
        or exists (
          select 1
          from public.academia_miembros m
          where m.academia_id = p_academia_id
            and m.user_id = auth.uid()
            and m.activo = true
        )
      )
  ) then
    raise exception 'forbidden' using errcode = 'P0001';
  end if;

  return query
  select distinct
    j.alta_por_user_id,
    coalesce(
      nullif(trim(both from u.raw_user_meta_data->>'full_name'), ''),
      nullif(trim(both from u.raw_user_meta_data->>'name'), ''),
      u.email::text
    )::text
  from public.jugadores j
  join auth.users u on u.id = j.alta_por_user_id
  where j.academia_id = p_academia_id
    and j.alta_por_user_id is not null;
end;
$$;

grant execute on function public.alta_por_user_labels_for_academia(uuid) to authenticated;

comment on function public.alta_por_user_labels_for_academia(uuid) is
  'Mapa user_id -> etiqueta legible para altas de jugadores; solo miembros activos de la academia.';
