package com.escuelafutbol.academia.data.local

import android.content.Context

/**
 * Preferencias locales para primer uso (onboarding de bienvenida).
 * Claves por cuenta: cada `authUserKey` distinto tiene su propio flag «Comenzar».
 * Si [authUserKey] llega vacío, se usa el sufijo `anon` (varias sesiones sin id compartirían estado; en flujo normal
 * [AcademiaRootAuthenticatedContent] pasa el id de binding listo).
 */
class OnboardingPreferences(
    context: Context,
    /** Id de sesión Supabase (mismo criterio que el binding de academia). */
    authUserKey: String,
) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val userSuffix = authUserKey.trim().ifEmpty { "anon" }

    fun isFirstLaunchWelcomeCompleted(): Boolean =
        prefs.getBoolean(prefKey(KEY_FIRST_LAUNCH_WELCOME_DONE), false)

    fun setFirstLaunchWelcomeCompleted() {
        prefs.edit().putBoolean(prefKey(KEY_FIRST_LAUNCH_WELCOME_DONE), true).apply()
    }

    private fun prefKey(base: String): String = "${base}_$userSuffix"

    companion object {
        private const val PREFS_NAME = "academia_onboarding"
        private const val KEY_FIRST_LAUNCH_WELCOME_DONE = "first_launch_welcome_done"
    }
}
