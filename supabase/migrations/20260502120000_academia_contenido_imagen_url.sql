-- Foto de portada opcional para recursos (URL pública en bucket academia-media).

alter table public.academia_contenido_categoria
  add column if not exists imagen_url text;

comment on column public.academia_contenido_categoria.imagen_url is
  'URL pública en Storage (academia-media), primera carpeta = auth.uid().';
