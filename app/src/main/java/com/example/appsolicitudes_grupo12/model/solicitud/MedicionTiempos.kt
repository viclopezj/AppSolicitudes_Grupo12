package com.example.appsolicitudes_grupo12.model.solicitud

import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity

// Promedios de la medición de tiempos del panel. null = todavía no hay solicitudes para medir.
data class ResumenTiempos(
    val promedioAtencionMs: Long? = null,
    val atendidas: Int = 0,
    val promedioAbiertasMs: Long? = null,
    val abiertas: Int = 0
)

// Medición SIMULADA de tiempos: se estima con la fecha de creación y la última actualización,
// sin cronómetros reales. No usa Android, por eso se puede probar sola.
object MedicionTiempos {

    private const val MINUTO_MS = 60_000L
    private const val HORA_MS = 60 * MINUTO_MS
    private const val DIA_REAL_MS = 24 * HORA_MS

    // Tiempo que lleva (o llevó) una solicitud: abierta = hasta ahora; terminada = hasta su último cambio.
    fun tiempoTranscurridoMs(
        estadoTexto: String,
        fechaCreacion: Long,
        ultimaActualizacion: Long,
        ahora: Long = System.currentTimeMillis()
    ): Long {
        val estado = EstadoSolicitud.desdeTexto(estadoTexto)
        val terminada = estado == EstadoSolicitud.RESUELTA ||
                estado == EstadoSolicitud.CERRADA ||
                estado == EstadoSolicitud.CANCELADA
        val fin = if (terminada) ultimaActualizacion else ahora
        return (fin - fechaCreacion).coerceAtLeast(0L)
    }

    // Atendidas = Resuelta o Cerrada. Abiertas = todas las que no terminaron (Cancelada no cuenta).
    fun resumir(
        solicitudes: List<SolicitudEntity>,
        ahora: Long = System.currentTimeMillis()
    ): ResumenTiempos {
        val atendidas = solicitudes
            .filter {
                it.estado == EstadoSolicitud.RESUELTA.texto || it.estado == EstadoSolicitud.CERRADA.texto
            }
            .map { tiempoTranscurridoMs(it.estado, it.fechaCreacion, it.ultimaActualizacion, ahora) }

        val abiertas = solicitudes
            .filter {
                val estado = EstadoSolicitud.desdeTexto(it.estado)
                estado != null && estado != EstadoSolicitud.RESUELTA && !estado.esFinal
            }
            .map { tiempoTranscurridoMs(it.estado, it.fechaCreacion, it.ultimaActualizacion, ahora) }

        return ResumenTiempos(
            promedioAtencionMs = if (atendidas.isEmpty()) null else atendidas.sum() / atendidas.size,
            atendidas = atendidas.size,
            promedioAbiertasMs = if (abiertas.isEmpty()) null else abiertas.sum() / abiertas.size,
            abiertas = abiertas.size
        )
    }

    // Duración a texto corto: "2 d 3 h", "5 h 10 min" o "12 min".
    fun formatear(milisegundos: Long): String {
        val ms = milisegundos.coerceAtLeast(0L)
        val dias = ms / DIA_REAL_MS
        val horas = (ms % DIA_REAL_MS) / HORA_MS
        val minutos = (ms % HORA_MS) / MINUTO_MS
        return when {
            dias > 0 -> "$dias d $horas h"
            horas > 0 -> "$horas h $minutos min"
            else -> "$minutos min"
        }
    }
}
