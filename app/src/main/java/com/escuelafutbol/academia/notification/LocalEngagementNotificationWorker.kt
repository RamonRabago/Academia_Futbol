package com.escuelafutbol.academia.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.local.model.cloudCoachCategoriasPermitidasOperacion
import com.escuelafutbol.academia.data.local.model.esPadreMembresiaNube
import com.escuelafutbol.academia.data.remote.PadresAlumnosRepository
import io.github.jan.supabase.auth.auth
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Trabajo periódico ligero solo para **staff**: sugiere abrir secciones clave.
 * Los padres en nube usan [ParentRealNotificationCoordinator] con datos reales (sin duplicar genéricos aquí).
 */
class LocalEngagementNotificationWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? AcademiaApplication ?: return Result.success()
        val config = app.database.academiaConfigDao().getActual() ?: return Result.success()
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val hoy = LocalDate.now(zone)
        if (config.esPadreMembresiaNube()) {
            val claveHoyPadre = hoy.toString()
            val notificacionPadreHoyEnviada = prefs.getString(KEY_BIRTHDAY_PARENT_TODAY_SENT_DATE, null)
            if (!notificacionPadreHoyEnviada.equals(claveHoyPadre, ignoreCase = false)) {
                val client = app.supabaseClient
                val aid = config.remoteAcademiaId?.trim()?.takeIf { it.isNotEmpty() }
                val uid = client?.auth?.currentUserOrNull()?.id?.toString()
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                if (client != null && aid != null && uid != null) {
                    val vinculos = runCatching {
                        PadresAlumnosRepository(client).listVinculos(aid, uid)
                    }.getOrElse { emptyList() }
                    val remotesVinculados = vinculos.map { it.jugadorId.trim() }.filter { it.isNotEmpty() }.toSet()
                    if (remotesVinculados.isNotEmpty()) {
                        val cumpleanerosHoy = app.database.jugadorDao().getAll()
                            .asSequence()
                            .filter { it.fechaNacimientoMillis != null }
                            .filter { it.remoteId?.trim() in remotesVinculados }
                            .filter { jugador ->
                                val nacimiento = Instant.ofEpochMilli(jugador.fechaNacimientoMillis!!)
                                    .atZone(zone)
                                    .toLocalDate()
                                nacimiento.month == hoy.month && nacimiento.dayOfMonth == hoy.dayOfMonth
                            }
                            .sortedBy { it.nombre.lowercase() }
                            .toList()
                        if (cumpleanerosHoy.isNotEmpty()) {
                            val body = if (cumpleanerosHoy.size == 1) {
                                applicationContext.getString(
                                    R.string.local_notif_parent_birthday_today_body_one,
                                    cumpleanerosHoy.first().nombre.trim(),
                                )
                            } else {
                                applicationContext.getString(
                                    R.string.local_notif_parent_birthday_today_body_more,
                                    cumpleanerosHoy.first().nombre.trim(),
                                    cumpleanerosHoy.size - 1,
                                )
                            }
                            LocalNotificationPublisher.show(
                                context = applicationContext,
                                notificationId = NOTIFICATION_ID_BIRTHDAY_PARENT_TODAY,
                                kind = LocalNotificationKind.SIMPLE_REMINDER,
                                title = applicationContext.getString(R.string.local_notif_parent_birthday_today_title),
                                body = body,
                                navRoute = LocalNotificationContract.ROUTE_INICIO,
                            )
                            prefs.edit()
                                .putString(KEY_BIRTHDAY_PARENT_TODAY_SENT_DATE, claveHoyPadre)
                                .putLong(KEY_LAST_SHOWN_AT, now)
                                .apply()
                        }
                    }
                }
            }
            return Result.success()
        }
        val manana = LocalDate.now(zone).plusDays(1)
        val notificacionHoyEnviada = prefs.getString(KEY_BIRTHDAY_TODAY_SENT_DATE, null)
        val notificacionMananaEnviada = prefs.getString(KEY_BIRTHDAY_TOMORROW_SENT_DATE, null)
        val claveHoyReal = hoy.toString()
        val claveHoy = manana.toString()
        if (!notificacionHoyEnviada.equals(claveHoyReal, ignoreCase = false)) {
            val permitidasCoach = config.cloudCoachCategoriasPermitidasOperacion()
            val jugadores = app.database.jugadorDao().getAll()
            val cumpleanerosHoy = jugadores
                .asSequence()
                .filter { it.fechaNacimientoMillis != null }
                .filter { jugador ->
                    when {
                        permitidasCoach == null -> true
                        permitidasCoach.isEmpty() -> false
                        else -> jugador.categoria.trim() in permitidasCoach
                    }
                }
                .filter { jugador ->
                    val nacimiento = Instant.ofEpochMilli(jugador.fechaNacimientoMillis!!)
                        .atZone(zone)
                        .toLocalDate()
                    nacimiento.month == hoy.month && nacimiento.dayOfMonth == hoy.dayOfMonth
                }
                .sortedBy { it.nombre.lowercase() }
                .toList()
            if (cumpleanerosHoy.isNotEmpty()) {
                val body = if (cumpleanerosHoy.size == 1) {
                    val unico = cumpleanerosHoy.first()
                    applicationContext.getString(
                        R.string.local_notif_staff_birthday_today_body_one,
                        unico.nombre.trim(),
                        unico.categoria.trim(),
                    )
                } else {
                    val principal = cumpleanerosHoy.first().nombre.trim()
                    val extra = cumpleanerosHoy.size - 1
                    applicationContext.getString(
                        R.string.local_notif_staff_birthday_today_body_more,
                        principal,
                        extra,
                    )
                }
                LocalNotificationPublisher.show(
                    context = applicationContext,
                    notificationId = NOTIFICATION_ID_BIRTHDAY_TODAY,
                    kind = LocalNotificationKind.SIMPLE_REMINDER,
                    title = applicationContext.getString(R.string.local_notif_staff_birthday_today_title),
                    body = body,
                    navRoute = LocalNotificationContract.ROUTE_INICIO,
                )
                prefs.edit()
                    .putString(KEY_BIRTHDAY_TODAY_SENT_DATE, claveHoyReal)
                    .putLong(KEY_LAST_SHOWN_AT, now)
                    .apply()
                return Result.success()
            }
        }
        if (!notificacionMananaEnviada.equals(claveHoy, ignoreCase = false)) {
            val permitidasCoach = config.cloudCoachCategoriasPermitidasOperacion()
            val jugadores = app.database.jugadorDao().getAll()
            val cumpleanerosManana = jugadores
                .asSequence()
                .filter { it.fechaNacimientoMillis != null }
                .filter { jugador ->
                    when {
                        permitidasCoach == null -> true
                        permitidasCoach.isEmpty() -> false
                        else -> jugador.categoria.trim() in permitidasCoach
                    }
                }
                .filter { jugador ->
                    val nacimiento = Instant.ofEpochMilli(jugador.fechaNacimientoMillis!!)
                        .atZone(zone)
                        .toLocalDate()
                    nacimiento.month == manana.month && nacimiento.dayOfMonth == manana.dayOfMonth
                }
                .sortedBy { it.nombre.lowercase() }
                .toList()
            if (cumpleanerosManana.isNotEmpty()) {
                val nombres = cumpleanerosManana.take(3).joinToString(", ") { it.nombre.trim() }
                val extra = cumpleanerosManana.size - 3
                val body = if (extra > 0) {
                    applicationContext.getString(
                        R.string.local_notif_staff_birthday_tomorrow_body_more,
                        nombres,
                        extra,
                    )
                } else {
                    applicationContext.getString(
                        R.string.local_notif_staff_birthday_tomorrow_body,
                        nombres,
                    )
                }
                LocalNotificationPublisher.show(
                    context = applicationContext,
                    notificationId = NOTIFICATION_ID_BIRTHDAY_TOMORROW,
                    kind = LocalNotificationKind.SIMPLE_REMINDER,
                    title = applicationContext.getString(R.string.local_notif_staff_birthday_tomorrow_title),
                    body = body,
                    navRoute = LocalNotificationContract.ROUTE_INICIO,
                )
                prefs.edit()
                    .putString(KEY_BIRTHDAY_TOMORROW_SENT_DATE, claveHoy)
                    .putLong(KEY_LAST_SHOWN_AT, now)
                    .apply()
                return Result.success()
            }
        }
        val last = prefs.getLong(KEY_LAST_SHOWN_AT, 0L)
        if (last > 0 && now - last < TimeUnit.HOURS.toMillis(MIN_HOURS_BETWEEN)) {
            return Result.success()
        }
        val cycle = prefs.getInt(KEY_CYCLE, 0) % 3
        val kind = LocalNotificationKind.SIMPLE_REMINDER
        val route: String
        val titleRes: Int
        val bodyRes: Int
        when (cycle) {
            0 -> {
                route = LocalNotificationContract.ROUTE_ASISTENCIA
                titleRes = R.string.local_notif_staff_attendance_title
                bodyRes = R.string.local_notif_staff_attendance_body
            }
            1 -> {
                route = LocalNotificationContract.ROUTE_JUGADORES
                titleRes = R.string.local_notif_staff_players_title
                bodyRes = R.string.local_notif_staff_players_body
            }
            else -> {
                route = LocalNotificationContract.ROUTE_INICIO
                titleRes = R.string.local_notif_staff_home_title
                bodyRes = R.string.local_notif_staff_home_body
            }
        }
        val title = applicationContext.getString(titleRes)
        val body = applicationContext.getString(bodyRes)
        LocalNotificationPublisher.show(
            context = applicationContext,
            notificationId = NOTIFICATION_ID,
            kind = kind,
            title = title,
            body = body,
            navRoute = route,
        )
        prefs.edit()
            .putLong(KEY_LAST_SHOWN_AT, now)
            .putInt(KEY_CYCLE, cycle + 1)
            .apply()
        return Result.success()
    }

    companion object {
        private const val PREFS_NAME = "academia_local_engagement"
        private const val KEY_LAST_SHOWN_AT = "last_shown_at"
        private const val KEY_CYCLE = "content_cycle"
        private const val KEY_BIRTHDAY_TODAY_SENT_DATE = "birthday_today_sent_date"
        private const val KEY_BIRTHDAY_TOMORROW_SENT_DATE = "birthday_tomorrow_sent_date"
        private const val KEY_BIRTHDAY_PARENT_TODAY_SENT_DATE = "birthday_parent_today_sent_date"
        private const val MIN_HOURS_BETWEEN = 30L
        private const val NOTIFICATION_ID = 91_001
        private const val NOTIFICATION_ID_BIRTHDAY_TODAY = 91_010
        private const val NOTIFICATION_ID_BIRTHDAY_TOMORROW = 91_011
        private const val NOTIFICATION_ID_BIRTHDAY_PARENT_TODAY = 91_012
    }
}
