-- Fase 2: unirse a una academia con código (sin insert directo en academia_miembros por RLS).
-- Ejecutar después de 20260402140000_academia_miembros_rls.sql

create or replace function public.join_academia_by_code(p_codigo text, p_rol text default 'coach')
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
  v_norm text;
  v_aid uuid;
begin
  v_norm := upper(trim(both from p_codigo));
  if length(v_norm) < 4 then
    raise exception 'invalid_code' using errcode = 'P0001';
  end if;
  if p_rol is null or p_rol not in ('coach', 'coordinator', 'parent') then
    raise exception 'invalid_role' using errcode = 'P0001';
  end if;

  select a.id into v_aid
  from public.academias a
  where a.codigo_club is not null
    and upper(trim(both from a.codigo_club)) = v_norm
  limit 1;

  if v_aid is null then
    raise exception 'code_not_found' using errcode = 'P0001';
  end if;

  insert into public.academia_miembros (academia_id, user_id, rol, activo)
  values (v_aid, auth.uid(), p_rol, true)
  on conflict (academia_id, user_id) do update
  set
    activo = true,
    rol = excluded.rol,
    updated_at = now();

  return v_aid;
end;
$$;

grant execute on function public.join_academia_by_code(text, text) to authenticated;

comment on function public.join_academia_by_code(text, text) is
  'Valida codigo_club, inserta o reactiva fila en academia_miembros. Roles permitidos: coach, coordinator, parent.';
