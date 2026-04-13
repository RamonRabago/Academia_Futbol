package com.escuelafutbol.academia.ui.sync

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.sync.AcademiaCloudSync
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Sincronización con Supabase en segundo plano: sin mensajes de éxito, con intervalo mínimo
 * entre ejecuciones y avisos solo si algo falla o falta configuración de academia.
 */
class CloudSyncViewModel(
    private val application: Application,
) : ViewModel() {

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing.asStateFlow()

    private val _userVisibleSyncIssues = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val userVisibleSyncIssues: SharedFlow<String> = _userVisibleSyncIssues.asSharedFlow()

    private val syncMutex = Mutex()

    /** [SystemClock.elapsedRealtime] al terminar el último intento (éxito o error). */
    private var lastSyncEndedElapsedMs = 0L

    fun scheduleInitialDelayedSync() {
        viewModelScope.launch {
            delay(INITIAL_DELAY_MS)
            runAutoSyncLocked(mostrarErrorGenericoSiFalla = false)
        }
    }

    fun onLifecycleResume() {
        viewModelScope.launch {
            runSyncLocked(
                respectMinInterval = true,
                mostrarErrorGenericoSiFalla = false,
                skipPush = false,
            )
        }
    }

    /**
     * Pull-to-refresh: no aplica el intervalo mínimo y **solo descarga** (pull), sin subidas.
     * Así un error en push (RLS, red al subir fotos, etc.) no bloquea traer categorías y jugadores.
     */
    fun requestManualSync() {
        viewModelScope.launch {
            runSyncLocked(
                respectMinInterval = false,
                mostrarErrorGenericoSiFalla = true,
                skipPush = true,
            )
        }
    }

    private suspend fun runAutoSyncLocked(mostrarErrorGenericoSiFalla: Boolean) {
        runSyncLocked(
            respectMinInterval = true,
            mostrarErrorGenericoSiFalla = mostrarErrorGenericoSiFalla,
            skipPush = false,
        )
    }

    /**
     * @param respectMinInterval Si true, respeta [MIN_INTERVAL_MS] tras el último intento (sync automático).
     * @param mostrarErrorGenericoSiFalla Si es false (sync al abrir / al reanudar), no se muestra el snackbar
     * genérico ante timeouts o fallos puntuales: evita spam cuando la sesión aún no está lista o la red va justa.
     * Siguen mostrándose los avisos que requieren acción (onboarding / elegir academia).
     */
    private suspend fun runSyncLocked(
        respectMinInterval: Boolean,
        mostrarErrorGenericoSiFalla: Boolean,
        skipPush: Boolean,
    ) {
        syncMutex.withLock {
            val app = application as AcademiaApplication
            val client = app.supabaseClient ?: return@withLock

            // Tras bloquear pantalla o volver de otra actividad, `currentUserOrNull()` puede ser null un instante:
            // syncAll() fallaría con "No hay sesión..." y disparaba el snackbar genérico en bucle.
            if (client.auth.currentUserOrNull()?.id == null) {
                return@withLock
            }

            if (respectMinInterval) {
                val now = SystemClock.elapsedRealtime()
                if (lastSyncEndedElapsedMs != 0L && now - lastSyncEndedElapsedMs < MIN_INTERVAL_MS) {
                    return@withLock
                }
            }

            _syncing.value = true
            try {
                val result = AcademiaCloudSync(client, app.database).syncAll(skipPush = skipPush)
                val failure = result.exceptionOrNull()
                if (failure != null) {
                    Log.e(TAG, "syncAll skipPush=$skipPush falló", failure)
                }
                when (val err = failure?.message) {
                    "NEEDS_ACADEMY_ONBOARDING" ->
                        _userVisibleSyncIssues.tryEmit(
                            application.getString(R.string.sync_needs_onboarding),
                        )
                    "NEEDS_ACADEMY_PICK" ->
                        _userVisibleSyncIssues.tryEmit(
                            application.getString(R.string.sync_needs_pick_academy),
                        )
                    null -> { }
                    else ->
                        if (mostrarErrorGenericoSiFalla) {
                            _userVisibleSyncIssues.tryEmit(
                                application.getString(R.string.sync_error_generic),
                            )
                        }
                }
            } finally {
                lastSyncEndedElapsedMs = SystemClock.elapsedRealtime()
                _syncing.value = false
            }
        }
    }

    companion object {
        private const val TAG = "CloudSyncVM"
        private const val MIN_INTERVAL_MS = 70_000L
        private const val INITIAL_DELAY_MS = 650L
    }
}
