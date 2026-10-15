package com.example.appsolicitudes_grupo12.model

// Agrupa los mensajes de error de cada campo del formulario.
// null  = el campo es válido (no se muestra nada).
// texto = mensaje que se muestra en rojo bajo el campo.
// El ViewModel los llena al validar; la pantalla solo los muestra.
data class SolicitudErrores(
    val titulo: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null,
    val servicio: String? = null
)