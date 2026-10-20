package com.example.appsolicitudes_grupo12.viewModel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.model.SolicitudErrores
import com.example.appsolicitudes_grupo12.model.SolicitudUiState
import com.example.appsolicitudes_grupo12.utils.EvidenciaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// ViewModel del formulario. Mantiene el estado, valida los datos y pide el guardado.
// No conoce nada de Compose: la pantalla solo lo observa y le envía eventos.
// AndroidViewModel recibe el "Application", que necesitamos para crear la base de datos.
class SolicitudViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    private val usuarioId = savedStateHandle.get<Int>("usuarioId") ?: -1
    private val rol = savedStateHandle.get<String>("rol") ?: ""

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

    // ---------- Eventos de la foto de evidencia ----------

    // Entrega un archivo vacío donde la cámara escribirá la foto.
    // La pantalla lo convierte en una dirección segura (FileProvider) y se lo pasa a la cámara.
    fun crearArchivoParaCamara(): File {
        return EvidenciaUtils.crearArchivoNuevo(getApplication<Application>())
    }

    // La cámara terminó bien: la foto ya está escrita en el archivo.
    // Se comprueba que realmente exista y tenga contenido antes de guardarla en el estado.
    fun onFotoTomada(archivo: File) {
        if (archivo.exists() && archivo.length() > 0) {
            usarFoto(EvidenciaUtils.aTexto(archivo))
        } else {
            archivo.delete()
            _estado.update {
                it.copy(errorFoto = "No se pudo guardar la foto. Inténtalo de nuevo.")
            }
        }
    }

    // La persona cerró la cámara sin tomar la foto: se borra el archivo vacío que quedó.
    fun onFotoCancelada(archivo: File) {
        archivo.delete()
    }

    // La persona eligió una imagen de la galería. "uri" es una dirección temporal que da el
    // sistema, por eso se COPIA a un archivo propio de la app antes de guardarla.
    fun onFotoElegida(uri: Uri) {
        // Evita copiar dos veces si la persona toca varias veces
        if (_estado.value.cargandoFoto) return

        // Estado Loading mientras se copia la imagen
        _estado.update { it.copy(cargandoFoto = true, errorFoto = null) }

        // launch inicia una corrutina; withContext(Dispatchers.IO) manda la copia a un hilo
        // de segundo plano, así la pantalla no se congela con fotos grandes.
        viewModelScope.launch {
            val archivo = withContext(Dispatchers.IO) {
                EvidenciaUtils.copiarDeGaleria(getApplication<Application>(), uri)
            }

            if (archivo != null) {
                usarFoto(EvidenciaUtils.aTexto(archivo))
            } else {
                // Estado Error: mensaje amable; el detalle técnico ya quedó en el Logcat
                _estado.update {
                    it.copy(
                        cargandoFoto = false,
                        errorFoto = "No se pudo cargar la imagen. Prueba con otra."
                    )
                }
            }
        }
    }

    // La persona no dio permiso de cámara: se avisa con un mensaje claro.
    fun onPermisoCamaraDenegado() {
        _estado.update {
            it.copy(errorFoto = "Se necesita el permiso de cámara para tomar la foto.")
        }
    }

    // Quita la foto elegida: se borra el archivo y el formulario queda sin evidencia.
    fun quitarFoto() {
        EvidenciaUtils.borrar(_estado.value.evidenciaUri)
        _estado.update { it.copy(evidenciaUri = null, errorFoto = null) }
    }

    // Quita el mensaje de error de la foto cuando ya se mostró en el Snackbar
    fun descartarErrorFoto() {
        _estado.update { it.copy(errorFoto = null) }
    }

    // Deja la foto nueva en el estado. Si ya había otra, borra su archivo
    // para no acumular fotos que nadie va a usar.
    private fun usarFoto(uriTexto: String) {
        val anterior = _estado.value.evidenciaUri
        if (anterior != null && anterior != uriTexto) {
            EvidenciaUtils.borrar(anterior)
        }
        _estado.update {
            it.copy(evidenciaUri = uriTexto, cargandoFoto = false, errorFoto = null)
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
    // La foto es opcional, así que no se valida.
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
        if (usuarioId <= 0 || rol != "cliente") {
            _estado.update {
                it.copy(errorGuardado = "Solo un cliente autenticado puede registrar solicitudes.")
            }
            return
        }

        // Evita guardar dos veces si la persona pulsa el botón repetidamente,
        // y también guardar mientras todavía se está copiando una foto
        if (_estado.value.guardando || _estado.value.cargandoFoto) return

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
                creadorUsuarioId = usuarioId,
                evidenciaUri = datos.evidenciaUri   // la foto elegida, o null si no hay
            )

            resultado
                .onSuccess { solicitud ->
                    // Estado Success: se guarda la solicitud creada para mostrar su código
                    // y dibujar su QR en el diálogo de confirmación
                    _estado.update { estadoActual ->
                        estadoActual.copy(
                            guardando = false,
                            guardadoExitosamente = true,
                            solicitudCreada = solicitud
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

    // Deja el formulario vacío (se usa después de guardar y navegar).
    // La foto NO se borra: ya quedó asociada a la solicitud guardada.
    fun reiniciarFormulario() {
        _estado.value = SolicitudUiState()
    }

    // Quita el mensaje de error de guardado cuando la persona lo cierra
    fun descartarErrorGuardado() {
        _estado.update { it.copy(errorGuardado = null) }
    }

    // Se ejecuta cuando el formulario se cierra definitivamente (por ejemplo, con la flecha
    // de volver). Si había una foto elegida y la solicitud NO se registró, se borra el archivo
    // para no dejar fotos huérfanas. Si se registró, reiniciarFormulario() ya dejó
    // la foto fuera del estado y no se toca.
    override fun onCleared() {
        val actual = _estado.value
        if (!actual.guardadoExitosamente) {
            EvidenciaUtils.borrar(actual.evidenciaUri)
        }
        super.onCleared()
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
