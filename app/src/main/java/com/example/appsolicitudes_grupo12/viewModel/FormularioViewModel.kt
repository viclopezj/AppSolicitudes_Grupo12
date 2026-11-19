package com.example.appsolicitudes_grupo12.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.model.data.files.EvidenciaArchivos
import com.example.appsolicitudes_grupo12.model.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.model.solicitud.ValidadorSolicitud
import com.example.appsolicitudes_grupo12.state.FormularioErrores
import com.example.appsolicitudes_grupo12.state.FormularioUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// ViewModel del formulario: guarda el estado, valida los datos, maneja la foto y pide el guardado.
// "contexto" solo se usa para crear y copiar los archivos de las fotos.
class FormularioViewModel(
    private val repositorio: SolicitudRepository,
    private val contexto: Context,
    private val usuarioId: Int,
    private val rol: String
) : ViewModel() {

    private val _estado = MutableStateFlow(FormularioUiState())
    val estado: StateFlow<FormularioUiState> = _estado.asStateFlow()

    // ---------- Eventos de los campos ----------
    // Cada función actualiza el valor y borra el error de ese campo.

    fun onTituloChange(valor: String) {
        _estado.update {
            it.copy(titulo = valor, errores = it.errores.copy(titulo = null))
        }
    }

    fun onDescripcionChange(valor: String) {
        _estado.update {
            it.copy(descripcion = valor, errores = it.errores.copy(descripcion = null))
        }
    }

    fun onCategoriaChange(valor: String) {
        // También limpia el error del servicio, porque su obligatoriedad depende de la categoría
        _estado.update {
            it.copy(
                categoria = valor,
                errores = it.errores.copy(categoria = null, servicio = null)
            )
        }
    }

    fun onServicioChange(valor: String) {
        _estado.update {
            it.copy(servicio = valor, errores = it.errores.copy(servicio = null))
        }
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
            _estado.update {
                it.copy(errorFoto = "No se pudo guardar la foto. Inténtalo de nuevo.")
            }
        }
    }

    // La persona cerró la cámara sin foto: se borra el archivo vacío.
    fun onFotoCancelada(archivo: File) {
        archivo.delete()
    }

    // La persona eligió una imagen de la galería: se copia a un archivo propio de la app.
    fun onFotoElegida(uri: Uri) {
        // Evita copiar dos veces
        if (_estado.value.cargandoFoto) return

        _estado.update { it.copy(cargandoFoto = true, errorFoto = null) }

        viewModelScope.launch {
            // La copia se hace en segundo plano para no congelar la pantalla
            val archivo = withContext(Dispatchers.IO) {
                EvidenciaArchivos.copiarDeGaleria(contexto, uri)
            }

            if (archivo != null) {
                usarFoto(EvidenciaArchivos.aTexto(archivo))
            } else {
                _estado.update {
                    it.copy(
                        cargandoFoto = false,
                        errorFoto = "No se pudo cargar la imagen. Prueba con otra."
                    )
                }
            }
        }
    }

    // No hay permiso de cámara: se avisa con un mensaje.
    fun onPermisoCamaraDenegado() {
        _estado.update {
            it.copy(errorFoto = "Se necesita el permiso de cámara para tomar la foto.")
        }
    }

    // Quita la foto elegida y borra su archivo.
    fun quitarFoto() {
        EvidenciaArchivos.borrar(_estado.value.evidenciaUri)
        _estado.update { it.copy(evidenciaUri = null, errorFoto = null) }
    }

    // Quita el mensaje de error de la foto cuando ya se mostró.
    fun descartarErrorFoto() {
        _estado.update { it.copy(errorFoto = null) }
    }

    // Deja la foto nueva en el estado y borra la anterior si había otra.
    private fun usarFoto(uriTexto: String) {
        val anterior = _estado.value.evidenciaUri
        if (anterior != null && anterior != uriTexto) {
            EvidenciaArchivos.borrar(anterior)
        }
        _estado.update {
            it.copy(evidenciaUri = uriTexto, cargandoFoto = false, errorFoto = null)
        }
    }

    // ---------- Validación ----------

    // Revisa los campos y guarda los errores en el estado. Devuelve true si todo es válido.
    // La foto es opcional, así que no se valida.
    private fun validarFormulario(): Boolean {
        val actual = _estado.value

        val errores = ValidadorSolicitud.validar(
            titulo = actual.titulo,
            descripcion = actual.descripcion,
            categoria = actual.categoria,
            servicio = actual.servicio
        )

        _estado.update { it.copy(errores = errores) }

        return errores == FormularioErrores()
    }

    // ---------- Guardado ----------

    // Valida y, si todo está bien, guarda la solicitud.
    fun guardar() {
        if (usuarioId <= 0 || rol != "cliente") {
            _estado.update {
                it.copy(errorGuardado = "Solo un cliente autenticado puede registrar solicitudes.")
            }
            return
        }

        // Evita guardar dos veces o mientras se copia una foto
        if (_estado.value.guardando || _estado.value.cargandoFoto) return
        if (!validarFormulario()) return

        _estado.update { it.copy(guardando = true, errorGuardado = null) }

        viewModelScope.launch {
            val datos = _estado.value

            repositorio.guardarSolicitud(
                titulo = datos.titulo,
                descripcion = datos.descripcion,
                categoria = datos.categoria,
                servicio = datos.servicio,
                creadorUsuarioId = usuarioId,
                evidenciaUri = datos.evidenciaUri
            )
                .onSuccess { solicitud ->
                    // Se guarda la solicitud creada para mostrar su código y su QR
                    _estado.update { estadoActual ->
                        estadoActual.copy(
                            guardando = false,
                            guardadoExitosamente = true,
                            solicitudCreada = solicitud
                        )
                    }
                }
                .onFailure { error ->
                    Log.e("FormularioViewModel", "Error al guardar la solicitud", error)
                    _estado.update { estadoActual ->
                        estadoActual.copy(
                            guardando = false,
                            errorGuardado = "No se pudo guardar la solicitud. Inténtalo de nuevo."
                        )
                    }
                }
        }
    }

    // ---------- Utilidades para la pantalla ----------

    // Deja el formulario vacío. La foto NO se borra: ya quedó asociada a la solicitud.
    fun reiniciarFormulario() {
        _estado.value = FormularioUiState()
    }

    // Quita el mensaje de error de guardado.
    fun descartarErrorGuardado() {
        _estado.update { it.copy(errorGuardado = null) }
    }

    // Si el formulario se cierra sin registrar, se borra la foto para no dejar archivos huérfanos.
    override fun onCleared() {
        val actual = _estado.value
        if (!actual.guardadoExitosamente) {
            EvidenciaArchivos.borrar(actual.evidenciaUri)
        }
        super.onCleared()
    }

    // ---------- Fábrica ----------

    companion object {
        fun factory(context: Context, usuarioId: Int, rol: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    // applicationContext evita retener la pantalla en memoria
                    val appContext = context.applicationContext
                    val dao = AppDatabase.getDatabase(appContext).solicitudDao()
                    return FormularioViewModel(
                        SolicitudRepository(dao), appContext, usuarioId, rol
                    ) as T
                }
            }
    }
}