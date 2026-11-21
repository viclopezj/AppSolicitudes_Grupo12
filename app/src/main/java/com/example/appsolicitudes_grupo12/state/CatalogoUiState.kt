package com.example.appsolicitudes_grupo12.state

import com.example.appsolicitudes_grupo12.model.catalogo.CategoriaCatalogo

// Estados posibles del catálogo (solo uno a la vez).
sealed class CatalogoUiState {

    // Pidiendo el catálogo a la API.
    data object Loading : CatalogoUiState()

    // Catálogo recibido: categorías para elegir en el formulario.
    data class Success(val categorias: List<CategoriaCatalogo>) : CatalogoUiState()

    // La API respondió, pero sin servicios.
    data object Empty : CatalogoUiState()

    // Falló la conexión o la respuesta: mensaje con botón "Reintentar".
    data class Error(val mensaje: String) : CatalogoUiState()
}