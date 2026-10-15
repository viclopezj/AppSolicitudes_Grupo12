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

    // Ruta de la foto de evidencia. Es opcional (null) y se usará cuando
    // agreguemos la cámara
    val evidenciaUri: String? = null,

    // Toda solicitud nueva comienza como "Recibida"
    val estado: String = "Recibida",

    // Fecha y hora de creación en milisegundos (System.currentTimeMillis()).
    // Usamos Long para no necesitar conversores de fecha
    val fechaCreacion: Long
)