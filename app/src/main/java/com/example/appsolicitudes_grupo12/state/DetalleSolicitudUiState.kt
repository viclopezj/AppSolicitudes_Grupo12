package com.example.appsolicitudes_grupo12.state

import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity

// Estados posibles de la pantalla de detalle (solo uno a la vez).
sealed class DetalleSolicitudUiState {

    // Leyendo la solicitud.
    data object Loading : DetalleSolicitudUiState()

    // Solicitud leída. edicion = null es modo lectura; con valor, modo edición.
    data class Success(
        val solicitud: SolicitudEntity,
        val edicion: EdicionSolicitud? = null
    ) : DetalleSolicitudUiState()

    // El id no existe o el usuario no tiene acceso.
    data object NoEncontrada : DetalleSolicitudUiState()

    // Falló la lectura: mensaje con botón "Reintentar".
    data class Error(val mensaje: String) : DetalleSolicitudUiState()
}