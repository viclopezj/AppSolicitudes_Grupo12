package com.example.appsolicitudes_grupo12.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsolicitudes_grupo12.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.data.local.UsuarioEntity
import com.example.appsolicitudes_grupo12.viewModel.AsignarSolicitudesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignarSolicitudesScreen(
    viewModel: AsignarSolicitudesViewModel = viewModel(),
    puedeAdministrar: Boolean,
    onVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Asignar solicitudes") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when {
            !puedeAdministrar -> MensajeAsignaciones(
                "Solo el administrador puede asignar solicitudes.",
                Modifier.padding(padding)
            )

            estado.cargando -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            estado.error != null -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(estado.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = viewModel::descartarAvisos) { Text("Cerrar") }
            }

            estado.solicitudes.isEmpty() -> MensajeAsignaciones(
                "Todavía no hay solicitudes para asignar.",
                Modifier.padding(padding)
            )

            estado.tecnicos.isEmpty() -> MensajeAsignaciones(
                "No hay cuentas técnicas disponibles.",
                Modifier.padding(padding)
            )

            else -> Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                estado.mensaje?.let { mensaje ->
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(estado.solicitudes, key = { it.id }) { solicitud ->
                        TarjetaAsignacion(
                            solicitud = solicitud,
                            tecnicos = estado.tecnicos,
                            asignando = estado.solicitudAsignandoId == solicitud.id,
                            onAsignar = { tecnicoId -> viewModel.asignar(solicitud.id, tecnicoId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaAsignacion(
    solicitud: SolicitudEntity,
    tecnicos: List<UsuarioEntity>,
    asignando: Boolean,
    onAsignar: (Int) -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }
    var tecnicoElegido by remember(solicitud.id, solicitud.asignadoAUsuarioId) {
        mutableStateOf(solicitud.asignadoAUsuarioId)
    }
    val nombreTecnico = tecnicos.firstOrNull { it.id == tecnicoElegido }?.nombre

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(solicitud.codigo, color = MaterialTheme.colorScheme.primary)
            Text(solicitud.titulo, style = MaterialTheme.typography.titleMedium)
            Text("Estado: ${solicitud.estado}", style = MaterialTheme.typography.bodyMedium)

            Box {
                TextButton(onClick = { menuAbierto = true }) {
                    Text(nombreTecnico ?: "Seleccionar técnico ▾")
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

            Button(
                onClick = { tecnicoElegido?.let(onAsignar) },
                enabled = tecnicoElegido != null && !asignando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (asignando) CircularProgressIndicator()
                else Text(if (solicitud.asignadoAUsuarioId == null) "Asignar" else "Guardar asignación")
            }
        }
    }
}

@Composable
private fun MensajeAsignaciones(mensaje: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(mensaje, style = MaterialTheme.typography.bodyLarge)
    }
}
