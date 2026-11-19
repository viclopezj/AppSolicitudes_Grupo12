package com.example.appsolicitudes_grupo12.model.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.example.appsolicitudes_grupo12.model.solicitud.PrioridadSolicitud

// Tabla "solicitudes". El índice único impide repetir el código.
@Entity(
    tableName = "solicitudes",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class SolicitudEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Código visible para el cliente, por ejemplo "SOL-1A2B3C4D"
    val codigo: String,

    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val servicio: String,

    // Foto de evidencia (cámara o galería). null = sin foto.
    val evidenciaUri: String? = null,

    val estado: String = "Recibida",

    // Fecha de creación en milisegundos
    val fechaCreacion: Long,

    // Cliente que la registró. null en registros anteriores a la versión 3.
    val creadoPorUsuarioId: Int? = null,

    // Técnico asignado. null = aún sin asignar.
    val asignadoAUsuarioId: Int? = null,

    // Baja, Media, Alta o Urgente (texto de PrioridadSolicitud). Desde la versión 4.
    val prioridad: String = PrioridadSolicitud.POR_DEFECTO.texto,

    // Fecha del último cambio, en milisegundos. Parte igual a la fecha de creación.
    val ultimaActualizacion: Long = fechaCreacion
)