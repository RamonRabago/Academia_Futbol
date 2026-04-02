package com.escuelafutbol.academia.ui.academia

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.data.local.dao.StaffDao
import com.escuelafutbol.academia.data.local.entity.Staff
import com.escuelafutbol.academia.data.local.model.RolStaff
import java.io.File
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StaffViewModel(
    application: Application,
    private val staffDao: StaffDao,
) : AndroidViewModel(application) {

    val staff = staffDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun agregar(
        nombre: String,
        rol: RolStaff,
        telefono: String?,
        email: String?,
        fotoRutaAbsoluta: String?,
    ) {
        viewModelScope.launch {
            staffDao.insert(
                Staff(
                    nombre = nombre.trim(),
                    rol = rol.name,
                    telefono = telefono?.trim()?.takeIf { it.isNotEmpty() },
                    email = email?.trim()?.takeIf { it.isNotEmpty() },
                    fotoRutaAbsoluta = fotoRutaAbsoluta,
                ),
            )
        }
    }

    fun actualizar(
        anterior: Staff,
        nombre: String,
        rol: RolStaff,
        telefono: String?,
        email: String?,
        fotoRutaAbsoluta: String?,
        quitarFoto: Boolean = false,
    ) {
        viewModelScope.launch {
            if (quitarFoto) {
                anterior.fotoRutaAbsoluta?.let { runCatching { File(it).delete() } }
                staffDao.insert(
                    anterior.copy(
                        nombre = nombre.trim(),
                        rol = rol.name,
                        telefono = telefono?.trim()?.takeIf { it.isNotEmpty() },
                        email = email?.trim()?.takeIf { it.isNotEmpty() },
                        fotoRutaAbsoluta = null,
                        fotoUrlSupabase = null,
                    ),
                )
                return@launch
            }
            val vieja = anterior.fotoRutaAbsoluta
            val nueva = fotoRutaAbsoluta
            if (vieja != null && vieja != nueva) {
                runCatching { File(vieja).delete() }
            }
            staffDao.insert(
                anterior.copy(
                    nombre = nombre.trim(),
                    rol = rol.name,
                    telefono = telefono?.trim()?.takeIf { it.isNotEmpty() },
                    email = email?.trim()?.takeIf { it.isNotEmpty() },
                    fotoRutaAbsoluta = nueva,
                    fotoUrlSupabase = if (nueva == anterior.fotoRutaAbsoluta) {
                        anterior.fotoUrlSupabase
                    } else {
                        null
                    },
                ),
            )
        }
    }

    fun eliminar(s: Staff) {
        viewModelScope.launch {
            s.fotoRutaAbsoluta?.let { runCatching { File(it).delete() } }
            staffDao.delete(s)
        }
    }
}
