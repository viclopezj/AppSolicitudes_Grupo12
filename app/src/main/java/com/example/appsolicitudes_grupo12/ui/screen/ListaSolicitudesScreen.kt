package com.example.appsolicitudes_grupo12.ui.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsolicitudes_grupo12.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.ListaSolicitudesUiState
import com.example.appsolicitudes_grupo12.viewModel.ListaSolicitudesViewModel

// Pantalla que muestra las solicitudes guardadas en Room.
// Solo MUESTRA el estado que le entrega el ViewModel: no lee la base de datos.
// onAgregarClick: lo que pasa al pulsar el botón + (la navegación lo usa para ir al formulario).
// onSolicitudClick: lo que pasa al tocar una tarjeta. Recibe el id de la solicitud
// (la navegación lo usa para abrir el detalle).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaSolicitudesScreen(
    viewModel: ListaSolicitudesViewModel = viewModel(),
    onAgregarClick: () -> Unit = {},
    onSolicitudClick: (Int) -> Unit = {}
) {
    // La pantalla observa el estado: cada cambio redibuja lo necesario
    val estado by viewModel.estado.collectAsState()

    // Scaffold = estructura base: barra superior, contenido y botón flotante
    Scaffold(
        topBar = { TopAppBar(title = { Text("Solicitudes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregarClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nueva solicitud"   // descripción para lectores de pantalla
                )
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
            when (val actual = estado) {
                is ListaSolicitudesUiState.Loading -> ContenidoCargando()
                is ListaSolicitudesUiState.Success -> ContenidoLista(
                    solicitudes = actual.solicitudes,
                    onSolicitudClick = onSolicitudClick
                )
                is ListaSolicitudesUiState.Empty -> ContenidoVacio()
                is ListaSolicitudesUiState.Error -> ContenidoError(
                    mensaje = actual.mensaje,
                    onReintentar = viewModel::cargarSolicitudes
                )
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
            start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp
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
// Hoy solo existe "Recibida", pero ya quedan previstos los demás estados del caso.
// Es pública (sin "private") porque DetalleSolicitudScreen también la usa.
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

    Surface(
        shape = RoundedCornerShape(50),   // bordes totalmente redondeados
        color = fondo
    ) {
        Text(
            text = estado,
            style = MaterialTheme.typography.labelMedium,
            color = texto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}