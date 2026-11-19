package com.example.appsolicitudes_grupo12.model.solicitud

// Prioridades de una solicitud. "texto" es lo que se guarda en Room y se muestra en pantalla.
enum class PrioridadSolicitud(val texto: String) {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta"),
    URGENTE("Urgente");

    companion object {
        // Prioridad inicial de toda solicitud nueva.
        val POR_DEFECTO = MEDIA

        // Convierte el texto guardado en Room; si no coincide, devuelve la prioridad por defecto.
        fun desdeTexto(texto: String?): PrioridadSolicitud {
            return entries.firstOrNull { it.texto == texto } ?: POR_DEFECTO
        }
    }
}