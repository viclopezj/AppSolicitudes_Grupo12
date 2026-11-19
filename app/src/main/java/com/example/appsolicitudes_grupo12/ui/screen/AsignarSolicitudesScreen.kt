package com.example.appsolicitudes_grupo12.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.data.local.UsuarioEntity
import com.example.appsolicitudes_grupo12.ui.adaptive.AppWindowSizeClass
import com.example.appsolicitudes_grupo12.ui.components.EtiquetaEstado
import com.example.appsolicitudes_grupo12.viewmodel.AsignarSolicitudesViewModel
import kotlinx.coroutines.delay

// Pantalla del administrador para asignar cada solicitud a un técnico.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignarSolicitudesScreen(
    rol: String,
    puedeAdministrar: Boolean,
    viewModel: AsignarSolicitudesViewModel = viewModel(
        factory = AsignarSolicitudesViewModel.factory(LocalContext.current, rol)
    ),
    windowSizeClass: AppWindowSizeClass = AppWindowSizeClass.Compact,
    onVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    // Recuerda el último mensaje de éxito para que no desaparezca de golpe mientras el aviso se esconde
    var ultimoMensaje by remember { mutableStateOf("") }
    estado.mensaje?.let { ultimoMensaje = it }

    // El aviso de éxito se cierra solo a los 3 segundos
    LaunchedEffect(estado.mensaje) {
        if (estado.mensaje != null) {
            delay(3000)
            viewModel.descartarAvisos()
        }
    }

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
                // El aviso de éxito aparece y se esconde con animación
                AnimatedVisibility(
                    visible = estado.mensaje != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = ultimoMensaje,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                val paddingList = PaddingValues(16.dp)
                if (windowSizeClass == AppWindowSizeClass.Compact) {
                    LazyColumn(
                        contentPadding = paddingList,
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
                } else {
                    // En ventanas amplias, las tarjetas se reparten en columnas
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 340.dp),
                        contentPadding = paddingList,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
}

// Tarjeta de una solicitud con el selector de técnico y el botón de asignar.
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Estado:", style = MaterialTheme.typography.bodyMedium)
                EtiquetaEstado(solicitud.estado)
            }

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
                if (asignando) CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                else Text(if (solicitud.asignadoAUsuarioId == null) "Asignar" else "Guardar asignación")
            }
        }
    }
}

// Mensaje centrado para los casos sin contenido o sin permiso.
@Composable
private fun MensajeAsignaciones(mensaje: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(mensaje, style = MaterialTheme.typography.bodyLarge)
    }
}