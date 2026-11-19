package com.example.appsolicitudes_grupo12.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appsolicitudes_grupo12.model.data.local.HistorialSolicitudEntity

// Lista cronológica de los cambios de una solicitud.
@Composable
fun HistorialSolicitud(
    historial: List<HistorialSolicitudEntity>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Historial", style = MaterialTheme.typography.titleMedium)

        if (historial.isEmpty()) {
            Text(
                text = "Aún no hay movimientos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            historial.forEach { registro -> FilaHistorial(registro) }
        }
    }
}

// Un movimiento: acción, cambio de estado, comentario, y quién y cuándo.
@Composable
private fun FilaHistorial(registro: HistorialSolicitudEntity) {
    val anterior = registro.estadoAnterior
    val cambioDeEstado = when {
        anterior == null -> registro.estadoNuevo
        anterior != registro.estadoNuevo -> "$anterior → ${registro.estadoNuevo}"
        else -> null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = registro.accion, style = MaterialTheme.typography.titleSmall)

            cambioDeEstado?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            registro.comentario?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }

            Text(
                text = "${registro.usuarioNombre} (${etiquetaRol(registro.rol)}) · " +
                        formatearFecha(registro.fecha),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}