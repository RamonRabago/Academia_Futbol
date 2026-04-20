-- Fase 1: ligas / competencias / partidos (multideporte, marcador neutro).
-- Catálogo global de deportes + tablas por academia. RLS alineado a padres (categoría vinculada) y staff/coach.

-- ---------------------------------------------------------------------------
-- Catálogo de deportes (lectura para usuarios autenticados; datos semilla).
-- ---------------------------------------------------------------------------
create table if not exists public.catalogo_deporte (
  id uuid primary key default gen_random_uuid(),
  codigo text not null,
  nombre text not null,
  etiqueta_score_singular text not null,
  etiqueta_score_plural text not null,
  etiqueta_score_favor text,
  etiqueta_score_contra text,
  permite_empate boolean not null default true,
  puntos_por_victoria integer not null default 3,
  puntos_por_empate integer not null default 1,
  puntos_por_derrota integer not null default 0,
  maneja_sets boolean not null default false,
  maneja_periodos boolean not null default false,
  maneja_entradas boolean not null default false,
  activo boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint catalogo_deporte_codigo_chk check (char_length(trim(codigo)) > 0)
);

create unique index if not exists idx_catalogo_deporte_codigo_normalized
  on public.catalogo_deporte (lower(trim(codigo)));

comment on table public.catalogo_deporte is
  'Deportes soportados: etiquetas y reglas base de puntos para competencias (marcador neutro score_propio/score_rival).';

drop trigger if exists trg_catalogo_deporte_updated on public.catalogo_deporte;
create trigger trg_catalogo_deporte_updated
  before update on public.catalogo_deporte
  for each row execute function public.set_updated_at();

alter table public.catalogo_deporte enable row level security;

grant select on public.catalogo_deporte to authenticated;

drop policy if exists "catalogo_deporte_select" on public.catalogo_deporte;
create policy "catalogo_deporte_select"
  on public.catalogo_deporte
  for select
  to authenticated
  using (activo = true);

-- Semilla idempotente (por codigo normalizado)
insert into public.catalogo_deporte (
  codigo, nombre,
  etiqueta_score_singular, etiqueta_score_plural,
  etiqueta_score_favor, etiqueta_score_contra,
  permite_empate, puntos_por_victoria, puntos_por_empate, puntos_por_derrota,
  maneja_sets, maneja_periodos, maneja_entradas, activo
)
select v.* from (
  values
    (
      'futbol', 'Fútbol',
      'Gol', 'Goles', 'Goles a favor', 'Goles en contra',
      true, 3, 1, 0,
      false, false, false, true
    ),
    (
      'basquetbol', 'Básquetbol',
      'Punto', 'Puntos', 'Puntos a favor', 'Puntos en contra',
      true, 2, 1, 0,
      false, true, false, true
    ),
    (
      'beisbol', 'Béisbol',
      'Carrera', 'Carreras', 'Carreras a favor', 'Carreras en contra',
      false, 2, 0, 0,
      false, false, true, true
    ),
    (
      'voleibol', 'Voleibol',
      'Set', 'Sets', 'Sets a favor', 'Sets en contra',
      false, 2, 0, 0,
      true, false, false, true
    ),
    (
      'generico', 'Genérico',
      'Punto', 'Puntos', 'Marcador a favor', 'Marcador en contra',
      true, 3, 1, 0,
      false, false, false, true
    )
) as v(codigo, nombre, etiqueta_score_singular, etiqueta_score_plural, etiqueta_score_favor, etiqueta_score_contra,
       permite_empate, puntos_por_victoria, puntos_por_empate, puntos_por_derrota,
       maneja_sets, maneja_periodos, maneja_entradas, activo)
where not exists (
  select 1 from public.catalogo_deporte d where lower(trim(d.codigo)) = lower(trim(v.codigo))
);

-- ---------------------------------------------------------------------------
-- Competencia (liga / torneo) por academia
-- ---------------------------------------------------------------------------
create table if not exists public.academia_competencia (
  id uuid primary key default gen_random_uuid(),
  academia_id uuid not null references public.academias (id) on delete cascade,
  deporte_id uuid not null references public.catalogo_deporte (id) on delete restrict,
  nombre text not null,
  temporada text,
  descripcion text,
  sede_o_zona text,
  tipo_competencia text not null default 'liga'
    check (tipo_competencia in ('liga', 'copa', 'torneo', 'amistoso', 'otro')),
  fecha_inicio date,
  fecha_fin date,
  puntos_por_victoria integer,
  puntos_por_empate integer,
  puntos_por_derrota integer,
  activa boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint academia_competencia_nombre_chk check (char_length(trim(nombre)) > 0)
);

create index if not exists idx_academia_competencia_academia
  on public.academia_competencia (academia_id, activa, created_at desc);

comment on table public.academia_competencia is
  'Competencia o liga; deporte_id enlaza reglas de marcador y puntos base (sobreescribibles con columnas nullable).';

drop trigger if exists trg_academia_competencia_updated on public.academia_competencia;
create trigger trg_academia_competencia_updated
  before update on public.academia_competencia
  for each row execute function public.set_updated_at();

-- ---------------------------------------------------------------------------
-- Inscripción categoría ↔ competencia
-- ---------------------------------------------------------------------------
create table if not exists public.academia_competencia_categoria (
  id uuid primary key default gen_random_uuid(),
  competencia_id uuid not null references public.academia_competencia (id) on delete cascade,
  categoria_nombre text not null,
  nombre_equipo_mostrado text,
  grupo text,
  activo boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint academia_comp_cat_nombre_chk check (char_length(trim(categoria_nombre)) > 0)
);

create unique index if not exists idx_academia_comp_cat_comp_categoria_norm
  on public.academia_competencia_categoria (competencia_id, lower(trim(categoria_nombre)));

create index if not exists idx_academia_comp_cat_competencia
  on public.academia_competencia_categoria (competencia_id);

comment on table public.academia_competencia_categoria is
  'Categoría inscrita en una competencia (nombre alineado a jugadores.categoria).';

drop trigger if exists trg_academia_comp_cat_updated on public.academia_competencia_categoria;
create trigger trg_academia_comp_cat_updated
  before update on public.academia_competencia_categoria
  for each row execute function public.set_updated_at();

-- ---------------------------------------------------------------------------
-- Partido (marcador neutro)
-- ---------------------------------------------------------------------------
create table if not exists public.academia_competencia_partido (
  id uuid primary key default gen_random_uuid(),
  competencia_id uuid not null references public.academia_competencia (id) on delete cascade,
  categoria_en_competencia_id uuid not null references public.academia_competencia_categoria (id) on delete cascade,
  categoria_nombre text not null,
  jornada integer not null default 1,
  fecha date not null,
  hora time without time zone,
  sede text,
  rival text not null default '',
  local_visitante text not null default 'neutral'
    check (local_visitante in ('local', 'visitante', 'neutral')),
  score_propio integer,
  score_rival integer,
  jugado boolean not null default false,
  estado text not null default 'programado'
    check (estado in ('programado', 'jugado', 'pospuesto', 'cancelado')),
  notas text,
  detalle_marcador_json jsonb,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists idx_academia_comp_partido_comp_cat
  on public.academia_competencia_partido (competencia_id, categoria_en_competencia_id, jugado);

create index if not exists idx_academia_comp_partido_fecha
  on public.academia_competencia_partido (competencia_id, fecha);

comment on table public.academia_competencia_partido is
  'Partido o evento competitivo; score_propio/score_rival neutros; detalle_marcador_json reservado para sets/periodos.';

drop trigger if exists trg_academia_comp_partido_updated on public.academia_competencia_partido;
create trigger trg_academia_comp_partido_updated
  before update on public.academia_competencia_partido
  for each row execute function public.set_updated_at();

-- ---------------------------------------------------------------------------
-- RLS: competencia
-- ---------------------------------------------------------------------------
alter table public.academia_competencia enable row level security;

grant select, insert, update on public.academia_competencia to authenticated;

drop policy if exists "academia_competencia_select" on public.academia_competencia;
create policy "academia_competencia_select"
  on public.academia_competencia
  for select
  to authenticated
  using (
    exists (
      select 1
      from public.academia_competencia_categoria cc
      join public.academia_padres_alumnos apa on apa.academia_id = academia_competencia.academia_id
      join public.jugadores j on j.id = apa.jugador_id
      where cc.competencia_id = academia_competencia.id
        and apa.parent_user_id = auth.uid()
        and j.academia_id = academia_competencia.academia_id
        and j.activo = true
        and lower(trim(j.categoria)) = lower(trim(cc.categoria_nombre))
    )
    or public.academia_is_owner(academia_competencia.academia_id)
    or public.academia_miembro_activo_rol(
      academia_competencia.academia_id,
      array['owner', 'admin', 'coordinator']::text[]
    )
  );

drop policy if exists "academia_competencia_insert" on public.academia_competencia;
create policy "academia_competencia_insert"
  on public.academia_competencia
  for insert
  to authenticated
  with check (
    public.academia_is_owner(academia_id)
    or public.academia_miembro_activo_rol(
      academia_id,
      array['owner', 'admin', 'coordinator']::text[]
    )
  );

drop policy if exists "academia_competencia_update" on public.academia_competencia;
create policy "academia_competencia_update"
  on public.academia_competencia
  for update
  to authenticated
  using (
    public.academia_is_owner(academia_id)
    or public.academia_miembro_activo_rol(
      academia_id,
      array['owner', 'admin', 'coordinator']::text[]
    )
  );

-- ---------------------------------------------------------------------------
-- RLS: inscripción categoría
-- ---------------------------------------------------------------------------
alter table public.academia_competencia_categoria enable row level security;

grant select, insert, update on public.academia_competencia_categoria to authenticated;

drop policy if exists "academia_comp_cat_select" on public.academia_competencia_categoria;
create policy "academia_comp_cat_select"
  on public.academia_competencia_categoria
  for select
  to authenticated
  using (
    exists (
      select 1
      from public.academia_competencia c
      where c.id = academia_competencia_categoria.competencia_id
        and (
          exists (
            select 1
            from public.academia_padres_alumnos apa
            join public.jugadores j on j.id = apa.jugador_id
            where apa.academia_id = c.academia_id
              and apa.parent_user_id = auth.uid()
              and j.academia_id = c.academia_id
              and j.activo = true
              and lower(trim(j.categoria)) = lower(trim(academia_competencia_categoria.categoria_nombre))
          )
          or public.academia_is_owner(c.academia_id)
          or public.academia_miembro_activo_rol(c.academia_id, array['owner', 'admin', 'coordinator']::text[])
          or public.academia_mensaje_coach_categoria_coincide(c.academia_id, categoria_nombre)
        )
    )
  );

drop policy if exists "academia_comp_cat_insert" on public.academia_competencia_categoria;
create policy "academia_comp_cat_insert"
  on public.academia_competencia_categoria
  for insert
  to authenticated
  with check (
    exists (
      select 1
      from public.academia_competencia c
      where c.id = competencia_id
        and (
          public.academia_is_owner(c.academia_id)
          or public.academia_miembro_activo_rol(c.academia_id, array['owner', 'admin', 'coordinator']::text[])
          or public.academia_mensaje_coach_categoria_coincide(c.academia_id, categoria_nombre)
        )
    )
  );

drop policy if exists "academia_comp_cat_update" on public.academia_competencia_categoria;
create policy "academia_comp_cat_update"
  on public.academia_competencia_categoria
  for update
  to authenticated
  using (
    exists (
      select 1
      from public.academia_competencia c
      where c.id = competencia_id
        and (
          public.academia_is_owner(c.academia_id)
          or public.academia_miembro_activo_rol(c.academia_id, array['owner', 'admin', 'coordinator']::text[])
          or public.academia_mensaje_coach_categoria_coincide(c.academia_id, categoria_nombre)
        )
    )
  );

-- ---------------------------------------------------------------------------
-- RLS: partido
-- ---------------------------------------------------------------------------
alter table public.academia_competencia_partido enable row level security;

grant select, insert, update on public.academia_competencia_partido to authenticated;

drop policy if exists "academia_comp_partido_select" on public.academia_competencia_partido;
create policy "academia_comp_partido_select"
  on public.academia_competencia_partido
  for select
  to authenticated
  using (
    exists (
      select 1
      from public.academia_competencia c
      where c.id = academia_competencia_partido.competencia_id
        and (
          exists (
            select 1
            from public.academia_padres_alumnos apa
            join public.jugadores j on j.id = apa.jugador_id
            where apa.academia_id = c.academia_id
              and apa.parent_user_id = auth.uid()
              and j.academia_id = c.academia_id
              and j.activo = true
              and lower(trim(j.categoria)) = lower(trim(academia_competencia_partido.categoria_nombre))
          )
          or public.academia_is_owner(c.academia_id)
          or public.academia_miembro_activo_rol(c.academia_id, array['owner', 'admin', 'coordinator']::text[])
          or public.academia_mensaje_coach_categoria_coincide(c.academia_id, categoria_nombre)
        )
    )
  );

drop policy if exists "academia_comp_partido_insert" on public.academia_competencia_partido;
create policy "academia_comp_partido_insert"
  on public.academia_competencia_partido
  for insert
  to authenticated
  with check (
    exists (
      select 1
      from public.academia_competencia c
      where c.id = competencia_id
        and (
          public.academia_is_owner(c.academia_id)
          or public.academia_miembro_activo_rol(c.academia_id, array['owner', 'admin', 'coordinator']::text[])
          or public.academia_mensaje_coach_categoria_coincide(c.academia_id, categoria_nombre)
        )
    )
  );

drop policy if exists "academia_comp_partido_update" on public.academia_competencia_partido;
create policy "academia_comp_partido_update"
  on public.academia_competencia_partido
  for update
  to authenticated
  using (
    exists (
      select 1
      from public.academia_competencia c
      where c.id = competencia_id
        and (
          public.academia_is_owner(c.academia_id)
          or public.academia_miembro_activo_rol(c.academia_id, array['owner', 'admin', 'coordinator']::text[])
          or public.academia_mensaje_coach_categoria_coincide(c.academia_id, categoria_nombre)
        )
    )
  );
