package com.example.appsolicitudes_grupo12.model.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

// Tabla "historial_solicitud": una fila por cada cambio hecho a una solicitud.
@Entity(
    tableName = "historial_solicitud",
    indices = [Index(value = ["solicitudId"])]
)
data class HistorialSolicitudEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val solicitudId: Int,

    // Estado antes del cambio. null cuando la solicitud se registra.
    val estadoAnterior: String? = null,

    val estadoNuevo: String,

    // Texto simple, por ejemplo "Solicitud registrada", "Cambio de estado",
    // "Técnico asignado", "Técnico reasignado", "Prioridad modificada" o "Solicitud cancelada"
    val accion: String,

    // Motivo o detalle (por ejemplo el motivo de cancelación). null si no hay.
    val comentario: String? = null,

    // Perfil y usuario que hicieron el cambio
    val rol: String,
    val usuarioId: Int,
    val usuarioNombre: String,

    // Fecha y hora del cambio, en milisegundos
    val fecha: Long
)