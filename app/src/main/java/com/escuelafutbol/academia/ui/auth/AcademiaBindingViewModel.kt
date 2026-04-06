package com.escuelafutbol.academia.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.data.sync.AcademiaBindingOption
import com.escuelafutbol.academia.data.sync.AcademiaBindingResult
import com.escuelafutbol.academia.data.sync.AcademiaCloudSync
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AcademiaBindingUiState {
    data object Loading : AcademiaBindingUiState()
    data object Ready : AcademiaBindingUiState()
    data object NeedsOnboarding : AcademiaBindingUiState()
    data class PickAcademy(val options: List<AcademiaBindingOption>) : AcademiaBindingUiState()
    data class Error(val message: String) : AcademiaBindingUiState()
}

class AcademiaBindingViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<AcademiaBindingUiState>(AcademiaBindingUiState.Loading)
    val uiState: StateFlow<AcademiaBindingUiState> = _uiState.asStateFlow()

    /**
     * @param mostrarPantallaCarga Si es `true`, fuerza «Comprobando academia…» (p. ej. Reintentar).
     * Si es `false` y el estado ya es [AcademiaBindingUiState.Ready], la comprobación corre en segundo plano
     * sin pasar a [Loading]: evita desmontar Jugadores/alta al volver del selector de archivos o al
     * desbloquear pantalla cuando [LaunchedEffect] se reinicia tras un `SessionStatus` intermedio.
     */
    fun refresh(mostrarPantallaCarga: Boolean = false) {
        val app = getApplication<AcademiaApplication>()
        val client = app.supabaseClient ?: run {
            _uiState.value = AcademiaBindingUiState.Error("Supabase no configurado.")
            return
        }
        val uid = client.auth.currentUserOrNull()?.id?.toString() ?: run {
            _uiState.value = AcademiaBindingUiState.Error("Sin sesión.")
            return
        }
        viewModelScope.launch {
            val soloSegundoPlano =
                !mostrarPantallaCarga && _uiState.value is AcademiaBindingUiState.Ready
            if (!soloSegundoPlano) {
                _uiState.value = AcademiaBindingUiState.Loading
            }
            runCatching {
                AcademiaCloudSync(client, app.database).resolveAcademiaBinding(uid)
            }.fold(
                onSuccess = { r ->
                    _uiState.value = when (r) {
                        is AcademiaBindingResult.Ok -> AcademiaBindingUiState.Ready
                        AcademiaBindingResult.NeedsOnboarding -> AcademiaBindingUiState.NeedsOnboarding
                        is AcademiaBindingResult.PickAcademy ->
                            if (r.options.isEmpty()) {
                                AcademiaBindingUiState.NeedsOnboarding
                            } else {
                                AcademiaBindingUiState.PickAcademy(r.options)
                            }
                    }
                },
                onFailure = { e ->
                    if (soloSegundoPlano) {
                        return@launch
                    }
                    _uiState.value = AcademiaBindingUiState.Error(
                        e.message ?: "No se pudo comprobar la academia.",
                    )
                },
            )
        }
    }

    fun createOwnedAcademia(onDone: (Result<Unit>) -> Unit) {
        val app = getApplication<AcademiaApplication>()
        val client = app.supabaseClient ?: return
        val uid = client.auth.currentUserOrNull()?.id?.toString() ?: return
        viewModelScope.launch {
            _uiState.value = AcademiaBindingUiState.Loading
            val result = AcademiaCloudSync(client, app.database).createOwnedAcademia(uid)
            result.fold(
                onSuccess = {
                    _uiState.value = AcademiaBindingUiState.Ready
                    onDone(Result.success(Unit))
                },
                onFailure = { e ->
                    _uiState.value = AcademiaBindingUiState.Error(e.message ?: "Error al crear academia.")
                    onDone(Result.failure(e))
                },
            )
        }
    }

    fun joinByInviteCode(code: String, onDone: (Result<Unit>) -> Unit) {
        val app = getApplication<AcademiaApplication>()
        val client = app.supabaseClient ?: return
        val codeNorm = code.trim().uppercase()
        viewModelScope.launch {
            _uiState.value = AcademiaBindingUiState.Loading
            val result = AcademiaCloudSync(client, app.database).joinAcademiaByInviteCode(codeNorm)
            result.fold(
                onSuccess = {
                    _uiState.value = AcademiaBindingUiState.Ready
                    onDone(Result.success(Unit))
                },
                onFailure = { e ->
                    val msg = when {
                        e.message?.contains("code_not_found", ignoreCase = true) == true ->
                            "Código no encontrado. Pide al club el código correcto para tu caso (entrenador, coordinador o familia)."
                        e.message?.contains("invalid_code", ignoreCase = true) == true ->
                            "Código demasiado corto."
                        else -> e.message ?: "No se pudo unir a la academia."
                    }
                    _uiState.value = AcademiaBindingUiState.NeedsOnboarding
                    onDone(Result.failure(Exception(msg)))
                },
            )
        }
    }

    fun selectAcademia(academiaId: String, onDone: (Result<Unit>) -> Unit) {
        val app = getApplication<AcademiaApplication>()
        val client = app.supabaseClient ?: return
        viewModelScope.launch {
            _uiState.value = AcademiaBindingUiState.Loading
            runCatching {
                AcademiaCloudSync(client, app.database).bindAcademiaIdAndPullConfig(academiaId)
            }.fold(
                onSuccess = {
                    _uiState.value = AcademiaBindingUiState.Ready
                    onDone(Result.success(Unit))
                },
                onFailure = { e ->
                    _uiState.value = AcademiaBindingUiState.Error(e.message ?: "Error al elegir academia.")
                    onDone(Result.failure(e))
                },
            )
        }
    }
}
