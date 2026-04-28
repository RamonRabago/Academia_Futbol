package com.escuelafutbol.academia.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Programa el recordatorio periódico. Idempotente con [ExistingPeriodicWorkPolicy.KEEP].
 */
object LocalEngagementWorkScheduler {

    private const val UNIQUE_NAME = "academia_local_engagement_v1"

    fun ensureScheduled(context: Context) {
        LocalNotificationPublisher.ensureChannels(context)
        val wm = WorkManager.getInstance(context)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        val request = PeriodicWorkRequestBuilder<LocalEngagementNotificationWorker>(
            24L,
            TimeUnit.HOURS,
        )
            .setConstraints(constraints)
            .setInitialDelay(2L, TimeUnit.HOURS)
            .build()
        wm.enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
