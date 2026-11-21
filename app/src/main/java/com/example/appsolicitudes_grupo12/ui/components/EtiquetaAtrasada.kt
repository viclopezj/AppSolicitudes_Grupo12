package com.example.appsolicitudes_grupo12.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Etiqueta roja "Atrasada": se muestra junto al estado cuando venció el plazo de la solicitud.
@Composable
fun EtiquetaAtrasada() {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = "Atrasada",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
