package com.example.appsolicitudes_grupo12.state

import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.PrioridadSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.ResumenTiempos

// Estados posibles del panel resumen (solo uno a la vez).
sealed class ResumenUiState {

    // Leyendo las solicitudes.
    data object Loading : ResumenUiState()

    // Total, cantidad por estado, por prioridad y por responsable (también los que están en 0),
    // solicitudes atrasadas y medición simulada de tiempos.
    data class Success(
        val total: Int,
        val porEstado: List<Pair<EstadoSolicitud, Int>>,
        val atrasadas: Int = 0,
        val porPrioridad: List<Pair<PrioridadSolicitud, Int>> = emptyList(),
        // Nombre del técnico (o "Sin asignar") y cantidad de solicitudes
        val porResponsable: List<Pair<String, Int>> = emptyList(),
        val tiempos: ResumenTiempos = ResumenTiempos()
    ) : ResumenUiState()

    // Falló la lectura: mensaje con botón "Reintentar".
    data class Error(val mensaje: String) : ResumenUiState()
}