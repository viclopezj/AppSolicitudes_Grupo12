package com.example.appsolicitudes_grupo12.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.model.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.model.data.repository.AuthRepository
import com.example.appsolicitudes_grupo12.model.data.repository.CredencialesInvalidasException
import com.example.appsolicitudes_grupo12.state.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel del login: guarda el estado, valida los campos y pide revisar las credenciales.
class LoginViewModel(private val repositorio: AuthRepository) : ViewModel() {

    // Estado modificable (privado) y de solo lectura (público)
    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    // ---------- Eventos de los campos ----------

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

    // Revisa que no haya campos vacíos. Devuelve true si todo está bien.
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

    // Valida y, si todo está bien, revisa las credenciales.
    fun iniciarSesion() {
        // Evita enviar dos veces si se pulsa el botón repetidamente
        if (_estado.value.cargando) return
        if (!validarCampos()) return

        _estado.update { it.copy(cargando = true, errorLogin = null) }

        viewModelScope.launch {
            val datos = _estado.value

            repositorio.iniciarSesion(datos.nombreUsuario, datos.contrasena)
                .onSuccess { usuario ->
                    _estado.update {
                        it.copy(
                            cargando = false,
                            nombreAutenticado = usuario.nombre,
                            usuarioIdAutenticado = usuario.id,
                            rolAutenticado = usuario.rol
                        )
                    }
                }
                .onFailure { error ->
                    val mensaje = if (error is CredencialesInvalidasException) {
                        "Usuario o clave incorrectos"
                    } else {
                        Log.e("LoginViewModel", "Error al iniciar sesión", error)
                        "No se pudo iniciar sesión. Inténtalo de nuevo."
                    }
                    _estado.update { it.copy(cargando = false, errorLogin = mensaje) }
                }
        }
    }

    // ---------- Fábrica ----------

    companion object {
        // Arma el ViewModel con su repositorio (viewModel() no sabe hacerlo solo)
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dao = AppDatabase.getDatabase(context).usuarioDao()
                    return LoginViewModel(AuthRepository(dao)) as T
                }
            }
    }
}