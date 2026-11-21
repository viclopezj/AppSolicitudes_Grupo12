package com.example.appsolicitudes_grupo12.model.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

// Tabla "adjuntos_solicitud": imágenes y documentos adicionales de una solicitud (desde la versión 6).
@Entity(
    tableName = "adjuntos_solicitud",
    indices = [Index(value = ["solicitudId"])]
)
data class AdjuntoEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val solicitudId: Int,

    // Quién lo adjuntó
    val usuarioId: Int,
    val usuarioNombre: String,

    // Nombre visible, "imagen" o "documento", y dirección del archivo guardado en la app
    val nombre: String,
    val tipo: String,
    val uri: String,

    // Fecha y hora en milisegundos
    val fecha: Long
)
