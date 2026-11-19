package com.example.appsolicitudes_grupo12.model.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

// Tabla "comentarios_solicitud": avances y comentarios de seguimiento.
@Entity(
    tableName = "comentarios_solicitud",
    indices = [Index(value = ["solicitudId"])]
)
data class ComentarioSolicitudEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val solicitudId: Int,

    // Quién lo escribió
    val usuarioId: Int,
    val usuarioNombre: String,
    val rol: String,

    val texto: String,

    // true = comentario interno: el cliente NO lo ve
    val esInterno: Boolean = false,

    // Evidencia opcional que acompaña el comentario (foto del técnico). null = sin foto.
    val evidenciaUri: String? = null,

    // Fecha y hora en milisegundos
    val fecha: Long
)