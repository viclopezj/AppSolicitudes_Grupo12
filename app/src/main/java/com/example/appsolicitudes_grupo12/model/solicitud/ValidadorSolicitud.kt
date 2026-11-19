package com.example.appsolicitudes_grupo12.model.solicitud

import com.example.appsolicitudes_grupo12.state.FormularioErrores

// Reglas de validación compartidas por el formulario y la edición de solicitudes.
object ValidadorSolicitud {

    // Indica si el servicio relacionado es obligatorio para esa categoría.
    fun servicioObligatorio(categoria: String): Boolean {
        return categoria.isNotBlank() && categoria !in CategoriasSolicitud.SIN_SERVICIO
    }

    // Revisa los cuatro campos y devuelve un error por campo (null = campo válido).
    fun validar(
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String
    ): FormularioErrores {
        return FormularioErrores(
            titulo = if (titulo.isBlank()) {
                "El título es obligatorio"
            } else null,

            descripcion = when {
                descripcion.isBlank() -> "La descripción es obligatoria"
                descripcion.trim().length < CategoriasSolicitud.DESCRIPCION_MINIMA ->
                    "La descripción debe tener al menos ${CategoriasSolicitud.DESCRIPCION_MINIMA} caracteres"

                else -> null
            },

            categoria = if (categoria.isBlank()) {
                "Selecciona una categoría"
            } else null,

            servicio = if (servicioObligatorio(categoria) && servicio.isBlank()) {
                "Indica el servicio relacionado"
            } else null
        )
    }
}
