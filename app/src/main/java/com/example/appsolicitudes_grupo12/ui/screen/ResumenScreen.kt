package com.example.appsolicitudes_grupo12.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

// Panel del administrador: total de solicitudes y cantidad por estado.
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

                    is ResumenUiState.Success -> ContenidoResumen(actual)

                    is ResumenUiState.Error -> ContenidoErrorResumen(
                        mensaje = actual.mensaje,
                        onReintentar = viewModel::cargarResumen
                    )
                }
            }
        }
    }
}

// Success: tarjeta con el total y una tarjeta por cada estado.
@Composable
private fun ContenidoResumen(resumen: ResumenUiState.Success) {
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
    }
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