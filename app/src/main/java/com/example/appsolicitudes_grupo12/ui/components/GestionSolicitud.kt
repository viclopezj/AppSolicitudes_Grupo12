package com.example.appsolicitudes_grupo12.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.data.local.UsuarioEntity
import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.PrioridadSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.ReglasTransicion

// Prioridad y técnico responsable de una solicitud. Lo que se muestra depende del perfil y del estado.
@Composable
fun GestionSolicitud(
    solicitud: SolicitudEntity,
    rol: String,
    tecnicos: List<UsuarioEntity>,
    procesando: Boolean,
    onCambiarPrioridad: (PrioridadSolicitud) -> Unit,
    onAsignarTecnico: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuAbierto by remember { mutableStateOf(false) }
    var tecnicoElegido by remember(solicitud.id, solicitud.asignadoAUsuarioId) {
        mutableStateOf(solicitud.asignadoAUsuarioId)
    }

    val estado = EstadoSolicitud.desdeTexto(solicitud.estado)
    // Cerrada y Cancelada ya no se gestionan
    if (estado == null || estado.esFinal) return

    val puedePrioridad = ReglasTransicion.puedeCambiarPrioridad(rol)
    val puedeAsignar = ReglasTransicion.puedeAsignar(rol, estado) ||
            ReglasTransicion.puedeReasignar(rol, estado)
    if (!puedePrioridad && !puedeAsignar) return

    val prioridadActual = PrioridadSolicitud.desdeTexto(solicitud.prioridad)
    val nombreActual = tecnicos.firstOrNull { it.id == solicitud.asignadoAUsuarioId }?.nombre
    val nombreElegido = tecnicos.firstOrNull { it.id == tecnicoElegido }?.nombre

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Gestión", style = MaterialTheme.typography.titleMedium)

        if (puedePrioridad) {
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

        if (puedeAsignar) {
            Text(
                text = "Técnico asignado: ${nombreActual ?: "Sin asignar"}",
                style = MaterialTheme.typography.bodyMedium
            )

            Box {
                OutlinedButton(
                    onClick = { menuAbierto = true },
                    enabled = !procesando && tecnicos.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(nombreElegido ?: "Seleccionar técnico ▾")
                }
                DropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { menuAbierto = false }
                ) {
                    tecnicos.forEach { tecnico ->
                        DropdownMenuItem(
                            text = { Text(tecnico.nombre) },
                            onClick = {
                                tecnicoElegido = tecnico.id
                                menuAbierto = false
                            }
                        )
                    }
                }
            }

            // Se activa solo si se eligió un técnico distinto del actual
            val idElegido = tecnicoElegido
            Button(
                onClick = { if (idElegido != null) onAsignarTecnico(idElegido) },
                enabled = idElegido != null &&
                        idElegido != solicitud.asignadoAUsuarioId &&
                        !procesando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (solicitud.asignadoAUsuarioId == null) "Asignar técnico"
                    else "Reasignar técnico"
                )
            }
        }
    }
}