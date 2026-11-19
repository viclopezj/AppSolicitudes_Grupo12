package com.example.appsolicitudes_grupo12.model.solicitud

// Estados de una solicitud. "texto" es lo que se guarda en Room y se muestra en pantalla.
enum class EstadoSolicitud(val texto: String) {
    RECIBIDA("Recibida"),
    EN_REVISION("En revisión"),
    PENDIENTE_INFORMACION("Pendiente de información"),
    ASIGNADA("Asignada"),
    EN_PROCESO("En proceso"),
    RESUELTA("Resuelta"),
    CERRADA("Cerrada"),
    CANCELADA("Cancelada");

    // Cerrada y Cancelada no tienen salida.
    val esFinal: Boolean
        get() = this == CERRADA || this == CANCELADA

    companion object {
        // Convierte el texto guardado en Room; null si no corresponde a ningún estado.
        fun desdeTexto(texto: String): EstadoSolicitud? {
            return entries.firstOrNull { it.texto == texto }
        }
    }
}