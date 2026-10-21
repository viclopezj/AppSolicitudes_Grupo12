package com.example.appsolicitudes_grupo12.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsolicitudes_grupo12.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.FiltroSolicitudes
import com.example.appsolicitudes_grupo12.model.ListaSolicitudesUiState
import com.example.appsolicitudes_grupo12.model.OrdenSolicitudes
import com.example.appsolicitudes_grupo12.viewModel.ListaSolicitudesViewModel
import com.example.appsolicitudes_grupo12.viewModel.SolicitudViewModel

// Pantalla que muestra las solicitudes guardadas en Room.
// Solo MUESTRA el estado que le entrega el ViewModel: no lee la base de datos ni filtra por su cuenta.
// onAgregarClick: lo que pasa al pulsar el botón + (la navegación lo usa para ir al formulario).
// onSolicitudClick: lo que pasa al tocar una tarjeta. Recibe el id de la solicitud
// (la navegación lo usa para abrir el detalle).
// onEscanearClick: lo que pasa al pulsar "Escanear QR" de la barra superior
// (la navegación lo usa para abrir el escáner).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaSolicitudesScreen(
    viewModel: ListaSolicitudesViewModel = viewModel(),
    rolUsuario: String,
    onAgregarClick: () -> Unit = {},
    onSolicitudClick: (Int) -> Unit = {},
    onEscanearClick: () -> Unit = {}
) {
    // La pantalla observa el estado: cada cambio redibuja lo necesario
    val estado by viewModel.estado.collectAsState()

    // Scaffold = estructura base: barra superior, contenido y botón flotante
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Solicitudes") },
                // "actions" son los botones que van a la derecha de la barra superior.
                // Se usa un botón de texto porque Material3 ya no trae un ícono de QR
                // en el paquete básico de íconos y no queremos agregar el paquete extendido.
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
            // Según el estado del ViewModel, se dibuja una cosa u otra.
            // Kotlin obliga a cubrir los 4 casos.
            // AnimatedContent hace un fundido suave al pasar de un estado a otro
            // (Loading -> Success, Success -> Empty, etc.). contentKey usa solo el TIPO de estado:
            // así cambiar un filtro (que crea otro Success) no repite la animación.
            AnimatedContent(
                targetState = estado,
                contentKey = { it::class },
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "estadoLista"
            ) { actual ->
            when (actual) {
                is ListaSolicitudesUiState.Loading -> ContenidoCargando()

                is ListaSolicitudesUiState.Success -> {
                    // Los filtros van arriba y la lista (o el aviso de "sin resultados") debajo
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
                                onSolicitudClick = onSolicitudClick
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

// ---------- Filtros: categoría y orden ----------
// Dos "chips" (botones ovalados) que abren un menú al tocarlos.
// Solo MUESTRAN lo elegido y avisan qué se eligió: el filtrado lo hace el ViewModel.
// La fila se puede deslizar hacia los lados, por si los textos no caben en pantallas pequeñas.
@Composable
private fun FiltrosLista(
    filtro: FiltroSolicitudes,
    onCategoriaChange: (String?) -> Unit,
    onOrdenChange: (OrdenSolicitudes) -> Unit
) {
    // Recuerdan si cada menú está abierto o cerrado
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

        // ---------- Chip de categoría ----------
        // El Box agrupa el chip con su menú, para que el menú se abra justo debajo de él
        Box {
            FilterChip(
                selected = filtro.categoria != null,   // se resalta cuando hay un filtro activo
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
                // Una opción por cada categoría definida en SolicitudViewModel
                SolicitudViewModel.CATEGORIAS.forEach { categoria ->
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

        // ---------- Chip de orden ----------
        Box {
            FilterChip(
                selected = filtro.orden != OrdenSolicitudes.RECIENTES,   // se resalta si no es el orden normal
                onClick = { menuOrdenAbierto = true },
                label = { Text("Orden: ${filtro.orden.etiqueta} ▾") }
            )

            DropdownMenu(
                expanded = menuOrdenAbierto,
                onDismissRequest = { menuOrdenAbierto = false }
            ) {
                // entries = las dos opciones del enum: RECIENTES y ANTIGUAS
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

// ---------- Estado Loading: indicador de carga al centro ----------
@Composable
private fun ContenidoCargando() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// ---------- Estado Empty: todavía no hay solicitudes ----------
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

// ---------- Sin resultados: hay solicitudes, pero ninguna coincide con el filtro ----------
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

// ---------- Estado Error: mensaje y botón para reintentar ----------
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

// ---------- Estado Success: la lista de solicitudes ----------
@Composable
private fun ContenidoLista(
    solicitudes: List<SolicitudEntity>,
    onSolicitudClick: (Int) -> Unit
) {
    // LazyColumn solo dibuja los elementos visibles: es la forma correcta de hacer listas
    LazyColumn(
        // El espacio inferior evita que el botón + tape la última tarjeta
        contentPadding = PaddingValues(
            start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // key = id ayuda a Compose a identificar cada tarjeta cuando la lista cambia
        items(items = solicitudes, key = { it.id }) { solicitud ->
            TarjetaSolicitud(
                solicitud = solicitud,
                onClick = { onSolicitudClick(solicitud.id) }   // avisa qué solicitud se tocó
            )
        }
    }
}

// Una tarjeta por solicitud: código, estado, título y categoría.
// Al pasarle onClick a la Card, toda la tarjeta se puede tocar.
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
            // Fila superior: código a la izquierda, etiqueta de estado a la derecha
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

// Etiqueta de color con el nombre del estado.
// Cada estado tiene su color; cuando el estado cambia (por ejemplo, al asignar un técnico)
// el color se transforma suavemente gracias a animateColorAsState.
// Es pública (sin "private") porque DetalleSolicitudScreen y QrScannerScreen también la usan.
@Composable
fun EtiquetaEstado(estado: String) {
    val colores = MaterialTheme.colorScheme

    // Cada estado tiene un par: color de fondo y color del texto
    val (fondo: Color, texto: Color) = when (estado) {
        "Recibida" -> colores.primaryContainer to colores.onPrimaryContainer
        "En revisión", "Pendiente de información" ->
            colores.tertiaryContainer to colores.onTertiaryContainer
        "Asignada", "En proceso", "Resuelta", "Cerrada" ->
            colores.secondaryContainer to colores.onSecondaryContainer
        "Cancelada" -> colores.errorContainer to colores.onErrorContainer
        else -> colores.surfaceVariant to colores.onSurfaceVariant
    }

    // animateColorAsState: en vez de saltar de un color a otro, lo hace en 400 ms
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
        shape = RoundedCornerShape(50),   // bordes totalmente redondeados
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
