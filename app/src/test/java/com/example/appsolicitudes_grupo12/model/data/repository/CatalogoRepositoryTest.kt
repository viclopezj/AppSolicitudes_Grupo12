package com.example.appsolicitudes_grupo12.model.data.repository

import com.example.appsolicitudes_grupo12.model.catalogo.ServicioCatalogo
import com.example.appsolicitudes_grupo12.model.data.remote.ApiService
import com.example.appsolicitudes_grupo12.model.data.remote.ProductoDto
import com.example.appsolicitudes_grupo12.model.data.remote.ProductosRespuesta
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.io.IOException

// Pruebas del repositorio del catálogo con un ApiService simulado (sin internet).
class CatalogoRepositoryTest : FunSpec({

    test("convierte los productos de la API en servicios del catálogo") {
        runTest {
            val api = mockk<ApiService>()
            coEvery { api.obtenerProductos(any(), any()) } returns ProductosRespuesta(
                products = listOf(
                    ProductoDto(
                        id = 1,
                        title = "  Mantenimiento web ",
                        category = "home-decoration",
                        description = "Actualización de sitios"
                    )
                )
            )

            val resultado = CatalogoRepository(api).obtenerCatalogo()

            resultado.isSuccess shouldBe true
            resultado.getOrNull() shouldBe listOf(
                ServicioCatalogo(
                    id = 1,
                    servicio = "Mantenimiento web",
                    categoria = "Home decoration",
                    descripcion = "Actualización de sitios"
                )
            )
            // Se pide el catálogo con el límite y los campos acordados
            coVerify(exactly = 1) {
                api.obtenerProductos(10, "title,category,description")
            }
        }
    }

    test("descarta los productos sin id o sin nombre y completa los datos que faltan") {
        runTest {
            val api = mockk<ApiService>()
            coEvery { api.obtenerProductos(any(), any()) } returns ProductosRespuesta(
                products = listOf(
                    ProductoDto(id = null, title = "Sin id"),
                    ProductoDto(id = 2, title = "   "),
                    ProductoDto(id = 3, title = "Hosting", category = null, description = null)
                )
            )

            val resultado = CatalogoRepository(api).obtenerCatalogo()

            resultado.getOrNull() shouldBe listOf(
                ServicioCatalogo(
                    id = 3,
                    servicio = "Hosting",
                    categoria = "Sin categoría",
                    descripcion = ""
                )
            )
        }
    }

    test("una respuesta sin productos da una lista vacía, no un error") {
        runTest {
            val api = mockk<ApiService>()
            coEvery { api.obtenerProductos(any(), any()) } returns ProductosRespuesta(products = null)

            val resultado = CatalogoRepository(api).obtenerCatalogo()

            resultado.isSuccess shouldBe true
            resultado.getOrNull() shouldBe emptyList<ServicioCatalogo>()
        }
    }

    test("un error de red llega como fallo controlado y no como excepción") {
        runTest {
            val api = mockk<ApiService>()
            coEvery { api.obtenerProductos(any(), any()) } throws IOException("Sin conexión")

            val resultado = CatalogoRepository(api).obtenerCatalogo()

            resultado.isFailure shouldBe true
            (resultado.exceptionOrNull() is IOException) shouldBe true
        }
    }
})
