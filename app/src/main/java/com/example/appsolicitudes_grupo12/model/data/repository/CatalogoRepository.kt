package com.example.appsolicitudes_grupo12.model.data.repository

import com.example.appsolicitudes_grupo12.model.catalogo.CategoriaCatalogo
import com.example.appsolicitudes_grupo12.model.data.remote.ApiService
import com.example.appsolicitudes_grupo12.model.solicitud.CategoriasSolicitud
import kotlinx.coroutines.CancellationException

// Intermediario entre el ViewModel y la API del catálogo. El servicio se inyecta por constructor.
class CatalogoRepository(private val api: ApiService) {

    // Pide el catálogo y lo convierte en categorías. Un error de red llega como Result.failure.
    suspend fun obtenerCatalogo(): Result<List<CategoriaCatalogo>> {
        return try {
            val respuesta = api.obtenerProductos(LIMITE, CAMPOS)
            val nombres = CategoriasSolicitud.LISTA

            // Solo valen los productos con id y título; el orden los asigna a los nombres del caso
            val categorias = respuesta.products.orEmpty()
                .filter { !it.title.isNullOrBlank() }
                .mapNotNull { it.id }
                .take(nombres.size)
                .mapIndexed { posicion, id -> CategoriaCatalogo(id = id, nombre = nombres[posicion]) }

            Result.success(categorias)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        const val LIMITE = 10
        const val CAMPOS = "title,category,description"
    }
}