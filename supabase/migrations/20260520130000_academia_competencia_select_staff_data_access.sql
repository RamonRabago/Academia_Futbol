-- SELECT de competencias: padres solo si su hijo está inscrito en la categoría de la competencia;
-- todo el staff operativo (`academia_staff_data_access`) ve todas las competencias de esa academia.
--
-- El caso «padre» NO puede ir en un EXISTS directo sobre `academia_competencia_categoria` dentro
-- de esta política: la RLS de `academia_competencia_categoria` vuelve a leer `academia_competencia`
-- y Postgres detecta recursión infinita. Se encapsula en SECURITY DEFINER (bypass RLS al leer).

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
