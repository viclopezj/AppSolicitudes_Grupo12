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
import com.example.appsolicitudes_grupo12.model.data.local.UsuarioEntity
import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.PrioridadSolicitud
import com.example.appsolicitudes_grupo12.ui.components.ChipFiltro
import com.example.appsolicitudes_grupo12.ui.components.EtiquetaAtrasada
import com.example.appsolicitudes_grupo12.model.solicitud.PlazosSolicitud
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect

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
    val clientes by viewModel.clientes.collectAsState()
    val tecnicos by viewModel.tecnicos.collectAsState()

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
                            CampoBusqueda(
                                texto = actual.filtro.texto,
                                onTextoChange = viewModel::onTextoChange
                            )
                            FiltrosLista(
                                filtro = actual.filtro,
                                esAdministrador = rolUsuario == "administrador",
                                clientes = clientes,
                                tecnicos = tecnicos,
                                onCategoriaChange = viewModel::onCategoriaFiltroChange,
                                onEstadoChange = viewModel::onEstadoFiltroChange,
                                onPrioridadChange = viewModel::onPrioridadFiltroChange,
                                onClienteChange = viewModel::onClienteFiltroChange,
                                onResponsableChange = viewModel::onResponsableFiltroChange,
                                onAtrasadasChange = viewModel::onSoloAtrasadasChange,
                                onOrdenChange = viewModel::onOrdenChange,
                                onLimpiar = viewModel::limpiarFiltros
                            )


                            if (actual.solicitudes.isEmpty()) {
                                // Hay solicitudes guardadas, pero ninguna coincide con el filtro
                                ContenidoSinResultados(
                                    onQuitarFiltro = viewModel::limpiarFiltros
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

// Chips de filtro y orden. Solo muestran lo elegido: el filtrado lo hace el ViewModel.
// Cliente y responsable son solo del administrador. La fila se desliza si no caben.
@Composable
private fun FiltrosLista(
    filtro: FiltroSolicitudes,
    esAdministrador: Boolean,
    clientes: List<UsuarioEntity>,
    tecnicos: List<UsuarioEntity>,
    onCategoriaChange: (String?) -> Unit,
    onEstadoChange: (String?) -> Unit,
    onPrioridadChange: (String?) -> Unit,
    onClienteChange: (Int?) -> Unit,
    onResponsableChange: (Int?) -> Unit,
    onAtrasadasChange: (Boolean) -> Unit,
    onOrdenChange: (OrdenSolicitudes) -> Unit,
    onLimpiar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChipFiltro(
            titulo = "Estado",
            seleccion = filtro.estado,
            opciones = EstadoSolicitud.entries.map { it.texto to it.texto },
            onElegir = onEstadoChange
        )

        ChipFiltro(
            titulo = "Prioridad",
            seleccion = filtro.prioridad,
            opciones = PrioridadSolicitud.entries.map { it.texto to it.texto },
            onElegir = onPrioridadChange,
            textoSinFiltro = "Todas"
        )

        ChipFiltro(
            titulo = "Categoría",
            seleccion = filtro.categoria,
            opciones = CategoriasSolicitud.LISTA.map { it to it },
            onElegir = onCategoriaChange,
            textoSinFiltro = "Todas"
        )

        // Solicitudes con el plazo vencido (el administrador las consulta desde aquí)
        FilterChip(
            selected = filtro.soloAtrasadas,
            onClick = { onAtrasadasChange(!filtro.soloAtrasadas) },
            label = { Text("Solo atrasadas") }
        )

        if (esAdministrador) {
            ChipFiltro(
                titulo = "Cliente",
                seleccion = filtro.clienteId,
                opciones = clientes.map { it.id to it.nombre },
                onElegir = onClienteChange
            )

            ChipFiltro(
                titulo = "Responsable",
                seleccion = filtro.responsableId,
                opciones = listOf(FiltroSolicitudes.SIN_ASIGNAR to "Sin asignar") +
                        tecnicos.map { it.id to it.nombre },
                onElegir = onResponsableChange
            )
        }

        // El orden siempre tiene un valor: no hay opción "quitar"
        ChipFiltro(
            titulo = "Orden",
            seleccion = filtro.orden,
            opciones = OrdenSolicitudes.entries.map { it to it.etiqueta },
            onElegir = { orden -> orden?.let(onOrdenChange) },
            textoSinFiltro = null,
            resaltado = filtro.orden != OrdenSolicitudes.RECIENTES
        )

        if (filtro.hayFiltros) {
            TextButton(onClick = onLimpiar) { Text("Quitar filtros") }
        }
    }
}


// Campo de búsqueda. Guarda el texto localmente para escribir sin saltos y avisa al ViewModel.
// Si "Quitar filtros" borra el texto del filtro, el campo también se vacía.
@Composable
private fun CampoBusqueda(
    texto: String,
    onTextoChange: (String) -> Unit
) {
    var valor by remember { mutableStateOf(texto) }

    LaunchedEffect(texto) {
        if (texto.isEmpty() && valor.isNotEmpty()) valor = ""
    }

    OutlinedTextField(
        value = valor,
        onValueChange = {
            valor = it
            onTextoChange(it)
        },
        label = { Text("Buscar por código, título o descripción") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (valor.isNotEmpty()) {
                IconButton(onClick = {
                    valor = ""
                    onTextoChange("")
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Borrar búsqueda")
                }
            }
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
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
            Text("Quitar filtros")
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (PlazosSolicitud.estaAtrasada(solicitud)) EtiquetaAtrasada()
                    EtiquetaEstado(solicitud.estado)
                }
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