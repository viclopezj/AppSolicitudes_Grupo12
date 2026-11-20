package com.example.appsolicitudes_grupo12.model.data.repository

import com.example.appsolicitudes_grupo12.model.catalogo.ServicioCatalogo
import com.example.appsolicitudes_grupo12.model.data.remote.ApiService
import com.example.appsolicitudes_grupo12.model.data.remote.ProductoDto
import kotlinx.coroutines.CancellationException

// Intermediario entre el ViewModel y la API del catálogo. El servicio se inyecta por constructor.
class CatalogoRepository(private val api: ApiService) {

    // Pide el catálogo y lo convierte a ServicioCatalogo. Un error de red llega como Result.failure.
    suspend fun obtenerCatalogo(): Result<List<ServicioCatalogo>> {
        return try {
            val respuesta = api.obtenerProductos(LIMITE, CAMPOS)
            val servicios = respuesta.products.orEmpty().mapNotNull { it.aServicio() }
            Result.success(servicios)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Convierte un producto de la API; se descartan los que vienen sin id o sin nombre.
    private fun ProductoDto.aServicio(): ServicioCatalogo? {
        val idProducto = id ?: return null
        val nombre = title?.trim().orEmpty()
        if (nombre.isEmpty()) return null

        return ServicioCatalogo(
            id = idProducto,
            servicio = nombre,
            categoria = formatearCategoria(category),
            descripcion = description?.trim().orEmpty()
        )
    }

    // "home-decoration" -> "Home decoration"
    private fun formatearCategoria(texto: String?): String {
        val limpio = texto?.trim().orEmpty().replace('-', ' ')
        return if (limpio.isEmpty()) "Sin categoría" else limpio.replaceFirstChar { it.uppercase() }
    }

    private companion object {
        const val LIMITE = 10
        const val CAMPOS = "title,category,description"
    }
}