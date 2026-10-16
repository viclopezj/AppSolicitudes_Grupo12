package com.example.appsolicitudes_grupo12.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.data.repository.AuthRepository
import com.example.appsolicitudes_grupo12.data.repository.CredencialesInvalidasException
import com.example.appsolicitudes_grupo12.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel del login. Mantiene el estado, valida los campos y pide revisar las credenciales.
// No conoce nada de Compose: la pantalla solo lo observa y le envía eventos.
// Funciona igual que SolicitudViewModel.
class LoginViewModel(app: Application) : AndroidViewModel(app) {

    // Cadena de acceso a datos: Database -> DAO -> Repository
    private val repositorio = AuthRepository(
        AppDatabase.getDatabase(app).usuarioDao()
    )

    // Estado privado y modificable (solo el ViewModel lo cambia)
    private val _estado = MutableStateFlow(LoginUiState())

    // Estado público de solo lectura: la pantalla lo observa con collectAsState()
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    // ---------- Eventos de los campos ----------
    // Cada función actualiza el valor y borra los errores
    // cuando la persona vuelve a escribir.

    fun onNombreUsuarioChange(valor: String) {
        _estado.update {
            it.copy(nombreUsuario = valor, errorNombreUsuario = null, errorLogin = null)
        }
    }

    fun onContrasenaChange(valor: String) {
        _estado.update {
            it.copy(contrasena = valor, errorContrasena = null, errorLogin = null)
        }
    }

    // ---------- Validación ----------

    // Revisa que ningún campo esté vacío. Guarda los errores en el estado y devuelve
    // true si todo está bien, false si hay al menos un error.
    private fun validarCampos(): Boolean {
        val actual = _estado.value

        val errorUsuario = if (actual.nombreUsuario.isBlank()) {
            "El usuario es obligatorio"
        } else null

        val errorClave = if (actual.contrasena.isBlank()) {
            "La clave es obligatoria"
        } else null

        _estado.update {
            it.copy(errorNombreUsuario = errorUsuario, errorContrasena = errorClave)
        }

        return errorUsuario == null && errorClave == null
    }

    // ---------- Ingreso ----------

    // Valida y, solo si todo está bien, revisa las credenciales.
    // La pantalla únicamente llama a esta función al pulsar "Ingresar".
    fun iniciarSesion() {
        // Evita enviar dos veces si la persona pulsa el botón repetidamente
        if (_estado.value.cargando) return

        // Si hay campos vacíos, se muestran los errores y no se revisa nada
        if (!validarCampos()) return

        // Estado Loading
        _estado.update { it.copy(cargando = true, errorLogin = null) }

        // launch inicia una corrutina: la consulta ocurre sin congelar la pantalla
        viewModelScope.launch {
            val datos = _estado.value

            val resultado = repositorio.iniciarSesion(
                nombreUsuario = datos.nombreUsuario,
                contrasena = datos.contrasena
            )

            resultado
                .onSuccess { usuario ->
                    // Estado Success: guardamos el nombre; la pantalla lo detecta y navega
                    _estado.update {
                        it.copy(cargando = false, nombreAutenticado = usuario.nombre)
                    }
                }
                .onFailure { error ->
                    // Estado Error: según el tipo de error, se elige el mensaje
                    val mensaje = if (error is CredencialesInvalidasException) {
                        "Usuario o clave incorrectos"
                    } else {
                        // Fallo técnico: se registra en el Logcat y se muestra un mensaje amable
                        Log.e("LoginViewModel", "Error al iniciar sesión", error)
                        "No se pudo iniciar sesión. Inténtalo de nuevo."
                    }
                    _estado.update { it.copy(cargando = false, errorLogin = mensaje) }
                }
        }
    }
}