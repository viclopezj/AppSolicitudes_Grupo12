package com.example.appsolicitudes_grupo12.model.qr

// Datos de una solicitud que viajan dentro del QR (copia reducida, sin la foto).
data class DatosQr(
    val codigo: String,
    val titulo: String,
    val descripcion: String,    // se corta a unos 300 caracteres
    val categoria: String,
    val servicio: String,
    val estado: String,
    val fechaCreacion: Long     // milisegundos, igual que en SolicitudEntity
)