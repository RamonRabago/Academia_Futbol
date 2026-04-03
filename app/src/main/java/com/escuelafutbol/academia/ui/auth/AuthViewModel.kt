package com.escuelafutbol.academia.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    fun signUp(email: String, password: String) {
        val c = client ?: return
        viewModelScope.launch {
            _busy.value = true
            _errorMessage.value = null
            _infoMessage.value = null
            try {
                val user = c.auth.signUpWith(Email) {
                    this.email = email.trim()
                    this.password = password
                }
                when {
                    user != null && user.identities?.isEmpty() == true ->
                        _errorMessage.value = getApplication<Application>().getString(
                            R.string.auth_error_email_in_use_signup,
                        )
                    user != null ->
                        _infoMessage.value = getApplication<Application>().getString(
                            R.string.auth_signup_email_sent,
                        )
                    else -> {
                        // p. ej. confirmación automática: la sesión se importa sola; no mostramos el aviso de correo
                    }
                }
            } catch (e: Throwable) {
                _errorMessage.value = mapAuthFailure(e)
            } finally {
                _busy.value = false
            }
        }
    }

    fun signOut() {
        val c = client ?: return
        viewModelScope.launch {
            try {
                c.auth.signOut()
            } catch (_: Throwable) {
            }
        }
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

    /** Tras abrir el enlace del correo y tener sesión de recuperación. */
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
                val reasons = e.reasons.joinToString("\n") { "• $it" }
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
