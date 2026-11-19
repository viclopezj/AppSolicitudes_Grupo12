package com.example.appsolicitudes_grupo12.state

import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity

// Todo lo que necesita la pantalla del formulario: campos, foto, errores y guardado.
data class FormularioUiState(

    // Campos del formulario
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val servicio: String = "",

    // Foto de evidencia (opcional): dirección del archivo guardado en la app
    val evidenciaUri: String? = null,
    val cargandoFoto: Boolean = false,
    val errorFoto: String? = null,

    // Errores de validación
    val errores: FormularioErrores = FormularioErrores(),

    // Estado del guardado
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,
    // Solicitud recién creada: de ahí salen el código y el QR
    val solicitudCreada: SolicitudEntity? = null,
    val errorGuardado: String? = null
)