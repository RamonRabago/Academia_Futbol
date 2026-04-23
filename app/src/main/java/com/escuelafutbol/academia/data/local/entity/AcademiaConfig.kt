package com.escuelafutbol.academia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.escuelafutbol.academia.data.local.model.RolDispositivo

@Entity(tableName = "academia_config")
data class AcademiaConfig(
    @PrimaryKey val id: Int = 1,
    val nombreAcademia: String,
    /** Logo circular tipo perfil (red social). */
    val logoRutaAbsoluta: String?,
    /** Imagen ancha de portada / equipo / instalaciones. */
    val portadaRutaAbsoluta: String?,
    /** Logo en Storage (URL pública). */
    val logoUrlSupabase: String? = null,
    /** Portada en Storage (URL pública). */
    val portadaUrlSupabase: String? = null,
    val mensualidadVisibleProfesor: Boolean = true,
    val mensualidadVisibleCoordinador: Boolean = true,
    val mensualidadVisibleDueno: Boolean = true,
    /** [RolDispositivo.name] — espejo persistido en sync con la nube; en UI usar `rolDispositivoEfectivo()`. */
    val rolDispositivo: String = RolDispositivo.PADRE_TUTOR.name,
    /** Hash SHA-256 (con sal) del PIN; null hasta que el club lo defina. */
    val pinStaffHash: String? = null,
    /** UUID de la fila `academias` en Supabase para este usuario. */
    val remoteAcademiaId: String? = null,
    /**
     * UUID Auth del dueño de la cuenta de la academia (`academias.user_id` en Supabase).
     * Se rellena al sincronizar; sirve para saber si la sesión actual puede editar reglas reservadas al dueño (p. ej. día límite de pago).
     */
    val remoteAcademiaCuentaUserId: String? = null,
    /** Código de club en nube (`academias.codigo_club`); legado; puede quedar null tras códigos por rol. */
    val codigoClubRemoto: String? = null,
    /** Invitación solo entrenador (`academias.codigo_invite_coach`). */
    val codigoInviteCoachRemoto: String? = null,
    /** Invitación solo coordinador. */
    val codigoInviteCoordinatorRemoto: String? = null,
    /** Invitación solo padre/tutor. */
    val codigoInviteParentRemoto: String? = null,
    /** Color primario de la interfaz (#RRGGBB). Null = tema por defecto de la app. */
    val temaColorPrimarioHex: String? = null,
    /** Color secundario / acentos (#RRGGBB). Null = tema por defecto. */
    val temaColorSecundarioHex: String? = null,
    /**
     * Si la academia está enlazada a la nube: el usuario actual puede editar marca, nombre, staff, etc.
     * (dueño de `academias.user_id` o miembro activo owner/admin/coordinator, alineado con RLS). Sin nube o true = sin restricción local.
     */
    val academiaGestionNubePermitida: Boolean = true,
    /**
     * Rol del usuario en `academia_miembros` (minúsculas) o `owner` si es dueño de `academias.user_id`.
     * Null si no hay sesión nube o aún no sincronizado.
     */
    val cloudMembresiaRol: String? = null,
    /** JSON `["Cat A","Cat B"]` — categorías asignadas al miembro coach en Supabase. */
    val cloudCoachCategoriasJson: String? = null,
    /**
     * Día del mes (1–28) hasta el cual se espera el pago mensual; después, con saldo pendiente, el padre ve recordatorio.
     * Null = la academia no aplica esta regla.
     */
    val diaLimitePagoMes: Int? = null,
    /**
     * Última vez que el usuario abrió la pestaña Recursos (`System.currentTimeMillis()`).
     * 0 = aún no fijada; la primera carga del listado la inicializa para no marcar todo el histórico como no leído.
     */
    val recursosUltimaVistaAtMillis: Long = 0L,
    /**
     * JSON array de enteros ISO-8601 `DayOfWeek` (lunes=1 … domingo=7), p. ej. `[2,4]` martes y jueves.
     * Sirve para detectar automáticamente si el día del calendario es habitual de entreno.
     */
    val diasEntrenoSemanaIsoJson: String = "[2,4]",
) {
    companion object {
        val DEFAULT = AcademiaConfig(
            id = 1,
            nombreAcademia = "Mi Academia",
            logoRutaAbsoluta = null,
            portadaRutaAbsoluta = null,
            logoUrlSupabase = null,
            portadaUrlSupabase = null,
            mensualidadVisibleProfesor = true,
            mensualidadVisibleCoordinador = true,
            mensualidadVisibleDueno = true,
            rolDispositivo = RolDispositivo.PADRE_TUTOR.name,
            pinStaffHash = null,
            remoteAcademiaId = null,
            remoteAcademiaCuentaUserId = null,
            codigoClubRemoto = null,
            codigoInviteCoachRemoto = null,
            codigoInviteCoordinatorRemoto = null,
            codigoInviteParentRemoto = null,
            temaColorPrimarioHex = null,
            temaColorSecundarioHex = null,
            academiaGestionNubePermitida = true,
            cloudMembresiaRol = null,
            cloudCoachCategoriasJson = null,
            diaLimitePagoMes = null,
            recursosUltimaVistaAtMillis = 0L,
            diasEntrenoSemanaIsoJson = "[2,4]",
        )
    }
}
