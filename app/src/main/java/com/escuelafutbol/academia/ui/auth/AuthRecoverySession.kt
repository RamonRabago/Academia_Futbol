package com.escuelafutbol.academia.ui.auth

import android.util.Base64
import io.github.jan.supabase.auth.user.UserSession
import java.nio.charset.StandardCharsets

/**
 * Tras abrir el enlace del correo de recuperación, Supabase deja una sesión con `amr` recovery en el JWT
 * o el campo `type` en la sesión decodificada.
 */
fun isPasswordRecoverySession(session: UserSession): Boolean {
    if (session.type.equals("recovery", ignoreCase = true)) return true
    return jwtPayloadIndicatesPasswordRecovery(session.accessToken)
}

private fun jwtPayloadIndicatesPasswordRecovery(accessToken: String): Boolean {
    val payload = accessToken.split('.').getOrNull(1) ?: return false
    val json = try {
        val decoded = Base64.decode(
            payload,
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
        )
        String(decoded, StandardCharsets.UTF_8)
    } catch (_: Throwable) {
        return false
    }
    return json.contains("\"method\":\"recovery\"") ||
        (json.contains("\"amr\"") && json.contains("recovery"))
}
