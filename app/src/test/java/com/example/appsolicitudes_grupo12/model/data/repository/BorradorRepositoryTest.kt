package com.example.appsolicitudes_grupo12.model.data.repository

import com.example.appsolicitudes_grupo12.model.data.local.BorradorDao
import com.example.appsolicitudes_grupo12.model.data.local.BorradorEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import java.io.IOException

// Pruebas del borrador del formulario con un BorradorDao simulado.
class BorradorRepositoryTest : FunSpec({

    test("guarda por primera vez: si no se actualizó ninguna fila, inserta el borrador") {
        runTest {
            val dao = mockk<BorradorDao>()
            val guardado = slot<BorradorEntity>()
            coEvery { dao.actualizar(any()) } returns 0
            coEvery { dao.insertar(capture(guardado)) } returns 1L

            val resultado = BorradorRepository(dao).guardar(3, "Título", "Descripción larga", "Desarrollo web", "")

            resultado.isSuccess shouldBe true
            guardado.captured.usuarioId shouldBe 3
            guardado.captured.titulo shouldBe "Título"
            guardado.captured.categoria shouldBe "Desarrollo web"
        }
    }

    test("si el cliente ya tenía borrador lo actualiza y no inserta otro") {
        runTest {
            val dao = mockk<BorradorDao>()
            coEvery { dao.actualizar(any()) } returns 1

            BorradorRepository(dao).guardar(3, "Nuevo título", "", "", "").isSuccess shouldBe true

            coVerify(exactly = 0) { dao.insertar(any()) }
        }
    }

    test("obtiene y elimina el borrador del cliente") {
        runTest {
            val dao = mockk<BorradorDao>()
            val borrador = BorradorEntity(3, "T", "D", "C", "S", 1L)
            coEvery { dao.obtener(3) } returns borrador
            coEvery { dao.obtener(4) } returns null
            coEvery { dao.eliminar(3) } returns 1
            val repositorio = BorradorRepository(dao)

            repositorio.obtener(3).getOrNull() shouldBe borrador
            repositorio.obtener(4).getOrNull() shouldBe null
            repositorio.eliminar(3).isSuccess shouldBe true
            coVerify(exactly = 1) { dao.eliminar(3) }
        }
    }

    test("un error de la base de datos se devuelve como fallo") {
        runTest {
            val dao = mockk<BorradorDao>()
            coEvery { dao.actualizar(any()) } throws IOException("disco")
            coEvery { dao.obtener(any()) } throws IOException("disco")
            coEvery { dao.eliminar(any()) } throws IOException("disco")
            val repositorio = BorradorRepository(dao)

            repositorio.guardar(3, "T", "", "", "").isFailure shouldBe true
            repositorio.obtener(3).isFailure shouldBe true
            repositorio.eliminar(3).isFailure shouldBe true
        }
    }
})
