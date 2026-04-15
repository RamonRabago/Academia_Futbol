package com.escuelafutbol.academia.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.data.local.AcademiaDatabase
import com.escuelafutbol.academia.ui.academia.AcademiaConfigViewModel
import com.escuelafutbol.academia.ui.academia.AcademiaMiembrosViewModel
import com.escuelafutbol.academia.ui.academia.StaffViewModel
import com.escuelafutbol.academia.ui.attendance.AttendanceViewModel
import com.escuelafutbol.academia.ui.auth.AcademiaBindingViewModel
import com.escuelafutbol.academia.ui.auth.AuthViewModel
import com.escuelafutbol.academia.ui.categoria.CategoriaPickerViewModel
import com.escuelafutbol.academia.ui.contenido.ContenidoViewModel
import com.escuelafutbol.academia.ui.finanzas.FinanzasViewModel
import com.escuelafutbol.academia.ui.parents.ParentsViewModel
import com.escuelafutbol.academia.ui.players.PlayersViewModel
import com.escuelafutbol.academia.ui.stats.StatsViewModel
import com.escuelafutbol.academia.ui.sync.CloudSyncViewModel

class AcademiaViewModelFactory(
    private val application: Application,
    private val database: AcademiaDatabase,
    private val session: SessionViewModel? = null,
    /**
     * UUID del usuario autenticado (Supabase Auth). Si no es vacío, [SessionViewModel] persiste la categoría
     * elegida en Room por usuario.
     */
    private val sessionAuthUserId: String = "",
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(SessionViewModel::class.java) ->
                return SessionViewModel(database, sessionAuthUserId) as T
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                val app = application as AcademiaApplication
                return AuthViewModel(application, app.supabaseClient) as T
            }
            modelClass.isAssignableFrom(CloudSyncViewModel::class.java) ->
                return CloudSyncViewModel(application) as T
            modelClass.isAssignableFrom(AcademiaBindingViewModel::class.java) ->
                return AcademiaBindingViewModel(application) as T
            modelClass.isAssignableFrom(CategoriaPickerViewModel::class.java) ->
                return CategoriaPickerViewModel(
                    application,
                    database.categoriaDao(),
                    database.jugadorDao(),
                ) as T
            modelClass.isAssignableFrom(AcademiaConfigViewModel::class.java) ->
                return AcademiaConfigViewModel(application, database) as T
            modelClass.isAssignableFrom(StaffViewModel::class.java) ->
                return StaffViewModel(
                    application,
                    database.staffDao(),
                    database.staffCategoriaDao(),
                    database.categoriaDao(),
                    database.academiaConfigDao(),
                ) as T
            modelClass.isAssignableFrom(AcademiaMiembrosViewModel::class.java) ->
                return AcademiaMiembrosViewModel(application, database) as T
        }
        val s = session
            ?: throw IllegalStateException("Se requiere SessionViewModel para ${modelClass.name}")
        return when {
            modelClass.isAssignableFrom(PlayersViewModel::class.java) ->
                PlayersViewModel(
                    application,
                    database.jugadorDao(),
                    database.cobroMensualDao(),
                    database.academiaConfigDao(),
                    s.filtroCategoria,
                    s.categoriasPermitidasOperacion,
                ) as T
            modelClass.isAssignableFrom(AttendanceViewModel::class.java) ->
                AttendanceViewModel(
                    database.jugadorDao(),
                    database.asistenciaDao(),
                    database.diaEntrenamientoDao(),
                    s.filtroCategoria,
                    s.categoriasPermitidasOperacion,
                ) as T
            modelClass.isAssignableFrom(StatsViewModel::class.java) ->
                StatsViewModel(
                    database.jugadorDao(),
                    database.asistenciaDao(),
                    database.diaEntrenamientoDao(),
                    s.filtroCategoria,
                    s.categoriasPermitidasOperacion,
                ) as T
            modelClass.isAssignableFrom(ContenidoViewModel::class.java) ->
                ContenidoViewModel(
                    application,
                    database,
                    s.filtroCategoria,
                    s.categoriasPermitidasOperacion,
                ) as T
            modelClass.isAssignableFrom(FinanzasViewModel::class.java) ->
                FinanzasViewModel(
                    application,
                    database.jugadorDao(),
                    database.cobroMensualDao(),
                    database.staffDao(),
                    database.academiaConfigDao(),
                    s.categoriasPermitidasOperacion,
                ) as T
            modelClass.isAssignableFrom(ParentsViewModel::class.java) ->
                ParentsViewModel(
                    application,
                    database,
                    s.categoriasPermitidasOperacion,
                ) as T
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
