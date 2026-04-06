package com.escuelafutbol.academia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jugadores")
data class Jugador(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val categoria: String,
    /**
     * Medianoche UTC del día de nacimiento (selector Material); null si no consta.
     * [anioNacimiento] se deriva al guardar para compatibilidad y columnas remotas antiguas.
     */
    val fechaNacimientoMillis: Long? = null,
    val anioNacimiento: Int?,
    val telefonoTutor: String?,
    val emailTutor: String?,
    val notas: String?,
    val fotoRutaAbsoluta: String?,
    /** URL pública en Storage (Supabase) tras sincronizar. */
    val fotoUrlSupabase: String? = null,
    /** CURP (México); se guarda en mayúsculas sin espacios. */
    val curp: String? = null,
    /** PDF o imagen del documento CURP (opcional, además del texto). */
    val curpDocumentoRutaAbsoluta: String? = null,
    val curpDocumentoUrlSupabase: String? = null,
    /** Copia local del acta (PDF o imagen). */
    val actaNacimientoRutaAbsoluta: String? = null,
    /** URL pública del acta en Storage tras sincronizar. */
    val actaNacimientoUrlSupabase: String? = null,
    /** Momento en que se registró en la academia (epoch millis). */
    val fechaAltaMillis: Long,
    val activo: Boolean = true,
    val fechaBajaMillis: Long? = null,
    /** Cuota mensual (moneda local); null o 0 si no aplica. Ignorada si [becado]. */
    val mensualidad: Double? = null,
    /** Becado: no paga mensualidad. */
    val becado: Boolean = false,
    /** UUID en Supabase tras sincronizar. */
    val remoteId: String? = null,
    /** UUID del usuario Auth que dio de alta (local y nube); null en datos antiguos o sin sesión. */
    val altaPorUserId: String? = null,
    /** Nombre o correo visible de quien dio de alta (copiado al guardar desde metadata de sesión). */
    val altaPorNombre: String? = null,
)
