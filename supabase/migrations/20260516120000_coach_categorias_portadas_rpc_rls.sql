-- Portadas de categoría visibles para el entrenador en el selector local:
-- 1) SELECT explícito por categorías asignadas (OR con la política «staff» existente).
-- 2) RPC security definer para que la app sincronice nombre + portada_url aunque falle el SELECT masivo.

create policy "categorias_coach_assigned_read"
  on public.categorias
  for select
  to authenticated
  using (
    public.academia_mensaje_coach_categoria_coincide(
      categorias.academia_id,
      categorias.nombre
    )
  );

-- Detalle de las categorías asignadas al coach (auth.uid()) en la academia: id, nombre, portada.
create or replace function public.list_my_coach_categorias_portadas(p_academia_id uuid)
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
  select c.id, c.nombre, c.portada_url
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

grant execute on function public.list_my_coach_categorias_portadas(uuid) to authenticated;

comment on function public.list_my_coach_categorias_portadas(uuid) is
  'Categorías asignadas al coach con portada_url; la app llama tras pull categorías para rellenar Room si RLS limitaba el SELECT.';
