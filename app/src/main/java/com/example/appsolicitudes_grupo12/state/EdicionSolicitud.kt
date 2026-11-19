package com.example.appsolicitudes_grupo12.state

// Lo que se está editando en el detalle: campos, errores y estado del guardado.
// Es como FormularioUiState, pero parte con los valores ya guardados.
data class EdicionSolicitud(

    // Datos que se pueden modificar
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val servicio: String,

    // Errores de validación, uno por campo
    val errores: FormularioErrores = FormularioErrores(),

    // Estado del guardado
    val guardando: Boolean = false,
    val errorGuardado: String? = null
)