-- Alinear RLS con producto: coordinador puede gestionar miembros como admin en la app.
-- Ver conversación Fase 3 (opción A — gestión miembros).

create or replace function public.academia_can_manage_members(p_academia_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select public.academia_is_owner(p_academia_id)
    or public.academia_miembro_activo_rol(
      p_academia_id,
      array['owner', 'admin', 'coordinator']::text[]
    );
$$;
