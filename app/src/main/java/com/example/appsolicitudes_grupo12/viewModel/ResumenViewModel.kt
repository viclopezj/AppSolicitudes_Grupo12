package com.example.appsolicitudes_grupo12.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.model.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud
import com.example.appsolicitudes_grupo12.state.ResumenUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

// ViewModel del panel resumen: cuenta las solicitudes por estado (solo administrador).
class ResumenViewModel(
    private val repositorio: SolicitudRepository,
    private val rol: String
) : ViewModel() {

    private val _estado = MutableStateFlow<ResumenUiState>(ResumenUiState.Loading)
    val estado: StateFlow<ResumenUiState> = _estado.asStateFlow()

    // Lectura en curso, para cancelarla antes de reintentar
    private var trabajoLectura: Job? = null

    init {
        cargarResumen()
    }

    // ---------- Lectura ----------

    // Lee todas las solicitudes y cuenta por estado. También la usa "Reintentar".
    fun cargarResumen() {
        if (rol != "administrador") {
            _estado.value = ResumenUiState.Error("Solo el administrador puede ver el panel resumen.")
            return
        }

        trabajoLectura?.cancel()
        _estado.value = ResumenUiState.Loading

        trabajoLectura = viewModelScope.launch {
            repositorio.obtenerTodasParaAdministracion()
                .catch { error ->
                    Log.e("ResumenVM", "Error al leer las solicitudes", error)
                    _estado.value = ResumenUiState.Error(
                        "No se pudo cargar el resumen. Inténtalo de nuevo."
                    )
                }
                .collect { solicitudes ->
                    _estado.value = ResumenUiState.Success(
                        total = solicitudes.size,
                        porEstado = EstadoSolicitud.entries.map { estado ->
                            estado to solicitudes.count { it.estado == estado.texto }
                        }
                    )
                }
        }
    }

    // ---------- Fábrica ----------

    companion object {
        fun factory(context: Context, rol: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val baseDatos = AppDatabase.getDatabase(context)
                    return ResumenViewModel(SolicitudRepository(baseDatos), rol) as T
                }
            }
    }
}