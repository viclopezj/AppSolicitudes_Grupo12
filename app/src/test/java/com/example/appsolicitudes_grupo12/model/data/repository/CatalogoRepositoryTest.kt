package com.example.appsolicitudes_grupo12.model.data.repository

import com.example.appsolicitudes_grupo12.model.catalogo.CategoriaCatalogo
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

    test("asigna por orden los nombres de categoría del caso a los productos de la API") {
        runTest {
            val api = mockk<ApiService>()
            coEvery { api.obtenerProductos(any(), any()) } returns ProductosRespuesta(
                products = listOf(
                    ProductoDto(id = 11, title = "Producto A"),
                    ProductoDto(id = 12, title = "Producto B")
                )
            )

            val resultado = CatalogoRepository(api).obtenerCatalogo()

            resultado.isSuccess shouldBe true
            resultado.getOrNull() shouldBe listOf(
                CategoriaCatalogo(id = 11, nombre = "Desarrollo web"),
                CategoriaCatalogo(id = 12, nombre = "Mantenimiento web")
            )
            // Se pide el catálogo con el límite y los campos acordados
            coVerify(exactly = 1) {
                api.obtenerProductos(10, "title,category,description")
            }
        }
    }

    test("descarta los productos sin id o sin título") {
        runTest {
            val api = mockk<ApiService>()
            coEvery { api.obtenerProductos(any(), any()) } returns ProductosRespuesta(
                products = listOf(
                    ProductoDto(id = null, title = "Sin id"),
                    ProductoDto(id = 2, title = "   "),
                    ProductoDto(id = 3, title = "Válido")
                )
            )

            val resultado = CatalogoRepository(api).obtenerCatalogo()

            resultado.getOrNull() shouldBe listOf(
                CategoriaCatalogo(id = 3, nombre = "Desarrollo web")
            )
        }
    }

    test("una respuesta sin productos da una lista vacía, no un error") {
        runTest {
            val api = mockk<ApiService>()
            coEvery { api.obtenerProductos(any(), any()) } returns ProductosRespuesta(products = null)

            val resultado = CatalogoRepository(api).obtenerCatalogo()

            resultado.isSuccess shouldBe true
            resultado.getOrNull() shouldBe emptyList<CategoriaCatalogo>()
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