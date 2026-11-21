package com.example.appsolicitudes_grupo12.state

// Estado de la sección de adjuntos del detalle: copia en curso y mensajes.
data class AdjuntosUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)
