-- Publicaciones visibles al publicar: entrenador (categoría asignada) y coordinador sin pasar por pendiente.
-- La app ya enviará estado_publicacion = 'published' para esos roles; la política INSERT debe permitirlo.

drop policy if exists "academia_contenido_cat_insert" on public.academia_contenido_categoria;

create policy "academia_contenido_cat_insert"
  on public.academia_contenido_categoria
  for insert
  to authenticated
  with check (
    author_user_id = auth.uid()
    and (
      (
        estado_publicacion = 'published'
        and (
          public.academia_is_owner(academia_id)
          or public.academia_miembro_activo_rol(academia_id, array['admin']::text[])
          or public.academia_miembro_activo_rol(academia_id, array['coordinator']::text[])
          or public.academia_mensaje_coach_categoria_coincide(academia_id, categoria_nombre)
        )
      )
      or (
        estado_publicacion = 'pending_approval'
        and (
          public.academia_is_owner(academia_id)
          or public.academia_miembro_activo_rol(academia_id, array['admin', 'coordinator']::text[])
          or public.academia_mensaje_coach_categoria_coincide(academia_id, categoria_nombre)
        )
      )
    )
  );

comment on policy "academia_contenido_cat_insert" on public.academia_contenido_categoria is
  'published: dueño, admin, coordinador (academia) o coach asignado a la categoría; pending_approval: flujo opcional / compatibilidad.';
