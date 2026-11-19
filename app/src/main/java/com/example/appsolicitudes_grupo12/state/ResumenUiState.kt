package com.example.appsolicitudes_grupo12.state

import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud

// Estados posibles del panel resumen (solo uno a la vez).
sealed class ResumenUiState {

    // Leyendo las solicitudes.
    data object Loading : ResumenUiState()

    // Total y cantidad por cada estado (también los que están en 0).
    data class Success(
        val total: Int,
        val porEstado: List<Pair<EstadoSolicitud, Int>>
    ) : ResumenUiState()

    // Falló la lectura: mensaje con botón "Reintentar".
    data class Error(val mensaje: String) : ResumenUiState()
}