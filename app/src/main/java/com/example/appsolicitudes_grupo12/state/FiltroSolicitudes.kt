package com.example.appsolicitudes_grupo12.state

// Formas de ordenar la lista por fecha. "etiqueta" es el texto que se muestra.
enum class OrdenSolicitudes(val etiqueta: String) {
    RECIENTES("Más recientes"),
    ANTIGUAS("Más antiguas")
}

// Filtros y orden elegidos en la lista. null en un filtro = sin filtrar por ese dato.
data class FiltroSolicitudes(
    val categoria: String? = null,
    val estado: String? = null,
    val prioridad: String? = null,
    val clienteId: Int? = null,
    // Id del técnico, o SIN_ASIGNAR para las solicitudes sin técnico
    val responsableId: Int? = null,
    // Texto de la búsqueda (vacío = sin buscar)
    val texto: String = "",
    val orden: OrdenSolicitudes = OrdenSolicitudes.RECIENTES
) {
    // True si hay algún filtro activo (el orden no cuenta)
    val hayFiltros: Boolean
        get() = categoria != null || estado != null || prioridad != null ||
                clienteId != null || responsableId != null || texto.isNotBlank()

    companion object {
        const val SIN_ASIGNAR = -1
    }
}