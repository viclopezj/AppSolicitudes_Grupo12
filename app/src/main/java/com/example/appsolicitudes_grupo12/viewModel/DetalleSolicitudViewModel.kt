package com.example.appsolicitudes_grupo12.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.model.data.local.AppDatabase
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

// ViewModel del detalle: lee UNA solicitud, pasa de lectura a edición, valida y guarda cambios.
class DetalleSolicitudViewModel(
    private val repositorio: SolicitudRepository,
    private val id: Int,
    private val usuarioId: Int,
    private val rol: String
) : ViewModel() {

    // Parte en Loading mientras llega la solicitud
    private val _estado = MutableStateFlow<DetalleSolicitudUiState>(
        DetalleSolicitudUiState.Loading
    )
    val estado: StateFlow<DetalleSolicitudUiState> = _estado.asStateFlow()

    // El cliente edita sus solicitudes y el administrador puede corregir cualquiera
    val puedeEditar: Boolean
        get() = rol == "administrador" || rol == "cliente"

    init {
        cargarSolicitud()
    }

    // ---------- Lectura ----------

    // Lee la solicitud de Room. También la usa el botón "Reintentar".
    fun cargarSolicitud() {
        _estado.value = DetalleSolicitudUiState.Loading

        viewModelScope.launch {
            repositorio.obtenerSolicitud(id, usuarioId, rol)
                .onSuccess { solicitud ->
                    _estado.value = DetalleSolicitudUiState.Success(solicitud)
                }
                .onFailure { error -> mostrarErrorDeLectura(error) }
        }
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

    // ---------- Modo lectura <-> modo edición ----------

    // Pasa al modo edición con los valores ya guardados.
    fun iniciarEdicion() {
        if (!puedeEditar) return
        val actual = _estado.value as? DetalleSolicitudUiState.Success ?: return
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
        // También limpia el error del servicio, porque su obligatoriedad depende de la categoría
        modificarEdicion {
            it.copy(
                categoria = valor,
                errores = it.errores.copy(categoria = null, servicio = null)
            )
        }
    }

    fun onServicioChange(valor: String) {
        modificarEdicion {
            it.copy(servicio = valor, errores = it.errores.copy(servicio = null))
        }
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
            categoria = edicion.categoria,
            servicio = edicion.servicio
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
                    val dao = AppDatabase.getDatabase(context).solicitudDao()
                    return DetalleSolicitudViewModel(
                        SolicitudRepository(dao), id, usuarioId, rol
                    ) as T
                }
            }
    }
}