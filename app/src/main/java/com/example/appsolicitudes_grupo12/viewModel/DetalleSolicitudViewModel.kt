package com.example.appsolicitudes_grupo12.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.model.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.model.data.local.ComentarioSolicitudEntity
import com.example.appsolicitudes_grupo12.model.data.local.HistorialSolicitudEntity
import kotlinx.coroutines.flow.catch
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudNoEncontradaException
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.model.solicitud.ValidadorSolicitud
import com.example.appsolicitudes_grupo12.state.DetalleSolicitudUiState
import com.example.appsolicitudes_grupo12.state.EdicionSolicitud
import com.example.appsolicitudes_grupo12.state.FormularioErrores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.appsolicitudes_grupo12.model.data.repository.MotivoObligatorioException
import com.example.appsolicitudes_grupo12.model.data.repository.TransicionNoPermitidaException
import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.ReglasTransicion
import com.example.appsolicitudes_grupo12.state.AccionesUiState
import kotlinx.coroutines.flow.update
import com.example.appsolicitudes_grupo12.model.solicitud.PrioridadSolicitud
import com.example.appsolicitudes_grupo12.model.aviso.NotificadorAndroid
import com.example.appsolicitudes_grupo12.model.aviso.Avisos
import com.example.appsolicitudes_grupo12.model.aviso.Notificador
import com.example.appsolicitudes_grupo12.model.aviso.NotificadorNulo
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.data.local.UsuarioEntity

// ViewModel del detalle: lee UNA solicitud, pasa de lectura a edición, valida y guarda cambios.
class DetalleSolicitudViewModel(
    private val repositorio: SolicitudRepository,
    private val id: Int,
    private val usuarioId: Int,
    private val rol: String,
    private val notificador: Notificador = NotificadorNulo
) : ViewModel() {

    // Parte en Loading mientras llega la solicitud
    private val _estado = MutableStateFlow<DetalleSolicitudUiState>(
        DetalleSolicitudUiState.Loading
    )
    val estado: StateFlow<DetalleSolicitudUiState> = _estado.asStateFlow()

    // Historial y comentarios de la solicitud (se actualizan solos al cambiar Room)
    private val _historial = MutableStateFlow<List<HistorialSolicitudEntity>>(emptyList())
    val historial: StateFlow<List<HistorialSolicitudEntity>> = _historial.asStateFlow()

    private val _comentarios = MutableStateFlow<List<ComentarioSolicitudEntity>>(emptyList())
    val comentarios: StateFlow<List<ComentarioSolicitudEntity>> = _comentarios.asStateFlow()

    // Cliente que registró la solicitud: de ahí salen la empresa y el teléfono ficticios
    private val _cliente = MutableStateFlow<UsuarioEntity?>(null)
    val cliente: StateFlow<UsuarioEntity?> = _cliente.asStateFlow()

    // Estado de las acciones (cambiar estado, etc.)
    private val _acciones = MutableStateFlow(AccionesUiState())
    val acciones: StateFlow<AccionesUiState> = _acciones.asStateFlow()

    // Cliente y administrador editan solo en los estados permitidos
    fun puedeEditar(estadoTexto: String): Boolean {
        val estado = EstadoSolicitud.desdeTexto(estadoTexto) ?: return false
        return ReglasTransicion.puedeEditar(rol, estado)
    }

    // Cliente y administrador eliminan solo solicitudes Cerradas o Canceladas
    fun puedeEliminar(estadoTexto: String): Boolean {
        val estado = EstadoSolicitud.desdeTexto(estadoTexto) ?: return false
        return ReglasTransicion.puedeEliminar(rol, estado)
    }

    init {
        cargarSolicitud()
        observarSeguimiento()
    }

    // ---------- Lectura ----------

    // Lee la solicitud de Room. También la usa el botón "Reintentar".
    fun cargarSolicitud() {
        _estado.value = DetalleSolicitudUiState.Loading

        viewModelScope.launch {
            repositorio.obtenerSolicitud(id, usuarioId, rol)
                .onSuccess { solicitud ->
                    _estado.value = DetalleSolicitudUiState.Success(solicitud)
                    cargarCliente(solicitud.creadoPorUsuarioId)
                }
                .onFailure { error -> mostrarErrorDeLectura(error) }
        }
    }

    // Lee los datos de contacto del cliente (solo si todavía no se leyeron). Si falla, no se muestran.
    private suspend fun cargarCliente(clienteId: Int?) {
        if (clienteId == null || _cliente.value?.id == clienteId) return
        repositorio.obtenerUsuario(clienteId)
            .onSuccess { _cliente.value = it }
            .onFailure { Log.e("DetalleSolicitudVM", "Error al leer los datos del cliente", it) }
    }

    // Si la solicitud no existe -> NoEncontrada; si falló otra cosa -> Error con mensaje amable.
    private fun mostrarErrorDeLectura(error: Throwable) {
        _estado.value = if (error is SolicitudNoEncontradaException) {
            DetalleSolicitudUiState.NoEncontrada
        } else {
            Log.e("DetalleSolicitudVM", "Error al leer la solicitud", error)
            DetalleSolicitudUiState.Error(
                "No se pudo cargar la solicitud. Inténtalo de nuevo."
            )
        }
    }

    // Escucha el historial y los comentarios. Si no hay acceso, llegan vacíos.
    private fun observarSeguimiento() {
        viewModelScope.launch {
            repositorio.obtenerHistorial(id, usuarioId, rol)
                .catch { Log.e("DetalleSolicitudVM", "Error al leer el historial", it) }
                .collect { _historial.value = it }
        }
        viewModelScope.launch {
            repositorio.obtenerComentarios(id, usuarioId, rol)
                .catch { Log.e("DetalleSolicitudVM", "Error al leer los comentarios", it) }
                .collect { _comentarios.value = it }
        }
    }

    // ---------- Modo lectura <-> modo edición ----------

    // Pasa al modo edición con los valores ya guardados.
    fun iniciarEdicion() {
        val actual = _estado.value as? DetalleSolicitudUiState.Success ?: return
        if (!puedeEditar(actual.solicitud.estado)) return
        _estado.value = actual.copy(
            edicion = EdicionSolicitud(
                titulo = actual.solicitud.titulo,
                descripcion = actual.solicitud.descripcion,
                categoria = actual.solicitud.categoria,
                servicio = actual.solicitud.servicio
            )
        )
    }

    // Vuelve al modo lectura sin guardar.
    fun cancelarEdicion() {
        val actual = _estado.value as? DetalleSolicitudUiState.Success ?: return
        _estado.value = actual.copy(edicion = null)
    }

    // ---------- Eventos de los campos ----------
    // Cada función actualiza el valor y borra el error de ese campo.

    fun onTituloChange(valor: String) {
        modificarEdicion {
            it.copy(titulo = valor, errores = it.errores.copy(titulo = null))
        }
    }

    fun onDescripcionChange(valor: String) {
        modificarEdicion {
            it.copy(descripcion = valor, errores = it.errores.copy(descripcion = null))
        }
    }

    fun onCategoriaChange(valor: String) {
        modificarEdicion {
            it.copy(categoria = valor, errores = it.errores.copy(categoria = null))
        }
    }

    fun onServicioChange(valor: String) {
        modificarEdicion { it.copy(servicio = valor) }
    }

    // Aplica un cambio a la edición actual (solo si está en modo edición).
    private fun modificarEdicion(cambio: (EdicionSolicitud) -> EdicionSolicitud) {
        val actual = _estado.value as? DetalleSolicitudUiState.Success ?: return
        val edicion = actual.edicion ?: return
        _estado.value = actual.copy(edicion = cambio(edicion))
    }

    // ---------- Validación ----------

    // Revisa los campos de la edición. Devuelve true si todo es válido.
    private fun validarEdicion(): Boolean {
        val actual = _estado.value as? DetalleSolicitudUiState.Success ?: return false
        val edicion = actual.edicion ?: return false

        val errores = ValidadorSolicitud.validar(
            titulo = edicion.titulo,
            descripcion = edicion.descripcion,
            categoria = edicion.categoria
        )

        modificarEdicion { it.copy(errores = errores) }

        return errores == FormularioErrores()
    }

    // ---------- Guardado ----------

    // Valida y, si todo está bien, guarda los cambios.
    fun guardarCambios() {
        val actual = _estado.value as? DetalleSolicitudUiState.Success ?: return
        val edicion = actual.edicion ?: return

        // Evita guardar dos veces
        if (edicion.guardando) return
        if (!validarEdicion()) return

        modificarEdicion { it.copy(guardando = true, errorGuardado = null) }

        viewModelScope.launch {
            val datos = (_estado.value as? DetalleSolicitudUiState.Success)?.edicion
                ?: return@launch

            repositorio.actualizarSolicitud(
                id = id,
                usuarioId = usuarioId,
                rol = rol,
                titulo = datos.titulo,
                descripcion = datos.descripcion,
                categoria = datos.categoria,
                servicio = datos.servicio
            )
                .onSuccess {
                    // Se vuelve a leer para mostrar exactamente lo guardado
                    cargarDespuesDeGuardar()
                }
                .onFailure { error ->
                    val mensaje = if (error is SolicitudNoEncontradaException) {
                        "La solicitud ya no existe."
                    } else {
                        Log.e("DetalleSolicitudVM", "Error al guardar los cambios", error)
                        "No se pudieron guardar los cambios. Inténtalo de nuevo."
                    }
                    modificarEdicion { it.copy(guardando = false, errorGuardado = mensaje) }
                }
        }
    }

    // Lee la solicitud modificada y vuelve al modo lectura (sin pasar por Loading).
    private suspend fun cargarDespuesDeGuardar() {
        repositorio.obtenerSolicitud(id, usuarioId, rol)
            .onSuccess { solicitud ->
                _estado.value = DetalleSolicitudUiState.Success(solicitud)
            }
            .onFailure { error -> mostrarErrorDeLectura(error) }
    }

    // Vuelve a leer la solicitud sin pasar por Loading (para mostrar la nueva "última actualización").
    fun refrescarSolicitud() {
        viewModelScope.launch { cargarDespuesDeGuardar() }
    }

    // ---------- Acciones por perfil ----------

    // Cambia el estado. "texto" es el mensaje o motivo (si la acción lo pide).
    // Si el cliente responde una solicitud pendiente de información, se guarda su respuesta
    // y el repositorio decide el estado de destino.
    fun cambiarEstado(nuevoEstado: EstadoSolicitud, texto: String?) {
        val estadoActual = (_estado.value as? DetalleSolicitudUiState.Success)?.solicitud?.estado

        if (rol == "cliente" && estadoActual == EstadoSolicitud.PENDIENTE_INFORMACION.texto) {
            ejecutarAccion("Respuesta enviada.") {
                repositorio.responderInformacion(id, usuarioId, rol, texto.orEmpty())
            }
            return
        }

        ejecutarAccion("Estado actualizado a ${nuevoEstado.texto}.") {
            repositorio.cambiarEstado(id, usuarioId, rol, nuevoEstado, texto)
        }
    }

    // Cambia la prioridad (solo administrador).
    fun cambiarPrioridad(prioridad: PrioridadSolicitud) {
        ejecutarAccion("Prioridad actualizada a ${prioridad.texto}.") {
            repositorio.cambiarPrioridad(id, usuarioId, rol, prioridad)
        }
    }

    // Eliminación lógica (solo Cerrada o Cancelada). Si sale bien, avisa a la pantalla para volver.
    fun eliminar(onEliminada: () -> Unit) {
        if (_acciones.value.procesando) return
        _acciones.update { it.copy(procesando = true, error = null, mensaje = null) }

        viewModelScope.launch {
            repositorio.eliminarSolicitud(id, usuarioId, rol)
                .onSuccess {
                    _acciones.update { it.copy(procesando = false) }
                    onEliminada()
                }
                .onFailure { error ->
                    val mensaje = when (error) {
                        is TransicionNoPermitidaException ->
                            "Solo se pueden eliminar solicitudes Cerradas o Canceladas."

                        is SolicitudNoEncontradaException -> "La solicitud ya no existe."
                        else -> {
                            Log.e("DetalleSolicitudVM", "Error al eliminar la solicitud", error)
                            "No se pudo eliminar la solicitud. Inténtalo de nuevo."
                        }
                    }
                    _acciones.update { it.copy(procesando = false, error = mensaje) }
                }
        }
    }

    // WhatsApp simulado (solo cliente): publica el aviso local y confirma con un mensaje breve.
    fun compartirPorWhatsApp(solicitud: SolicitudEntity) {
        if (rol != "cliente") return
        notificador.publicar(Avisos.referenciaCompartida(solicitud.codigo))
        _acciones.update {
            it.copy(error = null, mensaje = "Referencia compartida por WhatsApp.")
        }
    }

    // Ejecuta una acción del repositorio: bloquea los botones, avisa el resultado y recarga.
    private fun ejecutarAccion(mensajeExito: String, accion: suspend () -> Result<Unit>) {
        if (_acciones.value.procesando) return
        _acciones.update { it.copy(procesando = true, error = null, mensaje = null) }

        viewModelScope.launch {
            accion()
                .onSuccess {
                    _acciones.update { it.copy(procesando = false, mensaje = mensajeExito) }
                    cargarDespuesDeGuardar()
                }
                .onFailure { error ->
                    val mensaje = when (error) {
                        is MotivoObligatorioException -> "Debes escribir un mensaje."
                        is TransicionNoPermitidaException -> "Ese cambio no está permitido."
                        is SolicitudNoEncontradaException -> "La solicitud ya no existe."
                        is IllegalArgumentException -> "El usuario elegido no es técnico."
                        else -> {
                            Log.e("DetalleSolicitudVM", "Error en la acción", error)
                            "No se pudo completar la acción. Inténtalo de nuevo."
                        }
                    }
                    _acciones.update { it.copy(procesando = false, error = mensaje) }
                }
        }
    }

    // Quita el error o aviso de éxito de las acciones cuando ya se mostró.
    fun descartarAvisosAccion() {
        _acciones.update { it.copy(error = null, mensaje = null) }
    }

    // ---------- Utilidades para la pantalla ----------

    // Quita el mensaje de error de guardado cuando ya se mostró.
    fun descartarErrorGuardado() {
        modificarEdicion { it.copy(errorGuardado = null) }
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
                    val baseDatos = AppDatabase.getDatabase(context)
                    val notificador = NotificadorAndroid(context)
                    return DetalleSolicitudViewModel(
                        SolicitudRepository(baseDatos, notificador = notificador),
                        id, usuarioId, rol, notificador
                    ) as T
                }
            }
    }
}