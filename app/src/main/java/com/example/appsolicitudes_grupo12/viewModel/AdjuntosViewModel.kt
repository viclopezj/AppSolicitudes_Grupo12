package com.example.appsolicitudes_grupo12.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.model.data.files.EvidenciaArchivos
import com.example.appsolicitudes_grupo12.model.data.files.EvidenciaInvalidaException
import com.example.appsolicitudes_grupo12.model.data.local.AdjuntoEntity
import com.example.appsolicitudes_grupo12.model.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudNoEncontradaException
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.model.data.repository.TransicionNoPermitidaException
import com.example.appsolicitudes_grupo12.state.AdjuntosUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ViewModel de los adjuntos de una solicitud en el detalle: lista las imágenes y documentos
// y agrega nuevos (cliente y administrador, solo donde la regla lo permite).
class AdjuntosViewModel(
    private val repositorio: SolicitudRepository,
    private val contexto: Context,
    private val id: Int,
    private val usuarioId: Int,
    private val rol: String
) : ViewModel() {

    private val _adjuntos = MutableStateFlow<List<AdjuntoEntity>>(emptyList())
    val adjuntos: StateFlow<List<AdjuntoEntity>> = _adjuntos.asStateFlow()

    private val _estado = MutableStateFlow(AdjuntosUiState())
    val estado: StateFlow<AdjuntosUiState> = _estado.asStateFlow()

    init {
        // Se actualiza sola cuando cambia Room. Si no hay acceso, llega vacía.
        viewModelScope.launch {
            repositorio.obtenerAdjuntos(id, usuarioId, rol)
                .catch { Log.e("AdjuntosVM", "Error al leer los adjuntos", it) }
                .collect { _adjuntos.value = it }
        }
    }

    // La persona eligió un archivo: se copia a la app en segundo plano y se guarda en la solicitud.
    fun onArchivoElegido(uri: Uri, alAgregar: () -> Unit) {
        if (_estado.value.cargando) return
        _estado.update { it.copy(cargando = true, error = null, mensaje = null) }

        viewModelScope.launch {
            val copia = withContext(Dispatchers.IO) {
                EvidenciaArchivos.copiarAdjunto(contexto, uri)
            }

            val adjunto = copia.getOrNull()
            if (adjunto == null) {
                val error = copia.exceptionOrNull()
                _estado.update {
                    it.copy(
                        cargando = false,
                        error = (error as? EvidenciaInvalidaException)?.message
                            ?: "No se pudo cargar el archivo. Prueba con otro."
                    )
                }
                return@launch
            }

            repositorio.agregarAdjunto(id, usuarioId, rol, adjunto)
                .onSuccess {
                    _estado.update { it.copy(cargando = false, mensaje = "Archivo adjuntado.") }
                    alAgregar()
                }
                .onFailure { error ->
                    // No quedó asociado a la solicitud: se borra la copia
                    EvidenciaArchivos.borrar(adjunto.uri)
                    val mensaje = when (error) {
                        is TransicionNoPermitidaException ->
                            "En este estado ya no se pueden agregar archivos."

                        is SolicitudNoEncontradaException -> "La solicitud ya no existe."
                        else -> {
                            Log.e("AdjuntosVM", "Error al guardar el adjunto", error)
                            "No se pudo guardar el archivo. Inténtalo de nuevo."
                        }
                    }
                    _estado.update { it.copy(cargando = false, error = mensaje) }
                }
        }
    }

    // Archivo que no se pudo abrir (no existe o no hay una app que lo abra).
    fun avisarNoSePudoAbrir() {
        _estado.update { it.copy(error = "No se pudo abrir el archivo.", mensaje = null) }
    }

    // ---------- Fábrica ----------

    companion object {
        fun factory(context: Context, id: Int, usuarioId: Int, rol: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val appContext = context.applicationContext
                    val baseDatos = AppDatabase.getDatabase(appContext)
                    return AdjuntosViewModel(
                        SolicitudRepository(baseDatos),
                        appContext, id, usuarioId, rol
                    ) as T
                }
            }
    }
}
