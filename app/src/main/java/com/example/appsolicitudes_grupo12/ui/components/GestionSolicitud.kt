package com.example.appsolicitudes_grupo12.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.PrioridadSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.ReglasTransicion

// Prioridad de una solicitud. La asignación de técnico va en el panel de asignación.
@Composable
fun GestionSolicitud(
    solicitud: SolicitudEntity,
    rol: String,
    procesando: Boolean,
    onCambiarPrioridad: (PrioridadSolicitud) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado = EstadoSolicitud.desdeTexto(solicitud.estado)
    // Cerrada y Cancelada ya no se gestionan
    if (estado == null || estado.esFinal) return
    if (!ReglasTransicion.puedeCambiarPrioridad(rol)) return

    val prioridadActual = PrioridadSolicitud.desdeTexto(solicitud.prioridad)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Gestión", style = MaterialTheme.typography.titleMedium)

        Text(text = "Prioridad", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrioridadSolicitud.entries.forEach { prioridad ->
                FilterChip(
                    selected = prioridad == prioridadActual,
                    onClick = {
                        if (prioridad != prioridadActual) onCambiarPrioridad(prioridad)
                    },
                    enabled = !procesando,
                    label = { Text(prioridad.texto) }
                )
            }
        }
    }
}
