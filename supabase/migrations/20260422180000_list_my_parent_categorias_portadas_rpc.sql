-- Portadas de categoría para tutores en la app (Inicio, selector por hijo):
-- el SELECT directo a [categorias] usa academia_staff_data_access (sin rol parent),
-- así que Room quedaba sin portada_url. Esta RPC devuelve solo categorías de hijos
-- vinculados al auth.uid() en la academia.

create or replace function public.list_my_parent_categorias_portadas(p_academia_id uuid)
returns table (
  categoria_id uuid,
  nombre text,
  portada_url text
)
language sql
stable
security definer
set search_path = public
as $$
  select distinct on (c.id)
    c.id,
    c.nombre,
    c.portada_url
  from public.academia_padres_alumnos p
  join public.jugadores j
    on j.id = p.jugador_id
   and j.academia_id = p.academia_id
  join public.categorias c
    on c.academia_id = p.academia_id
   and lower(trim(c.nombre)) = lower(trim(j.categoria))
  where p.academia_id = p_academia_id
    and p.parent_user_id = auth.uid()
    and j.activo = true
  order by c.id, c.nombre;
$$;

grant execute on function public.list_my_parent_categorias_portadas(uuid) to authenticated;

comment on function public.list_my_parent_categorias_portadas(uuid) is
  'Categorías (con portada_url) de los hijos vinculados al tutor en la academia; la app fusiona en Room tras pull categorías.';
