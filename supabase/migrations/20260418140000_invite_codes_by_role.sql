-- Códigos de invitación por rol (entrenador, coordinador, padre). El rol lo define el código, no el cliente.
-- Tras migración: codigo_invite_parent copiado desde codigo_club; entrenador/coordinador hasta regenerar en la app.
-- codigo_club antiguo: join_academia_by_invite_code lo trata como código solo para padre (compatibilidad).

alter table public.academias
  add column if not exists codigo_invite_coach text,
  add column if not exists codigo_invite_coordinator text,
  add column if not exists codigo_invite_parent text;

create unique index if not exists idx_academias_codigo_invite_coach_norm
  on public.academias (upper(trim(both from codigo_invite_coach)))
  where codigo_invite_coach is not null;

create unique index if not exists idx_academias_codigo_invite_coordinator_norm
  on public.academias (upper(trim(both from codigo_invite_coordinator)))
  where codigo_invite_coordinator is not null;

create unique index if not exists idx_academias_codigo_invite_parent_norm
  on public.academias (upper(trim(both from codigo_invite_parent)))
  where codigo_invite_parent is not null;

update public.academias
set codigo_invite_parent = codigo_club
where codigo_invite_parent is null
  and codigo_club is not null
  and length(trim(both from codigo_club)) >= 4;

-- ---------------------------------------------------------------------------
-- Unirse: el rol lo determina qué columna coincide (coach / coordinator / parent).
-- Si solo coincide codigo_club (legado), el rol es parent.
-- ---------------------------------------------------------------------------

create or replace function public.join_academia_by_invite_code(p_codigo text)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
  v_norm text;
  v_aid uuid;
  v_rol text;
begin
  v_norm := upper(trim(both from p_codigo));
  if length(v_norm) < 4 then
    raise exception 'invalid_code' using errcode = 'P0001';
  end if;

  select a.id into v_aid
  from public.academias a
  where a.codigo_invite_coach is not null
    and upper(trim(both from a.codigo_invite_coach)) = v_norm
  limit 1;
  if v_aid is not null then
    v_rol := 'coach';
  else
    select a.id into v_aid
    from public.academias a
    where a.codigo_invite_coordinator is not null
      and upper(trim(both from a.codigo_invite_coordinator)) = v_norm
    limit 1;
    if v_aid is not null then
      v_rol := 'coordinator';
    else
      select a.id into v_aid
      from public.academias a
      where a.codigo_invite_parent is not null
        and upper(trim(both from a.codigo_invite_parent)) = v_norm
      limit 1;
      if v_aid is not null then
        v_rol := 'parent';
      else
        select a.id into v_aid
        from public.academias a
        where a.codigo_club is not null
          and upper(trim(both from a.codigo_club)) = v_norm
        limit 1;
        if v_aid is not null then
          v_rol := 'parent';
        end if;
      end if;
    end if;
  end if;

  if v_aid is null then
    raise exception 'code_not_found' using errcode = 'P0001';
  end if;

  insert into public.academia_miembros (academia_id, user_id, rol, activo)
  values (v_aid, auth.uid(), v_rol, true)
  on conflict (academia_id, user_id) do update
  set
    activo = true,
    rol = excluded.rol,
    updated_at = now();

  return v_aid;
end;
$$;

grant execute on function public.join_academia_by_invite_code(text) to authenticated;

comment on function public.join_academia_by_invite_code(text) is
  'Une por código de invitación; rol fijado por columna (coach/coordinator/parent) o legado codigo_club→parent.';

-- ---------------------------------------------------------------------------
-- Regenerar los tres códigos (dueño / admin / coordinador).
-- ---------------------------------------------------------------------------

create or replace function public.regenerate_academia_invite_codes(p_academia_id uuid)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  charset constant text := 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  new_coach text;
  new_coord text;
  new_parent text;
  c text;
  attempt int;
  i int;
  pos int;
  taken boolean;
begin
  if not public.academia_can_manage_members(p_academia_id) then
    raise exception 'forbidden' using errcode = 'P0001';
  end if;

  new_coach := null;
  for attempt in 1..150 loop
    c := '';
    for i in 1..6 loop
      pos := 1 + floor(random() * length(charset))::int;
      c := c || substr(charset, pos, 1);
    end loop;
    select exists (
      select 1
      from public.academias a
      where a.id <> p_academia_id
        and (
          (a.codigo_invite_coach is not null
            and upper(trim(both from a.codigo_invite_coach)) = upper(trim(both from c)))
          or (a.codigo_invite_coordinator is not null
            and upper(trim(both from a.codigo_invite_coordinator)) = upper(trim(both from c)))
          or (a.codigo_invite_parent is not null
            and upper(trim(both from a.codigo_invite_parent)) = upper(trim(both from c)))
          or (a.codigo_club is not null
            and upper(trim(both from a.codigo_club)) = upper(trim(both from c)))
        )
    ) into taken;
    if not taken then
      new_coach := c;
      exit;
    end if;
  end loop;
  if new_coach is null then
    raise exception 'invite_gen_failed' using errcode = 'P0001';
  end if;

  new_coord := null;
  for attempt in 1..150 loop
    c := '';
    for i in 1..6 loop
      pos := 1 + floor(random() * length(charset))::int;
      c := c || substr(charset, pos, 1);
    end loop;
    continue when upper(trim(both from c)) = upper(trim(both from new_coach));
    select exists (
      select 1
      from public.academias a
      where a.id <> p_academia_id
        and (
          (a.codigo_invite_coach is not null
            and upper(trim(both from a.codigo_invite_coach)) = upper(trim(both from c)))
          or (a.codigo_invite_coordinator is not null
            and upper(trim(both from a.codigo_invite_coordinator)) = upper(trim(both from c)))
          or (a.codigo_invite_parent is not null
            and upper(trim(both from a.codigo_invite_parent)) = upper(trim(both from c)))
          or (a.codigo_club is not null
            and upper(trim(both from a.codigo_club)) = upper(trim(both from c)))
        )
    ) into taken;
    if not taken then
      new_coord := c;
      exit;
    end if;
  end loop;
  if new_coord is null then
    raise exception 'invite_gen_failed' using errcode = 'P0001';
  end if;

  new_parent := null;
  for attempt in 1..150 loop
    c := '';
    for i in 1..6 loop
      pos := 1 + floor(random() * length(charset))::int;
      c := c || substr(charset, pos, 1);
    end loop;
    continue when upper(trim(both from c)) = upper(trim(both from new_coach))
      or upper(trim(both from c)) = upper(trim(both from new_coord));
    select exists (
      select 1
      from public.academias a
      where a.id <> p_academia_id
        and (
          (a.codigo_invite_coach is not null
            and upper(trim(both from a.codigo_invite_coach)) = upper(trim(both from c)))
          or (a.codigo_invite_coordinator is not null
            and upper(trim(both from a.codigo_invite_coordinator)) = upper(trim(both from c)))
          or (a.codigo_invite_parent is not null
            and upper(trim(both from a.codigo_invite_parent)) = upper(trim(both from c)))
          or (a.codigo_club is not null
            and upper(trim(both from a.codigo_club)) = upper(trim(both from c)))
        )
    ) into taken;
    if not taken then
      new_parent := c;
      exit;
    end if;
  end loop;
  if new_parent is null then
    raise exception 'invite_gen_failed' using errcode = 'P0001';
  end if;

  update public.academias
  set
    codigo_invite_coach = new_coach,
    codigo_invite_coordinator = new_coord,
    codigo_invite_parent = new_parent,
    codigo_club = null
  where id = p_academia_id;

  return jsonb_build_object(
    'coach', new_coach,
    'coordinator', new_coord,
    'parent', new_parent
  );
end;
$$;

grant execute on function public.regenerate_academia_invite_codes(uuid) to authenticated;

comment on function public.regenerate_academia_invite_codes(uuid) is
  'Genera tres códigos únicos por rol y limpia codigo_club legado. Requiere academia_can_manage_members.';
