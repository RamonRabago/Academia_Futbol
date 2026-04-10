-- Día límite de pago mensual (academia) y lectura de cobros para padres vinculados

alter table public.academias
  add column if not exists dia_limite_pago_mes integer;

comment on column public.academias.dia_limite_pago_mes is
  'Día del mes (1–28) hasta el cual se espera el pago; tras esa fecha, si hay saldo, el padre ve recordatorio en la app. Null = sin regla.';

-- Padres: solo lectura de cobros de sus hijos vinculados
drop policy if exists "jugador_cobros_mensual_parent_select" on public.jugador_cobros_mensual;
create policy "jugador_cobros_mensual_parent_select"
  on public.jugador_cobros_mensual
  for select
  to authenticated
  using (
    exists (
      select 1 from public.academia_padres_alumnos p
      where p.jugador_id = jugador_cobros_mensual.jugador_id
        and p.parent_user_id = auth.uid()
    )
  );
