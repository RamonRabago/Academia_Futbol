package com.escuelafutbol.academia.notification

import android.content.Context
import android.content.SharedPreferences
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.parents.HijoRendimientoCompPadreUi
import com.escuelafutbol.academia.ui.parents.HijoResumenUi
import com.escuelafutbol.academia.ui.parents.MensajeCategoriaUi
import com.escuelafutbol.academia.ui.parents.ParentsMensajesUiState
import com.escuelafutbol.academia.ui.parents.ProximoPartidoPadreUi
import java.text.DateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Notificaciones locales con datos reales (padre en nube, pestaña Padres).
 *
 * - **Partido:** id estable = huella `fecha|rival|competencia` (no hay id remoto en el modelo UI).
 * - **Aviso:** [ParentsMensajesUiState.items] — compara con [last_notified_notice_id] y marca de tiempo asociada.
 * - **Anti-spam:** prioridad aviso > partido (como mucho un envío por [evaluate]); hueco mínimo de 1 h entre dos reales (prefs); **como mucho una notificación real por sesión de proceso** (flag en memoria, sin prefs).
 *
 * No modifica ViewModels ni backend.
 */
object ParentRealNotificationCoordinator {

    private const val PREFS_PREFIX = "academia_real_notif_v2_"

    /** Huella del último partido del que ya se notificó (o línea de agua inicial sin notificar). */
    private const val KEY_LAST_NOTIFIED_MATCH_ID = "last_notified_match_id"

    /** Id del último aviso notificado (o línea de agua tras la primera carga). */
    private const val KEY_LAST_NOTIFIED_NOTICE_ID = "last_notified_notice_id"

    /** Marca temporal del aviso en [KEY_LAST_NOTIFIED_NOTICE_ID] (orden lexicográfico con id). */
    private const val KEY_LAST_NOTIFIED_NOTICE_AT = "last_notified_notice_at"

    /** Última vez que se mostró cualquier notificación real (partido o aviso). */
    private const val KEY_LAST_ANY_REAL_NOTIF_AT = "last_any_real_notif_at"

    private const val NOTIF_ID_NEXT_MATCH = 91_020
    private const val NOTIF_ID_NEW_MESSAGE = 91_021

    private val MIN_MS_BETWEEN_ANY_REAL_NOTIF = TimeUnit.HOURS.toMillis(1L)

    /** Máximo una notificación real por vida del proceso (app «abierta» hasta que el sistema mate el proceso). */
    private val realNotifSentThisAppSession = AtomicBoolean(false)

    fun evaluate(
        context: Context,
        remoteAcademiaId: String?,
        rendimientoCargando: Boolean,
        rendimientoMap: Map<Long, HijoRendimientoCompPadreUi>,
        hijos: List<HijoResumenUi>,
        mensajesState: ParentsMensajesUiState,
    ) {
        val academyKey = remoteAcademiaId?.trim()?.takeIf { it.isNotEmpty() } ?: return
        val prefs = prefs(context, academyKey)

        val noticeCandidate = buildNoticeCandidate(mensajesState)
        val matchCandidate = buildMatchCandidate(
            rendimientoCargando = rendimientoCargando,
            rendimientoMap = rendimientoMap,
            hijos = hijos,
        )

        val nc = noticeCandidate
        if (nc != null && shouldNotifyNotice(prefs, nc)) {
            if (canSendThisAppSession() && canSendAnotherRealNotif(prefs)) {
                sendNotice(context, prefs, nc)
            }
            return
        }
        val mc = matchCandidate
        if (mc != null && shouldNotifyMatch(prefs, mc) && canSendThisAppSession() && canSendAnotherRealNotif(prefs)) {
            sendMatch(context, prefs, mc)
        }
    }

    private data class NoticeCandidate(val id: String, val createdAtMillis: Long, val body: String)

    private data class MatchCandidate(val matchId: String, val rival: String, val fechaIso: String)

    private fun buildNoticeCandidate(state: ParentsMensajesUiState): NoticeCandidate? {
        if (state.cargando) return null
        val items = state.items
        if (items.isEmpty()) return null
        val newest = items.maxWithOrNull(
            compareBy<MensajeCategoriaUi>({ it.createdAtMillis }, { it.id }),
        ) ?: return null
        val titulo = newest.titulo.trim().ifEmpty { return null }
        val cuerpo = newest.cuerpo.trim()
        val body = if (cuerpo.isEmpty()) {
            titulo
        } else {
            val max = 180
            val chunk = if (cuerpo.length <= max) cuerpo else cuerpo.take(max).trimEnd() + "…"
            "$titulo\n$chunk"
        }
        return NoticeCandidate(newest.id, newest.createdAtMillis, body)
    }

    private fun buildMatchCandidate(
        rendimientoCargando: Boolean,
        rendimientoMap: Map<Long, HijoRendimientoCompPadreUi>,
        hijos: List<HijoResumenUi>,
    ): MatchCandidate? {
        if (rendimientoCargando) return null
        if (hijos.isEmpty()) return null
        val candidatos = rendimientoMap.values.mapNotNull { it.proximoPartido }
        if (candidatos.isEmpty()) return null
        val mejor = candidatos.minWith(
            compareBy<ProximoPartidoPadreUi>(
                { parseFechaPartido(it.fechaIso) ?: LocalDate.MAX },
                { it.competenciaNombre.lowercase(Locale.ROOT) },
                { it.rival.lowercase(Locale.ROOT) },
            ),
        )
        val matchId = fingerprintPartido(mejor)
        val rival = mejor.rival.trim().ifEmpty { return null }
        val fecha = mejor.fechaIso.trim().ifEmpty { return null }
        return MatchCandidate(matchId = matchId, rival = rival, fechaIso = fecha)
    }

    private fun shouldNotifyNotice(prefs: SharedPreferences, c: NoticeCandidate): Boolean {
        val lastId = prefs.getString(KEY_LAST_NOTIFIED_NOTICE_ID, null)
        val lastAt = prefs.getLong(KEY_LAST_NOTIFIED_NOTICE_AT, Long.MIN_VALUE)
        if (lastId == null) {
            prefs.edit()
                .putString(KEY_LAST_NOTIFIED_NOTICE_ID, c.id)
                .putLong(KEY_LAST_NOTIFIED_NOTICE_AT, c.createdAtMillis)
                .apply()
            return false
        }
        val newer = c.createdAtMillis > lastAt || (c.createdAtMillis == lastAt && c.id != lastId)
        if (!newer) return false
        if (c.id == lastId) return false
        return true
    }

    private fun shouldNotifyMatch(prefs: SharedPreferences, c: MatchCandidate): Boolean {
        val last = prefs.getString(KEY_LAST_NOTIFIED_MATCH_ID, null)
        if (last == null) {
            prefs.edit().putString(KEY_LAST_NOTIFIED_MATCH_ID, c.matchId).apply()
            return false
        }
        if (last == c.matchId) return false
        return true
    }

    private fun sendNotice(
        context: Context,
        prefs: SharedPreferences,
        c: NoticeCandidate,
    ) {
        val title = context.getString(R.string.local_notif_new_notice_title)
        LocalNotificationPublisher.show(
            context = context,
            notificationId = NOTIF_ID_NEW_MESSAGE,
            kind = LocalNotificationKind.CLUB_NOTICE,
            title = title,
            body = c.body,
            navRoute = LocalNotificationContract.ROUTE_PADRES,
        )
        prefs.edit()
            .putString(KEY_LAST_NOTIFIED_NOTICE_ID, c.id)
            .putLong(KEY_LAST_NOTIFIED_NOTICE_AT, c.createdAtMillis)
            .putLong(KEY_LAST_ANY_REAL_NOTIF_AT, System.currentTimeMillis())
            .apply()
        markRealNotifSentThisAppSession()
    }

    private fun sendMatch(
        context: Context,
        prefs: SharedPreferences,
        c: MatchCandidate,
    ) {
        val fechaTxt = formatFechaCorta(context, c.fechaIso)
        val title = context.getString(R.string.local_notif_parent_match_title)
        val body = context.getString(
            R.string.local_notif_next_match_real_body,
            c.rival,
            fechaTxt,
        )
        LocalNotificationPublisher.show(
            context = context,
            notificationId = NOTIF_ID_NEXT_MATCH,
            kind = LocalNotificationKind.NEXT_MATCH,
            title = title,
            body = body,
            navRoute = LocalNotificationContract.ROUTE_COMPETENCIAS,
        )
        prefs.edit()
            .putString(KEY_LAST_NOTIFIED_MATCH_ID, c.matchId)
            .putLong(KEY_LAST_ANY_REAL_NOTIF_AT, System.currentTimeMillis())
            .apply()
        markRealNotifSentThisAppSession()
    }

    private fun canSendThisAppSession(): Boolean = !realNotifSentThisAppSession.get()

    private fun markRealNotifSentThisAppSession() {
        realNotifSentThisAppSession.set(true)
    }

    /**
     * Hueco mínimo entre dos notificaciones reales (partido o aviso), en tiempo de pared.
     */
    private fun canSendAnotherRealNotif(prefs: SharedPreferences): Boolean {
        val last = prefs.getLong(KEY_LAST_ANY_REAL_NOTIF_AT, 0L)
        if (last == 0L) return true
        return System.currentTimeMillis() - last >= MIN_MS_BETWEEN_ANY_REAL_NOTIF
    }

    private fun prefs(context: Context, academyKey: String) =
        context.applicationContext.getSharedPreferences(PREFS_PREFIX + academyKey, Context.MODE_PRIVATE)

    private fun fingerprintPartido(p: ProximoPartidoPadreUi): String =
        listOf(
            p.fechaIso.trim().lowercase(Locale.ROOT),
            p.rival.trim().lowercase(Locale.ROOT),
            p.competenciaNombre.trim().lowercase(Locale.ROOT),
        ).joinToString("|")

    private fun parseFechaPartido(fechaIso: String): LocalDate? =
        runCatching { LocalDate.parse(fechaIso.trim()) }.getOrNull()

    private fun formatFechaCorta(context: Context, fechaIso: String): String {
        val ld = parseFechaPartido(fechaIso) ?: return fechaIso.trim()
        val ms = ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(Date(ms))
    }
}
