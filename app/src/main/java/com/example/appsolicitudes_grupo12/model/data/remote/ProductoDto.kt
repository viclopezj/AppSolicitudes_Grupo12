package com.example.appsolicitudes_grupo12.model.data.remote

// Un producto tal como lo entrega DummyJSON. Los campos son nulables porque la API podría omitirlos.
data class ProductoDto(
    val id: Int? = null,
    val title: String? = null,
    val category: String? = null,
    val description: String? = null
)

// Respuesta de GET /products: los productos vienen dentro de un objeto contenedor.
data class ProductosRespuesta(
    val products: List<ProductoDto>? = null,
    val total: Int? = null
)