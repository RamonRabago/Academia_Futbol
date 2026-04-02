package com.escuelafutbol.academia.ui.auth



import androidx.lifecycle.ViewModel

import androidx.lifecycle.viewModelScope

import io.github.jan.supabase.SupabaseClient

import io.github.jan.supabase.auth.auth

import io.github.jan.supabase.auth.providers.builtin.Email

import io.github.jan.supabase.auth.status.SessionStatus

import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.StateFlow

import kotlinx.coroutines.flow.asStateFlow

import kotlinx.coroutines.launch



class AuthViewModel(

    private val client: SupabaseClient?,

) : ViewModel() {



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

                _errorMessage.value = e.message ?: e.javaClass.simpleName

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

                c.auth.signUpWith(Email) {

                    this.email = email.trim()

                    this.password = password

                }

                _infoMessage.value =

                    "Si tu proyecto exige confirmar el correo, revisa la bandeja de entrada antes de iniciar sesión."

            } catch (e: Throwable) {

                _errorMessage.value = e.message ?: e.javaClass.simpleName

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

}

