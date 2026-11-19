package com.example.appsolicitudes_grupo12.state

import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.data.local.UsuarioEntity

// Estado de la pantalla de asignaciones: datos cargados, asignación en curso y avisos.
data class AsignarSolicitudesUiState(
    val cargando: Boolean = true,
    val solicitudes: List<SolicitudEntity> = emptyList(),
    val tecnicos: List<UsuarioEntity> = emptyList(),
    val solicitudAsignandoId: Int? = null,
    val error: String? = null,
    val mensaje: String? = null
)
