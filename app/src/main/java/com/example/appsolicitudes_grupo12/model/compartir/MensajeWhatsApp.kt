package com.example.appsolicitudes_grupo12.model.compartir

// Arma el mensaje que se "comparte" por WhatsApp (simulado). No usa Android.
// Solo lleva código, título, categoría y un enlace ficticio: sin descripción ni datos personales.
object MensajeWhatsApp {

    // Dominio de ejemplo: el enlace no existe
    private const val ENLACE_BASE = "https://seguimiento.solicitudes.example/"

    fun enlace(codigo: String): String = ENLACE_BASE + codigo

    fun armar(codigo: String, titulo: String, categoria: String): String =
        "Hola, te comparto la referencia de mi solicitud:\n" +
                "Código: $codigo\n" +
                "Título: $titulo\n" +
                "Categoría: $categoria\n" +
                "Seguimiento: ${enlace(codigo)}"
}