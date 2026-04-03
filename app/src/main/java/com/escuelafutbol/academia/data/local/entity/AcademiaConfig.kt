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
    /** [RolDispositivo.name] — quién usa este teléfono/tablet. */
    val rolDispositivo: String = RolDispositivo.PADRE_TUTOR.name,
    /** Hash SHA-256 (con sal) del PIN; null hasta que el club lo defina. */
    val pinStaffHash: String? = null,
    /** UUID de la fila `academias` en Supabase para este usuario. */
    val remoteAcademiaId: String? = null,
    /** Código de club en nube (`academias.codigo_club`); solo referencia local tras pull/sync. */
    val codigoClubRemoto: String? = null,
    /** Color primario de la interfaz (#RRGGBB). Null = tema por defecto de la app. */
    val temaColorPrimarioHex: String? = null,
    /** Color secundario / acentos (#RRGGBB). Null = tema por defecto. */
    val temaColorSecundarioHex: String? = null,
    /**
     * Si la academia está enlazada a la nube: el usuario actual puede editar marca, nombre, staff, etc.
     * (dueño de `academias.user_id` o miembro con rol owner/admin). Sin nube o valor true = sin restricción local.
     */
    val academiaGestionNubePermitida: Boolean = true,
    /**
     * Rol del usuario en `academia_miembros` (minúsculas) o `owner` si es dueño de `academias.user_id`.
     * Null si no hay sesión nube o aún no sincronizado.
     */
    val cloudMembresiaRol: String? = null,
    /** JSON `["Cat A","Cat B"]` — categorías asignadas al miembro coach en Supabase. */
    val cloudCoachCategoriasJson: String? = null,
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
            codigoClubRemoto = null,
            temaColorPrimarioHex = null,
            temaColorSecundarioHex = null,
            academiaGestionNubePermitida = true,
            cloudMembresiaRol = null,
            cloudCoachCategoriasJson = null,
        )
    }
}
