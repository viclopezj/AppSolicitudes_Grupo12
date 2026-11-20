package com.example.appsolicitudes_grupo12.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.model.data.files.EvidenciaArchivos
import com.example.appsolicitudes_grupo12.model.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudNoEncontradaException
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.model.data.repository.TransicionNoPermitidaException
import com.example.appsolicitudes_grupo12.state.AvanceUiState
import com.example.appsolicitudes_grupo12.model.aviso.NotificadorAndroid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// ViewModel del formulario de avances del técnico: texto, interruptor "interno" y foto opcional.
class AvanceSolicitudViewModel(
    private val repositorio: SolicitudRepository,
    private val contexto: Context,
    private val id: Int,
    private val usuarioId: Int,
    private val rol: String
) : ViewModel() {

    private val _estado = MutableStateFlow(AvanceUiState())
    val estado: StateFlow<AvanceUiState> = _estado.asStateFlow()

    // ---------- Eventos de los campos ----------

    fun onTextoChange(valor: String) {
        _estado.update { it.copy(texto = valor, errorTexto = null, mensaje = null) }
    }

    fun onInternoChange(valor: Boolean) {
        _estado.update { it.copy(esInterno = valor) }
    }

    // ---------- Eventos de la foto de evidencia ----------

    // Entrega un archivo vacío donde la cámara escribirá la foto.
    fun crearArchivoParaCamara(): File {
        return EvidenciaArchivos.crearArchivoNuevo(contexto)
    }

    // La cámara terminó: se comprueba que la foto exista y se deja en el estado.
    fun onFotoTomada(archivo: File) {
        if (archivo.exists() && archivo.length() > 0) {
            usarFoto(EvidenciaArchivos.aTexto(archivo))
        } else {
            archivo.delete()
            _estado.update { it.copy(error = "No se pudo guardar la foto. Inténtalo de nuevo.") }
        }
    }

    // La persona cerró la cámara sin foto: se borra el archivo vacío.
    fun onFotoCancelada(archivo: File) {
        archivo.delete()
    }

    // Foto elegida de la galería: se copia a un archivo propio de la app en segundo plano.
    fun onFotoElegida(uri: Uri) {
        if (_estado.value.cargandoFoto) return
        _estado.update { it.copy(cargandoFoto = true, error = null) }

        viewModelScope.launch {
            val archivo = withContext(Dispatchers.IO) {
                EvidenciaArchivos.copiarDeGaleria(contexto, uri)
            }
            if (archivo != null) {
                usarFoto(EvidenciaArchivos.aTexto(archivo))
            } else {
                _estado.update {
                    it.copy(
                        cargandoFoto = false,
                        error = "No se pudo cargar la imagen. Prueba con otra."
                    )
                }
            }
        }
    }

    fun onPermisoCamaraDenegado() {
        _estado.update { it.copy(error = "Se necesita el permiso de cámara para tomar la foto.") }
    }

    // Quita la foto elegida y borra su archivo.
    fun quitarFoto() {
        EvidenciaArchivos.borrar(_estado.value.evidenciaUri)
        _estado.update { it.copy(evidenciaUri = null, error = null) }
    }

    // Deja la foto nueva en el estado y borra la anterior si había otra.
    private fun usarFoto(uriTexto: String) {
        val anterior = _estado.value.evidenciaUri
        if (anterior != null && anterior != uriTexto) {
            EvidenciaArchivos.borrar(anterior)
        }
        _estado.update { it.copy(evidenciaUri = uriTexto, cargandoFoto = false, error = null) }
    }

    // ---------- Envío ----------

    // Valida y guarda el avance. onExito avisa a la pantalla para que refresque la solicitud.
    fun enviar(onExito: () -> Unit) {
        val actual = _estado.value
        if (actual.enviando || actual.cargandoFoto) return

        if (actual.texto.isBlank()) {
            _estado.update { it.copy(errorTexto = "Escribe el avance o comentario") }
            return
        }

        _estado.update { it.copy(enviando = true, error = null, mensaje = null) }

        viewModelScope.launch {
            repositorio.agregarComentario(
                id = id,
                usuarioId = usuarioId,
                rol = rol,
                texto = actual.texto,
                esInterno = actual.esInterno,
                evidenciaUri = actual.evidenciaUri
            )
                .onSuccess {
                    // La foto ya quedó en el comentario: se limpia el formulario sin borrarla
                    _estado.value = AvanceUiState(
                        esInterno = actual.esInterno,
                        mensaje = "Avance registrado."
                    )
                    onExito()
                }
                .onFailure { error ->
                    val mensaje = when (error) {
                        is SolicitudNoEncontradaException -> "La solicitud ya no existe."
                        is TransicionNoPermitidaException ->
                            "Ya no puedes registrar avances en este estado."

                        else -> {
                            Log.e("AvanceSolicitudVM", "Error al registrar el avance", error)
                            "No se pudo registrar el avance. Inténtalo de nuevo."
                        }
                    }
                    _estado.update { it.copy(enviando = false, error = mensaje) }
                }
        }
    }

    // Si se sale sin enviar, se borra la foto que quedó sin usar.
    override fun onCleared() {
        EvidenciaArchivos.borrar(_estado.value.evidenciaUri)
    }

    // ---------- Fábrica ----------

    companion object {
        fun factory(
            context: Context,
            id: Int,
            usuarioId: Int,
            rol: String
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val appContext = context.applicationContext
                    val baseDatos = AppDatabase.getDatabase(appContext)
                    return AvanceSolicitudViewModel(
                        SolicitudRepository(baseDatos, notificador = NotificadorAndroid(appContext)),
                        appContext,
                        id, usuarioId, rol
                    ) as T
                }
            }
    }
}