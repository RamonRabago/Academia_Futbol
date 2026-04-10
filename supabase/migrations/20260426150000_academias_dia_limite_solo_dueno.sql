-- Solo el dueño de la cuenta (`academias.user_id`) puede cambiar `dia_limite_pago_mes`.
-- Coordinadores/admin siguen pudiendo actualizar otros campos de `academias`.
-- Debe aplicarse después de `20260426120000_academia_dia_limite_pago_y_padre_cobros.sql`.

create or replace function public.academias_guardar_dia_limite_solo_dueno()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if new.dia_limite_pago_mes is distinct from old.dia_limite_pago_mes then
    if not public.academia_is_owner(new.id) then
      raise exception 'Solo el dueño de la academia puede modificar dia_limite_pago_mes'
        using errcode = '42501';
    end if;
  end if;
  return new;
end;
$$;

drop trigger if exists trg_academias_dia_limite_solo_dueno on public.academias;
create trigger trg_academias_dia_limite_solo_dueno
  before update on public.academias
  for each row
  execute function public.academias_guardar_dia_limite_solo_dueno();
