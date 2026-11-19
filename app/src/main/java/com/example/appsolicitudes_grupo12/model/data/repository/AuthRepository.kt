package com.example.appsolicitudes_grupo12.model.data.repository

import com.example.appsolicitudes_grupo12.model.data.local.UsuarioDao
import com.example.appsolicitudes_grupo12.model.data.local.UsuarioEntity
import kotlinx.coroutines.CancellationException

// Usuario o clave incorrectos (distinto de un fallo técnico).
class CredencialesInvalidasException : Exception("Usuario o clave incorrectos")

// Regla del login: busca al usuario y compara la clave.
class AuthRepository(private val dao: UsuarioDao) {

    // Devuelve el usuario si las credenciales son correctas, o el error si no.
    suspend fun iniciarSesion(nombreUsuario: String, contrasena: String): Result<UsuarioEntity> {
        return try {
            crearUsuariosDemoSiHaceFalta()

            val usuario = dao.buscarPorNombre(nombreUsuario.trim())

            if (usuario != null && usuario.contrasena == contrasena) {
                Result.success(usuario)
            } else {
                // Mismo error si falla el usuario o la clave, para no revelar cuál fue
                Result.failure(CredencialesInvalidasException())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cuentas ficticias de prueba: 3 clientes, 1 administrador y 2 técnicos (clave 1234).
    private val usuariosDemo = listOf(
        UsuarioEntity(nombreUsuario = "demo", contrasena = "1234", nombre = "Cliente Demo", rol = "cliente"),
        UsuarioEntity(nombreUsuario = "cliente2", contrasena = "1234", nombre = "Cliente Dos", rol = "cliente"),
        UsuarioEntity(nombreUsuario = "cliente3", contrasena = "1234", nombre = "Cliente Tres", rol = "cliente"),
        UsuarioEntity(nombreUsuario = "admin", contrasena = "1234", nombre = "Administradora Demo", rol = "administrador"),
        UsuarioEntity(nombreUsuario = "tecnico", contrasena = "1234", nombre = "Técnico Demo", rol = "tecnico"),
        UsuarioEntity(nombreUsuario = "tecnico2", contrasena = "1234", nombre = "Técnico Dos", rol = "tecnico")
    )

    // Crea las cuentas de prueba que todavía no existan.
    private suspend fun crearUsuariosDemoSiHaceFalta() {
        usuariosDemo.forEach { usuarioDemo ->
            if (dao.buscarPorNombre(usuarioDemo.nombreUsuario) == null) {
                dao.insertar(usuarioDemo)
            }
        }
    }
}