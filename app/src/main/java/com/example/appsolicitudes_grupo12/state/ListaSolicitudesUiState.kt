package com.example.appsolicitudes_grupo12.state

import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity

// Estados posibles de la pantalla de lista (solo uno a la vez).
sealed class ListaSolicitudesUiState {

    // Leyendo las solicitudes.
    data object Loading : ListaSolicitudesUiState()

    // Hay solicitudes. Trae la lista ya filtrada y ordenada, y el filtro vigente.
    // La lista puede estar vacía si ninguna coincide con el filtro (eso no es Empty).
    data class Success(
        val solicitudes: List<SolicitudEntity>,
        val filtro: FiltroSolicitudes = FiltroSolicitudes()
    ) : ListaSolicitudesUiState()

    // La lectura salió bien, pero no hay ninguna solicitud.
    data object Empty : ListaSolicitudesUiState()

    // Falló la lectura: mensaje con botón "Reintentar".
    data class Error(val mensaje: String) : ListaSolicitudesUiState()
}