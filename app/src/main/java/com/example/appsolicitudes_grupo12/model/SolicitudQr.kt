package com.example.appsolicitudes_grupo12.model

// Los datos de una solicitud que viajan DENTRO del código QR.
// No es la tabla de la base de datos (esa es SolicitudEntity): es una copia
// reducida de lo que se quiere mostrar al leer el QR en cualquier teléfono.
// No incluye la foto, porque su archivo solo existe en el teléfono que la tomó.
data class SolicitudQr(
    val codigo: String,
    val titulo: String,
    val descripcion: String,    // se corta a unos 300 caracteres para que el QR sea legible
    val categoria: String,
    val servicio: String,
    val estado: String,
    val fechaCreacion: Long     // milisegundos, igual que en SolicitudEntity
)