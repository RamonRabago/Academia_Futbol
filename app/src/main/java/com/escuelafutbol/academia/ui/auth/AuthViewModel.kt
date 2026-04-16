package com.escuelafutbol.academia.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.R
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.RestException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

/** Valores de nombre/apellido y correo le?dos de la sesi?n para el formulario de perfil. */
data class AuthProfileSnapshot(
    val nombre: String,
    val apellido: String,
    val email: String?,
)

class AuthViewModel(
    application: Application,
    private val client: SupabaseClient?,
) : AndroidViewModel(application) {

    private val _session = MutableStateFlow<SessionStatus>(
        if (client == null) {
            SessionStatus.NotAuthenticated(isSignOut = false)
        } else {
            SessionStatus.Initializing
        },
    )

    val sessionStatus: StateFlow<SessionStatus> = _session.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        val c = client
        if (c != null) {
            viewModelScope.launch {
                c.auth.sessionStatus.collect { _session.value = it }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearInfo() {
        _infoMessage.value = null
    }

    fun signIn(email: String, password: String) {
        val c = client ?: return
        viewModelScope.launch {
            _busy.value = true
            _errorMessage.value = null
            _infoMessage.value = null
            try {
                c.auth.signInWith(Email) {
                    this.email = email.trim()
                    this.password = password
                }
            } catch (e: Throwable) {
                _errorMessage.value = mapAuthFailure(e)
            } finally {
                _busy.value = false
            }
        }
    }

    fun signUp(email: String, password: String, nombre: String, apellido: String) {
        val c = client ?: return
        val app = getApplication<Application>()
        val nombreNorm = nombre.trim().replace(Regex("\\s+"), " ")
        val apellidoNorm = apellido.trim().replace(Regex("\\s+"), " ")
        if (nombreNorm.isBlank() || apellidoNorm.isBlank()) {
            _errorMessage.value = app.getString(R.string.auth_error_names_required)
            return
        }
        val fullName = "$nombreNorm $apellidoNorm"
        viewModelScope.launch {
            _busy.value = true
            _errorMessage.value = null
            _infoMessage.value = null
            try {
                val user = c.auth.signUpWith(Email) {
                    this.email = email.trim()
                    this.password = password
                    data = buildJsonObject {
                        put("given_name", nombreNorm)
                        put("family_name", apellidoNorm)
                        put("full_name", fullName)
                        put("name", fullName)
                    }
                }
                when {
                    user != null && user.identities?.isEmpty() == true ->
                        _errorMessage.value = app.getString(
                            R.string.auth_error_email_in_use_signup,
                        )
                    user != null ->
                        _infoMessage.value = app.getString(
                            R.string.auth_signup_email_sent,
                        )
                    else -> {
                        // p. ej. confirmaci?n autom?tica: la sesi?n se importa sola; no mostramos el aviso de correo
                    }
                }
            } catch (e: Throwable) {
                _errorMessage.value = mapAuthFailure(e)
            } finally {
                _busy.value = false
            }
        }
    }

    /** Lee nombre y apellido desde metadata (`given_name`/`family_name` o `full_name`/`name`). */
    fun editableProfileSnapshot(): AuthProfileSnapshot? {
        val c = client ?: return null
        val user = c.auth.currentUserOrNull() ?: return null
        val meta = user.userMetadata
        val given = metaString(meta, "given_name")
        val family = metaString(meta, "family_name")
        if (!given.isNullOrBlank() && !family.isNullOrBlank()) {
            return AuthProfileSnapshot(given.trim(), family.trim(), user.email)
        }
        val full = metaString(meta, "full_name") ?: metaString(meta, "name")
        if (!full.isNullOrBlank()) {
            val (n, a) = splitNombreApellidoDesdeCompleto(full)
            return AuthProfileSnapshot(n, a, user.email)
        }
        return AuthProfileSnapshot("", "", user.email)
    }

    /**
     * Etiqueta para la UI (barra superior, inicio): nombre y apellidos de metadata si existen;
     * si no, el correo de la sesión.
     */
    fun cuentaEtiquetaVisible(): String? {
        val s = editableProfileSnapshot() ?: return null
        val name = "${s.nombre} ${s.apellido}".trim()
        when {
            name.isNotBlank() -> return name
            !s.email.isNullOrBlank() -> return s.email.trim()
            else -> return null
        }
    }

    fun updateProfile(nombre: String, apellido: String, onDone: (Result<Unit>) -> Unit) {
        val c = client ?: run {
            onDone(Result.failure(IllegalStateException()))
            return
        }
        val app = getApplication<Application>()
        val nombreNorm = nombre.trim().replace(Regex("\\s+"), " ")
        val apellidoNorm = apellido.trim().replace(Regex("\\s+"), " ")
        if (nombreNorm.isBlank() || apellidoNorm.isBlank()) {
            onDone(Result.failure(IllegalArgumentException(app.getString(R.string.auth_error_names_required))))
            return
        }
        val fullName = "$nombreNorm $apellidoNorm"
        viewModelScope.launch {
            _busy.value = true
            try {
                c.auth.updateUser(updateCurrentUser = true) {
                    data {
                        put("given_name", nombreNorm)
                        put("family_name", apellidoNorm)
                        put("full_name", fullName)
                        put("name", fullName)
                    }
                }
                onDone(Result.success(Unit))
            } catch (e: Throwable) {
                onDone(Result.failure(Exception(mapAuthFailure(e), e)))
            } finally {
                _busy.value = false
            }
        }
    }

    fun signOut() {
        val c = client ?: return
        viewModelScope.launch {
            val uid = c.auth.currentUserOrNull()?.id?.toString()
            try {
                c.auth.signOut()
            } catch (_: Throwable) {
            }
            withContext(Dispatchers.IO) {
                runCatching {
                    val app = getApplication<AcademiaApplication>()
                    if (!uid.isNullOrBlank()) {
                        app.database.sessionCategoriaRecienteDao().deleteForUser(uid)
                    }
                    val dao = app.database.academiaConfigDao()
                    val cfg = dao.getActual() ?: return@runCatching
                    dao.upsert(
                        cfg.copy(
                            cloudMembresiaRol = null,
                            cloudCoachCategoriasJson = null,
                            academiaGestionNubePermitida = true,
                            recursosUltimaVistaAtMillis = 0L,
                        ),
                    )
                }
            }
        }
    }

    private fun metaString(meta: JsonObject?, key: String): String? {
        val el = meta?.get(key) ?: return null
        return (el as? JsonPrimitive)?.contentOrNull
    }

    /** Primera palabra = nombre, el resto = apellidos (cuentas antiguas sin metadata separada). */
    private fun splitNombreApellidoDesdeCompleto(full: String): Pair<String, String> {
        val t = full.trim().replace(Regex("\\s+"), " ")
        if (t.isEmpty()) return "" to ""
        val i = t.indexOf(' ')
        if (i < 0) return t to ""
        return t.substring(0, i).trim() to t.substring(i + 1).trim()
    }

    /** Solicita correo con enlace; el redirect usa el deep link configurado en [AcademiaApplication] (p. ej. academiafutbol://auth). */
    fun sendPasswordRecoveryEmail(email: String) {
        val c = client ?: return
        viewModelScope.launch {
            _busy.value = true
            _errorMessage.value = null
            _infoMessage.value = null
            try {
                c.auth.resetPasswordForEmail(email.trim())
                _infoMessage.value = getApplication<Application>().getString(R.string.auth_forgot_email_sent)
            } catch (e: Throwable) {
                _errorMessage.value = mapAuthFailure(e)
            } finally {
                _busy.value = false
            }
        }
    }

    /** Tras abrir el enlace del correo y tener sesi?n de recuperaci?n. */
    fun completePasswordRecovery(newPassword: String, confirmPassword: String) {
        val c = client ?: return
        val app = getApplication<Application>()
        if (newPassword != confirmPassword) {
            _errorMessage.value = app.getString(R.string.auth_password_mismatch)
            return
        }
        if (newPassword.isBlank()) {
            _errorMessage.value = app.getString(R.string.auth_password_required)
            return
        }
        viewModelScope.launch {
            _busy.value = true
            _errorMessage.value = null
            try {
                c.auth.updateUser {
                    password = newPassword
                }
            } catch (e: Throwable) {
                _errorMessage.value = mapAuthFailure(e)
            } finally {
                _busy.value = false
            }
        }
    }

    private fun mapAuthFailure(e: Throwable): String {
        val app = getApplication<Application>()
        when (e) {
            is AuthWeakPasswordException -> {
                val base = app.getString(R.string.auth_error_weak_password)
                val reasons = e.reasons.joinToString("\n") { "��� $it" }
                return if (reasons.isNotBlank()) "$base\n$reasons" else base
            }
            is AuthRestException ->
                return mapAuthRestException(e, app)
            else -> { }
        }
        if (looksLikeNetworkFailure(e)) {
            return app.getString(R.string.auth_error_network)
        }
        if (e is RestException) {
            return when (e) {
                is AuthRestException -> mapAuthRestException(e, app)
                else -> mapRestExceptionToMessage(e, app)
            }
        }
        val hint = e.message?.lowercase().orEmpty()
        return when {
            hint.contains("invalid login credentials") ||
                hint.contains("invalid_credentials") ||
                hint.contains("invalid_grant") ->
                app.getString(R.string.auth_error_invalid_credentials)
            hint.contains("email not confirmed") ||
                hint.contains("email_not_confirmed") ->
                app.getString(R.string.auth_error_email_not_confirmed)
            hint.contains("user already registered") ||
                hint.contains("already registered") ||
                hint.contains("email_exists") ||
                hint.contains("user_already_exists") ->
                app.getString(R.string.auth_error_email_in_use_signup)
            else -> app.getString(R.string.auth_error_unexpected)
        }
    }

    /** Errores HTTP de Supabase que no vienen como [AuthRestException] (p. ej. algunas variantes de cliente). */
    private fun mapRestExceptionToMessage(e: RestException, app: Application): String {
        val code = AuthErrorCode.fromValue(e.error)
        if (code != null) {
            when (code) {
                AuthErrorCode.EmailExists,
                AuthErrorCode.UserAlreadyExists,
                AuthErrorCode.IdentityAlreadyExists,
                -> return app.getString(R.string.auth_error_email_in_use_signup)
                AuthErrorCode.InvalidCredentials ->
                    return app.getString(R.string.auth_error_invalid_credentials)
                AuthErrorCode.EmailNotConfirmed,
                AuthErrorCode.ProviderEmailNeedsVerification,
                -> return app.getString(R.string.auth_error_email_not_confirmed)
                AuthErrorCode.WeakPassword ->
                    return app.getString(R.string.auth_error_weak_password)
                AuthErrorCode.SignupDisabled ->
                    return app.getString(R.string.auth_error_signup_disabled)
                AuthErrorCode.UserBanned ->
                    return app.getString(R.string.auth_error_user_banned)
                AuthErrorCode.OverRequestRateLimit,
                AuthErrorCode.OverEmailSendRateLimit,
                AuthErrorCode.OverSmsSendRateLimit,
                -> return app.getString(R.string.auth_error_rate_limit)
                else -> { }
            }
        }
        val raw = e.error.lowercase()
        val desc = e.description?.lowercase().orEmpty()
        return when {
            raw == AuthErrorCode.InvalidCredentials.value ||
                desc.contains("invalid login credentials") ||
                raw == "invalid_grant" ->
                app.getString(R.string.auth_error_invalid_credentials)
            raw == AuthErrorCode.EmailNotConfirmed.value ->
                app.getString(R.string.auth_error_email_not_confirmed)
            raw == AuthErrorCode.EmailExists.value ||
                raw == AuthErrorCode.UserAlreadyExists.value ->
                app.getString(R.string.auth_error_email_in_use_signup)
            raw == "email_address_invalid" ->
                app.getString(R.string.auth_error_email_invalid)
            else -> app.getString(R.string.auth_error_unexpected)
        }
    }

    private fun mapAuthRestException(e: AuthRestException, app: Application): String {
        when (val code = e.errorCode) {
            null -> { }
            AuthErrorCode.EmailExists,
            AuthErrorCode.UserAlreadyExists,
            AuthErrorCode.IdentityAlreadyExists,
            -> return app.getString(R.string.auth_error_email_in_use_signup)
            AuthErrorCode.InvalidCredentials ->
                return app.getString(R.string.auth_error_invalid_credentials)
            AuthErrorCode.EmailNotConfirmed,
            AuthErrorCode.ProviderEmailNeedsVerification,
            -> return app.getString(R.string.auth_error_email_not_confirmed)
            AuthErrorCode.WeakPassword ->
                return app.getString(R.string.auth_error_weak_password)
            AuthErrorCode.SignupDisabled ->
                return app.getString(R.string.auth_error_signup_disabled)
            AuthErrorCode.UserBanned ->
                return app.getString(R.string.auth_error_user_banned)
            AuthErrorCode.OverRequestRateLimit,
            AuthErrorCode.OverEmailSendRateLimit,
            AuthErrorCode.OverSmsSendRateLimit,
            -> return app.getString(R.string.auth_error_rate_limit)
            else -> { }
        }
        val raw = e.error.lowercase()
        val desc = e.description?.lowercase().orEmpty()
        return when {
            raw == AuthErrorCode.InvalidCredentials.value ||
                desc.contains("invalid login credentials") ||
                raw == "invalid_grant" ->
                app.getString(R.string.auth_error_invalid_credentials)
            raw == AuthErrorCode.EmailNotConfirmed.value ->
                app.getString(R.string.auth_error_email_not_confirmed)
            raw == AuthErrorCode.EmailExists.value ||
                raw == AuthErrorCode.UserAlreadyExists.value ->
                app.getString(R.string.auth_error_email_in_use_signup)
            raw == "email_address_invalid" ->
                app.getString(R.string.auth_error_email_invalid)
            else -> app.getString(R.string.auth_error_unexpected)
        }
    }

    private fun looksLikeNetworkFailure(e: Throwable): Boolean {
        var x: Throwable? = e
        while (x != null) {
            when (x) {
                is IOException,
                is SocketTimeoutException,
                is UnknownHostException,
                -> return true
            }
            val name = x.javaClass.name
            if (name.contains("HttpRequestTimeout", ignoreCase = true)) return true
            if (x.message?.contains("timeout", ignoreCase = true) == true) return true
            x = x.cause
        }
        return false
    }
}
