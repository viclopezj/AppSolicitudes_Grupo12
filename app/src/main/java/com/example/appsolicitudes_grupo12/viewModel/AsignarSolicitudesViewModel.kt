package com.example.appsolicitudes_grupo12.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import com.example.appsolicitudes_grupo12.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.data.local.UsuarioEntity
import com.example.appsolicitudes_grupo12.data.repository.SolicitudRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AsignarSolicitudesUiState(
    val cargando: Boolean = true,
    val solicitudes: List<SolicitudEntity> = emptyList(),
    val tecnicos: List<UsuarioEntity> = emptyList(),
    val solicitudAsignandoId: Int? = null,
    val error: String? = null,
    val mensaje: String? = null
)

// El administrador ve todas las solicitudes y puede asignarlas a cuentas técnicas.
class AsignarSolicitudesViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    private val rol = savedStateHandle.get<String>("rol").orEmpty()
    private val baseDatos = AppDatabase.getDatabase(app)
    private val repositorio = SolicitudRepository(baseDatos.solicitudDao())
    private val daoUsuarios = baseDatos.usuarioDao()

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

    private fun cargarDatos() {
        viewModelScope.launch {
            try {
                val tecnicos = daoUsuarios.obtenerTecnicos()
                _estado.update { it.copy(tecnicos = tecnicos) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e("AsignarSolicitudesVM", "No se pudieron cargar los técnicos", error)
                _estado.update {
                    it.copy(cargando = false, error = "No se pudieron cargar los técnicos.")
                }
                return@launch
            }

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

    fun descartarAvisos() {
        _estado.update { it.copy(error = null, mensaje = null) }
    }
}
