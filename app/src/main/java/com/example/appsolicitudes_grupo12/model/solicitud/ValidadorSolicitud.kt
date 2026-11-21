package com.example.appsolicitudes_grupo12.model.solicitud

import com.example.appsolicitudes_grupo12.state.FormularioErrores

// Reglas de validación compartidas por el formulario y la edición de solicitudes.
object ValidadorSolicitud {

    // Revisa título, descripción y categoría (null = campo válido). El servicio es opcional.
    fun validar(
        titulo: String,
        descripcion: String,
        categoria: String
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
            } else null
        )
    }
}