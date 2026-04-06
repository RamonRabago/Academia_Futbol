-- Nombres de categorías asignadas al coach actual (auth.uid()) en una academia.
-- SECURITY DEFINER: evita depender de RLS en academia_miembro_categorias / categorias para el entrenador.
-- La app puede llamar a esta RPC desde resolveMembresiaCloud; si no existe, se sigue el camino PostgREST anterior.

create or replace function public.list_my_coach_category_names(p_academia_id uuid)
returns table (nombre text)
language sql
stable
security definer
set search_path = public
as $$
  select c.nombre
  from public.academia_miembros m
  join public.academia_miembro_categorias l on l.miembro_id = m.id
  join public.categorias c
    on c.id = l.categoria_id
   and c.academia_id = p_academia_id
  where m.academia_id = p_academia_id
    and m.user_id = auth.uid()
    and m.activo = true
    and lower(trim(m.rol)) = 'coach'
  order by c.nombre;
$$;

grant execute on function public.list_my_coach_category_names(uuid) to authenticated;

comment on function public.list_my_coach_category_names(uuid) is
  'Categorías asignadas al miembro coach actual en la academia; uso en app para sync sin depender solo de RLS en tablas de enlace.';
