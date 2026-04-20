# Competencias — tabla, variación y líderes (evolución prevista)

Documento breve para **desambiguar** el comportamiento actual en código/UI y anotar **extensiones naturales** sin reescribir todo el módulo.

## Variación de posición (columna «Var.»)

**Hoy:** entre los partidos que cuentan para estadística (`jugado`, no cancelado, marcador completo), sea \(J = \max(\text{jornada})\). La tabla **actual** usa todos esos partidos. La tabla **de referencia** usa solo partidos con \(\text{jornada} < J\). La variación por fila es el cambio de rango entre referencia y actual.

- Es un criterio por **número de jornada almacenado**, no por **fecha** del encuentro ni por «fecha de cierre» de la liga.
- Si \(J \le 1\) o no hay partidos con jornada estrictamente menor, no hay referencia útil → la UI muestra «—» (`variacionPosicion == null`).

**Posibles evoluciones:**

1. **Por fecha:** misma idea pero el corte es la **última fecha de partido** (o la última fecha de una «jornada oficial» derivada de calendario).
2. **Ventana móvil:** variación vs. los últimos \(N\) partidos jugados por equipo (requiere enlazar partidos al equipo o duplicar lógica por inscripción).
3. **Jornada oficial distinta:** si la liga usa jornadas no correlativas o replanifica números, puede necesitarse un campo `jornada_ordinal` o `ronda` separado del entero mostrado.

Implementación actual: `CompetenciaTablaCalculator.kt` (`enriquecerVariacionJornadaAnterior`).

## Líderes ofensivos (bloque bajo la tabla)

**Hoy:** solo se muestra un **ranking acumulado** si:

- No existe ningún partido jugado con lista de anotadores **no vacía** cuya suma de cantidades **≠** `score_propio` (marcador a favor del equipo inscrito).
- Existe al menos un partido jugado con anotadores que **sí suman** `score_propio`; el ranking agrega solo esos partidos «coherentes».

Si hay inconsistencia en cualquier partido con desglose parcial, la UI muestra un **aviso** y no un ranking parcial.

**Posibles evoluciones:**

1. **Por jornada:** top anotadores solo de la última jornada o de la jornada seleccionada.
2. **Estadísticas individuales completas:** minutos, tiros, tarjetas, etc., según deporte (probablemente nuevas columnas o tabla hija, no solo `detalle_marcador_json`).
3. **Múltiples tablas por grupo:** hoy `grupo` en inscripción es informativo para la línea; un corte real implicaría `calcularTablaPosiciones` por subconjunto de inscripciones o por clave `(competencia_id, grupo)` y pestañas o selector en UI.

Implementación actual: `CompetenciaLideresOfensivos.kt` (`construirLideresOfensivosTabla`, `calcularTopLideresOfensivos`).

## Enlaces

- Modelo y RLS base: `docs/COMPETENCIAS_FASE1.md`
- Cambios de producto: `CHANGELOG.md`
