package com.example.appsolicitudes_grupo12.model.catalogo

// Un servicio del catálogo externo: ID, Servicio, Categoría y Descripción (como en el PNG).
data class ServicioCatalogo(
    val id: Int,
    val servicio: String,
    val categoria: String,
    val descripcion: String
)