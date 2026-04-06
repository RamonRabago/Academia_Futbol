-- Permitir que cada miembro lea sus filas en academia_miembro_categorias aunque
-- academia_staff_data_access falle (p. ej. rol con mayúsculas/espacios fuera del CHECK histórico).
-- Sin esto, el entrenador puede quedar con cloudCoachCategoriasJson vacío y no ver categorías asignadas.

drop policy if exists "miembro_cat_select" on public.academia_miembro_categorias;

create policy "miembro_cat_select"
  on public.academia_miembro_categorias
  for select
  to authenticated
  using (
    exists (
      select 1 from public.academia_miembros m
      where m.id = academia_miembro_categorias.miembro_id
        and (
          public.academia_staff_data_access(m.academia_id)
          or (
            m.user_id = auth.uid()
            and m.activo = true
          )
        )
    )
  );

-- Comparación de rol insensible a mayúsculas/espacios (coherente con la app, que normaliza a minúsculas).
create or replace function public.academia_miembro_activo_rol(p_academia_id uuid, p_roles text[])
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1
    from public.academia_miembros m
    cross join lateral unnest(p_roles) as pr(role_txt)
    where m.academia_id = p_academia_id
      and m.user_id = auth.uid()
      and m.activo = true
      and lower(trim(m.rol)) = lower(trim(pr.role_txt))
  );
$$;
