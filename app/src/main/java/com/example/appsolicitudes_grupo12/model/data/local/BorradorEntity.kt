package com.example.appsolicitudes_grupo12.model.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

// Tabla "borradores_solicitud": el borrador del formulario de cada cliente (uno por cliente).
// Guarda solo el texto; las fotos y los archivos no se guardan en el borrador. Desde la versión 6.
@Entity(tableName = "borradores_solicitud")
data class BorradorEntity(

    // Cliente dueño del borrador (también es la clave: solo hay uno por cliente)
    @PrimaryKey
    val usuarioId: Int,

    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val servicio: String,

    // Cuándo se guardó, en milisegundos
    val fecha: Long
)
