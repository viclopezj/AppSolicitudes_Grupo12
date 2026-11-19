package com.example.appsolicitudes_grupo12.state

// Estado de las acciones del detalle (cambios de estado, prioridad, asignación).
data class AccionesUiState(
    val procesando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)