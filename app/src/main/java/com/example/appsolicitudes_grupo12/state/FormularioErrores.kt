package com.example.appsolicitudes_grupo12.state

// Mensaje de error de cada campo del formulario. null = campo válido.
data class FormularioErrores(
    val titulo: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null,
    val servicio: String? = null
)