package com.example.appsolicitudes_grupo12.model

import com.example.appsolicitudes_grupo12.data.local.SolicitudEntity

// Representa TODO lo que la pantalla del formulario necesita para dibujarse.
// No es la tabla de la base de datos (esa es SolicitudEntity): aquí vive
// lo que la persona está escribiendo, los errores y el estado del guardado.
// Es inmutable: para cambiarlo se crea una copia con copy().
data class SolicitudUiState(

    // --- Lo que la persona escribe en el formulario ---
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val servicio: String = "",

    // Foto de evidencia (opcional). Es la dirección del archivo guardado dentro de la app,
    // con la forma "file:///.../evidencias/EVI_xxxx.jpg". Puede venir de la cámara o de la galería.
    val evidenciaUri: String? = null,

    // --- Estado de la foto ---
    val cargandoFoto: Boolean = false,           // true mientras se copia la foto de la galería (Loading)
    val errorFoto: String? = null,               // mensaje si falló la foto o falta el permiso (Error)

    // --- Errores de validación, uno por campo ---
    val errores: SolicitudErrores = SolicitudErrores(),

    // --- Estado del guardado ---
    val guardando: Boolean = false,              // true mientras se guarda (Loading)
    val guardadoExitosamente: Boolean = false,   // true cuando se guardó bien (Success)
    // La solicitud recién creada: de ahí salen el código y los datos para dibujar el QR
    val solicitudCreada: SolicitudEntity? = null,
    val errorGuardado: String? = null            // mensaje si falló el guardado (Error)
)