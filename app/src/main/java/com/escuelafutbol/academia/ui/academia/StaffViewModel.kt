package com.escuelafutbol.academia.ui.academia

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.data.local.dao.AcademiaConfigDao
import com.escuelafutbol.academia.data.local.dao.CategoriaDao
import com.escuelafutbol.academia.data.local.dao.StaffCategoriaDao
import com.escuelafutbol.academia.data.local.dao.StaffDao
import com.escuelafutbol.academia.data.local.entity.Staff
import com.escuelafutbol.academia.data.local.model.RolStaff
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StaffViewModel(
    application: Application,
    private val staffDao: StaffDao,
    private val staffCategoriaDao: StaffCategoriaDao,
    private val categoriaDao: CategoriaDao,
    private val academiaConfigDao: AcademiaConfigDao,
) : AndroidViewModel(application) {

    private suspend fun puedeGestionarStaffEnNube(): Boolean {
        val cfg = academiaConfigDao.getActual() ?: return true
        if (cfg.remoteAcademiaId == null) return true
        return cfg.academiaGestionNubePermitida
    }

    val staff = staffDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categoriasDisponibles = categoriaDao.observeNombres()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun categoriasFlowPara(staffId: Long): Flow<List<String>> =
        staffCategoriaDao.observeNombresForStaff(staffId)

    suspend fun getCategoriasForStaffOnce(staffId: Long): List<String> =
        staffCategoriaDao.getNombresForStaff(staffId)

    fun agregar(
        nombre: String,
        rol: RolStaff,
        telefono: String?,
        email: String?,
        fotoRutaAbsoluta: String?,
        categorias: Set<String>,
    ) {
        viewModelScope.launch {
            if (!puedeGestionarStaffEnNube()) return@launch
            val id = staffDao.insert(
                Staff(
                    nombre = nombre.trim(),
                    rol = rol.name,
                    telefono = telefono?.trim()?.takeIf { it.isNotEmpty() },
                    email = email?.trim()?.takeIf { it.isNotEmpty() },
                    fotoRutaAbsoluta = fotoRutaAbsoluta,
                ),
            )
            staffCategoriaDao.setCategoriasForStaff(id, categorias)
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
        categorias: Set<String>,
    ) {
        viewModelScope.launch {
            if (!puedeGestionarStaffEnNube()) return@launch
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
                staffCategoriaDao.setCategoriasForStaff(anterior.id, categorias)
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
            staffCategoriaDao.setCategoriasForStaff(anterior.id, categorias)
        }
    }

    fun eliminar(s: Staff) {
        viewModelScope.launch {
            if (!puedeGestionarStaffEnNube()) return@launch
            s.fotoRutaAbsoluta?.let { runCatching { File(it).delete() } }
            staffDao.delete(s)
        }
    }
}
