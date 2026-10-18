package com.example.appsolicitudes_grupo12.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.data.repository.SolicitudNoEncontradaException
import com.example.appsolicitudes_grupo12.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.model.DetalleSolicitudUiState
import com.example.appsolicitudes_grupo12.model.EdicionSolicitud
import com.example.appsolicitudes_grupo12.model.SolicitudErrores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel de la pantalla de detalle. Lee UNA solicitud, deja pasar del modo lectura
// al modo edición, valida los cambios y los guarda.
// No conoce nada de Compose: la pantalla solo lo observa y le envía eventos.
// SavedStateHandle es una "mochila" que trae los datos de la ruta de navegación.
// De ahí sacamos el id de la solicitud que se pulsó en la lista.
class DetalleSolicitudViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    // Cadena de acceso a datos: Database -> DAO -> Repository
    private val repositorio = SolicitudRepository(
        AppDatabase.getDatabase(app).solicitudDao()
    )

    // Id de la solicitud que se está viendo. Viene en la ruta como "detalle/{id}".
    // Si por alguna razón no llega, se usa -1: no existe ninguna solicitud con ese id,
    // así que la pantalla mostrará "No se encontró la solicitud" en vez de cerrarse.
    private val id: Int = savedStateHandle.get<Int>("id") ?: -1

    // Estado privado y modificable. Parte en Loading mientras llega la solicitud.
    private val _estado = MutableStateFlow<DetalleSolicitudUiState>(
        DetalleSolicitudUiState.Loading
    )

    // Estado público de solo lectura: la pantalla lo observa con collectAsState()
    val estado: StateFlow<DetalleSolicitudUiState> = _estado.asStateFlow()

    // Se ejecuta al crear el ViewModel: empieza a leer la solicitud
    init {
        cargarSolicitud()
    }

    // ---------- Lectura ----------

    // Lee la solicitud de Room y actualiza el estado.
    // Es pública porque el botón "Reintentar" de la pantalla también la llama.
    fun cargarSolicitud() {
        // Estado Loading mientras llegan los datos
        _estado.value = DetalleSolicitudUiState.Loading

        // launch inicia una corrutina: la lectura ocurre sin congelar la pantalla
        viewModelScope.launch {
            repositorio.obtenerSolicitud(id)
                .onSuccess { solicitud ->
                    // Estado Success en modo lectura (edicion = null)
                    _estado.value = DetalleSolicitudUiState.Success(solicitud)
                }
                .onFailure { error -> mostrarErrorDeLectura(error) }
        }
    }

    // Convierte un fallo de lectura en el estado que corresponde:
    // si la solicitud no existe -> NoEncontrada; si falló otra cosa -> Error con mensaje amable.
    private fun mostrarErrorDeLectura(error: Throwable) {
        _estado.value = if (error is SolicitudNoEncontradaException) {
            DetalleSolicitudUiState.NoEncontrada
        } else {
            // Fallo técnico: se registra en el Logcat y se muestra un mensaje amable
            Log.e("DetalleSolicitudVM", "Error al leer la solicitud", error)
            DetalleSolicitudUiState.Error(
                "No se pudo cargar la solicitud. Inténtalo de nuevo."
            )
        }
    }

    // ---------- Modo lectura <-> modo edición ----------

    // Pasa al modo edición. Los campos parten con los valores que ya están guardados.
    fun iniciarEdicion() {
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

    // Vuelve al modo lectura sin guardar: lo escrito se descarta y Room no se toca.
    fun cancelarEdicion() {
        val actual = _estado.value as? DetalleSolicitudUiState.Success ?: return
        _estado.value = actual.copy(edicion = null)
    }

    // ---------- Eventos de los campos ----------
    // Cada función actualiza el valor y borra el error de ese campo
    // cuando la persona vuelve a escribir (igual que en SolicitudViewModel).

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
        // También limpia el error del servicio, porque si es obligatorio depende de la categoría
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

    // Ayudante: aplica un cambio a la edición actual.
    // Solo funciona si la pantalla está en modo edición; si no, no hace nada.
    private fun modificarEdicion(cambio: (EdicionSolicitud) -> EdicionSolicitud) {
        val actual = _estado.value as? DetalleSolicitudUiState.Success ?: return
        val edicion = actual.edicion ?: return
        _estado.value = actual.copy(edicion = cambio(edicion))
    }

    // ---------- Reglas de negocio ----------

    // Indica si el servicio relacionado es obligatorio para esa categoría.
    // Usa la misma regla del formulario: las categorías sin servicio están en SolicitudViewModel.
    // Es público para que la pantalla pueda mostrar "(opcional)" en la etiqueta.
    fun servicioObligatorio(categoria: String): Boolean {
        return categoria.isNotBlank() &&
                categoria !in SolicitudViewModel.CATEGORIAS_SIN_SERVICIO
    }

    // Revisa todos los campos de la edición. Guarda los errores en el estado y devuelve
    // true si todo es válido, false si hay al menos un error.
    // Son las mismas reglas que valida el formulario de nueva solicitud.
    private fun validarEdicion(): Boolean {
        val actual = _estado.value as? DetalleSolicitudUiState.Success ?: return false
        val edicion = actual.edicion ?: return false

        val errores = SolicitudErrores(
            titulo = if (edicion.titulo.isBlank()) {
                "El título es obligatorio"
            } else null,

            descripcion = when {
                edicion.descripcion.isBlank() -> "La descripción es obligatoria"
                edicion.descripcion.trim().length < SolicitudViewModel.DESCRIPCION_MINIMA ->
                    "La descripción debe tener al menos ${SolicitudViewModel.DESCRIPCION_MINIMA} caracteres"
                else -> null
            },

            categoria = if (edicion.categoria.isBlank()) {
                "Selecciona una categoría"
            } else null,

            servicio = if (servicioObligatorio(edicion.categoria) && edicion.servicio.isBlank()) {
                "Indica el servicio relacionado"
            } else null
        )

        modificarEdicion { it.copy(errores = errores) }

        // Si ningún campo tiene error, la edición es válida
        return errores == SolicitudErrores()
    }

    // ---------- Guardado ----------

    // Valida y, solo si todo está bien, guarda los cambios.
    // La pantalla únicamente llama a esta función al pulsar "Guardar cambios".
    fun guardarCambios() {
        val actual = _estado.value as? DetalleSolicitudUiState.Success ?: return
        val edicion = actual.edicion ?: return

        // Evita guardar dos veces si la persona pulsa el botón repetidamente
        if (edicion.guardando) return

        // Si hay errores, se muestran en pantalla y no se guarda nada
        if (!validarEdicion()) return

        // Estado Loading
        modificarEdicion { it.copy(guardando = true, errorGuardado = null) }

        // launch inicia una corrutina: el guardado ocurre sin congelar la pantalla
        viewModelScope.launch {
            // Se vuelven a leer los datos del estado, porque ya incluyen la validación
            val datos = (_estado.value as? DetalleSolicitudUiState.Success)?.edicion
                ?: return@launch

            val resultado = repositorio.actualizarSolicitud(
                id = id,
                titulo = datos.titulo,
                descripcion = datos.descripcion,
                categoria = datos.categoria,
                servicio = datos.servicio
            )

            resultado
                .onSuccess {
                    // Se vuelve a leer la solicitud para mostrar exactamente lo guardado.
                    // Esa lectura deja la pantalla en modo lectura con los datos nuevos.
                    cargarDespuesDeGuardar()
                }
                .onFailure { error ->
                    // Estado Error: se queda en modo edición y se muestra un mensaje
                    val mensaje = if (error is SolicitudNoEncontradaException) {
                        "La solicitud ya no existe."
                    } else {
                        // Fallo técnico: se registra en el Logcat y se muestra un mensaje amable
                        Log.e("DetalleSolicitudVM", "Error al guardar los cambios", error)
                        "No se pudieron guardar los cambios. Inténtalo de nuevo."
                    }
                    modificarEdicion { it.copy(guardando = false, errorGuardado = mensaje) }
                }
        }
    }

    // Lee la solicitud ya modificada y vuelve al modo lectura.
    // No pasa por Loading para que la pantalla no parpadee.
    private suspend fun cargarDespuesDeGuardar() {
        repositorio.obtenerSolicitud(id)
            .onSuccess { solicitud ->
                _estado.value = DetalleSolicitudUiState.Success(solicitud)
            }
            .onFailure { error -> mostrarErrorDeLectura(error) }
    }

    // Quita el mensaje de error de guardado cuando ya se mostró en el Snackbar
    fun descartarErrorGuardado() {
        modificarEdicion { it.copy(errorGuardado = null) }
    }
}