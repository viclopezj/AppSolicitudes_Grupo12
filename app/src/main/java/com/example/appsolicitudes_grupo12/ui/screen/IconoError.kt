package com.example.appsolicitudes_grupo12.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Ícono de advertencia que se muestra al final de un campo cuando tiene un error.
// Se usa junto con isError = true: además del borde rojo y del mensaje, el ícono ayuda
// a notar el error sin depender solo del color.
@Composable
fun IconoError() {
    Icon(
        imageVector = Icons.Default.Warning,
        contentDescription = "Campo con error",
        tint = MaterialTheme.colorScheme.error
    )
}
