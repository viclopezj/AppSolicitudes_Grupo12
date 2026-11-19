package com.example.appsolicitudes_grupo12.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Ícono de advertencia al final de un campo con error (junto a isError = true),
// para no depender solo del color rojo.
@Composable
fun IconoError() {
    Icon(
        imageVector = Icons.Default.Warning,
        contentDescription = "Campo con error",
        tint = MaterialTheme.colorScheme.error
    )
}