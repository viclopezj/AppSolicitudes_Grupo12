package com.example.appsolicitudes_grupo12.model

// Las dos formas de ordenar la lista de solicitudes por fecha de creación.
// "etiqueta" es el texto que se muestra en pantalla.
enum class OrdenSolicitudes(val etiqueta: String) {
    RECIENTES("Más recientes"),   // la más nueva primero
    ANTIGUAS("Más antiguas")      // la más vieja primero
}

// Lo que la persona eligió para filtrar y ordenar la lista.
// No es la tabla de la base de datos: es solo la configuración de la vista.
// Es inmutable: para cambiarla se crea una copia con copy().
data class FiltroSolicitudes(
    // null = sin filtro, se muestran todas las categorías
    val categoria: String? = null,
    val orden: OrdenSolicitudes = OrdenSolicitudes.RECIENTES
)