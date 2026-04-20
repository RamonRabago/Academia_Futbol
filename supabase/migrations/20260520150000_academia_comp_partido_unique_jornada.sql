-- ---------------------------------------------------------------------------
-- academia_competencia_partido: una sola fila por
-- (competencia_id, categoria_en_competencia_id, jornada).
--
-- 1) Reasigna duplicados existentes: en cada paso se toma una fila que aún
--    comparte (competencia, categoría inscrita, jornada) con otra (la más
--    reciente por created_at, id) y se le asigna max(jornada)+1 en esa
--    categoría hasta no quedar duplicados.
-- 2) Índice único para que INSERT/UPDATE incoherentes fallen en Postgres.
-- ---------------------------------------------------------------------------

DO $mig$
DECLARE
  vid uuid;
  vcomp uuid;
  vcat uuid;
  new_j int;
BEGIN
  LOOP
    SELECT p.id, p.competencia_id, p.categoria_en_competencia_id
    INTO vid, vcomp, vcat
    FROM public.academia_competencia_partido p
    INNER JOIN (
      SELECT competencia_id, categoria_en_competencia_id, jornada
      FROM public.academia_competencia_partido
      GROUP BY competencia_id, categoria_en_competencia_id, jornada
      HAVING COUNT(*) > 1
    ) d ON d.competencia_id = p.competencia_id
       AND d.categoria_en_competencia_id = p.categoria_en_competencia_id
       AND d.jornada = p.jornada
    ORDER BY p.created_at DESC NULLS LAST, p.id DESC
    LIMIT 1;

    EXIT WHEN NOT FOUND;

    SELECT COALESCE(MAX(jornada), 0) + 1
    INTO new_j
    FROM public.academia_competencia_partido
    WHERE competencia_id = vcomp
      AND categoria_en_competencia_id = vcat;

    UPDATE public.academia_competencia_partido
    SET jornada = new_j
    WHERE id = vid;
  END LOOP;
END
$mig$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_academia_comp_partido_comp_cat_jornada
  ON public.academia_competencia_partido (competencia_id, categoria_en_competencia_id, jornada);

COMMENT ON INDEX public.uq_academia_comp_partido_comp_cat_jornada IS
  'Un partido por jornada y categoría inscrita dentro de la misma competencia.';
