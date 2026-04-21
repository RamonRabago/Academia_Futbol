package com.escuelafutbol.academia.session

import android.content.Context
import com.escuelafutbol.academia.AcademiaApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Coordina aislamiento de datos locales entre sesiones de auth (mismo dispositivo, distintas cuentas).
 *
 * - Tras [onSignedOut] la caché operativa en Room queda vacía y se olvida el último uid persistido.
 * - Tras [ensureLocalDataIsolationForAuthUser], si el uid autenticado cambió respecto al persistido sin pasar
 *   por nuestro cierre de sesión (caso raro), se purga antes de volver a enlazar/sync.
 */
class AcademiaSessionManager(private val app: AcademiaApplication) {

    private val prefs =
        app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun onSignedOut() {
        withContext(Dispatchers.IO) {
            prefs.edit().remove(KEY_LAST_AUTH_UID_FOR_ROOM_MIRROR).apply()
            app.database.sessionOperationalMirrorDao().clearOperationalMirrorTables()
        }
    }

    /**
     * Llamar en hilo de IO antes de [AcademiaBindingViewModel.refresh] cuando ya hay [currentAuthUserId] estable.
     */
    suspend fun ensureLocalDataIsolationForAuthUser(currentAuthUserId: String) {
        if (currentAuthUserId.isBlank()) return
        withContext(Dispatchers.IO) {
            val prev = prefs.getString(KEY_LAST_AUTH_UID_FOR_ROOM_MIRROR, null)
            if (prev != null && prev != currentAuthUserId) {
                app.database.sessionOperationalMirrorDao().clearOperationalMirrorTables()
            }
            prefs.edit().putString(KEY_LAST_AUTH_UID_FOR_ROOM_MIRROR, currentAuthUserId).apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "academia_session_scope"
        private const val KEY_LAST_AUTH_UID_FOR_ROOM_MIRROR = "last_auth_uid_room_mirror"
    }
}
