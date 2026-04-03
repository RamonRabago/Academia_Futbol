package com.escuelafutbol.academia.ui.sync

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.sync.AcademiaCloudSync
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
            runAutoSyncLocked()
        }
    }

    fun onLifecycleResume() {
        viewModelScope.launch {
            runAutoSyncLocked()
        }
    }

    private suspend fun runAutoSyncLocked() {
        syncMutex.withLock {
            val app = application as AcademiaApplication
            val client = app.supabaseClient ?: return@withLock

            val now = SystemClock.elapsedRealtime()
            if (lastSyncEndedElapsedMs != 0L && now - lastSyncEndedElapsedMs < MIN_INTERVAL_MS) {
                return@withLock
            }

            _syncing.value = true
            try {
                val result = AcademiaCloudSync(client, app.database).syncAll()
                when (val err = result.exceptionOrNull()?.message) {
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
                        _userVisibleSyncIssues.tryEmit(
                            application.getString(R.string.sync_error_generic),
                        )
                }
            } finally {
                lastSyncEndedElapsedMs = SystemClock.elapsedRealtime()
                _syncing.value = false
            }
        }
    }

    companion object {
        private const val MIN_INTERVAL_MS = 70_000L
        private const val INITIAL_DELAY_MS = 650L
    }
}
