package com.escuelafutbol.academia.push

import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.data.remote.FcmTokenRepository
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

object FcmRegistration {

    fun syncTokenIfPossible(app: AcademiaApplication) {
        val client = app.supabaseClient ?: return
        if (client.auth.currentUserOrNull()?.id == null) return
        scope.launch {
            runCatching {
                val token = FirebaseMessaging.getInstance().token.await()
                FcmTokenRepository(client).registerToken(token)
            }
        }
    }
}
