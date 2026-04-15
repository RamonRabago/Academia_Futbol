-- Archivado fiable de Recursos: bypass RLS en UPDATE con comprobación explícita (evita fallos si la migración
-- de política UPDATE con autor no está aplicada o hay matices con RETURNING/visibilidad).

create or replace function public.archivar_academia_contenido_categoria(
  p_id uuid,
  p_academia_id uuid
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  r record;
begin
  select
    id,
    academia_id,
    categoria_nombre,
    author_user_id,
    archived_at
  into r
  from public.academia_contenido_categoria
  where id = p_id
  for update;

  if not found then
    raise exception 'contenido_no_encontrado' using errcode = 'P0002';
  end if;

  if r.academia_id <> p_academia_id then
    raise exception 'academia_no_coincide' using errcode = '42501';
  end if;

  if r.archived_at is not null then
    return;
  end if;

  if not (
    r.author_user_id = auth.uid()
    or public.academia_is_owner(r.academia_id)
    or public.academia_miembro_activo_rol(
      r.academia_id,
      array['owner', 'admin', 'coordinator']::text[]
    )
    or public.academia_mensaje_coach_categoria_coincide(
      r.academia_id,
      r.categoria_nombre
    )
  ) then
    raise exception 'sin_permiso_archivar' using errcode = '42501';
  end if;

  update public.academia_contenido_categoria
  set archived_at = now()
  where id = p_id;
end;
$$;

comment on function public.archivar_academia_contenido_categoria(uuid, uuid) is
  'Marca archived_at en un post de Recursos; valida auth.uid() (autor, dueño, admin/coord o coach de categoría).';

grant execute on function public.archivar_academia_contenido_categoria(uuid, uuid) to authenticated;
