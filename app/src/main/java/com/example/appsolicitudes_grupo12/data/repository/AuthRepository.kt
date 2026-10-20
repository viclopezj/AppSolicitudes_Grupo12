package com.example.appsolicitudes_grupo12.data.repository

import com.example.appsolicitudes_grupo12.data.local.UsuarioDao
import com.example.appsolicitudes_grupo12.data.local.UsuarioEntity
import kotlinx.coroutines.CancellationException

// Error propio para cuando el usuario o la clave no coinciden.
// Sirve para distinguirlo de un fallo técnico (por ejemplo, un problema con la base de datos):
// el ViewModel muestra un mensaje distinto en cada caso.
class CredencialesInvalidasException : Exception("Usuario o clave incorrectos")

// El Repository es el intermediario entre el ViewModel y la base de datos.
// Aquí vive la regla del login: buscar al usuario y comparar la clave.
// Recibe el DAO por el constructor, igual que SolicitudRepository.
class AuthRepository(private val dao: UsuarioDao) {

    // Revisa las credenciales.
    // Result: si salió bien trae el usuario completo (con su nombre y rol);
    // si falló trae el error, ya sea CredencialesInvalidasException u otro fallo técnico.
    suspend fun iniciarSesion(nombreUsuario: String, contrasena: String): Result<UsuarioEntity> {
        return try {
            // Asegura que las tres cuentas de prueba existan antes de validar el ingreso.
            crearUsuarioDemoSiHaceFalta()

            val usuario = dao.buscarPorNombre(nombreUsuario.trim())

            if (usuario != null && usuario.contrasena == contrasena) {
                Result.success(usuario)
            } else {
                // Mismo mensaje si el usuario no existe o la clave está mal:
                // así no se revela cuál de los dos datos falló
                Result.failure(CredencialesInvalidasException())
            }
        } catch (e: CancellationException) {
            // Si la corrutina se cancela, hay que dejar pasar esa señal y no tratarla como error
            throw e
        } catch (e: Exception) {
            // Cualquier otro fallo se devuelve como error controlado
            Result.failure(e)
        }
    }

    // Crea cuentas ficticias de prueba para los tres perfiles si todavía no existen.
    // Se revisa cada nombre por separado para conservar los usuarios que ya estén en Room.
    private suspend fun crearUsuarioDemoSiHaceFalta() {
        val usuariosDemo = listOf(
            UsuarioEntity(
                nombreUsuario = "demo",
                contrasena = "1234",
                nombre = "Cliente Demo",
                rol = "cliente"
            ),
            UsuarioEntity(
                nombreUsuario = "admin",
                contrasena = "1234",
                nombre = "Administradora Demo",
                rol = "administrador"
            ),
            UsuarioEntity(
                nombreUsuario = "tecnico",
                contrasena = "1234",
                nombre = "Técnico Demo",
                rol = "tecnico"
            )
        )

        usuariosDemo.forEach { usuarioDemo ->
            if (dao.buscarPorNombre(usuarioDemo.nombreUsuario) == null) {
                dao.insertar(usuarioDemo)
            }
        }
    }
}
