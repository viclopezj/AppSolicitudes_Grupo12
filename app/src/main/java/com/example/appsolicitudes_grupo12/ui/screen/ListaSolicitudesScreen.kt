package com.example.appsolicitudes_grupo12.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.solicitud.CategoriasSolicitud
import com.example.appsolicitudes_grupo12.state.FiltroSolicitudes
import com.example.appsolicitudes_grupo12.state.ListaSolicitudesUiState
import com.example.appsolicitudes_grupo12.state.OrdenSolicitudes
import com.example.appsolicitudes_grupo12.ui.adaptive.AppWindowSizeClass
import com.example.appsolicitudes_grupo12.ui.components.EtiquetaEstado
import com.example.appsolicitudes_grupo12.viewmodel.ListaSolicitudesViewModel

// Pantalla con las solicitudes visibles para el usuario. Solo muestra el estado del ViewModel.
// onAgregarClick: botón + (cliente). onSolicitudClick: recibe el id de la tarjeta tocada.
// onEscanearClick: botón "Escanear QR" de la barra superior.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaSolicitudesScreen(
    usuarioId: Int,
    rolUsuario: String,
    viewModel: ListaSolicitudesViewModel = viewModel(
        factory = ListaSolicitudesViewModel.factory(LocalContext.current, usuarioId, rolUsuario)
    ),
    windowSizeClass: AppWindowSizeClass = AppWindowSizeClass.Compact,
    onAgregarClick: () -> Unit = {},
    onSolicitudClick: (Int) -> Unit = {},
    onEscanearClick: () -> Unit = {}
) {
    val estado by viewModel.estado.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Solicitudes") },
                // Botón de texto: Material3 básico no trae ícono de QR
                actions = {
                    TextButton(onClick = onEscanearClick) {
                        Text("Escanear QR")
                    }
                }
            )
        },
        floatingActionButton = {
            if (rolUsuario == "cliente") {
                FloatingActionButton(onClick = onAgregarClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nueva solicitud"
                    )
                }
            }
        }
    ) { paddingInterno ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
        ) {
            // Fundido entre estados. contentKey usa solo el tipo de estado,
            // así cambiar un filtro no repite la animación.
            AnimatedContent(
                targetState = estado,
                contentKey = { it::class },
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "estadoLista"
            ) { actual ->
                when (actual) {
                    is ListaSolicitudesUiState.Loading -> ContenidoCargando()

                    is ListaSolicitudesUiState.Success -> {
                        // Filtros arriba; debajo la lista o el aviso de "sin resultados"
                        Column(modifier = Modifier.fillMaxSize()) {
                            FiltrosLista(
                                filtro = actual.filtro,
                                onCategoriaChange = viewModel::onCategoriaFiltroChange,
                                onOrdenChange = viewModel::onOrdenChange
                            )

                            if (actual.solicitudes.isEmpty()) {
                                // Hay solicitudes guardadas, pero ninguna coincide con el filtro
                                ContenidoSinResultados(
                                    onQuitarFiltro = { viewModel.onCategoriaFiltroChange(null) }
                                )
                            } else {
                                ContenidoLista(
                                    solicitudes = actual.solicitudes,
                                    onSolicitudClick = onSolicitudClick,
                                    windowSizeClass = windowSizeClass
                                )
                            }
                        }
                    }

                    is ListaSolicitudesUiState.Empty -> ContenidoVacio()

                    is ListaSolicitudesUiState.Error -> ContenidoError(
                        mensaje = actual.mensaje,
                        onReintentar = viewModel::cargarSolicitudes
                    )
                }
            }
        }
    }
}

// Chips de categoría y orden. Solo muestran lo elegido: el filtrado lo hace el ViewModel.
// La fila se desliza hacia los lados si no caben en pantallas pequeñas.
@Composable
private fun FiltrosLista(
    filtro: FiltroSolicitudes,
    onCategoriaChange: (String?) -> Unit,
    onOrdenChange: (OrdenSolicitudes) -> Unit
) {
    var menuCategoriaAbierto by remember { mutableStateOf(false) }
    var menuOrdenAbierto by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Chip de categoría (el Box agrupa el chip con su menú)
        Box {
            FilterChip(
                selected = filtro.categoria != null,
                onClick = { menuCategoriaAbierto = true },
                label = { Text("Categoría: ${filtro.categoria ?: "Todas"} ▾") }
            )

            DropdownMenu(
                expanded = menuCategoriaAbierto,
                onDismissRequest = { menuCategoriaAbierto = false }
            ) {
                // Primera opción: quitar el filtro
                DropdownMenuItem(
                    text = { Text("Todas") },
                    onClick = {
                        onCategoriaChange(null)
                        menuCategoriaAbierto = false
                    }
                )
                CategoriasSolicitud.LISTA.forEach { categoria ->
                    DropdownMenuItem(
                        text = { Text(categoria) },
                        onClick = {
                            onCategoriaChange(categoria)
                            menuCategoriaAbierto = false
                        }
                    )
                }
            }
        }

        // Chip de orden (se resalta si no es el orden normal)
        Box {
            FilterChip(
                selected = filtro.orden != OrdenSolicitudes.RECIENTES,
                onClick = { menuOrdenAbierto = true },
                label = { Text("Orden: ${filtro.orden.etiqueta} ▾") }
            )

            DropdownMenu(
                expanded = menuOrdenAbierto,
                onDismissRequest = { menuOrdenAbierto = false }
            ) {
                OrdenSolicitudes.entries.forEach { orden ->
                    DropdownMenuItem(
                        text = { Text(orden.etiqueta) },
                        onClick = {
                            onOrdenChange(orden)
                            menuOrdenAbierto = false
                        }
                    )
                }
            }
        }
    }
}

// Loading: indicador de carga al centro.
@Composable
private fun ContenidoCargando() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// Empty: todavía no hay solicitudes.
@Composable
private fun ContenidoVacio() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Aún no hay solicitudes",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Pulsa el botón + para registrar la primera",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// Hay solicitudes, pero ninguna coincide con el filtro.
@Composable
private fun ContenidoSinResultados(onQuitarFiltro: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ninguna solicitud coincide con el filtro",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onQuitarFiltro,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Quitar filtro")
        }
    }
}

// Error: mensaje y botón para reintentar.
@Composable
private fun ContenidoError(
    mensaje: String,
    onReintentar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onReintentar,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Reintentar")
        }
    }
}

// Success: la lista de solicitudes (una columna en ventanas compactas, varias en amplias).
@Composable
private fun ContenidoLista(
    solicitudes: List<SolicitudEntity>,
    onSolicitudClick: (Int) -> Unit,
    windowSizeClass: AppWindowSizeClass
) {
    // El espacio inferior evita que el botón + tape la última tarjeta
    val padding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp)
    if (windowSizeClass == AppWindowSizeClass.Compact) {
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = solicitudes, key = { it.id }) { solicitud ->
                TarjetaSolicitud(
                    solicitud = solicitud,
                    onClick = { onSolicitudClick(solicitud.id) }
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            contentPadding = padding,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = solicitudes, key = { it.id }) { solicitud ->
                TarjetaSolicitud(solicitud) { onSolicitudClick(solicitud.id) }
            }
        }
    }
}

// Tarjeta de una solicitud: código, estado, título y categoría. Toda la tarjeta se puede tocar.
@Composable
private fun TarjetaSolicitud(
    solicitud: SolicitudEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = solicitud.codigo,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                EtiquetaEstado(solicitud.estado)
            }

            Text(
                text = solicitud.titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = solicitud.categoria,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}