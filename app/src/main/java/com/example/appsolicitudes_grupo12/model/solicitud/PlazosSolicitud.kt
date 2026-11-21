package com.example.appsolicitudes_grupo12.model.solicitud

import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity

// Plazos de atención ficticios según la prioridad y regla de "solicitud atrasada".
// Una solicitud está atrasada cuando sigue abierta y ya pasó el plazo de su prioridad,
// contado desde su fecha de creación. No usa la base de datos, por eso se puede probar sola.
object PlazosSolicitud {

    // Duración de un "día" del plazo. Para probar los atrasos en el emulador sin esperar días
    // se puede bajar (por ejemplo a 60_000L = 1 minuto) y dejar en 24 horas al terminar.
    const val DIA_MS: Long = 24L * 60 * 60 * 1000

    // Días de plazo de cada prioridad (datos ficticios del equipo).
    fun plazoEnDias(prioridad: PrioridadSolicitud): Int = when (prioridad) {
        PrioridadSolicitud.URGENTE -> 1
        PrioridadSolicitud.ALTA -> 2
        PrioridadSolicitud.MEDIA -> 5
        PrioridadSolicitud.BAJA -> 7
    }

    fun plazoMs(prioridad: PrioridadSolicitud): Long = plazoEnDias(prioridad) * DIA_MS

    // El plazo corre mientras el equipo tiene la solicitud en sus manos. Pendiente de información
    // (espera al cliente), Resuelta, Cerrada y Cancelada no se consideran atrasadas.
    fun correPlazo(estado: EstadoSolicitud): Boolean {
        return estado == EstadoSolicitud.RECIBIDA ||
                estado == EstadoSolicitud.EN_REVISION ||
                estado == EstadoSolicitud.ASIGNADA ||
                estado == EstadoSolicitud.EN_PROCESO
    }

    fun estaAtrasada(
        estadoTexto: String,
        prioridadTexto: String?,
        fechaCreacion: Long,
        ahora: Long = System.currentTimeMillis()
    ): Boolean {
        val estado = EstadoSolicitud.desdeTexto(estadoTexto) ?: return false
        if (!correPlazo(estado)) return false
        val prioridad = PrioridadSolicitud.desdeTexto(prioridadTexto)
        return ahora - fechaCreacion > plazoMs(prioridad)
    }

    fun estaAtrasada(solicitud: SolicitudEntity, ahora: Long = System.currentTimeMillis()): Boolean {
        return estaAtrasada(solicitud.estado, solicitud.prioridad, solicitud.fechaCreacion, ahora)
    }
}
