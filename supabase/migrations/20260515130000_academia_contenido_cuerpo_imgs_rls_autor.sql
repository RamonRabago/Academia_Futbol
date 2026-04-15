-- Galería de imágenes en el cuerpo del artículo (JSON array de URLs en texto).
-- RLS UPDATE: el autor puede archivar/editar sus propias filas aunque ya no coincida coach/categoría.

alter table public.academia_contenido_categoria
  add column if not exists cuerpo_imagenes_urls text;

comment on column public.academia_contenido_categoria.cuerpo_imagenes_urls is
  'JSON array de URLs públicas (Storage) para fotos dentro del artículo, p. ej. ["https://...","https://..."]';

drop policy if exists "academia_contenido_cat_update" on public.academia_contenido_categoria;
create policy "academia_contenido_cat_update"
  on public.academia_contenido_categoria
  for update
  to authenticated
  using (
    author_user_id = auth.uid()
    or public.academia_is_owner(academia_id)
    or public.academia_miembro_activo_rol(
      academia_id,
      array['owner', 'admin', 'coordinator']::text[]
    )
    or public.academia_mensaje_coach_categoria_coincide(academia_id, categoria_nombre)
  )
  with check (
    author_user_id = auth.uid()
    or public.academia_is_owner(academia_id)
    or public.academia_miembro_activo_rol(
      academia_id,
      array['owner', 'admin', 'coordinator']::text[]
    )
    or public.academia_mensaje_coach_categoria_coincide(academia_id, categoria_nombre)
  );
