package com.escuelafutbol.academia.ui.categoria

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escuelafutbol.academia.data.local.dao.CategoriaDao
import com.escuelafutbol.academia.data.local.dao.JugadorDao
import com.escuelafutbol.academia.data.local.entity.Categoria
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriaPickerViewModel(
    application: Application,
    private val categoriaDao: CategoriaDao,
    jugadorDao: JugadorDao,
) : AndroidViewModel(application) {

    val categoriasUi = combine(
        categoriaDao.observeAllOrdered(),
        jugadorDao.observeCategorias(),
    ) { desdeTabla, desdeJugadores ->
        fun Categoria.portadaScore(): Int = when {
            !portadaUrlSupabase.isNullOrBlank() -> 2
            portadaRutaAbsoluta != null && File(portadaRutaAbsoluta).exists() -> 1
            else -> 0
        }
        fun claveNormalizada(nombre: String): String =
            nombre.trim().lowercase(Locale.ROOT)

        val map = mutableMapOf<String, Categoria>()
        for (c in desdeTabla) {
            val key = claveNormalizada(c.nombre)
            if (key.isEmpty()) continue
            val prev = map[key]
            if (prev == null || c.portadaScore() > prev.portadaScore()) {
                map[key] = c
            }
        }
        for (jn in desdeJugadores) {
            val key = claveNormalizada(jn)
            if (key.isEmpty()) continue
            if (!map.containsKey(key)) {
                val display = jn.trim()
                map[key] = Categoria(nombre = display)
            }
        }
        map.values.sortedWith(compareBy { it.nombre.lowercase(Locale.ROOT) })
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun agregarCategoria(nombre: String) {
        viewModelScope.launch {
            val n = nombre.trim()
            if (n.isNotEmpty()) {
                categoriaDao.insert(Categoria(nombre = n))
            }
        }
    }

    fun guardarPortadaCategoria(nombreCategoria: String, uri: Uri) {
        viewModelScope.launch {
            val n = nombreCategoria.trim()
            if (n.isEmpty()) return@launch
            val app = getApplication<Application>()
            withContext(Dispatchers.IO) {
                val dir = File(app.filesDir, "categoria_portadas").apply { mkdirs() }
                val safe = n.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(48)
                val dest = File(dir, "${safe}_${UUID.randomUUID()}.jpg")
                runCatching {
                    app.contentResolver.openInputStream(uri)?.use { input ->
                        dest.outputStream().use { out -> input.copyTo(out) }
                    }
                }
                if (!dest.exists() || dest.length() == 0L) return@withContext
                val existing = categoriaDao.getByNombre(n)
                if (existing == null) {
                    categoriaDao.insert(Categoria(nombre = n))
                }
                val current = categoriaDao.getByNombre(n) ?: return@withContext
                current.portadaRutaAbsoluta?.let { old ->
                    if (old != dest.absolutePath) runCatching { File(old).delete() }
                }
                categoriaDao.update(
                    current.copy(
                        portadaRutaAbsoluta = dest.absolutePath,
                        portadaUrlSupabase = null,
                    ),
                )
            }
        }
    }

    fun quitarPortadaCategoria(nombreCategoria: String) {
        viewModelScope.launch {
            val n = nombreCategoria.trim()
            if (n.isEmpty()) return@launch
            val c = categoriaDao.getByNombre(n) ?: return@launch
            c.portadaRutaAbsoluta?.let { runCatching { File(it).delete() } }
            categoriaDao.update(
                c.copy(portadaRutaAbsoluta = null, portadaUrlSupabase = null),
            )
        }
    }
}
