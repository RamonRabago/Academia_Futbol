package com.escuelafutbol.academia.ui.sync

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.sync.AcademiaCloudSync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CloudSyncViewModel(
    private val application: Application,
) : ViewModel() {

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    fun clearMessage() {
        _syncMessage.value = null
    }

    fun syncNow() {
        val app = application as AcademiaApplication
        val client = app.supabaseClient
        if (client == null) {
            _syncMessage.value = "Supabase no está configurado."
            return
        }
        viewModelScope.launch {
            _syncing.value = true
            _syncMessage.value = null
            val result = AcademiaCloudSync(client, app.database).syncAll()
            _syncMessage.value = when (val err = result.exceptionOrNull()?.message) {
                "NEEDS_ACADEMY_ONBOARDING" ->
                    application.getString(R.string.sync_needs_onboarding)
                "NEEDS_ACADEMY_PICK" ->
                    application.getString(R.string.sync_needs_pick_academy)
                null -> application.getString(R.string.sync_cloud_done)
                else -> err
            }
            _syncing.value = false
        }
    }
}
