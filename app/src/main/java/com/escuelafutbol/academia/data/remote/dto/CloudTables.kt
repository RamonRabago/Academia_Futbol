package com.escuelafutbol.academia.data.remote.dto



import com.escuelafutbol.academia.data.local.entity.Asistencia
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.data.local.entity.JugadorHistorial
import com.escuelafutbol.academia.data.local.entity.Staff

import kotlinx.serialization.SerialName

import kotlinx.serialization.Serializable



@Serializable

data class AcademiaRow(

    val id: String,

    @SerialName("user_id") val userId: String,

    val nombre: String,

    @SerialName("logo_url") val logoUrl: String? = null,

    @SerialName("portada_url") val portadaUrl: String? = null,

    @SerialName("mensualidad_visible_profesor") val mensualidadVisibleProfesor: Boolean = true,

    @SerialName("mensualidad_visible_coordinador") val mensualidadVisibleCoordinador: Boolean = true,

    @SerialName("mensualidad_visible_dueno") val mensualidadVisibleDueno: Boolean = true,

    @SerialName("rol_dispositivo") val rolDispositivo: String = "PADRE_TUTOR",

    @SerialName("pin_staff_hash") val pinStaffHash: String? = null,

    @SerialName("color_primario_hex") val colorPrimarioHex: String? = null,

    @SerialName("color_secundario_hex") val colorSecundarioHex: String? = null,

    @SerialName("codigo_club") val codigoClub: String? = null,

    @SerialName("codigo_invite_coach") val codigoInviteCoach: String? = null,

    @SerialName("codigo_invite_coordinator") val codigoInviteCoordinator: String? = null,

    @SerialName("codigo_invite_parent") val codigoInviteParent: String? = null,

)

@Serializable
data class AcademiaMiembroRow(
    val id: String,
    @SerialName("academia_id") val academiaId: String,
    @SerialName("user_id") val userId: String,
    val rol: String,
    val activo: Boolean = true,
)

/** Respuesta de RPC `alta_por_user_labels_for_academia`. */
@Serializable
data class AltaPorUserLabelRow(
    @SerialName("user_id") val userId: String,
    @SerialName("display_label") val displayLabel: String,
)

/** Respuesta de RPC `list_academia_miembros_for_manage` (gestión de miembros con identidad legible). */
@Serializable
data class AcademiaMiembroListRow(
    val id: String,
    @SerialName("academia_id") val academiaId: String,
    @SerialName("user_id") val userId: String,
    val rol: String,
    val activo: Boolean = true,
    @SerialName("display_label") val displayLabel: String? = null,
    @SerialName("member_email") val memberEmail: String? = null,
)

@Serializable
data class AcademiaMiembroCategoriaLinkRow(
    @SerialName("categoria_id") val categoriaId: String,
)

/** Fila devuelta por RPC `list_my_coach_category_names`. */
@Serializable
data class CoachCategoryNombreRow(val nombre: String)

@Serializable
data class AcademiaMiembroCategoriaInsert(
    @SerialName("miembro_id") val miembroId: String,
    @SerialName("categoria_id") val categoriaId: String,
)

@Serializable
data class AcademiaMiembroActivoPatch(val activo: Boolean)

@Serializable
data class AcademiaMiembroRolPatch(val rol: String)

@Serializable
data class AcademiaPadresAlumnoRow(
    val id: String,
    @SerialName("academia_id") val academiaId: String,
    @SerialName("parent_user_id") val parentUserId: String,
    @SerialName("jugador_id") val jugadorId: String,
)

@Serializable
data class AcademiaPadresAlumnoInsert(
    @SerialName("academia_id") val academiaId: String,
    @SerialName("parent_user_id") val parentUserId: String,
    @SerialName("jugador_id") val jugadorId: String,
)

@Serializable

data class AcademiaInsert(

    @SerialName("user_id") val userId: String,

    val nombre: String,

    @SerialName("logo_url") val logoUrl: String? = null,

    @SerialName("portada_url") val portadaUrl: String? = null,

    @SerialName("mensualidad_visible_profesor") val mensualidadVisibleProfesor: Boolean,

    @SerialName("mensualidad_visible_coordinador") val mensualidadVisibleCoordinador: Boolean,

    @SerialName("mensualidad_visible_dueno") val mensualidadVisibleDueno: Boolean,

    @SerialName("rol_dispositivo") val rolDispositivo: String,

    @SerialName("pin_staff_hash") val pinStaffHash: String? = null,

    @SerialName("color_primario_hex") val colorPrimarioHex: String? = null,

    @SerialName("color_secundario_hex") val colorSecundarioHex: String? = null,

)



@Serializable

data class CategoriaRow(

    val id: String,

    @SerialName("academia_id") val academiaId: String,

    val nombre: String,

    @SerialName("portada_url") val portadaUrl: String? = null,

)



@Serializable

data class CategoriaInsert(

    @SerialName("academia_id") val academiaId: String,

    val nombre: String,

    @SerialName("portada_url") val portadaUrl: String? = null,

)

@Serializable
data class CategoriaPortadaUrlPatch(@SerialName("portada_url") val portadaUrl: String)



@Serializable

data class JugadorRow(

    val id: String,

    @SerialName("academia_id") val academiaId: String,

    val nombre: String,

    val categoria: String,

    @SerialName("fecha_nacimiento_ms") val fechaNacimientoMs: Long? = null,

    @SerialName("anio_nacimiento") val anioNacimiento: Int? = null,

    @SerialName("telefono_tutor") val telefonoTutor: String? = null,

    @SerialName("email_tutor") val emailTutor: String? = null,

    val notas: String? = null,

    val curp: String? = null,

    @SerialName("curp_documento_url") val curpDocumentoUrl: String? = null,

    @SerialName("foto_url") val fotoUrl: String? = null,

    @SerialName("acta_nacimiento_url") val actaNacimientoUrl: String? = null,

    @SerialName("fecha_alta_ms") val fechaAltaMs: Long,

    val activo: Boolean = true,

    @SerialName("fecha_baja_ms") val fechaBajaMs: Long? = null,

    val mensualidad: Double? = null,

    val becado: Boolean = false,

    @SerialName("alta_por_user_id") val altaPorUserId: String? = null,

    @SerialName("alta_por_nombre") val altaPorNombre: String? = null,

)



@Serializable

data class JugadorInsert(

    @SerialName("academia_id") val academiaId: String,

    val nombre: String,

    val categoria: String,

    @SerialName("fecha_nacimiento_ms") val fechaNacimientoMs: Long? = null,

    @SerialName("anio_nacimiento") val anioNacimiento: Int? = null,

    @SerialName("telefono_tutor") val telefonoTutor: String? = null,

    @SerialName("email_tutor") val emailTutor: String? = null,

    val notas: String? = null,

    val curp: String? = null,

    @SerialName("curp_documento_url") val curpDocumentoUrl: String? = null,

    @SerialName("foto_url") val fotoUrl: String? = null,

    @SerialName("acta_nacimiento_url") val actaNacimientoUrl: String? = null,

    @SerialName("fecha_alta_ms") val fechaAltaMs: Long,

    val activo: Boolean = true,

    @SerialName("fecha_baja_ms") val fechaBajaMs: Long? = null,

    val mensualidad: Double? = null,

    val becado: Boolean = false,

    @SerialName("alta_por_user_id") val altaPorUserId: String? = null,

    @SerialName("alta_por_nombre") val altaPorNombre: String? = null,

)



/** PATCH de datos editables en `jugadores` (no toca `fecha_alta_ms`, URLs ni auditoría de alta). */
@Serializable
data class JugadorRemoteUpdatePatch(
    val nombre: String,
    val categoria: String,
    @SerialName("fecha_nacimiento_ms") val fechaNacimientoMs: Long? = null,
    @SerialName("anio_nacimiento") val anioNacimiento: Int? = null,
    @SerialName("telefono_tutor") val telefonoTutor: String? = null,
    @SerialName("email_tutor") val emailTutor: String? = null,
    val notas: String? = null,
    val curp: String? = null,
    val mensualidad: Double? = null,
    val becado: Boolean = false,
) {
    companion object {
        fun fromJugador(j: Jugador) = JugadorRemoteUpdatePatch(
            nombre = j.nombre,
            categoria = j.categoria,
            fechaNacimientoMs = j.fechaNacimientoMillis,
            anioNacimiento = j.anioNacimiento,
            telefonoTutor = j.telefonoTutor,
            emailTutor = j.emailTutor,
            notas = j.notas,
            curp = j.curp,
            mensualidad = if (j.becado) null else j.mensualidad,
            becado = j.becado,
        )
    }
}

fun Jugador.toCloudInsert(academiaId: String) = JugadorInsert(

    academiaId = academiaId,

    nombre = nombre,

    categoria = categoria,

    fechaNacimientoMs = fechaNacimientoMillis,

    anioNacimiento = anioNacimiento,

    telefonoTutor = telefonoTutor,

    emailTutor = emailTutor,

    notas = notas,

    curp = curp,

    curpDocumentoUrl = null,

    fotoUrl = null,

    actaNacimientoUrl = null,

    fechaAltaMs = fechaAltaMillis,

    activo = activo,

    fechaBajaMs = fechaBajaMillis,

    mensualidad = mensualidad,

    becado = becado,

    altaPorUserId = altaPorUserId,

    altaPorNombre = altaPorNombre,

)



fun JugadorRow.toLocalMerged(existing: Jugador?) = Jugador(

    id = existing?.id ?: 0L,

    nombre = nombre,

    categoria = categoria.trim(),

    fechaNacimientoMillis = fechaNacimientoMs ?: existing?.fechaNacimientoMillis,

    anioNacimiento = anioNacimiento,

    telefonoTutor = telefonoTutor,

    emailTutor = emailTutor,

    notas = notas,

    curp = curp?.takeIf { it.isNotBlank() } ?: existing?.curp,

    curpDocumentoRutaAbsoluta = existing?.curpDocumentoRutaAbsoluta,

    curpDocumentoUrlSupabase = curpDocumentoUrl?.takeIf { it.isNotBlank() }
        ?: existing?.curpDocumentoUrlSupabase,

    fotoRutaAbsoluta = existing?.fotoRutaAbsoluta,

    fotoUrlSupabase = fotoUrl?.takeIf { !it.isNullOrBlank() } ?: existing?.fotoUrlSupabase,

    actaNacimientoRutaAbsoluta = existing?.actaNacimientoRutaAbsoluta,

    actaNacimientoUrlSupabase = actaNacimientoUrl?.takeIf { !it.isNullOrBlank() }
        ?: existing?.actaNacimientoUrlSupabase,

    fechaAltaMillis = fechaAltaMs,

    activo = activo,

    fechaBajaMillis = fechaBajaMs,

    mensualidad = mensualidad,

    becado = becado,

    remoteId = id,

    altaPorUserId = altaPorUserId?.takeIf { it.isNotBlank() } ?: existing?.altaPorUserId,

    altaPorNombre = altaPorNombre?.takeIf { it.isNotBlank() } ?: existing?.altaPorNombre,

)



@Serializable

data class HistorialRow(

    val id: String,

    @SerialName("academia_id") val academiaId: String,

    @SerialName("jugador_id") val jugadorId: String,

    val tipo: String,

    @SerialName("fecha_ms") val fechaMs: Long,

    val detalle: String? = null,

)



@Serializable

data class HistorialInsert(

    @SerialName("academia_id") val academiaId: String,

    @SerialName("jugador_id") val jugadorId: String,

    val tipo: String,

    @SerialName("fecha_ms") val fechaMs: Long,

    val detalle: String? = null,

)



@Serializable

data class AsistenciaRow(

    val id: String,

    @SerialName("academia_id") val academiaId: String,

    @SerialName("jugador_id") val jugadorId: String,

    @SerialName("fecha_dia_ms") val fechaDiaMs: Long,

    val presente: Boolean,

)



@Serializable

data class AsistenciaInsert(

    @SerialName("academia_id") val academiaId: String,

    @SerialName("jugador_id") val jugadorId: String,

    @SerialName("fecha_dia_ms") val fechaDiaMs: Long,

    val presente: Boolean,

)



@Serializable

data class StaffRow(

    val id: String,

    @SerialName("academia_id") val academiaId: String,

    val nombre: String,

    val rol: String,

    val telefono: String? = null,

    val email: String? = null,

    @SerialName("foto_url") val fotoUrl: String? = null,

)



@Serializable

data class StaffInsert(

    @SerialName("academia_id") val academiaId: String,

    val nombre: String,

    val rol: String,

    val telefono: String? = null,

    val email: String? = null,

    @SerialName("foto_url") val fotoUrl: String? = null,

)

@Serializable
data class StaffCategoriaRow(
    val id: String,
    @SerialName("academia_id") val academiaId: String,
    @SerialName("staff_id") val staffId: String,
    @SerialName("categoria_id") val categoriaId: String,
)

@Serializable
data class StaffCategoriaInsert(
    @SerialName("academia_id") val academiaId: String,
    @SerialName("staff_id") val staffId: String,
    @SerialName("categoria_id") val categoriaId: String,
)



fun Staff.toCloudInsert(academiaId: String) = StaffInsert(

    academiaId = academiaId,

    nombre = nombre,

    rol = rol,

    telefono = telefono,

    email = email,

    fotoUrl = null,

)



fun JugadorHistorial.toCloudInsert(academiaId: String, jugadorRemoteId: String) = HistorialInsert(
    academiaId = academiaId,
    jugadorId = jugadorRemoteId,
    tipo = tipo,
    fechaMs = fechaMillis,
    detalle = detalle,
)

fun Asistencia.toCloudInsert(academiaId: String, jugadorRemoteId: String) = AsistenciaInsert(
    academiaId = academiaId,
    jugadorId = jugadorRemoteId,
    fechaDiaMs = fechaDia,
    presente = presente,
)

fun StaffRow.toLocalMerged(existing: Staff?) = Staff(

    id = existing?.id ?: 0L,

    nombre = nombre,

    rol = rol,

    telefono = telefono,

    email = email,

    fotoRutaAbsoluta = existing?.fotoRutaAbsoluta,

    fotoUrlSupabase = fotoUrl?.takeIf { !it.isNullOrBlank() } ?: existing?.fotoUrlSupabase,

    remoteId = id,

)

@Serializable
data class AcademiaLogoUrlPatch(@SerialName("logo_url") val logoUrl: String)

@Serializable
data class AcademiaPortadaUrlPatch(@SerialName("portada_url") val portadaUrl: String)

@Serializable
data class AcademiaNombrePatch(val nombre: String)

@Serializable
data class JugadorFotoUrlPatch(@SerialName("foto_url") val fotoUrl: String)

@Serializable
data class JugadorActaUrlPatch(@SerialName("acta_nacimiento_url") val actaNacimientoUrl: String)

@Serializable
data class JugadorCurpDocUrlPatch(@SerialName("curp_documento_url") val curpDocumentoUrl: String)

@Serializable
data class StaffFotoUrlPatch(@SerialName("foto_url") val fotoUrl: String)

@Serializable
data class AcademiaColoresPatch(
    @SerialName("color_primario_hex") val colorPrimarioHex: String? = null,
    @SerialName("color_secundario_hex") val colorSecundarioHex: String? = null,
)

@Serializable
data class AcademiaCodigoClubPatch(
    @SerialName("codigo_club") val codigoClub: String,
)

/** Respuesta JSON de RPC `regenerate_academia_invite_codes`. */
@Serializable
data class RegenerateInviteCodesResult(
    val coach: String,
    val coordinator: String,
    val parent: String,
)

