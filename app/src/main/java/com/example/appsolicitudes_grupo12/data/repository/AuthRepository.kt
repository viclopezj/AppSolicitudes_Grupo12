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
            // Si la tabla está vacía (primera vez que se abre la app), se crea el usuario demo
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

    // Inserta el usuario genérico ficticio solo si todavía no hay usuarios guardados.
    // Más adelante aquí se crearán los tres perfiles: cliente, administrador y técnico.
    private suspend fun crearUsuarioDemoSiHaceFalta() {
        if (dao.contar() == 0) {
            dao.insertar(
                UsuarioEntity(
                    nombreUsuario = "demo",
                    contrasena = "1234",
                    nombre = "Usuario Demo",
                    rol = "general"
                )
            )
        }
    }
}