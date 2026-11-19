package com.example.appsolicitudes_grupo12.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.model.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.model.data.repository.UsuarioRepository
import com.example.appsolicitudes_grupo12.state.AsignarSolicitudesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel de asignaciones: el administrador ve todas las solicitudes y las asigna a técnicos.
class AsignarSolicitudesViewModel(
    private val repositorio: SolicitudRepository,
    private val repositorioUsuarios: UsuarioRepository,
    private val rol: String
) : ViewModel() {

    private val _estado = MutableStateFlow(AsignarSolicitudesUiState())
    val estado: StateFlow<AsignarSolicitudesUiState> = _estado.asStateFlow()

    init {
        if (rol != "administrador") {
            _estado.value = AsignarSolicitudesUiState(
                cargando = false,
                error = "Solo el administrador puede asignar solicitudes."
            )
        } else {
            cargarDatos()
        }
    }

    // ---------- Lectura ----------

    // Carga los técnicos y luego se queda escuchando las solicitudes.
    private fun cargarDatos() {
        viewModelScope.launch {
            val tecnicos = repositorioUsuarios.obtenerTecnicos().getOrElse { error ->
                Log.e("AsignarSolicitudesVM", "No se pudieron cargar los técnicos", error)
                _estado.update {
                    it.copy(cargando = false, error = "No se pudieron cargar los técnicos.")
                }
                return@launch
            }
            _estado.update { it.copy(tecnicos = tecnicos) }

            repositorio.obtenerTodasParaAdministracion()
                .catch { error ->
                    Log.e("AsignarSolicitudesVM", "No se pudieron cargar las solicitudes", error)
                    _estado.update {
                        it.copy(cargando = false, error = "No se pudieron cargar las solicitudes.")
                    }
                }
                .collect { solicitudes ->
                    _estado.update { it.copy(cargando = false, solicitudes = solicitudes) }
                }
        }
    }

    // ---------- Asignación ----------

    // Asigna la solicitud al técnico elegido.
    fun asignar(solicitudId: Int, tecnicoId: Int) {
        if (rol != "administrador" || _estado.value.solicitudAsignandoId != null) return
        _estado.update {
            it.copy(solicitudAsignandoId = solicitudId, error = null, mensaje = null)
        }

        viewModelScope.launch {
            repositorio.asignarTecnico(solicitudId, tecnicoId)
                .onSuccess {
                    _estado.update {
                        it.copy(
                            solicitudAsignandoId = null,
                            mensaje = "Solicitud asignada al técnico."
                        )
                    }
                }
                .onFailure { error ->
                    Log.e("AsignarSolicitudesVM", "No se pudo asignar la solicitud", error)
                    _estado.update {
                        it.copy(
                            solicitudAsignandoId = null,
                            error = "No se pudo asignar la solicitud. Inténtalo de nuevo."
                        )
                    }
                }
        }
    }

    // ---------- Utilidades para la pantalla ----------

    // Quita el error y el mensaje de éxito.
    fun descartarAvisos() {
        _estado.update { it.copy(error = null, mensaje = null) }
    }

    // ---------- Fábrica ----------

    companion object {
        fun factory(context: Context, rol: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val baseDatos = AppDatabase.getDatabase(context)
                    return AsignarSolicitudesViewModel(
                        SolicitudRepository(baseDatos.solicitudDao()),
                        UsuarioRepository(baseDatos.usuarioDao()),
                        rol
                    ) as T
                }
            }
    }
}