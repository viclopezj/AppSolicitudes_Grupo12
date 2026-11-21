package com.example.appsolicitudes_grupo12.model.solicitud

// Tipos de archivo que se pueden adjuntar a una solicitud.
object TiposAdjunto {
    const val IMAGEN = "imagen"
    const val DOCUMENTO = "documento"
}

// Un archivo ya copiado a la carpeta privada de la app, listo para guardarse con la solicitud.
data class AdjuntoPendiente(
    // Nombre que ve la persona, por ejemplo "informe.pdf"
    val nombre: String,
    // TiposAdjunto.IMAGEN o TiposAdjunto.DOCUMENTO
    val tipo: String,
    // Dirección del archivo guardado en la app (la misma forma que la evidencia)
    val uri: String
)
