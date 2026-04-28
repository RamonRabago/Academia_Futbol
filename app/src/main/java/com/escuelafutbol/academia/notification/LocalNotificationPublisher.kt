package com.escuelafutbol.academia.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.escuelafutbol.academia.MainActivity
import com.escuelafutbol.academia.R

/**
 * Publica notificaciones locales con título, texto y deep link a una ruta de la app.
 * No depende de ViewModels; pensado para Workers, FCM o futuros disparadores.
 */
object LocalNotificationPublisher {

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            channel(
                LocalNotificationContract.CHANNEL_MATCH,
                R.string.local_notif_channel_match_name,
                R.string.local_notif_channel_match_desc,
                context,
            ),
        )
        nm.createNotificationChannel(
            channel(
                LocalNotificationContract.CHANNEL_NOTICE,
                R.string.local_notif_channel_notice_name,
                R.string.local_notif_channel_notice_desc,
                context,
            ),
        )
        nm.createNotificationChannel(
            channel(
                LocalNotificationContract.CHANNEL_REMINDER,
                R.string.local_notif_channel_reminder_name,
                R.string.local_notif_channel_reminder_desc,
                context,
            ),
        )
    }

    private fun channel(
        id: String,
        nameRes: Int,
        descRes: Int,
        context: Context,
    ): NotificationChannel {
        val ch = NotificationChannel(
            id,
            context.getString(nameRes),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        ch.description = context.getString(descRes)
        return ch
    }

    fun show(
        context: Context,
        notificationId: Int,
        kind: LocalNotificationKind,
        title: String,
        body: String,
        navRoute: String,
    ) {
        ensureChannels(context)
        val appCtx = context.applicationContext
        val nm = appCtx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = when (kind) {
            LocalNotificationKind.NEXT_MATCH -> LocalNotificationContract.CHANNEL_MATCH
            LocalNotificationKind.CLUB_NOTICE -> LocalNotificationContract.CHANNEL_NOTICE
            LocalNotificationKind.SIMPLE_REMINDER -> LocalNotificationContract.CHANNEL_REMINDER
        }
        val intent = Intent(appCtx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(LocalNotificationContract.EXTRA_NAV_ROUTE, navRoute.trim())
        }
        val piFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val requestCode = notificationId + kind.ordinal * 10_000
        val pending = PendingIntent.getActivity(appCtx, requestCode, intent, piFlags)
        val notif = NotificationCompat.Builder(appCtx, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(notificationId, notif)
    }
}
