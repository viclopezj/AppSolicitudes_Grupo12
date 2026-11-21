package com.example.appsolicitudes_grupo12.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.model.data.files.EvidenciaArchivos
import com.example.appsolicitudes_grupo12.model.data.files.EvidenciaInvalidaException
import com.example.appsolicitudes_grupo12.model.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.model.data.repository.BorradorRepository
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.model.solicitud.AdjuntoPendiente
import com.example.appsolicitudes_grupo12.model.solicitud.ValidadorSolicitud
import com.example.appsolicitudes_grupo12.state.FormularioErrores
import com.example.appsolicitudes_grupo12.state.FormularioUiState
import com.example.appsolicitudes_grupo12.model.aviso.NotificadorAndroid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// ViewModel del formulario: guarda el estado, valida los datos, maneja la foto, los adjuntos y el
// borrador, y pide el guardado. "contexto" solo se usa para crear y copiar los archivos.
class FormularioViewModel(
    private val repositorio: SolicitudRepository,
    private val borradores: BorradorRepository,
    private val contexto: Context,
    private val usuarioId: Int,
    private val rol: String
) : ViewModel() {

    private val _estado = MutableStateFlow(FormularioUiState())
    val estado: StateFlow<FormularioUiState> = _estado.asStateFlow()

    init {
        if (usuarioId > 0 && rol == "cliente") {
            cargarBorrador()
            cargarContacto()
        }
    }

    // ---------- Borrador y datos de contacto ----------

    // Si el cliente dejó un borrador guardado, rellena los campos con él.
    private fun cargarBorrador() {
        viewModelScope.launch {
            borradores.obtener(usuarioId)
                .onSuccess { borrador ->
                    if (borrador != null) {
                        _estado.update {
                            it.copy(
                                titulo = borrador.titulo,
                                descripcion = borrador.descripcion,
                                categoria = borrador.categoria,
                                servicio = borrador.servicio,
                                hayBorrador = true,
                                mensaje = "Se recuperó tu borrador."
                            )
                        }
                    }
                }
                .onFailure { Log.e("FormularioViewModel", "Error al leer el borrador", it) }
        }
    }

    // Empresa y teléfono ficticios del cliente (se muestran como datos de contacto).
    private fun cargarContacto() {
        viewModelScope.launch {
            repositorio.obtenerUsuario(usuarioId)
                .onSuccess { usuario ->
                    if (usuario != null) {
                        _estado.update {
                            it.copy(empresa = usuario.empresa, telefono = usuario.telefono)
                        }
                    }
                }
                .onFailure { Log.e("FormularioViewModel", "Error al leer los datos del cliente", it) }
        }
    }

    // Guarda el texto del formulario como borrador (sin validar). Las fotos y los archivos no se guardan.
    fun guardarBorrador() {
        if (usuarioId <= 0 || rol != "cliente") return
        val datos = _estado.value
        if (datos.guardando) return

        if (datos.titulo.isBlank() && datos.descripcion.isBlank() &&
            datos.categoria.isBlank() && datos.servicio.isBlank()
        ) {
            _estado.update { it.copy(mensaje = "Escribe algo para guardar el borrador.") }
            return
        }

        viewModelScope.launch {
            borradores.guardar(
                usuarioId = usuarioId,
                titulo = datos.titulo,
                descripcion = datos.descripcion,
                categoria = datos.categoria,
                servicio = datos.servicio
            )
                .onSuccess {
                    _estado.update {
                        it.copy(
                            hayBorrador = true,
                            mensaje = "Borrador guardado (solo el texto; las fotos y los archivos no se guardan)."
                        )
                    }
                }
                .onFailure { error ->
                    Log.e("FormularioViewModel", "Error al guardar el borrador", error)
                    _estado.update {
                        it.copy(errorGuardado = "No se pudo guardar el borrador. Inténtalo de nuevo.")
                    }
                }
        }
    }

    // Borra el borrador guardado y vacía los campos de texto.
    fun descartarBorrador() {
        viewModelScope.launch {
            borradores.eliminar(usuarioId)
                .onSuccess {
                    _estado.update {
                        it.copy(
                            titulo = "",
                            descripcion = "",
                            categoria = "",
                            servicio = "",
                            errores = FormularioErrores(),
                            hayBorrador = false,
                            mensaje = "Borrador descartado."
                        )
                    }
                }
                .onFailure { error ->
                    Log.e("FormularioViewModel", "Error al descartar el borrador", error)
                    _estado.update {
                        it.copy(errorGuardado = "No se pudo descartar el borrador. Inténtalo de nuevo.")
                    }
                }
        }
    }

    // Quita el aviso breve cuando ya se mostró.
    fun descartarMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }

    // ---------- Imágenes y documentos adicionales ----------

    // La persona eligió un archivo: se copia a la app en segundo plano y se suma a la lista.
    fun onAdjuntoElegido(uri: Uri) {
        val actual = _estado.value
        if (actual.cargandoAdjunto) return
        if (actual.adjuntos.size >= EvidenciaArchivos.MAXIMO_ADJUNTOS) {
            _estado.update { it.copy(errorAdjunto = EvidenciaArchivos.MENSAJE_MAXIMO_ADJUNTOS) }
            return
        }

        _estado.update { it.copy(cargandoAdjunto = true, errorAdjunto = null) }

        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                EvidenciaArchivos.copiarAdjunto(contexto, uri)
            }

            resultado
                .onSuccess { adjunto ->
                    _estado.update {
                        it.copy(adjuntos = it.adjuntos + adjunto, cargandoAdjunto = false)
                    }
                }
                .onFailure { error ->
                    _estado.update {
                        it.copy(
                            cargandoAdjunto = false,
                            errorAdjunto = (error as? EvidenciaInvalidaException)?.message
                                ?: "No se pudo cargar el archivo. Prueba con otro."
                        )
                    }
                }
        }
    }

    // Quita un archivo de la lista y borra su copia.
    fun quitarAdjunto(adjunto: AdjuntoPendiente) {
        EvidenciaArchivos.borrar(adjunto.uri)
        _estado.update { it.copy(adjuntos = it.adjuntos - adjunto, errorAdjunto = null) }
    }

    // Quita el error del adjunto cuando ya se mostró.
    fun descartarErrorAdjunto() {
        _estado.update { it.copy(errorAdjunto = null) }
    }

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
        _estado.update {
            it.copy(categoria = valor, errores = it.errores.copy(categoria = null))
        }
    }

    fun onServicioChange(valor: String) {
        _estado.update { it.copy(servicio = valor) }
    }

    // ---------- Eventos de la foto de evidencia ----------

    // Entrega un archivo vacío donde la cámara escribirá la foto.
    fun crearArchivoParaCamara(): File {
        return EvidenciaArchivos.crearArchivoNuevo(contexto)
    }

    // La cámara terminó: se comprueba que la foto exista y se deja en el estado.
    fun onFotoTomada(archivo: File) {
        if (archivo.exists() && archivo.length() > 0) {
            if (EvidenciaArchivos.excedeTamano(archivo)) {
                archivo.delete()
                _estado.update { it.copy(errorFoto = EvidenciaArchivos.MENSAJE_TAMANO) }
            } else {
                usarFoto(EvidenciaArchivos.aTexto(archivo))
            }
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
            val resultado = withContext(Dispatchers.IO) {
                EvidenciaArchivos.copiarDeGaleria(contexto, uri)
            }

            resultado
                .onSuccess { archivo -> usarFoto(EvidenciaArchivos.aTexto(archivo)) }
                .onFailure { error ->
                    _estado.update {
                        it.copy(
                            cargandoFoto = false,
                            errorFoto = (error as? EvidenciaInvalidaException)?.message
                                ?: "No se pudo cargar la imagen. Prueba con otra."
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
            categoria = actual.categoria
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
        if (_estado.value.guardando || _estado.value.cargandoFoto || _estado.value.cargandoAdjunto) return
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
                evidenciaUri = datos.evidenciaUri,
                adjuntos = datos.adjuntos
            )
                .onSuccess { solicitud ->
                    // Ya quedó registrada: el borrador deja de servir
                    borradores.eliminar(usuarioId)
                    // Se guarda la solicitud creada para mostrar su código y su QR
                    _estado.update { estadoActual ->
                        estadoActual.copy(
                            guardando = false,
                            guardadoExitosamente = true,
                            solicitudCreada = solicitud,
                            hayBorrador = false
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

    // Deja el formulario vacío. La foto y los adjuntos NO se borran: ya quedaron asociados a la solicitud.
    fun reiniciarFormulario() {
        val actual = _estado.value
        _estado.value = FormularioUiState(empresa = actual.empresa, telefono = actual.telefono)
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
            actual.adjuntos.forEach { EvidenciaArchivos.borrar(it.uri) }
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
                    val baseDatos = AppDatabase.getDatabase(appContext)
                    return FormularioViewModel(
                        SolicitudRepository(baseDatos, notificador = NotificadorAndroid(appContext)),
                        BorradorRepository(baseDatos.borradorDao()),
                        appContext, usuarioId, rol
                    ) as T
                }
            }
    }
}