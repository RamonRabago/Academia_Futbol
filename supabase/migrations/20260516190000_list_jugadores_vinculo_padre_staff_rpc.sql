-- Lista completa de jugadores activos de la academia que aún no están vinculados al tutor indicado.
-- Usado por la app en «Gestionar miembros → vínculo tutor ↔ alumno» para no depender solo del SELECT directo a jugadores (RLS / cliente).

create or replace function public.list_jugadores_para_vinculo_padre_staff(
  p_academia_id uuid,
  p_parent_user_id uuid
)
returns table (
  id uuid,
  nombre text,
  categoria text
)
language plpgsql
security definer
set search_path = public
as $$
begin
  if not public.academia_can_manage_members(p_academia_id) then
    raise exception 'forbidden' using errcode = 'P0001';
  end if;

  if not exists (
    select 1
    from public.academia_miembros m
    where m.academia_id = p_academia_id
      and m.user_id = p_parent_user_id
      and coalesce(m.activo, true)
  ) then
    raise exception 'forbidden' using errcode = 'P0001';
  end if;

  return query
  select
    j.id,
    j.nombre,
    j.categoria
  from public.jugadores j
  where j.academia_id = p_academia_id
    and coalesce(j.activo, true)
    and not exists (
      select 1
      from public.academia_padres_alumnos p
      where p.jugador_id = j.id
        and p.parent_user_id = p_parent_user_id
    )
  order by j.categoria, j.nombre;
end;
$$;

grant execute on function public.list_jugadores_para_vinculo_padre_staff(uuid, uuid) to authenticated;

comment on function public.list_jugadores_para_vinculo_padre_staff(uuid, uuid) is
  'Staff con gestión de miembros: candidatos a vincular a un tutor (jugadores activos no enlazados a ese parent_user_id).';
