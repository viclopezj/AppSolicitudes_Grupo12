package com.example.appsolicitudes_grupo12.model.data.repository

import com.example.appsolicitudes_grupo12.model.data.local.UsuarioDao
import com.example.appsolicitudes_grupo12.model.data.local.UsuarioEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.io.IOException

// Pruebas del repositorio de usuarios (técnicos y clientes) con un DAO simulado.
class UsuarioRepositoryTest : FunSpec({

    val tecnico = UsuarioEntity(id = 2, nombreUsuario = "tecnico", contrasena = "1234", nombre = "Técnico Demo", rol = "tecnico")
    val cliente = UsuarioEntity(id = 1, nombreUsuario = "demo", contrasena = "1234", nombre = "Cliente Demo", rol = "cliente")

    test("devuelve los técnicos y los clientes que entrega el DAO") {
        runTest {
            val dao = mockk<UsuarioDao>()
            coEvery { dao.obtenerTecnicos() } returns listOf(tecnico)
            coEvery { dao.obtenerClientes() } returns listOf(cliente)
            val repositorio = UsuarioRepository(dao)

            repositorio.obtenerTecnicos().getOrNull() shouldBe listOf(tecnico)
            repositorio.obtenerClientes().getOrNull() shouldBe listOf(cliente)
        }
    }

    test("si el DAO falla devuelve el error en vez de lanzarlo") {
        runTest {
            val dao = mockk<UsuarioDao>()
            coEvery { dao.obtenerTecnicos() } throws IOException("disco")
            coEvery { dao.obtenerClientes() } throws IOException("disco")
            val repositorio = UsuarioRepository(dao)

            repositorio.obtenerTecnicos().isFailure shouldBe true
            repositorio.obtenerClientes().isFailure shouldBe true
        }
    }
})
