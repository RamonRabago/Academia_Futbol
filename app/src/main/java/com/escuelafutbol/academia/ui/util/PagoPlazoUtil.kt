package com.escuelafutbol.academia.ui.util

import com.escuelafutbol.academia.data.local.entity.CobroMensualAlumno
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

object PagoPlazoUtil {

    /** Último día válido para pagar ese mes (acota [diaLimite] al largo del mes). */
    fun fechaLimitePago(periodoYyyyMm: String, diaLimiteDelMes: Int): LocalDate {
        val ym = YearMonth.parse(periodoYyyyMm)
        val dia = diaLimiteDelMes.coerceIn(1, 28).coerceAtMost(ym.lengthOfMonth())
        return ym.atDay(dia)
    }

    fun etiquetaMesPeriodo(periodoYyyyMm: String): String {
        val ym = YearMonth.parse(periodoYyyyMm)
        return ym.month.getDisplayName(TextStyle.FULL, Locale("es", "MX")) + " " + ym.year
    }

    /**
     * true si hay saldo pendiente y ya pasó el día límite de ese periodo.
     * [diaLimite] null o fuera de 1..28 → no aplica recordatorio a padres.
     */
    fun cobroVencidoConSaldo(
        cobro: CobroMensualAlumno,
        diaLimite: Int?,
        hoy: LocalDate,
    ): Boolean {
        if (diaLimite == null || diaLimite !in 1..28) return false
        val pendiente = cobro.importeEsperado - cobro.importePagado
        if (pendiente <= 0.0001) return false
        val limite = fechaLimitePago(cobro.periodoYyyyMm, diaLimite)
        return hoy.isAfter(limite)
    }
}
