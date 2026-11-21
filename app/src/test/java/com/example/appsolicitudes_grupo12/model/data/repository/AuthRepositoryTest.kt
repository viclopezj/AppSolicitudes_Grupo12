package com.example.appsolicitudes_grupo12.model.data.repository

import com.example.appsolicitudes_grupo12.model.data.local.UsuarioDao
import com.example.appsolicitudes_grupo12.model.data.local.UsuarioEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.io.IOException

// Pruebas del login con un UsuarioDao simulado (sin base de datos).
class AuthRepositoryTest : FunSpec({

    fun usuario(clave: String) = UsuarioEntity(
        id = 7, nombreUsuario = "demo", contrasena = clave, nombre = "Cliente Demo", rol = "cliente"
    )

    test("inicia sesión con usuario y clave correctos y quita los espacios del nombre") {
        runTest {
            val dao = mockk<UsuarioDao>()
            coEvery { dao.buscarPorNombre(any()) } returns usuario("1234")

            val resultado = AuthRepository(dao).iniciarSesion(" demo ", "1234")

            resultado.isSuccess shouldBe true
            resultado.getOrNull()?.nombreUsuario shouldBe "demo"
            coVerify { dao.buscarPorNombre("demo") }
        }
    }

    test("rechaza una clave incorrecta con CredencialesInvalidasException") {
        runTest {
            val dao = mockk<UsuarioDao>()
            coEvery { dao.buscarPorNombre(any()) } returns usuario("9999")

            val resultado = AuthRepository(dao).iniciarSesion("demo", "1234")

            resultado.isFailure shouldBe true
            (resultado.exceptionOrNull() is CredencialesInvalidasException) shouldBe true
        }
    }

    test("si el usuario no existe crea las 6 cuentas de prueba y rechaza el acceso") {
        runTest {
            val dao = mockk<UsuarioDao>()
            coEvery { dao.buscarPorNombre(any()) } returns null
            coEvery { dao.insertar(any()) } returns 1L

            val resultado = AuthRepository(dao).iniciarSesion("fantasma", "1234")

            (resultado.exceptionOrNull() is CredencialesInvalidasException) shouldBe true
            coVerify(exactly = 6) { dao.insertar(any()) }
        }
    }

    test("un error técnico de la base de datos se devuelve como fallo") {
        runTest {
            val dao = mockk<UsuarioDao>()
            coEvery { dao.buscarPorNombre(any()) } throws IOException("disco")

            val resultado = AuthRepository(dao).iniciarSesion("demo", "1234")

            resultado.isFailure shouldBe true
            (resultado.exceptionOrNull() is IOException) shouldBe true
        }
    }
})
