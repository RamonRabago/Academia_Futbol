package com.escuelafutbol.academia.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.MainActivity
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.notification.LocalNotificationContract
import com.escuelafutbol.academia.data.remote.FcmTokenRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AcademiaFcmMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        val app = application as? AcademiaApplication ?: return
        val client = app.supabaseClient ?: return
        if (client.auth.currentUserOrNull()?.id == null) return
        scope.launch {
            runCatching { FcmTokenRepository(client).registerToken(token) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.notification != null) {
            showLocalNotification(
                title = message.notification!!.title ?: getString(R.string.push_default_title),
                body = message.notification!!.body.orEmpty(),
                data = message.data,
            )
            return
        }
        val title = message.data["title"] ?: getString(R.string.push_default_title)
        val body = message.data["body"] ?: message.data["cuerpo"] ?: ""
        if (body.isNotBlank()) {
            showLocalNotification(title = title, body = body, data = message.data)
        }
    }

    private fun showLocalNotification(title: String, body: String, data: Map<String, String>) {
        val channelId = CHANNEL_ID
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                channelId,
                getString(R.string.push_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            ch.description = getString(R.string.push_channel_desc)
            nm.createNotificationChannel(ch)
        }
        val openPadres = data["open_tab"] == NAV_ROUTE_PADRES
        val navFromData = data["nav_route"]?.trim().orEmpty()
        val effectiveRoute = navFromData.ifEmpty {
            if (openPadres) NAV_ROUTE_PADRES else ""
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (effectiveRoute.isNotEmpty()) {
                putExtra(LocalNotificationContract.EXTRA_NAV_ROUTE, effectiveRoute)
            } else if (openPadres) {
                putExtra(EXTRA_OPEN_PADRES, true)
            }
        }
        val piFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val pending = PendingIntent.getActivity(this, 0, intent, piFlags)
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notif)
    }

    companion object {
        const val CHANNEL_ID = "academia_avisos"
        const val EXTRA_OPEN_PADRES = "extra_open_padres"
        const val NAV_ROUTE_PADRES = "padres"
    }
}
