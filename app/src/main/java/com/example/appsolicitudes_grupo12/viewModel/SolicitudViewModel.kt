package com.example.appsolicitudes_grupo12.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.model.SolicitudErrores
import com.example.appsolicitudes_grupo12.model.SolicitudUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel del formulario. Mantiene el estado, valida los datos y pide el guardado.
// No conoce nada de Compose: la pantalla solo lo observa y le envía eventos.
// AndroidViewModel recibe el "Application", que necesitamos para crear la base de datos.
class SolicitudViewModel(app: Application) : AndroidViewModel(app) {

    // Cadena de acceso a datos: Database -> DAO -> Repository
    private val repositorio = SolicitudRepository(
        AppDatabase.getDatabase(app).solicitudDao()
    )

    // Estado privado y modificable (solo el ViewModel lo cambia)
    private val _estado = MutableStateFlow(SolicitudUiState())

    // Estado público de solo lectura: la pantalla lo observa con collectAsState()
    val estado: StateFlow<SolicitudUiState> = _estado.asStateFlow()

    // ---------- Eventos de los campos ----------
    // Cada función actualiza el valor y borra el error de ese campo
    // cuando la persona vuelve a escribir.

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
        // También limpia el error del servicio, porque si es obligatorio depende de la categoría
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

    // ---------- Reglas de negocio ----------

    // Indica si el servicio relacionado es obligatorio para esa categoría.
    // Es público para que la pantalla pueda mostrar "(opcional)" en la etiqueta.
    fun servicioObligatorio(categoria: String): Boolean {
        return categoria.isNotBlank() && categoria !in CATEGORIAS_SIN_SERVICIO
    }

    // Revisa todos los campos. Guarda los errores en el estado y devuelve
    // true si el formulario es válido, false si hay al menos un error.
    private fun validarFormulario(): Boolean {
        val actual = _estado.value

        val errores = SolicitudErrores(
            titulo = if (actual.titulo.isBlank()) {
                "El título es obligatorio"
            } else null,

            descripcion = when {
                actual.descripcion.isBlank() -> "La descripción es obligatoria"
                actual.descripcion.trim().length < DESCRIPCION_MINIMA ->
                    "La descripción debe tener al menos $DESCRIPCION_MINIMA caracteres"
                else -> null
            },

            categoria = if (actual.categoria.isBlank()) {
                "Selecciona una categoría"
            } else null,

            servicio = if (servicioObligatorio(actual.categoria) && actual.servicio.isBlank()) {
                "Indica el servicio relacionado"
            } else null
        )

        _estado.update { it.copy(errores = errores) }

        // Si ningún campo tiene error, el formulario es válido
        return errores == SolicitudErrores()
    }

    // ---------- Guardado ----------

    // Valida y, solo si todo está bien, guarda la solicitud.
    // La pantalla únicamente llama a esta función al pulsar "Registrar".
    fun guardar() {
        // Evita guardar dos veces si la persona pulsa el botón repetidamente
        if (_estado.value.guardando) return

        // Si hay errores, se muestran en pantalla y no se guarda nada
        if (!validarFormulario()) return

        // Estado Loading
        _estado.update { it.copy(guardando = true, errorGuardado = null) }

        // launch inicia una corrutina: el guardado ocurre sin congelar la pantalla
        viewModelScope.launch {
            val datos = _estado.value

            val resultado = repositorio.guardarSolicitud(
                titulo = datos.titulo,
                descripcion = datos.descripcion,
                categoria = datos.categoria,
                servicio = datos.servicio,
                evidenciaUri = datos.evidenciaUri // por ahora siempre null (cámara pendiente)
            )

            resultado
                .onSuccess { codigo ->
                // Estado Success: se guarda el código para mostrárselo al cliente
                _estado.update { estadoActual ->
                    estadoActual.copy(
                        guardando = false,
                        guardadoExitosamente = true,
                        codigoGenerado = codigo
                    )
                }
            }
                .onFailure { error ->
                    // Estado Error: se registra en el Logcat y se muestra un mensaje amable
                    Log.e("SolicitudViewModel", "Error al guardar la solicitud", error)
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

    // Deja el formulario vacío (se usa después de guardar y navegar)
    fun reiniciarFormulario() {
        _estado.value = SolicitudUiState()
    }

    // Quita el mensaje de error de guardado cuando la persona lo cierra
    fun descartarErrorGuardado() {
        _estado.update { it.copy(errorGuardado = null) }
    }

    // ---------- Valores configurables por el equipo ----------
    companion object {

        // Largo mínimo de la descripción (el equipo puede cambiarlo)
        const val DESCRIPCION_MINIMA = 10

        // Categorías sugeridas por el caso (el caso indica que deben validarse)
        val CATEGORIAS = listOf(
            "Desarrollo web",
            "Mantenimiento web",
            "Incidente técnico",
            "Hosting",
            "Soporte de correo",
            "Marketing digital",
            "Solicitud de cambio",
            "Consulta",
            "Acceso o configuración",
            "Otro requerimiento"
        )

        // Categorías donde el servicio relacionado NO es obligatorio
        val CATEGORIAS_SIN_SERVICIO = setOf("Consulta", "Otro requerimiento")
    }
}