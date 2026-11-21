package com.example.appsolicitudes_grupo12.model.data.repository

import com.example.appsolicitudes_grupo12.model.aviso.DestinatarioAviso
import com.example.appsolicitudes_grupo12.model.aviso.Notificador
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudDao
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.io.IOException

// Pruebas de las lecturas y del aviso de atrasadas de SolicitudRepository con DAO simulado.
class SolicitudRepositoryTest : FunSpec({

    fun solicitud(codigo: String, estado: String, creada: Long) = SolicitudEntity(
        id = 1, codigo = codigo, titulo = "Título", descripcion = "Descripción larga",
        categoria = "Desarrollo web", servicio = "", estado = estado, fechaCreacion = creada
    )

    // Todos los DAO son simulados; la base de datos solo se pasa porque el constructor la pide.
    fun repositorio(dao: SolicitudDao, notificador: Notificador) = SolicitudRepository(
        db = mockk(),
        dao = dao,
        historialDao = mockk(),
        comentarioDao = mockk(),
        usuarioDao = mockk(),
        adjuntoDao = mockk(),
        notificador = notificador
    )

    test("obtenerSolicitud devuelve la solicitud o SolicitudNoEncontradaException") {
        runTest {
            val dao = mockk<SolicitudDao>()
            val existente = solicitud("SOL-1", "Recibida", 0L)
            coEvery { dao.obtenerPorId(1, 5, "cliente") } returns existente
            coEvery { dao.obtenerPorId(2, 5, "cliente") } returns null
            val repo = repositorio(dao, mockk(relaxed = true))

            repo.obtenerSolicitud(1, 5, "cliente").getOrNull() shouldBe existente
            (repo.obtenerSolicitud(2, 5, "cliente").exceptionOrNull() is SolicitudNoEncontradaException) shouldBe true
        }
    }

    test("buscarPorCodigo limpia espacios y pasa el código a mayúsculas") {
        runTest {
            val dao = mockk<SolicitudDao>()
            coEvery { dao.obtenerPorCodigoVisible("SOL-ABC", 5, "cliente") } returns solicitud("SOL-ABC", "Recibida", 0L)
            val repo = repositorio(dao, mockk(relaxed = true))

            repo.buscarPorCodigo("  sol-abc ", 5, "cliente").isSuccess shouldBe true
            coVerify { dao.obtenerPorCodigoVisible("SOL-ABC", 5, "cliente") }
        }
    }

    test("un error del DAO en una lectura se devuelve como fallo") {
        runTest {
            val dao = mockk<SolicitudDao>()
            coEvery { dao.obtenerPorId(any(), any(), any()) } throws IOException("disco")

            repositorio(dao, mockk(relaxed = true)).obtenerSolicitud(1, 5, "cliente").isFailure shouldBe true
        }
    }

    test("el administrador recibe el aviso con el total de solicitudes atrasadas") {
        runTest {
            val dao = mockk<SolicitudDao>()
            val notificador = mockk<Notificador>(relaxed = true)
            val ahora = System.currentTimeMillis()
            every { dao.obtenerTodas() } returns flowOf(
                listOf(
                    solicitud("A", "En proceso", 0L),
                    solicitud("B", "Recibida", 0L),
                    solicitud("C", "Recibida", ahora),
                    solicitud("D", "Cerrada", 0L)
                )
            )

            val resultado = repositorio(dao, notificador).avisarSolicitudesAtrasadas(1, "administrador")

            resultado.getOrNull() shouldBe 2
            verify(exactly = 1) {
                notificador.publicar(match {
                    it.destinatario == DestinatarioAviso.ADMINISTRADOR &&
                            it.mensaje.contains("2 solicitudes atrasadas")
                })
            }
        }
    }

    test("el técnico recibe el aviso de las suyas y el cliente no recibe ninguno") {
        runTest {
            val dao = mockk<SolicitudDao>()
            val notificador = mockk<Notificador>(relaxed = true)
            every { dao.obtenerVisibles(9, "tecnico") } returns flowOf(listOf(solicitud("A", "Asignada", 0L)))
            val repo = repositorio(dao, notificador)

            repo.avisarSolicitudesAtrasadas(9, "tecnico").getOrNull() shouldBe 1
            repo.avisarSolicitudesAtrasadas(5, "cliente").getOrNull() shouldBe 0
            verify(exactly = 1) {
                notificador.publicar(match { it.destinatario == DestinatarioAviso.TECNICO })
            }
        }
    }

    test("sin solicitudes atrasadas no se publica ningún aviso") {
        runTest {
            val dao = mockk<SolicitudDao>()
            val notificador = mockk<Notificador>(relaxed = true)
            every { dao.obtenerTodas() } returns flowOf(listOf(solicitud("A", "Recibida", System.currentTimeMillis())))

            repositorio(dao, notificador).avisarSolicitudesAtrasadas(1, "administrador").getOrNull() shouldBe 0
            verify(exactly = 0) { notificador.publicar(any()) }
        }
    }
})
