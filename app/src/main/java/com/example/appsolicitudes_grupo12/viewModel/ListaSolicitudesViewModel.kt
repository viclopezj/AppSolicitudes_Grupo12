package com.example.appsolicitudes_grupo12.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.model.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.state.FiltroSolicitudes
import com.example.appsolicitudes_grupo12.state.ListaSolicitudesUiState
import com.example.appsolicitudes_grupo12.state.OrdenSolicitudes
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel de la lista: lee las solicitudes visibles para el usuario, aplica filtro y orden,
// y las convierte en Loading, Success, Empty o Error.
class ListaSolicitudesViewModel(
    private val repositorio: SolicitudRepository,
    private val usuarioId: Int,
    private val rol: String
) : ViewModel() {

    // Parte en Loading mientras llegan los datos
    private val _estado = MutableStateFlow<ListaSolicitudesUiState>(
        ListaSolicitudesUiState.Loading
    )
    val estado: StateFlow<ListaSolicitudesUiState> = _estado.asStateFlow()

    // Filtro y orden elegidos (sin filtro y más recientes primero al inicio)
    private val _filtro = MutableStateFlow(FiltroSolicitudes())

    // Lectura en curso, para cancelarla antes de reintentar
    private var trabajoLectura: Job? = null

    init {
        cargarSolicitudes()
    }

    // ---------- Lectura ----------

    // Lee las solicitudes de Room. También la usa el botón "Reintentar".
    fun cargarSolicitudes() {
        trabajoLectura?.cancel()
        _estado.value = ListaSolicitudesUiState.Loading

        trabajoLectura = viewModelScope.launch {
            // combine avisa cuando cambia la lista de Room o el filtro
            combine(repositorio.obtenerSolicitudes(usuarioId, rol), _filtro) { lista, filtro ->
                lista to filtro
            }
                .catch { error ->
                    Log.e("ListaSolicitudesVM", "Error al leer las solicitudes", error)
                    _estado.value = ListaSolicitudesUiState.Error(
                        "No se pudieron cargar las solicitudes. Inténtalo de nuevo."
                    )
                }
                .collect { (lista, filtro) ->
                    _estado.value = if (lista.isEmpty()) {
                        ListaSolicitudesUiState.Empty
                    } else {
                        ListaSolicitudesUiState.Success(
                            solicitudes = aplicarFiltro(lista, filtro),
                            filtro = filtro
                        )
                    }
                }
        }
    }

    // ---------- Eventos de los filtros ----------

    // Filtra por categoría. null = todas.
    fun onCategoriaFiltroChange(categoria: String?) {
        _filtro.update { it.copy(categoria = categoria) }
    }

    // Cambia el orden por fecha.
    fun onOrdenChange(orden: OrdenSolicitudes) {
        _filtro.update { it.copy(orden = orden) }
    }

    // ---------- Reglas del filtrado ----------

    // Deja solo la categoría elegida (si hay) y ordena por fecha.
    private fun aplicarFiltro(
        lista: List<SolicitudEntity>,
        filtro: FiltroSolicitudes
    ): List<SolicitudEntity> {
        val categoria = filtro.categoria

        val filtradas = if (categoria == null) {
            lista
        } else {
            lista.filter { it.categoria == categoria }
        }

        return when (filtro.orden) {
            OrdenSolicitudes.RECIENTES -> filtradas.sortedByDescending { it.fechaCreacion }
            OrdenSolicitudes.ANTIGUAS -> filtradas.sortedBy { it.fechaCreacion }
        }
    }

    // ---------- Fábrica ----------

    companion object {
        fun factory(context: Context, usuarioId: Int, rol: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dao = AppDatabase.getDatabase(context).solicitudDao()
                    return ListaSolicitudesViewModel(SolicitudRepository(dao), usuarioId, rol) as T
                }
            }
    }
}