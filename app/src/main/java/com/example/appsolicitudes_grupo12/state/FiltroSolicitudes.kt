package com.example.appsolicitudes_grupo12.state

// Formas de ordenar la lista por fecha. "etiqueta" es el texto que se muestra.
enum class OrdenSolicitudes(val etiqueta: String) {
    RECIENTES("Más recientes"),
    ANTIGUAS("Más antiguas")
}

// Filtro y orden elegidos en la lista. categoria = null muestra todas.
data class FiltroSolicitudes(
    val categoria: String? = null,
    val orden: OrdenSolicitudes = OrdenSolicitudes.RECIENTES
)