package com.example.appsolicitudes_grupo12.model

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
    val evidenciaUri: String? = null,      // foto opcional (se usará con la cámara)

    // --- Errores de validación, uno por campo ---
    val errores: SolicitudErrores = SolicitudErrores(),

    // --- Estado del guardado ---
    val guardando: Boolean = false,              // true mientras se guarda (Loading)
    val guardadoExitosamente: Boolean = false,   // true cuando se guardó bien (Success)
    val codigoGenerado: String? = null,          // código de la solicitud recién creada
    val errorGuardado: String? = null            // mensaje si falló el guardado (Error)
)