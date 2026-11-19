package com.example.appsolicitudes_grupo12.ui.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Milisegundos a texto legible, por ejemplo "17/10/2026 14:35".
fun formatearFecha(milisegundos: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formato.format(Date(milisegundos))
}

// Nombre visible de un perfil.
fun etiquetaRol(rol: String): String = when (rol) {
    "administrador" -> "Administrador"
    "tecnico" -> "Técnico"
    "cliente" -> "Cliente"
    else -> "Sin perfil"
}