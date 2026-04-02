package com.escuelafutbol.academia.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.data.local.AcademiaDatabase
import com.escuelafutbol.academia.ui.academia.AcademiaConfigViewModel
import com.escuelafutbol.academia.ui.academia.StaffViewModel
import com.escuelafutbol.academia.ui.attendance.AttendanceViewModel
import com.escuelafutbol.academia.ui.auth.AuthViewModel
import com.escuelafutbol.academia.ui.categoria.CategoriaPickerViewModel
import com.escuelafutbol.academia.ui.parents.ParentsViewModel
import com.escuelafutbol.academia.ui.players.PlayersViewModel
import com.escuelafutbol.academia.ui.stats.StatsViewModel
import com.escuelafutbol.academia.ui.sync.CloudSyncViewModel

class AcademiaViewModelFactory(
    private val application: Application,
    private val database: AcademiaDatabase,
    private val session: SessionViewModel? = null,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(SessionViewModel::class.java) ->
                return SessionViewModel() as T
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                val app = application as AcademiaApplication
                return AuthViewModel(app.supabaseClient) as T
            }
            modelClass.isAssignableFrom(CloudSyncViewModel::class.java) ->
                return CloudSyncViewModel(application) as T
            modelClass.isAssignableFrom(CategoriaPickerViewModel::class.java) ->
                return CategoriaPickerViewModel(
                    application,
                    database.categoriaDao(),
                    database.jugadorDao(),
                ) as T
            modelClass.isAssignableFrom(AcademiaConfigViewModel::class.java) ->
                return AcademiaConfigViewModel(application, database) as T
            modelClass.isAssignableFrom(StaffViewModel::class.java) ->
                return StaffViewModel(application, database.staffDao()) as T
        }
        val s = session
            ?: throw IllegalStateException("Se requiere SessionViewModel para ${modelClass.name}")
        return when {
            modelClass.isAssignableFrom(PlayersViewModel::class.java) ->
                PlayersViewModel(application, database.jugadorDao(), s.filtroCategoria) as T
            modelClass.isAssignableFrom(AttendanceViewModel::class.java) ->
                AttendanceViewModel(database.jugadorDao(), database.asistenciaDao(), s.filtroCategoria) as T
            modelClass.isAssignableFrom(StatsViewModel::class.java) ->
                StatsViewModel(database.jugadorDao(), database.asistenciaDao(), s.filtroCategoria) as T
            modelClass.isAssignableFrom(ParentsViewModel::class.java) ->
                ParentsViewModel() as T
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
