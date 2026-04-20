-- Si ya se aplicó la versión anterior de `20260520130000` (EXISTS sobre categoria sin función definer),
-- Postgres devuelve: infinite recursion detected in policy for relation "academia_competencia".
-- Esta migración deja la política en el mismo estado que la 20260520130000 corregida (idempotente).

create or replace function public.academia_competencia_padre_puede_ver(p_competencia_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1
    from public.academia_competencia comp
    join public.academia_competencia_categoria cc on cc.competencia_id = comp.id
    join public.academia_padres_alumnos apa on apa.academia_id = comp.academia_id
    join public.jugadores j on j.id = apa.jugador_id
    where comp.id = p_competencia_id
      and apa.parent_user_id = auth.uid()
      and j.academia_id = comp.academia_id
      and j.activo = true
      and lower(trim(j.categoria)) = lower(trim(cc.categoria_nombre))
  );
$$;

grant execute on function public.academia_competencia_padre_puede_ver(uuid) to authenticated;

drop policy if exists "academia_competencia_select" on public.academia_competencia;

create policy "academia_competencia_select"
  on public.academia_competencia
  for select
  to authenticated
  using (
    public.academia_competencia_padre_puede_ver(id)
    or public.academia_staff_data_access(academia_id)
  );
