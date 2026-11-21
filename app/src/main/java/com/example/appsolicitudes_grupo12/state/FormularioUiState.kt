package com.example.appsolicitudes_grupo12.state

import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.solicitud.AdjuntoPendiente

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

    // Imágenes y documentos adicionales (opcionales), ya copiados a la carpeta de la app
    val adjuntos: List<AdjuntoPendiente> = emptyList(),
    val cargandoAdjunto: Boolean = false,
    val errorAdjunto: String? = null,

    // Borrador: true si el cliente tiene uno guardado
    val hayBorrador: Boolean = false,

    // Datos de contacto ficticios del cliente (solo se muestran; vacío = sin datos)
    val empresa: String = "",
    val telefono: String = "",

    // Aviso breve para el snackbar (borrador guardado, recuperado o descartado)
    val mensaje: String? = null,

    // Errores de validación
    val errores: FormularioErrores = FormularioErrores(),

    // Estado del guardado
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,
    // Solicitud recién creada: de ahí salen el código y el QR
    val solicitudCreada: SolicitudEntity? = null,
    val errorGuardado: String? = null
)