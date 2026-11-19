package com.example.appsolicitudes_grupo12.model.solicitud

// Datos fijos de las solicitudes: categorías y largo mínimo de la descripción.
object CategoriasSolicitud {

    // Largo mínimo de la descripción (el equipo puede cambiarlo)
    const val DESCRIPCION_MINIMA = 10

    // Categorías sugeridas por el caso
    val LISTA = listOf(
        "Desarrollo web",
        "Mantenimiento web",
        "Incidente técnico",
        "Hosting",
        "Soporte de correo",
        "Marketing digital",
        "Solicitud de cambio",
        "Consulta",
        "Acceso o configuración",
        "Otro requerimiento"
    )

    // Categorías donde el servicio relacionado NO es obligatorio
    val SIN_SERVICIO = setOf("Consulta", "Otro requerimiento")
}
