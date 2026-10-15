package com.example.appsolicitudes_grupo12.model

import com.example.appsolicitudes_grupo12.data.local.SolicitudEntity

// Representa los 4 estados posibles de la pantalla de la lista.
// "sealed class" significa que solo puede ser UNO de estos cuatro a la vez,
// y que no se pueden inventar otros fuera de este archivo.
// Así la pantalla siempre sabe exactamente qué debe mostrar.
sealed class ListaSolicitudesUiState {

    // Se están leyendo las solicitudes de la base de datos.
    // La pantalla muestra un indicador de carga.
    data object Loading : ListaSolicitudesUiState()

    // Hay solicitudes guardadas. Trae la lista para dibujarla.
    data class Success(val solicitudes: List<SolicitudEntity>) : ListaSolicitudesUiState()

    // La lectura fue bien, pero todavía no hay ninguna solicitud.
    // La pantalla muestra el mensaje "Aún no hay solicitudes".
    data object Empty : ListaSolicitudesUiState()

    // Algo falló al leer los datos. Trae un mensaje para mostrar al usuario
    // (junto con el botón "Reintentar"), en vez de cerrar la app.
    data class Error(val mensaje: String) : ListaSolicitudesUiState()
}