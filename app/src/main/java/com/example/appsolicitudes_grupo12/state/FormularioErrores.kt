package com.example.appsolicitudes_grupo12.state

// Mensaje de error de cada campo del formulario. null = campo válido.
// El servicio relacionado es opcional, por eso no tiene error.
data class FormularioErrores(
    val titulo: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null
)