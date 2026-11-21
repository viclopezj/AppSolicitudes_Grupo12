package com.example.appsolicitudes_grupo12.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontFamily
import com.example.appsolicitudes_grupo12.model.exportacion.ReporteSimulado
import com.example.appsolicitudes_grupo12.model.solicitud.MedicionTiempos
import com.example.appsolicitudes_grupo12.model.solicitud.PlazosSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.PrioridadSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.ResumenTiempos
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsolicitudes_grupo12.state.ResumenUiState
import com.example.appsolicitudes_grupo12.ui.adaptive.AppWindowSizeClass
import com.example.appsolicitudes_grupo12.ui.adaptive.adaptiveContentWidth
import com.example.appsolicitudes_grupo12.ui.components.EtiquetaEstado
import com.example.appsolicitudes_grupo12.viewmodel.ResumenViewModel

// Panel del administrador: total de solicitudes, atrasadas, cantidad por estado, prioridad y
// responsable, tiempos simulados y exportación ficticia.
// Solo muestra el estado del ViewModel. onVolver: flecha de la barra superior.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenScreen(
    rol: String,
    viewModel: ResumenViewModel = viewModel(
        factory = ResumenViewModel.factory(LocalContext.current, rol)
    ),
    windowSizeClass: AppWindowSizeClass = AppWindowSizeClass.Compact,
    onVolver: () -> Unit = {}
) {
    val estado by viewModel.estado.collectAsState()
    val reporte by viewModel.reporte.collectAsState()

    // Aviso con el reporte de la exportación ficticia
    reporte?.let { DialogoReporte(reporte = it, onCerrar = viewModel::cerrarReporte) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Panel resumen") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingInterno ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno),
            contentAlignment = Alignment.TopCenter
        ) {
            // Centra el contenido y limita su ancho en ventanas amplias
            Box(
                modifier = Modifier
                    .adaptiveContentWidth(windowSizeClass, 600.dp, 720.dp, 900.dp)
                    .fillMaxSize()
            ) {
                when (val actual = estado) {
                    is ResumenUiState.Loading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }

                    is ResumenUiState.Success -> ContenidoResumen(
                        resumen = actual,
                        onExportar = viewModel::exportarResumen
                    )

                    is ResumenUiState.Error -> ContenidoErrorResumen(
                        mensaje = actual.mensaje,
                        onReintentar = viewModel::cargarResumen
                    )
                }
            }
        }
    }
}

// Success: total, atrasadas, indicadores por estado, prioridad y responsable, tiempos y exportación.
@Composable
private fun ContenidoResumen(
    resumen: ResumenUiState.Success,
    onExportar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total de solicitudes",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = resumen.total.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Solicitudes atrasadas (plazo vencido según la prioridad)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = if (resumen.atrasadas > 0) {
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            } else {
                CardDefaults.cardColors()
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Solicitudes atrasadas",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = resumen.atrasadas.toString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Text(
                    text = "Plazos ficticios: " + PrioridadSolicitud.entries.reversed()
                        .joinToString(", ") { "${it.texto} ${PlazosSolicitud.plazoEnDias(it)} d" } +
                            ". Se consultan en la lista con \"Solo atrasadas\".",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Text(text = "Por estado", style = MaterialTheme.typography.titleMedium)

        resumen.porEstado.forEach { (estado, cantidad) ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EtiquetaEstado(estado.texto)
                    Text(
                        text = cantidad.toString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }

        Text(text = "Por prioridad", style = MaterialTheme.typography.titleMedium)

        resumen.porPrioridad.reversed().forEach { (prioridad, cantidad) ->
            TarjetaIndicador(texto = prioridad.texto, cantidad = cantidad)
        }

        Text(text = "Por responsable", style = MaterialTheme.typography.titleMedium)

        resumen.porResponsable.forEach { (nombre, cantidad) ->
            TarjetaIndicador(texto = nombre, cantidad = cantidad)
        }

        Text(
            text = "Medición simulada de tiempos",
            style = MaterialTheme.typography.titleMedium
        )
        TarjetaTiempos(tiempos = resumen.tiempos)

        OutlinedButton(
            onClick = onExportar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Exportar resumen (simulado)")
        }
    }
}

// Una fila de indicador: texto a la izquierda y cantidad a la derecha.
@Composable
private fun TarjetaIndicador(texto: String, cantidad: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = texto, style = MaterialTheme.typography.bodyLarge)
            Text(text = cantidad.toString(), style = MaterialTheme.typography.titleLarge)
        }
    }
}

// Promedios de tiempo. Son una estimación con las fechas guardadas, no un cronómetro real.
@Composable
private fun TarjetaTiempos(tiempos: ResumenTiempos) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LineaTiempo(
                etiqueta = "Atención promedio (Resuelta o Cerrada)",
                milisegundos = tiempos.promedioAtencionMs,
                cantidad = tiempos.atendidas
            )
            LineaTiempo(
                etiqueta = "Tiempo abierto promedio (sin terminar)",
                milisegundos = tiempos.promedioAbiertasMs,
                cantidad = tiempos.abiertas
            )
            Text(
                text = "Simulación calculada con la fecha de creación y la última actualización.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LineaTiempo(etiqueta: String, milisegundos: Long?, cantidad: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$etiqueta ($cantidad)",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = milisegundos?.let { MedicionTiempos.formatear(it) } ?: "Sin datos",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

// Aviso de la exportación ficticia: nombre del archivo, filas y vista previa del contenido.
@Composable
private fun DialogoReporte(reporte: ReporteSimulado, onCerrar: () -> Unit) {
    val lineas = reporte.contenido.lines()
    val vistaPrevia = lineas.take(8).joinToString("\n") + if (lineas.size > 8) "\n…" else ""

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Exportación simulada") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Archivo ficticio: ${reporte.nombreArchivo}")
                Text("Solicitudes incluidas: ${reporte.filas}")
                Text(
                    text = "No se envió ni se guardó en ninguna parte. Vista previa:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = vistaPrevia,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) { Text("Cerrar") }
        }
    )
}

// Error: mensaje y botón para reintentar.
@Composable
private fun ContenidoErrorResumen(
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