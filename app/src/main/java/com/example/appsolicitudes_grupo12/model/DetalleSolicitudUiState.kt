package com.example.appsolicitudes_grupo12.model

import com.example.appsolicitudes_grupo12.data.local.SolicitudEntity

// Representa los estados posibles de la pantalla de detalle.
// Igual que ListaSolicitudesUiState: es una "sealed class", así que solo puede ser
// UNO de estos cuatro a la vez y la pantalla siempre sabe qué debe mostrar.
sealed class DetalleSolicitudUiState {

    // Se está leyendo la solicitud de la base de datos.
    // La pantalla muestra un indicador de carga.
    data object Loading : DetalleSolicitudUiState()

    // La solicitud se leyó bien. Trae los datos guardados para mostrarlos.
    // "edicion" decide si la pantalla está en modo lectura o en modo edición:
    //   - edicion = null      -> modo lectura (se ven los datos y el botón "Editar")
    //   - edicion = algo      -> modo edición (se ven los campos para modificar)
    data class Success(
        val solicitud: SolicitudEntity,
        val edicion: EdicionSolicitud? = null
    ) : DetalleSolicitudUiState()

    // El id de la solicitud no existe en la base de datos.
    // La pantalla muestra un mensaje y un botón para volver.
    data object NoEncontrada : DetalleSolicitudUiState()

    // Algo falló al leer los datos. Trae un mensaje para mostrar al usuario
    // (junto con el botón "Reintentar"), en vez de cerrar la app.
    data class Error(val mensaje: String) : DetalleSolicitudUiState()
}

// Lo que necesita la pantalla mientras la persona edita la solicitud.
// No es la tabla de la base de datos (esa es SolicitudEntity): aquí vive lo que
// se está escribiendo, los errores y el estado del guardado.
// Funciona igual que SolicitudUiState, pero parte con los valores ya guardados.
// Es inmutable: para cambiarlo se crea una copia con copy().
data class EdicionSolicitud(

    // --- Lo que la persona está modificando (solo los 4 datos básicos) ---
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val servicio: String,

    // --- Errores de validación, uno por campo (se reutiliza SolicitudErrores) ---
    val errores: SolicitudErrores = SolicitudErrores(),

    // --- Estado del guardado ---
    val guardando: Boolean = false,       // true mientras se guardan los cambios (Loading)
    val errorGuardado: String? = null     // mensaje si falló el guardado (Error)
)