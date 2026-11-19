package com.example.appsolicitudes_grupo12.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Etiqueta de color con el nombre del estado. El color cambia con una animación suave.
@Composable
fun EtiquetaEstado(estado: String) {
    val colores = MaterialTheme.colorScheme

    // Color de fondo y de texto de cada estado
    val (fondo: Color, texto: Color) = when (estado) {
        "Recibida" -> colores.primaryContainer to colores.onPrimaryContainer
        "En revisión", "Pendiente de información" ->
            colores.tertiaryContainer to colores.onTertiaryContainer
        "Asignada", "En proceso", "Resuelta", "Cerrada" ->
            colores.secondaryContainer to colores.onSecondaryContainer
        "Cancelada" -> colores.errorContainer to colores.onErrorContainer
        else -> colores.surfaceVariant to colores.onSurfaceVariant
    }

    val fondoAnimado by animateColorAsState(
        targetValue = fondo,
        animationSpec = tween(durationMillis = 400),
        label = "fondoEtiqueta"
    )
    val textoAnimado by animateColorAsState(
        targetValue = texto,
        animationSpec = tween(durationMillis = 400),
        label = "textoEtiqueta"
    )

    Surface(
        shape = RoundedCornerShape(50),
        color = fondoAnimado
    ) {
        Text(
            text = estado,
            style = MaterialTheme.typography.labelMedium,
            color = textoAnimado,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}