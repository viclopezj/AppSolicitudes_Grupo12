package com.example.appsolicitudes_grupo12.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

// Representa la TABLA "solicitudes". Cada propiedad es una columna.
// El índice único en "codigo" impide guardar dos solicitudes con el mismo código.
@Entity(
    tableName = "solicitudes",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class SolicitudEntity(

    // Identificador interno. Room lo genera solo (1, 2, 3...)
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Código visible para el cliente, por ejemplo "SOL-1A2B3C4D"
    val codigo: String,

    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val servicio: String,

    // Ruta de la foto de evidencia (tomada con la cámara o elegida de la galería).
    // Es opcional: null significa que la solicitud no tiene foto.
    val evidenciaUri: String? = null,

    // Toda solicitud nueva comienza como "Recibida"
    val estado: String = "Recibida",

    // Fecha y hora de creación en milisegundos (System.currentTimeMillis()).
    // Usamos Long para no necesitar conversores de fecha
    val fechaCreacion: Long,

    // Usuario que registró la solicitud. Los registros anteriores a la versión 3
    // quedan en null; los nuevos siempre reciben el id del cliente que inició sesión.
    val creadoPorUsuarioId: Int? = null,

    // Técnico asignado por el administrador. null significa que aún no se asigna.
    val asignadoAUsuarioId: Int? = null
)
