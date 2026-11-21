package com.example.appsolicitudes_grupo12.model.exportacion

import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.solicitud.MedicionTiempos
import com.example.appsolicitudes_grupo12.model.solicitud.PlazosSolicitud
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Resultado de la exportación FICTICIA: el reporte se arma en memoria y no se envía a ninguna parte.
data class ReporteSimulado(
    val nombreArchivo: String,
    val filas: Int,
    val contenido: String
)

// Arma el reporte de solicitudes en formato CSV. No usa Android, por eso se puede probar sola.
object ExportacionResumen {

    private val COLUMNAS = listOf(
        "codigo", "titulo", "categoria", "estado", "prioridad",
        "responsable", "atrasada", "creada", "tiempo"
    )

    // nombreResponsable: recibe el id del técnico (o null) y devuelve el nombre a mostrar.
    fun generar(
        solicitudes: List<SolicitudEntity>,
        nombreResponsable: (Int?) -> String,
        ahora: Long = System.currentTimeMillis()
    ): ReporteSimulado {
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val formatoArchivo = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

        val lineas = mutableListOf(COLUMNAS.joinToString(","))
        solicitudes.forEach { solicitud ->
            val tiempo = MedicionTiempos.tiempoTranscurridoMs(
                solicitud.estado, solicitud.fechaCreacion, solicitud.ultimaActualizacion, ahora
            )
            val campos = listOf(
                solicitud.codigo,
                solicitud.titulo,
                solicitud.categoria,
                solicitud.estado,
                solicitud.prioridad,
                nombreResponsable(solicitud.asignadoAUsuarioId),
                if (PlazosSolicitud.estaAtrasada(solicitud, ahora)) "si" else "no",
                formatoFecha.format(Date(solicitud.fechaCreacion)),
                MedicionTiempos.formatear(tiempo)
            )
            lineas.add(campos.joinToString(",") { comillas(it) })
        }

        return ReporteSimulado(
            nombreArchivo = "solicitudes_" + formatoArchivo.format(Date(ahora)) + ".csv",
            filas = solicitudes.size,
            contenido = lineas.joinToString("\n")
        )
    }

    // Encierra el texto entre comillas y duplica las comillas internas (formato CSV).
    private fun comillas(texto: String): String {
        return "\"" + texto.replace("\"", "\"\"").replace("\n", " ") + "\""
    }
}
