package com.escuelafutbol.academia.data.local.security

import java.security.MessageDigest

object StaffPinHasher {
    private const val SALT = "ef_academia_pin_v1"

    fun hash(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest((SALT + pin).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { b -> "%02x".format(b) }
    }

    fun pinValido(pin: String): Boolean =
        pin.length in 4..6 && pin.all { it.isDigit() }
}
